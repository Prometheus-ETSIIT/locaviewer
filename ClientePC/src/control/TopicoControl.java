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

package control;

import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.DomainParticipantFactory;
import com.rti.dds.dynamicdata.DynamicDataReader;

/**
 * Clase para crear y destruir lectores sobre un tópico.
 */
public abstract class TopicoControl {   

    private final DomainParticipant participante;
    
    /**
     * Crea una nueva instancia de control de tópico.
     * 
     * @param partName Nombre del participante en el XML.
     */
    protected TopicoControl(final String partName) {
        // Crea un participante de dominio
        this.participante = DomainParticipantFactory.get_instance()
                .create_participant_from_config(partName);
        if (this.participante == null) {
            System.err.println("No se pudo obtener el participante.");
            System.exit(1);
        }
    }
    
    /**
     * Libera recursos del sistema.
     */
    public void dispose() {
        this.participante.delete_contained_entities();
        DomainParticipantFactory.get_instance().delete_participant(this.participante);
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
}
