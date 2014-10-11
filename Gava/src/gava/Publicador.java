/*
 * Copyright (C) 2014 Prometheus
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package gava;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.gstreamer.Gst;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Publicador de vídeo.
 */
public class Publicador {
    private final static ShutdownThread Shutdown = new ShutdownThread();

    /**
     * Inicia la aplicación.
     *
     * @param args Opcional: ID de las cámaras a iniciar (Ej: 1 -> /dev/video1).
     * Si no se pasa ningún parámetro inicia todas las disponibles.
     */
    public static void main(String[] args) {
        // Inicializa GStreamer
        args = Gst.init("Gava", args);
        
        // Añade un listener que se ejecute al finalizar la aplicación (Ctl + C)
        Runtime.getRuntime().addShutdownHook(Shutdown);
        
        if (args.length > 0)
            for (String arg : args)
                CreaPublicador("/dev/video" + arg);
        else
            StartAll();
    }
    
    /**
     * Crea un publicador de vídeo en una nueva hebra.
     * Lo añade al listener de shutdown, para que elimine el participante.
     * 
     * @param dev Archivo de vídeo (ej: /dev/video0)
     */
    private static void CreaPublicador(String dev) {
        DatosCamara info = BuscaInfo(dev);
        System.out.println("Iniciando: " + dev + " | " + info.getCamId());
        EscritorVideo p = new EscritorVideo(dev, info);   // Crea un escritor
        
        Shutdown.addPublicador(p); // Lo añade al listener
        p.start();                 // Lo inica.
    }
    
    /**
     * Busca y devuelve la información sobre la cámara referida.
     * 
     * @param dev Archivo del sistema de la cámara (ej: /dev/video).
     * @return Datos de la cámara o null si no se han encontrado.
     * @throws Exception Las gilipolleces de Java y eso.
     */
    private static DatosCamara BuscaInfo(String dev) {
        try {        
            // Abrimos y procesamos el XML
            File fXmlFile = new File("InfoCamaras.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);
            doc.getDocumentElement().normalize();

            // Por cada entrada de información
            NodeList nList = doc.getElementsByTagName("camara"); 
            for (int i = 0; i < nList.getLength(); i++) {
                Element element = (Element)nList.item(i);

                // Comparamos si el dispositivo que buscamos
                String eDev = element.getElementsByTagName("device").item(0).getTextContent();
                if (!eDev.equals(dev))
                    continue;

                // Lo es, así que creamos la estructura y la devolvemos
                return DatosCamara.FromXml(element);
            }
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
        
        return null;
    }

    /**
     * Inicia un escritor por cada cámara conectada.
     */
    private static void StartAll() {        
        // Obtiene una lista de cámaras conectadas (/dev/video*)
        String[] cams = new File("/dev/").list(new FilenameFilter() {
            @Override
            public boolean accept(File parentDir, String filename) {
                return parentDir.getAbsolutePath().equals("/dev")
                        && filename.startsWith("video");
            }
        });

        // Para cada cámara crea un publicador
        for (String cam : cams)
            CreaPublicador("/dev/" + cam);
    }
    
    /**
     * Listener llamado cuando se finaliza la aplicación.
     * Termina los thread de publicación de vídeo para eliminar el participante.
     */
    private static class ShutdownThread extends Thread {
        List<EscritorVideo> publicadores = new ArrayList<>();
        
        @Override
        public void run() {
            System.out.println("Parando. . .");
            for (EscritorVideo p : publicadores) {
                p.parar();
                try { p.join(5000); }
                catch (InterruptedException ex) { System.err.println("TimeOver!"); }
            }
            
        }
        
        /**
         * Añade un publicador al listener.
         * 
         * @param p Publicador.
         */
        public void addPublicador(final EscritorVideo p) {
            this.publicadores.add(p);
        }
    }
}   
