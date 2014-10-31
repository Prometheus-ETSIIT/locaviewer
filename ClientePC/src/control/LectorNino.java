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

package control;

import com.rti.dds.dynamicdata.DynamicData;
import es.prometheus.dds.LectorBase;
import es.prometheus.dds.TopicoControl;
import org.gstreamer.swing.VideoComponent;

/**
 * Lector para recibir datos del tópico de niño.
 */
public class LectorNino extends LectorBase {
    private static final String EXPRESION = "id = %0, ORDER BY calidad";

    private final LectorCamara lectorCam;
    private DatosNino ultDato;
    private boolean auto;

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
        this.auto = true;
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
        this.ultDato = null;
    }

    @Override
    public void iniciar() {
        super.iniciar();
        this.lectorCam.iniciar();
    }

    @Override
    public void suspender() {
        super.suspender();
        this.lectorCam.suspender();
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

    /**
     * Establece si se cambia de cámara automáticamente.
     *
     * @param auto Cambio automático de cámara.
     */
    public void setAuto(final boolean auto) {
        this.auto = auto;

        if (this.auto && this.ultDato != null) {
            String[] params = new String[] { "'" + ultDato.getCamId() + "'" };
            this.lectorCam.cambioParametros(params);
        }
    }

    @Override
    protected void getDatos(DynamicData sample) {
        // Obtengo el dato recibido
        DatosNino datoActual = DatosNino.FromDds(sample);
        System.out.println(datoActual.getId() + " -> " + datoActual.getCamId() +
                " [" + datoActual.getPosX() + ", " + datoActual.getPosY() + "]");

        // Cambio la cámara si ha cambiado el ID
        if (this.auto && (this.ultDato == null ||
                !this.ultDato.getCamId().equals(datoActual.getCamId()))) {
            String[] params = new String[] { "'" + datoActual.getCamId() + "'" };
            this.lectorCam.cambioParametros(params);
        }

        this.ultDato = datoActual;
    }
}
