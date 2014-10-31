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

package es.prometheus.dds;

import com.rti.dds.dynamicdata.DynamicData;
import com.rti.dds.dynamicdata.DynamicDataProperty_t;
import com.rti.dds.dynamicdata.DynamicDataWriter;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.publication.DataWriterAdapter;
import com.rti.dds.publication.DataWriterQos;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Clase para escribir datos dinámicos en un tópico.
 */
public class Escritor {
    private final TopicoControl control;
    private final DynamicDataWriter writer;
    private final EscritorQueue cola;

    /**
     * Crea una nueva instancia del escritor sobre el tópico especificado.
     *
     * @param control Tópico con el que se creará el escritor.
     */
    public Escritor(final TopicoControl control) {
        this.control = control;
        this.writer  = control.creaEscritor(null);
        if (this.writer == null) {
            System.err.println("No se pudo crear el escritor");
            System.exit(1);
        }

        this.cola = new EscritorQueue(this.writer);
    }

    /**
     * Crea una nueva instancia del escritor sobre el tópico especificado.
     *
     * @param control Tópico con el que se creará el escritor.
     * @param qos QOS usado para crear el escritor.
     */
    public Escritor(final TopicoControl control, final DataWriterQos qos) {
        this.control = control;
        this.writer  = control.creaEscritor(qos);
        if (this.writer == null) {
            System.err.println("No se pudo crear el escritor");
            System.exit(1);
        }

        this.cola = new EscritorQueue(this.writer);
    }

    /**
     * Crea una nueva instancia del escritor sobre el tópico especificado.
     *
     * @param control Tópico con el que se creará el escritor.
     * @param user_data USER_DATA a introducir en el QOS del escritor.
     */
    public Escritor(final TopicoControl control, final byte[] user_data) {
        this.control = control;

        // Crea el QOS con USER_DATA.
        DataWriterQos qos = new DataWriterQos();
        control.getParticipante().get_default_datawriter_qos(qos);
        qos.user_data.value.clear();
        qos.user_data.value.addAllByte(user_data);

        // Crea el escritor.
        this.writer = control.creaEscritor(qos);
        if (this.writer == null) {
            System.err.println("No se pudo crear el escritor");
            System.exit(1);
        }

        this.cola = new EscritorQueue(this.writer);
    }

    /**
     * Libera recursos de este escritor.
     */
    public void dispose() {
        this.cola.para();
        try { this.cola.join(5000); }
        catch (InterruptedException ex) { }

        this.control.eliminaEscritor(this.writer);
    }

    /**
     * Establece el listener con la máscara.
     *
     * @param listener Listener del escritor
     * @param status Máscara para recepción de eventos.
     */
    public void setListener(DataWriterAdapter listener, int status) {
        this.writer.set_listener(listener, status);
    }

    /**
     * Crea una estructura de datos como la definida en el XML.
     * Se puede reusar para varias escrituras siempre y cuando se llame a
     * clear_all_members().
     *
     * @return Estructura de datos para enviar.
     */
    public DynamicData creaDatos() {
        // Para que la estructura pueda tener más de 64 KB de datos
        // (aunque sólo se utilizarán los que se necesiten, no el máximo).
        DynamicDataProperty_t propiedades = new DynamicDataProperty_t();
        propiedades.buffer_initial_size = 100;
        propiedades.buffer_max_size = 1048576;

        // Crea una estructura de datos como la que hemos definido en el XML.
        DynamicData data = this.writer.create_data(propiedades);
        if (data == null) {
            System.err.println("No se pudo crear la instancia de datos.");
            System.exit(1);
        }

        return data;
    }

    /**
     * Registra una esctructura de datos devolviendo su manejador.
     * De esta forma se puede mejorar el rendimiento.
     * De la estructura sólo se mira los parámetros "key".
     *
     * @param data Estructura de datos.
     * @return Manejador de la estructura.
     */
    public InstanceHandle_t registraDatos(DynamicData data) {
        return this.writer.register_instance(data);
    }

    /**
     * Escribe en DDS una estructura de datos.
     *
     * @param data Datos a escribir.
     */
    public void escribeDatos(final DynamicData data) {
        this.escribeDatos(data, InstanceHandle_t.HANDLE_NIL);
    }

    /**
     * Escribe en DDS una estructura de datos.
     *
     * @param data Datos a escribir.
     * @param inst Manejador asociado a la estructura. En caso de ser
     * InstanceHandle_t.HANDLE_NIL se buscará el manejador en DDS.
     */
    public void escribeDatos(final DynamicData data, final InstanceHandle_t inst) {
        if (!this.cola.isAlive())
            this.cola.start();

        this.cola.anadeMuestra(data, inst);
    }

    /**
     * Elimina y libera los recursos de una estructura de datos.
     *
     * @param data Datos a eliminar.
     */
    public void eliminaDatos(final DynamicData data) {
        data.clear_all_members();
        this.writer.delete_data(data);
    }

    private static class EscritorQueue extends Thread {
        private final Queue<EscritorData> cola = new LinkedList<>();
        private final DynamicDataWriter writer;
        private boolean parar = false;

        public EscritorQueue(DynamicDataWriter writer) {
            super();
            this.writer = writer;
        }

        public synchronized void para() {
            this.parar = true;
            this.notifyAll();
        }

        @Override
        public void run() {
            while (!this.parar) {
                // Escribe todas las muestras
                while (!this.cola.isEmpty()) {
                    if (this.cola.size() > 300)
                        System.out.println("[DDStheus]: La cola es inestable");

                    EscritorData data;
                    synchronized(this.cola) { data = this.cola.remove(); }
                    this.writer.write(data.data, data.instance);
                 }

                 // Espera a más muestras
                 synchronized(this) {
                    while (this.cola.isEmpty()) {
                         try { this.wait(); }
                         catch (InterruptedException ex) { }
                    }
                 }
            }
        }

        public synchronized void anadeMuestra(final DynamicData data,
                final InstanceHandle_t inst) {
            this.cola.offer(new EscritorData(data, inst));
            this.notifyAll();
        }
    }

    private static class EscritorData {
        public final DynamicData data;
        public final InstanceHandle_t instance;

        public EscritorData(final DynamicData data, final InstanceHandle_t inst) {
            this.data = data;
            this.instance = inst;
        }
    }
}
