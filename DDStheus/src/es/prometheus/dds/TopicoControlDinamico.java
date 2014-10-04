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
    public DynamicDataReader creaLector() {
        DynamicDataReader reader = (DynamicDataReader)this.getParticipante()
                .create_datareader(
                    topico,
                    Subscriber.DATAREADER_QOS_DEFAULT,
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
        
        // Lo eliminamos de la lista
        this.readers.remove(reader);
    }

    @Override
    public DynamicDataWriter creaEscritor() {
        DynamicDataWriter writer = (DynamicDataWriter)this.getParticipante()
                .create_datawriter(
                    topico,
                    Publisher.DATAWRITER_QOS_DEFAULT,
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
