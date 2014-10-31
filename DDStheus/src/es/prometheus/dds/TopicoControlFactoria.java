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

/**
 * Factoria de controles de tópico.
 * Mediante este patrón se pretende que las hebras creen los controles de
 * forma síncrona para que sólo se cree un participante por programa.
 */
public class TopicoControlFactoria {

    /**
     * Crea un control de tópico dinámico.
     * Los lectores y escritores se crean según se necesiten.
     *
     * @param partName Nombre de participante.
     * @param topicName Nombre de tópico.
     * @return Control de tópico dinámico.
     */
    public synchronized static TopicoControl crearControlDinamico(
            final String partName, final String topicName) {
        return new TopicoControlDinamico(partName, topicName);
    }

    /**
     * Crea un control de tópico fijo.
     * Los lectores y escritores ya han sido creados previamente en el XML.
     *
     * @param partName Nombre de participante.
     * @param suscripName Nombre de suscriptor.
     * @param publiName Nombre de publicador.
     * @return Control de tópico fijo.
     */
    public synchronized static TopicoControl crearControlFijo(
        final String partName, final String suscripName, final String publiName) {
        return new TopicoControlFijo(partName, suscripName, publiName);
    }
}
