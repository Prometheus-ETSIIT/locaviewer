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
public class TopicoControl {   
    private final static int MAX_LECTORES = 4;
    
    private final DomainParticipant participante;
    private final String[] keys;
    private final DynamicDataReader[] lectoresUsados = new DynamicDataReader[MAX_LECTORES];
    
    /**
     * Crea una nueva instancia de control de tópico.
     * 
     * @param keys Claves para discernir los datos en el tópico.
     */
    public TopicoControl(final String[] keys) {
        // Crea un participante de dominio
        this.participante = DomainParticipantFactory.get_instance()
                .create_participant_from_config("MyParticipantLibrary::SubscriptionParticipant");
        if (this.participante == null) {
            System.err.println("No se pudo obtener el dominio.");
            System.exit(1);
        }
        
        // Las claves se usan para filtrar datos en los lectores y obtener
        // vídeo de la cámara que se quiere. Este filtro se añadirá al crear
        // un lector.
        this.keys = keys;
    }
    
    /**
     * Libera recursos del sistema.
     */
    public void dispose() {
        this.participante.delete_contained_entities();
        DomainParticipantFactory.get_instance().delete_participant(this.participante);
    }
    
    /**
     * Número de claves de las que se dispone para discernir datos.
     * 
     * @return Claves.
     */
    public int getNumKeys() {
        return this.keys.length;
    }

    /**
     * Obtiene las claves para discernir datos en el tópico.
     * 
     * @return Claves para discernir datos.
     */
    public String[] getKeys() {
        return this.keys;
    }
    
    /**
     * Crear un lector del tópico.
     * 
     * @return Lector del tópico.
     */
    public DynamicDataReader creaLector() {
        // Primero buscamos un lector que no se esté usando
        int i;
        for (i = 0; i < this.lectoresUsados.length; i++)
            if (this.lectoresUsados[i] == null)
                break;
        
        this.lectoresUsados[i] = (DynamicDataReader)this.participante
                .lookup_datareader_by_name("MySubscriber::VideoDataReader" + i);
        
        if (this.lectoresUsados[i] == null)
            System.out.println("No se pudo crear el lector.");
        
        return this.lectoresUsados[i];
    }
    
    /**
     * Elimina un lector de este tópico.
     * 
     * @param reader Lector del tópico.
     */
    public void eliminaLector(final DynamicDataReader reader) {
        // Lo libera de los usados
        for (int i = 0; i < this.lectoresUsados.length; i++)
            if (this.lectoresUsados[i] == reader)
                this.lectoresUsados[i] = null;
        
        // NOTA:
        // NO se puede eliminar del dominio porque si no no se podría volver a
        // recuperar. Simplemente le quitamos el listener y las condiciones.
    }
}
