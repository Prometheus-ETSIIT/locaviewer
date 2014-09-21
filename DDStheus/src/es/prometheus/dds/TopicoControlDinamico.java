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

package es.prometheus.dds;

import com.rti.dds.dynamicdata.DynamicDataReader;
import com.rti.dds.dynamicdata.DynamicDataWriter;
import com.rti.dds.infrastructure.StatusKind;
import com.rti.dds.publication.Publisher;
import com.rti.dds.subscription.Subscriber;
import com.rti.dds.topic.Topic;

/**
 * Crea y elimina lectores y escritores sobre un tópico definido en el XML.
 */
public class TopicoControlDinamico extends TopicoControl {
    private final Topic topico;
    
    /**
     * Crea una nueva instancia del control de tópico a partir de los nombres
     * del XML del participante y tópico.
     * NOTA: En el XML se tiene que definir un Topic, no un ContentFilterTopic.
     * 
     * @param partName Nombre del participante en el XML
     *  (BibliotecaParticipantes::NombreParticipante).
     * @param topicName Nombre del tópico en el XML
     *  (NombreDominio::NombreTopico).
     */
    public TopicoControlDinamico(final String partName, final String topicName) {
        super(partName);
        
        this.topico = (Topic)this.getParticipante().lookup_topicdescription(topicName);
    }

    @Override
    public DynamicDataReader creaLector() {
        return (DynamicDataReader)this.getParticipante().create_datareader(
                topico,
                Subscriber.DATAREADER_QOS_DEFAULT,
                null,
                StatusKind.STATUS_MASK_NONE);
    }

    @Override
    public void eliminaLector(DynamicDataReader reader) {
        // Nos aseguramos de eliminar las condiciones que pueda tener
        reader.delete_contained_entities();
        
        // Lo eliminamos el tópico.
        reader.get_subscriber().delete_datareader(reader);
    }

    @Override
    public DynamicDataWriter creaEscritor() {
        return (DynamicDataWriter)this.getParticipante().create_datawriter(
                topico,
                Publisher.DATAWRITER_QOS_DEFAULT,
                null,
                StatusKind.STATUS_MASK_NONE);
    }

    @Override
    public void eliminaEscritor(DynamicDataWriter writer) {
        // Automáticamente desregistrará todas las instancias del writer.
        this.getParticipante().delete_datawriter(writer);
    }
}
