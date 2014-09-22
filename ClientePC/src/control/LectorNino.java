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

import com.rti.dds.dynamicdata.DynamicData;
import es.prometheus.dds.LectorBase;
import es.prometheus.dds.TopicoControl;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.gstreamer.swing.VideoComponent;

/**
 * Lector para recibir datos del tópico de niño.
 */
public class LectorNino extends LectorBase {
    private static final String EXPRESION = "id = %0, ORDER BY calidad";
    
    private final LectorCamara lectorCam;
    private DatosNino ultDato;
        
    /**
     * Crea una nueva instancia creando un lector sobre el tópico y un lector
     * para la cámara.
     * 
     * @param controlNino Tópico de datos de niños.
     * @param ninoId Clave identificadora del niño.
     * @param controlCam Tópico de datos de cámaras.
     */
    public LectorNino(final TopicoControl controlNino, final String ninoId,
            final TopicoControl controlCam) {
        super(controlNino, EXPRESION, new String[] { "'" + ninoId + "'" });
        this.lectorCam = new LectorCamara(controlCam, "'-1'", new VideoComponent());
    }

    /**
     * Obtiene el lector del tópico de cámaras.
     * 
     * @return Lector de cámaras.
     */
    public LectorCamara getSuscriptorCamara() {
        return this.lectorCam;
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
        
        // Ponemos el ID de la cámara a -1 para que no muestre el vídeo anterior.
        this.lectorCam.cambioParametros(new String[] { "'-1'" });
    }
    
    @Override
    public void parar() {
        super.parar();
        this.lectorCam.parar();
    }
    
    @Override
    public void reanudar() {
        super.reanudar();
        this.lectorCam.reanudar();
    }
    
    @Override
    public void dispose() {
        super.dispose();
        this.lectorCam.dispose();
    }
    
    @Override
    protected void getDatos(DynamicData sample) {
        // Obtengo el dato recibido
        DatosNino datoActual = DatosNino.FromDds(sample);
        System.out.println(datoActual.getId() + " -> " + datoActual.getCamId() +
                " [" + datoActual.getPosX() + ", " + datoActual.getPosY() + "]");
        
        // Cambio la cámara si ha cambiado el ID
        if (this.ultDato == null || !this.ultDato.getCamId().equals(datoActual.getCamId())) {
            String[] params = new String[] { "'" + datoActual.getCamId() + "'" };
            this.lectorCam.cambioParametros(params);
        }
        
        this.ultDato = datoActual;
    }
}
