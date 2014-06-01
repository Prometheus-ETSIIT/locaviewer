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

package triangulacion;

import comunicador.CamaraPos;
import comunicador.Dato;
import dk.ange.octave.OctaveEngine;
import dk.ange.octave.OctaveEngineFactory;
import dk.ange.octave.exception.OctaveEvalException;
import dk.ange.octave.type.Octave;
import dk.ange.octave.type.OctaveDouble;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Establece un enlace para ejecutar código de triangulación en Octave.
 */
public class TriangulacionOctave {
    private final OctaveEngine octave;
    private final String funcName;
    private final List<CamaraPos> cams;
    private final int width;
    private final int length;
    private boolean alive;
    private double[] lastPosition;
    private int lastCamIdx;
    
    private final TestFrame testFrame;
    
    /**
     * Crea una nueva instancia inicializando el engine.
     * 
     * @param scriptPath Ruta al script con las funciones de triangulación.
     * @param funcName Nombre de la función a ejecutar. Esta función tendrá
     * los siguientes argumentos en el siguiente orden:
     *  + Vector de double con posición X de los sensores.
     *  + Vector de double con posición Y de los sensores.
     *  + Valor RSSI de los sensores.
     * @param cams Datos de las cámaras disponibles.
     * @param width Ancho de la habitación.
     * @param length Largo de la habitación.
     * @param mostrarVentana Si se debe mostrar o no una ventana de prueba.
     */
    public TriangulacionOctave(final String scriptPath, final String funcName,
            final List<CamaraPos> cams, final int width, final int length,
            final boolean mostrarVentana) {
        this.octave    = new OctaveEngineFactory().getScriptEngine();
        this.funcName  = funcName;
        this.width     = width;
        this.length    = length;
        this.cams      = cams;
        this.alive     = this.initialize(scriptPath, width, length);
        this.testFrame = mostrarVentana ? new TestFrame(this) : null;
        
        if (mostrarVentana) {
            /* Set the Nimbus look and feel */
            //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
            /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
             * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
             */
            try {
                for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        javax.swing.UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (ClassNotFoundException ex) {
                java.util.logging.Logger.getLogger(TestFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            } catch (InstantiationException ex) {
                java.util.logging.Logger.getLogger(TestFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                java.util.logging.Logger.getLogger(TestFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            } catch (javax.swing.UnsupportedLookAndFeelException ex) {
                java.util.logging.Logger.getLogger(TestFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            }
            //</editor-fold>

            /* Create and display the form */
            java.awt.EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    testFrame.setVisible(true);
                }
            });
        }
    }
    
    /**
     * Obtiene si el sistema está en funcionamiento.
     * 
     * @return Estado de vida del sistema.
     */
    public boolean isAlive() {
        return this.alive;
    }
    
    /**
     * Apaga el sistema...
     */
    public void close() {
        this.octave.close();
        this.alive = false;
    }
    
    /**
     * Inicializa el sistema poniendo el script en memoria.
     * 
     * @param scriptPath Ruta al archivo con el script de triangulación.
     * @return Devuelve si la operación se llevó a cabo con éxito.
     */
    private boolean initialize(final String scriptPath, final int width, final int length) {
        // Información sobre los métodos: http://goo.gl/1kbd4y
        
        // Lee las funciones
        String script;
        try {
            Path path = FileSystems.getDefault().getPath(scriptPath);
            script = new String(Files.readAllBytes(path), Charset.forName("ISO-8859-15"));
        } catch (IOException ex) {
            System.err.printf(
                    "[JAVA] Error al leer el archivo: %s\n",
                    scriptPath
            );
            return false;
        }
        
        // Las carga en memoria
        try {
            this.octave.eval(script);
        } catch (OctaveEvalException ex) {
            System.err.printf(
                    "[JAVA] Error al evaluar el script\n%s\n",
                    ex.getMessage()
            );
            return false;
        }

        // Obtiene un array con la posición de las cámaras
        OctaveDouble camPos = new OctaveDouble(this.cams.size(), 2);
        for (int i = 0; i < this.cams.size(); i++) {
            camPos.set(this.cams.get(i).getPosX(), i + 1, 1);
            camPos.set(this.cams.get(i).getPosY(), i + 1, 2);
        }
        
        // Lo carga en memoria
        this.octave.put("camaras", camPos);
        this.octave.put("ancho", Octave.scalar(width));
        this.octave.put("largo", Octave.scalar(length));
        
        return true;
    }
    
    /**
     * Obtiene la lista de cámaras disponibles.
     * 
     * @return Cámaras disponibles.
     */
    public List<CamaraPos> getCamaras() {
        return this.cams;
    }
    
    /**
     * Obtiene el ancho de la habitación.
     * 
     * @return Ancho de la habitación.
     */
    public int getWidth() {
        return this.width;
    }
    
    /**
     * Obtiene el largo de la habitación.
     * 
     * @return Largo de la habitación.
     */
    public int getLength() {
        return this.length;
    }
    
    /**
     * Obtiene el último índice de la cámara elegida.
     * 
     * @return Índice de la cámara elegida.
     */
    public int getLastCamIndex() {
        return this.lastCamIdx;
    }
    
    /**
     * Obtiene la última posición triangulada.
     * 
     * @return Posición triangulada.
     */
    public double[] getLastPosition() {
        return this.lastPosition;
    }
    
    /**
     * Realiza una triangulación a partir de la señal recibida en los sensores
     * y de su posición.
     * 
     * @param datos Conjunto de sensores con valores de RSSI.
     * @return Posición X e Y de la triangulación.
     */
    public String triangular(final List<Dato> datos) {
        if (!this.alive) {
            System.err.println("[JAVA] Error ¡el sistema está muerto!");
            return null;
        }
        
        // Crea una array con la posición de los sensores y los RSSI
        OctaveDouble bluePos = new OctaveDouble(datos.size(), 2);
        OctaveDouble rssi    = new OctaveDouble(1, datos.size());
        for (int i = 0; i < datos.size(); i++) {
            bluePos.set(datos.get(i).getPosicionSensor().getPrimero(), i + 1, 1);
            bluePos.set(datos.get(i).getPosicionSensor().getSegundo(), i + 1, 2);
            rssi.set(datos.get(i).getIntensidad(), 1, i + 1);
        }
        
        // Lo pone en memoria
        this.octave.put("bluePos", bluePos);
        this.octave.put("rssi", rssi);

        // Llama a la función que tiene en memoria
        try {
            String funcCall = String.format(
                    "[ninoPos, idxCam] = %s(rssi, bluePos, camaras, ancho, largo);",
                    this.funcName
            );
            this.octave.eval(funcCall);
        } catch (OctaveEvalException ex) {
            System.err.printf(
                    "[JAVA] Error al evaluar el script\n%s\n",
                    ex.getMessage()
            );
            return null;
        }
 
        // Obtiene el resultado
        OctaveDouble idxCam_Oct = octave.get(OctaveDouble.class, "idxCam");
        int idxCam = (int)idxCam_Oct.get(1);
        
        // Lo establece en la ventana de prueba
        this.lastCamIdx = (idxCam != -1) ? idxCam - 1 : -1;
        this.lastPosition = octave.get(OctaveDouble.class, "ninoPos").getData();
        if (this.testFrame != null)
            this.testFrame.setNewSensors(datos);
        
        // Devuelve el ID de la cámara.
        if (this.lastCamIdx != -1) {
            return this.cams.get(this.lastCamIdx).getID();    
        } else {
            return null;
        }
    }

    /**
     * Prueba a ejecutar el script en Octave de triangulación
     * 
     * @param args Ninguno por el momento.
     */
    public static void main(String[] args) {
        // Prueba la clase
        String scriptPath = "../../Localizacion/detectarcamara.m";
        String funcName   = "detectarcamara";
        
        int width  = 6;
        int length = 6;
        
        List<CamaraPos> cams = new ArrayList<>();
        cams.add(new CamaraPos(3, 0, "ID1"));
        cams.add(new CamaraPos(3, 6, "ID2"));
        cams.add(new CamaraPos(0, 3, "ID3"));
        cams.add(new CamaraPos(6, 3, "ID4"));
        
        List<Dato> sensores = new ArrayList<>();
        sensores.add(new Dato("S1", 0, 0, "Juan", -89));  // Antes -38
        sensores.add(new Dato("S2", 6, 0, "Juan", -85));  // Antes -38
        sensores.add(new Dato("S3", 0, 6, "Juan", -85));  // Antes -38
        
        boolean showWindows = (args.length == 1 && args[0].equals("true"));
        
        final TriangulacionOctave octave = new TriangulacionOctave(
                scriptPath, funcName, cams, width, length, showWindows);
        if (octave.isAlive()) {
            String idCamara = octave.triangular(sensores);
            //System.out.printf("[JAVA] ID camara: %s\n", idCamara);
            octave.testFrame.activateRandomTests();
        } else {
            System.out.println("[JAVA] Error");
        }        
        //octave.close();
    }
}
