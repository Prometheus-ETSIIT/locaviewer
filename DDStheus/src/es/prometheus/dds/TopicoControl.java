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
