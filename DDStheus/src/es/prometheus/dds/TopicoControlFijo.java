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
import com.rti.dds.publication.DataWriterSeq;
import com.rti.dds.publication.Publisher;
import com.rti.dds.subscription.DataReaderQos;
import com.rti.dds.subscription.DataReaderSeq;
import com.rti.dds.subscription.Subscriber;
import com.rti.dds.topic.Topic;
import java.util.HashMap;
import java.util.Map;

/**
 * Crea y reutiliza lectores y escritores predefinidos en el XML.
 */
public class TopicoControlFijo extends TopicoControl {
    private final Subscriber suscriptor;
    private final Map<DynamicDataReader, Boolean> lectores;

    private final Publisher publicador;
    private final Map<DynamicDataWriter, Boolean> escritores;

    private Topic topic;

    /**
     * Crea una nueva instancia del control de tópico a partir de los nombres
     * del XML del participante, suscriptor y publicador.
     *
     * @param partName Nombre del participante en el XML
     *  (BibliotecaParticipantes::NombreParticipante).
     * @param suscripName Nombre del suscriptor en el XML
     *  (NombreParticipante::NombreSuscriptor).
     * @param publiName Nombre del publicador en el XML
     *  (NombreParticipante::NombrePublicador).
     */
    protected TopicoControlFijo(final String partName, final String suscripName,
            final String publiName) {
        super(partName);

        // Obtiene los lectores del XML.
        this.lectores = new HashMap<>();
        if (suscripName != null) {
            // Primero obtiene el suscriptor que los contiene.
            this.suscriptor = this.getParticipante().lookup_subscriber_by_name(suscripName);

            DataReaderSeq lectoresSeq = new DataReaderSeq();
            this.suscriptor.get_all_datareaders(lectoresSeq);
            for (Object reader : lectoresSeq.toArray()) {
                this.lectores.put((DynamicDataReader)reader, false);
                if (this.topic == null)
                    this.topic = (Topic)((DynamicDataReader)reader).get_topicdescription();
            }
        } else {
            this.suscriptor = null;
        }

        // Obtiene los escritores del XML.
        this.escritores = new HashMap<>();
        if (publiName != null) {
            // Primer obtiene el publicador que los contiene.
            this.publicador = this.getParticipante().lookup_publisher_by_name(publiName);

            DataWriterSeq escritoresSeq = new DataWriterSeq();
            this.publicador.get_all_datawriters(escritoresSeq);
            for (Object writer : escritoresSeq) {
                this.escritores.put((DynamicDataWriter)writer, false);
                if (this.topic == null)
                    this.topic = ((DynamicDataWriter)writer).get_topic();
            }
        } else {
            this.publicador = null;
        }
    }

    @Override
    public Topic getTopicDescription() {
        return this.topic;
    }

    @Override
    public DynamicDataReader creaLector(final DataReaderQos qos) {
        // Buscamos un lector que no esté en uso
        for (DynamicDataReader reader : this.lectores.keySet()) {
            // Comprobamos su estado y si no está en uso lo usamos
            if (!this.lectores.get(reader)) {
                this.lectores.put(reader, true);
                return reader;
            }
        }

        System.err.println("No hay suficiente lectores definidos en el XML.");
        return null;
    }

    @Override
    public void eliminaLector(final DynamicDataReader reader) {
        // Nos aseguramos de que no tenga listener ni condición
        reader.set_listener(null, StatusKind.STATUS_MASK_NONE);
        reader.delete_contained_entities();

        // Marca el lector como disponible
        for (DynamicDataReader mapReader : this.lectores.keySet()) {
            if (mapReader == reader) {
                this.lectores.put(reader, false);
                return;
            }
        }
    }

    @Override
    public DynamicDataWriter creaEscritor(final DataWriterQos qos) {
        // Buscamos un escritor que no esté en uso
        for (DynamicDataWriter writer : this.escritores.keySet()) {
            // Comprobamos su estado y si no está en uso lo usamos
            if (!this.escritores.get(writer)) {
                this.escritores.put(writer, true);
                return writer;
            }
        }

        System.err.println("No hay suficiente escritores definidos en el XML.");
        return null;
    }

    @Override
    public void eliminaEscritor(final DynamicDataWriter writer) {
        // Nos aseguramos de que no tenga listener
        writer.set_listener(null, StatusKind.STATUS_MASK_NONE);

        // Marca el lector como disponible
        for (DynamicDataWriter mapWriter : this.escritores.keySet()) {
            if (mapWriter == writer) {
                this.escritores.put(writer, false);
                return;
            }
        }
    }
}
