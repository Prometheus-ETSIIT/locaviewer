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

import com.rti.dds.dynamicdata.DynamicDataReader;
import com.rti.dds.dynamicdata.DynamicDataWriter;
import com.rti.dds.infrastructure.StatusKind;
import com.rti.dds.publication.DataWriterQos;
import com.rti.dds.publication.Publisher;
import com.rti.dds.subscription.DataReaderQos;
import com.rti.dds.subscription.Subscriber;
import com.rti.dds.topic.Topic;
import java.util.ArrayList;
import java.util.List;

/**
 * Crea y elimina lectores y escritores sobre un tópico definido en el XML.
 */
public class TopicoControlDinamico extends TopicoControl {
    private final Topic topico;
    private final List<DynamicDataReader> readers = new ArrayList<>();
    private final List<DynamicDataWriter> writers = new ArrayList<>();

    /**
     * Crea una nueva instancia del control de tópico a partir de los nombres
     * del XML del participante y tópico.
     *
     * @param partName Nombre del participante en el XML
     *  (BibliotecaParticipantes::NombreParticipante).
     * @param topicName Nombre del tópico en el XML (NombreTopico).
     */
    protected TopicoControlDinamico(final String partName, final String topicName) {
        super(partName);

        this.topico = (Topic)this.getParticipante().lookup_topicdescription(topicName);
        if (this.topico == null)
            System.err.println("No se pudo recuerar el tópico -> " + topicName);
    }

    @Override
    public void dispose() {
        while (this.readers.size() > 0)
            this.eliminaLector(this.readers.get(0));

        while (this.writers.size() > 0)
            this.eliminaEscritor(this.writers.get(0));

        super.dispose();
    }

    @Override
    public Topic getTopicDescription() {
        return this.topico;
    }

    @Override
    public DynamicDataReader creaLector(DataReaderQos qos) {
        if (qos == null)
            qos = Subscriber.DATAREADER_QOS_DEFAULT;

        DynamicDataReader reader = (DynamicDataReader)this.getParticipante()
                .create_datareader(
                    topico,
                    qos,
                    null,
                    StatusKind.STATUS_MASK_NONE);

        this.readers.add(reader);
        return reader;
    }

    @Override
    public void eliminaLector(DynamicDataReader reader) {
        // Comprueba que sea este controlador el que lo haya creado
        if (!this.readers.contains(reader))
            return;

        // Nos aseguramos de eliminar las condiciones que pueda tener
        reader.delete_contained_entities();

        // Lo eliminamos el tópico.
        reader.get_subscriber().delete_datareader(reader);
        this.getParticipante().delete_datareader(reader);

        // Lo eliminamos de la lista
        this.readers.remove(reader);
    }

    @Override
    public DynamicDataWriter creaEscritor(DataWriterQos qos) {
        if (qos == null)
            qos = Publisher.DATAWRITER_QOS_DEFAULT;

        DynamicDataWriter writer = (DynamicDataWriter)this.getParticipante()
                .create_datawriter(
                    topico,
                    qos,
                    null,
                    StatusKind.STATUS_MASK_NONE);

        this.writers.add(writer);
        return writer;
    }

    @Override
    public void eliminaEscritor(DynamicDataWriter writer) {
        // Comprueba que sea este controlador el que lo haya creado
        if (!this.writers.contains(writer))
            return;

        // Automáticamente desregistrará todas las instancias del writer.
        this.getParticipante().delete_datawriter(writer);

        // Lo eliminamos de la lista
        this.writers.remove(writer);
    }
}
