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

import com.rti.dds.dynamicdata.DynamicData;
import com.rti.dds.dynamicdata.DynamicDataProperty_t;
import com.rti.dds.dynamicdata.DynamicDataWriter;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.publication.DataWriterAdapter;
import com.rti.dds.publication.DataWriterQos;

/**
 * Clase para escribir datos dinámicos en un tópico.
 */
public class Escritor {
    private final TopicoControl control;
    private final DynamicDataWriter writer;
    
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
    }
    
    /**
     * Libera recursos de este escritor.
     */
    public void dispose() {
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
        this.writer.write(data, inst);
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
}
