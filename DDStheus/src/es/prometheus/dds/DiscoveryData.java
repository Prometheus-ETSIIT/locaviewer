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

import com.rti.dds.infrastructure.ByteSeq;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.infrastructure.StringSeq;

/**
 * Datos de las entidades descubiertas.
 */
public class DiscoveryData {
    private final String topicName;
    private final ByteSeq userData;
    private final StringSeq filterParams;
    private final InstanceHandle_t handle;

    /**
     * Crea una nueva instancia con datos de una entidad descubierta.
     * 
     * @param topicName Nombre del tópico en el que está.
     * @param userData Metadatos compartidos.
     * @param filterParams Parámetros del filtro.
     * @param handle Handler para encontrarlo cuando se elimine.
     */
    public DiscoveryData(String topicName, ByteSeq userData,
            StringSeq filterParams, InstanceHandle_t handle) {
        this.topicName = topicName;
        this.userData = userData;
        this.filterParams = filterParams;
        this.handle = handle;
    }

    /**
     * Obtiene el nombre de tópico en el que participa.
     * 
     * @return Nombre de tópico.
     */
    public String getTopicName() {
        return topicName;
    }

    /**
     * Obtiene los metadatos de la entidad.
     * 
     * @return Metdadatos de la entidad.
     */
    public ByteSeq getUserData() {
        return userData;
    }
    
    /**
     * Obtiene los parámetros del CFT.
     * 
     * @return Parámetros del filtro.
     */
    public StringSeq getFilterParams() {
        return filterParams;
    }

    /**
     * Obtiene el manejador de la entidad.
     * 
     * @return Manejador de la entidad.
     */
    public InstanceHandle_t getHandle() {
        return handle;
    }
}
