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
import com.rti.dds.domain.DomainParticipantFactory;
import com.rti.dds.dynamicdata.DynamicDataReader;
import com.rti.dds.dynamicdata.DynamicDataWriter;
import com.rti.dds.infrastructure.StringSeq;
import com.rti.dds.topic.ContentFilteredTopic;
import com.rti.dds.topic.Topic;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Clase para crear y destruir lectores y escritores sobre un tópico.
 */
public abstract class TopicoControl {   

    private static Map<DomainParticipant, Integer> Instances = new HashMap<>();
    private DomainParticipant participante;
    
    /**
     * Crea una nueva instancia de control de tópico.
     * 
     * @param partName Nombre del participante en el XML
     *  (BibliotecaParticipantes::NombreParticipante).
     */
    protected TopicoControl(final String partName) {                
        // Buscamos si ya está creado el participante para recuperarlo.
        // ¡Se asume que sólo se permite UN dominio!
        String nombre = partName.substring(partName.indexOf("::") + 2);
        this.participante = DomainParticipantFactory.get_instance()
                .lookup_participant_by_name(nombre);
        
        // No ha sido creado previamente -> Crea un participante de dominio.
        if (this.participante == null) {
            this.participante = DomainParticipantFactory.get_instance()
                    .create_participant_from_config(partName);
            
            if (this.participante == null) {
                System.err.println("No se puedo crear ni recuperar el participante.");
                System.exit(1);
            }
        }
        
        // Añade la instancia
        int num = (Instances.containsKey(participante) ? Instances.get(participante) : 0);
        Instances.put(participante, num + 1);
    }
    
    /**
     * Libera recursos del sistema.
     */
    public void dispose() {
        int num = Instances.get(participante);
        Instances.put(participante, --num);
        
        if (num == 0) {
            this.participante.delete_contained_entities();
            DomainParticipantFactory.get_instance().delete_participant(this.participante);
        }
    }
    
    public ContentFilteredTopic createCFT(final String name, final String expr,
            final String[] params) {
       ContentFilteredTopic topic = (ContentFilteredTopic)this.participante
               .lookup_topicdescription(name);
       if (topic != null)
           return topic;
        
        return this.participante.create_contentfilteredtopic(
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
    protected DomainParticipant getParticipante() {
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
     * @return Lector del tópico.
     */
    public abstract DynamicDataReader creaLector();
    
    /**
     * Elimina un lector de este tópico.
     * 
     * @param reader Lector del tópico.
     */
    public abstract void eliminaLector(final DynamicDataReader reader);
    
    /**
     * Crea un escritor del tópico.
     * 
     * @return Escritor del tópico.
     */
    public abstract DynamicDataWriter creaEscritor();
    
    /**
     * Elimina un escritor del este tópico.
     * 
     * @param writer Escritor del tópico.
     */
    public abstract void eliminaEscritor(final DynamicDataWriter writer);
}
