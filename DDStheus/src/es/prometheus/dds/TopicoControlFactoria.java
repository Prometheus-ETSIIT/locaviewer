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
