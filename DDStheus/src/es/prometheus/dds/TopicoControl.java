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

import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.dynamicdata.DynamicDataReader;
import com.rti.dds.dynamicdata.DynamicDataWriter;
import com.rti.dds.infrastructure.StringSeq;
import com.rti.dds.publication.DataWriterQos;
import com.rti.dds.subscription.DataReaderQos;
import com.rti.dds.topic.ContentFilteredTopic;
import com.rti.dds.topic.Topic;
import java.util.Arrays;

/**
 * Clase para crear y destruir lectores y escritores sobre un tópico.
 */
public abstract class TopicoControl {   

    private final Participante participante;
    
    /**
     * Crea una nueva instancia de control de tópico.
     * 
     * @param partName Nombre del participante en el XML
     *  (BibliotecaParticipantes::NombreParticipante).
     */
    protected TopicoControl(final String partName) {                
        this.participante = Participante.GetInstance(partName);
    }
    
    /**
     * Libera recursos del sistema.
     */
    public void dispose() {
        this.participante.dispose();
    }
    
    /**
     * Crea un ContentFilteredTopic a partir del tópico actual.
     * 
     * @param name Nombre del nuevo TopicDescription.
     * @param expr Expresión de filtrado.
     * @param params Parámetros de la expresión de filtrado.
     * @return Nuevo tópico con filtro.
     */
    public ContentFilteredTopic createCFT(final String name, final String expr,
            final String[] params) {
       ContentFilteredTopic topic = (ContentFilteredTopic)this.participante
               .getParticipante().lookup_topicdescription(name);
       if (topic != null)
           return topic;
        
        return this.participante.getParticipante().create_contentfilteredtopic(
                name,
                this.getTopicDescription(),
                expr,
                new StringSeq(Arrays.asList(params))
        );
    }
    
    /**
     * Obtiene el participante del dominio.
     * 
     * @return Participante del dominio.
     */
    public DomainParticipant getParticipante() {
        return this.participante.getParticipante();
    }
    
    /**
     * Obtiene el controlador de participante.
     * 
     * @return Controlador de participante.
     */
    public Participante getParticipanteControl() {
        return this.participante;
    }
    
    /**
     * Obtiene el tópico asociado.
     * 
     * @return Tópico.
     */
    public abstract Topic getTopicDescription();
    
    /**
     * Crear un lector del tópico.
     * 
     * @param qos QOS utilizado para crear el escritor.
     * @return Lector del tópico.
     */
    public abstract DynamicDataReader creaLector(final DataReaderQos qos);
    
    /**
     * Elimina un lector de este tópico.
     * 
     * @param reader Lector del tópico.
     */
    public abstract void eliminaLector(final DynamicDataReader reader);
    
    /**
     * Crea un escritor del tópico.
     * 
     * @param qos QOS utilizado para crear el escritor.
     * @return Escritor del tópico.
     */
    public abstract DynamicDataWriter creaEscritor(final DataWriterQos qos);
    
    /**
     * Elimina un escritor del este tópico.
     * 
     * @param writer Escritor del tópico.
     */
    public abstract void eliminaEscritor(final DynamicDataWriter writer);
}
