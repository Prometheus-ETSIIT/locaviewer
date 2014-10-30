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

package centroinfantil;

import com.rti.dds.dynamicdata.DynamicData;
import es.prometheus.dds.LectorBase;
import es.prometheus.dds.TopicoControl;

/**
 * Lector para recibir datos del tópico de niño.
 */
public class LectorNino extends LectorBase {
    private static final String EXPRESION = "id = %0, ORDER BY calidad";
    private DatosNino ultDato;
        
    /**
     * Crea una nueva instancia creando un lector sobre el tópico y un lector
     * para la cámara.
     * 
     * @param controlNino Tópico de datos de niños.
     * @param ninoId Clave identificadora del niño.
     */
    public LectorNino(final TopicoControl controlNino, final String ninoId) {
        super(controlNino, EXPRESION, new String[] { "'" + ninoId + "'" });
    }
    
    /**
     * Obtiene el último dato sobre información del niño en el tópico.
     * 
     * @return Dato de niño.
     */
    public DatosNino getUltimoDato() {
        return this.ultDato;
    }
    
    /**
     * Cambia la ID del niño en el que se están obteniendo los datos.
     * 
     * @param ninoId Nuevo ID de niño.
     */
    public void cambiarNinoId(final String ninoId) {
        this.cambioParametros(new String[] { "'" + ninoId + "'" });
        this.ultDato = null;
    }
    
    @Override
    protected void getDatos(DynamicData sample) {
        // Obtengo el dato recibido
        DatosNino datoActual = DatosNino.FromDds(sample);
        this.ultDato = datoActual;
        System.out.println(datoActual.getId() + " -> " + datoActual.getCamId() +
                " [" + datoActual.getPosX() + ", " + datoActual.getPosY() + "]");
    }
}
