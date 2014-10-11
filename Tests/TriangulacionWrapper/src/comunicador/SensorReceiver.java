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

package comunicador;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import triangulacion.TriangulacionOctave;

public class SensorReceiver {
    private static final String SCRIPT_PATH = "../../Localizacion/detectarcamara.m";
    private static final String FUNC_NAME   = "detectarcamara";
    
    public static void main(String[] args) {
        // Obtiene los sensores conectados al ordenador
        Sensor[] ss = new Sensor[3];    // GetSensors();
        ss[0] = new Sensor("", 0);
        ss[1] = new Sensor("", 1);
        ss[2] = new Sensor("", 2);
        
        // Dimensiones de la habitación
        int width  = 6;
        int length = 6;
        
        // Posición de las cámaras de prueba
        List<CamaraPos> cams = new ArrayList<>();
        cams.add(new CamaraPos(width/2, 0,        "ID1"));
        cams.add(new CamaraPos(width/2, length,   "ID2"));
        cams.add(new CamaraPos(0,       length/2, "ID3"));
        cams.add(new CamaraPos(width,   length/2, "ID4"));
        
        // Prepara la triangulación
        final TriangulacionOctave octave = new TriangulacionOctave(
                SCRIPT_PATH, FUNC_NAME, cams, width, length, true);
        if (octave.isAlive()) {
            String idCamara = octave.triangular(sensores);
            //System.out.printf("[JAVA] ID camara: %s\n", idCamara);
        } else {
            System.out.println("[JAVA] Error");
        }        
        
        // Empeiza a obtener lecturas de los sensores
        for (Sensor s : ss) {
            // Ejecuta el script
            try {
                String cmd = "sudo python sensor.py " + s.id + " " + s.getPort();
                Runtime.getRuntime().exec(cmd);
            } catch (IOException ex) { }
            
            // Empieza a recibir datos
            new Thread(new Receiver(s)).start();
        }
    }
    
    public static Sensor[] GetSensors() {
        // TODO:
        return null;
    }
    
    private static class Receiver implements Runnable {
        private final Sensor sensor;
        private DatagramSocket socket;
        
        public Receiver(final Sensor sensor) {
            this.sensor = sensor;
            
            try {
                this.socket = new DatagramSocket(sensor.getPort());
            } catch (SocketException ex) {
                System.err.println("No se pudo abrir el puerto: " + sensor.getPort());
            }
        }
        
        @Override
        public void run() {
            while (!socket.isClosed()) {
                // Recibe un paquete
                byte [] bufer = new byte [256];
                DatagramPacket paquete = new DatagramPacket(bufer,bufer.length);
                try { this.socket.receive(paquete); } catch (IOException ex) { }
                
                // Parse los datos
                String peticion = new String(paquete.getData());
                String[] recibido = peticion.split("\\s+");
                String addr = recibido[0];
                int rssi = Integer.parseInt(recibido[1].substring(0, 3));
                System.out.printf("[%d] MAC: %s | RSSI: %d\n", sensor.id, addr, rssi);
                Dato prueba = new Dato (recibido[0],0,0,"Dorayaki", rssi);
            }
        }
    }
}
