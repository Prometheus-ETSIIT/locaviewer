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
 * Datos de cambio en descubrimiento.
 */
public class DiscoveryChange {
    private final DiscoveryData data;
    private final DiscoveryChangeStatus status;
    
    /**
     * Nueva instancia con datos de cambio en descubrimiento.
     * 
     * @param data Datos de la entidad descubierta.
     * @param status Estado de descubrimiento de la entidad.
     */
    protected DiscoveryChange(final DiscoveryData data,
            final DiscoveryChangeStatus status) {
        this.data   = data;
        this.status = status;
    }
    
    /**
     * Obtiene los datos de la entidad descubierta.
     * 
     * @return Datos de la entidad descubierta.
     */
    public DiscoveryData getData() {
        return this.data;
    }
    
    /**
     * Obtiene el estado de la entidad descubierta.
     * 
     * @return Estado de la entidad descubierta.
     */
    public DiscoveryChangeStatus getStatus() {
        return this.status;
    }
}
