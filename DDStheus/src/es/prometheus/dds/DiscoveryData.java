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
        this.topicName = topicName.toString();
        this.userData = new ByteSeq(userData.toArrayByte(null));
        this.handle = new InstanceHandle_t(handle);
        this.filterParams = (filterParams != null) ?
                (StringSeq)filterParams.clone() : null;
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
