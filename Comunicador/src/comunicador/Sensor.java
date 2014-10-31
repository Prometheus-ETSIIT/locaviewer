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

import com.rti.dds.dynamicdata.DynamicData;
import es.prometheus.dds.Escritor;
import es.prometheus.dds.TopicoControl;
import es.prometheus.dds.TopicoControlFactoria;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Obtiene datos del script de Python y lo envía por DDS.
 */
public class Sensor extends Thread {
    private static final int PORT = 4554;

    private TopicoControl control;
    private Escritor escritor;
    private DynamicData data;
    private DatagramSocket socket;
    private DatosSensor[] baseSensor;

    private boolean debeParar;

    /**
     * Crea una nueva instancia de la clase.
     */
    public Sensor() {
        this.debeParar = false;
    }

    /**
     * Inicia el programa.
     *
     * @param args Ninguno.
     */
    public static void main(String[] args) {
        // Creamos el comunicador de sensor
        Sensor sensor = new Sensor();
        sensor.start();

        // Creamos una hebra para salidas forzosas (Control+C).
    	Runtime.getRuntime().addShutdownHook(new ShutdownThread(sensor));
    }

    @Override
    public void run() {
        // Iniciamos DDS
        this.iniciaDds();

        // Obtiene información sobre los sensores conectados
        this.procesaXml();

        // Creamos el socket
        try { socket = new DatagramSocket(PORT); }
        catch (SocketException ex) { System.exit(-1); }

        while (!this.debeParar) {
            // Recibe un dato y lo envía
            DatosSensor datos = this.recibeDato();
            if (datos == null)
                continue;

            System.out.printf("[Sensor] (%s) -> %s | %.2f\n",
                    datos.getID(), datos.getIDNino(), datos.getIntensidad());
            datos.escribeDds(this.data);
            this.escritor.escribeDatos(this.data);
        }

        // Liberamos los recursos
        socket.close();
        this.control.dispose();
    }

    /**
     * Pide parar la ejecución.
     */
    public void parar() {
        this.debeParar = true;
    }

    /**
     * Inicia las entidades de DDS.
     */
    private void iniciaDds() {
        this.control = TopicoControlFactoria.crearControlDinamico(
                "MisParticipantes::ParticipanteSensor",
                "SensorDataTopic");
        this.escritor = new Escritor(control);
        this.data = this.escritor.creaDatos();
    }

    /**
     * Procesa el XML con la información sobre los sensores conectados.
     */
    private void procesaXml() {
        try {
            // Abrimos y procesamos el XML
            File fXmlFile = new File("InfoSensores.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);
            doc.getDocumentElement().normalize();

            // Por cada entrada de información
            NodeList nList  = doc.getElementsByTagName("sensor");
            this.baseSensor = new DatosSensor[nList.getLength()];
            for (int i = 0; i < nList.getLength(); i++) {
                Element element = (Element)nList.item(i);
                this.baseSensor[i] = DatosSensor.FromXml(element);
            }
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
    }

    /**
     * Recibe un dato del script en Python que se conecta a los sensores.
     *
     * @return Dato recibido de un sensor.
     */
    private DatosSensor recibeDato() {
        // Recibe un paquete de datos.
        byte[] buffer = new byte[256];
        DatagramPacket paquete = new DatagramPacket(buffer, buffer.length);
        try { socket.receive(paquete); }
        catch (IOException ex) { return null; }

        // Analizo los datos
        String peticion = new String(paquete.getData());
        String[] campos = peticion.split("\\s+");
        String sensorId = campos[0];
        String ninoId   = campos[1];
        double rssi     = Double.parseDouble(campos[2]);

        // Buscamos en el sensores del XML para obtener la posición.
        DatosSensor base = this.buscaBase(sensorId);
        if (base == null)
            return null;

        // Creamos el dato
        return new DatosSensor(base, ninoId, rssi);
    }

    /**
     * Busca la información sobre el sensor con misma MAC.
     *
     * @param sensorId MAC del sensor a buscar.
     * @return Información del sensor o null si no se ha encontrado.
     */
    private DatosSensor buscaBase(String sensorId) {
        for (DatosSensor d : this.baseSensor)
            if (d.getID().equals(sensorId))
                return d;

        return null;
    }

    /**
     * Listener llamado cuando se finaliza la aplicación.
     */
    private static class ShutdownThread extends Thread {
        private final Sensor sensor;

        public ShutdownThread(final Sensor sensor) {
            this.sensor = sensor;
        }

        @Override
        public void run() {
            System.out.println("[Sensor] Parando. . .");
            this.sensor.parar();
            try { this.sensor.join(5000); }
            catch (InterruptedException e) { }
        }
    }
}
