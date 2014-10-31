/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Prometheus
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
