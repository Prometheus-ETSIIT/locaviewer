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

package centroinfantil;

import es.prometheus.dds.TopicoControl;
import es.prometheus.dds.TopicoControlFactoria;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Timer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Cada cierto tiempo pregunta por la localización de todos los niños.
 */
public class Daemon {
    private static final int INTERVAL =  1 * 60 * 1000; // 1 hora
    private static final int TIMEOUT  =  1 * 60 * 1000; // 1 minuto
    private static final String PARTICIPANT_NAME = "MisParticipantes::ParticipanteDaemon";
    private static final String CHILD_TOPIC_NAME = "ChildDataTopic";
    
    private String dbpath;
    private Document dbxml;
    private final Timer timeout;
    private int index;

    private final Map<String, Element> ninosXml = new HashMap<>();
    private List<DatosNino> ninos;
    private TopicoControl topico;
    private LectorNino lector;
    
    public Daemon() {
        // Crea el timer para el timeout
        this.timeout = new Timer(TIMEOUT, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                onTimeout();
            }
        });
        this.timeout.setRepeats(false);
        
        // Obtiene todos los niños registrados en la guardería.
        this.obtieneNinos();
        
        // Abrimos la base de datos
        this.abreDb();
        
        // Inicia DDS
        this.iniciaDds();
    }
    
    /**
     * Inicia el programa.
     * 
     * @param args Ninguno.
     */
    public static void main(String[] args) {
        final Daemon d = new Daemon();
        Timer timer = new Timer(INTERVAL, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                d.comenzar();
            }
        });
        timer.start();
        
        Runtime.getRuntime().addShutdownHook(new ShutdownThread(d));
    }
    
    private void iniciaDds() {
        // Creamos el control de tópico.
        this.topico = TopicoControlFactoria.crearControlDinamico(
            PARTICIPANT_NAME,
            CHILD_TOPIC_NAME);
        
        // Creamos el lector
        this.lector = new LectorNino(this.topico, "-1");
        this.lector.setExtraListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                onNuevoDato();
            }
        });
    }
    
    private void obtieneNinos() {
        this.ninos = new ArrayList<>();
        // TODO: Obtener lista de niños desde el servidor.
        this.ninos.add(new DatosNino(0, "20:14:04:11:34:37", "", "", 0, 0, 0, 0,
                "Benito Palacios Sánchez", "Benii"));
        this.ninos.add(new DatosNino(0, "00:14:01:14:18:26", "", "", 0, 0, 0, 0,
                "Nicolás Guerrero", "Nico"));
    }
    
    public void dispose() {
        this.escribirDb();
        this.lector.dispose();
        this.topico.dispose();
    }
    
    public void comenzar() {
        if (this.ninos.isEmpty())
            return;
        
        // Obtiene el primer nino y se suscribe
        this.index = 0;
        this.lector.cambiarNinoId(this.ninos.get(this.index).getId());
        this.lector.reanudar();
        this.timeout.start();
    }
    
    private void onTimeout() {
        this.siguienteNino();
    }
    
    private void onNuevoDato() {
        // Actualizamos la base datos con el nuevo valor
        this.actualizarDb(this.lector.getUltimoDato());
        
        // Pedimos el siguiente niño
        this.siguienteNino();
    }
    
    private void siguienteNino() {
        this.timeout.stop();
        this.index++;
        
        if (this.index >= this.ninos.size()) {
            // Hemos terminado
            this.lector.cambiarNinoId("-1");
            this.lector.suspender();
            this.escribirDb();
        } else {
            this.lector.cambiarNinoId(this.ninos.get(this.index).getId());
            this.timeout.start();
        }
    }
    
    private void abreDb() {
        try {
            this.dbpath = System.getProperty("user.home") + "historial.xml";
            System.out.println("[Daemon] DB: " + this.dbpath);
            
            // Abre la base de datos
            File fXmlFile = new File(dbpath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            this.dbxml = dBuilder.parse(fXmlFile);
            this.dbxml.getDocumentElement().normalize();
            
            // Miramos qué niños faltan por añadir
            List<DatosNino> noAnadidos = new ArrayList<>(this.ninos);
            NodeList nList = this.dbxml.getElementsByTagName("child");
            for (int i = 0; i < nList.getLength(); i++) {
                String id = GetXmlEntryValue((Element)nList.item(i), "id");
                
                // Buscamos coincidencia y eliminamos
                int idx = -1;
                for (int j = 0; j < noAnadidos.size() && idx == -1; j++)
                    if (noAnadidos.get(j).getId().equals(id))
                        idx = j;
                
                if (idx != -1) {
                    noAnadidos.remove(idx);
                    this.ninosXml.put(id, (Element)nList.item(i));
                }
            }
            
            // Añadimos esos niños
            for (DatosNino info : noAnadidos) {
                Element child = this.dbxml.createElement("child");
                AddXmlTextChild(dbxml, child, "name", info.getNombre());
                AddXmlTextChild(dbxml, child, "apodo", info.getApodo());
                AddXmlTextChild(dbxml, child, "id", info.getId());
                child.appendChild(this.dbxml.createElement("historial"));
                
                this.ninosXml.put(info.getId(), child);
                this.dbxml.appendChild(child);
            }
        } catch (SAXException | ParserConfigurationException | IOException ex) {
        }
    }
    
    /**
     * Shortcut to get the text in a XML entry.
     * 
     * @param el XML entry element.
     * @param name Tag name.
     * @return Value of the entry.
     */
    private static String GetXmlEntryValue(final Element el, final String name) {
        return el.getElementsByTagName(name).item(0).getTextContent();
    }
    
    private void actualizarDb(DatosNino dato) {
        try {        
            // Busca el ID del niño         
            Element childXml = this.ninosXml.get(dato.getId());
            Element historial = (Element)childXml.getElementsByTagName("historial").item(0);
            
            // Le añade una nueva entrada
            Element entrada = this.dbxml.createElement("localizacion");
            AddXmlTextChild(dbxml, entrada, "fecha", String.valueOf(new Date().getTime()));
            AddXmlTextChild(dbxml, entrada, "sala", dato.getSala());
            AddXmlTextChild(dbxml, entrada, "camara", dato.getCamId());
            AddXmlTextChild(dbxml, entrada, "posicionX", String.valueOf(dato.getPosX()));            
            AddXmlTextChild(dbxml, entrada, "posicionY", String.valueOf(dato.getPosY()));
            historial.appendChild(entrada);
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
    }
    
    private static void AddXmlTextChild(Document doc, Element el, String name, String value) {
        Element child = doc.createElement(name);
        child.appendChild(doc.createTextNode(value));
        el.appendChild(child);
    }
    
    private void escribirDb() {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(this.dbxml);
            StreamResult result = new StreamResult(new File(this.dbpath));
            transformer.transform(source, result);
        } catch (TransformerConfigurationException ex) {
        } catch (TransformerException ex) {
        }
    }
    
    /**
     * Listener llamado cuando se finaliza la aplicación.
     */
    private static class ShutdownThread extends Thread {
        private final Daemon daemon;
        
        public ShutdownThread(final Daemon daemon) {
            this.daemon = daemon;
        }
        
        @Override
        public void run() {
            System.out.println("[Daemon] Parando. . .");
            this.daemon.dispose();
        }
    }
}
