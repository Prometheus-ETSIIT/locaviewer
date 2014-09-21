/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control;

import com.rti.dds.dynamicdata.DynamicData;
import org.gstreamer.swing.VideoComponent;

/**
 *
 */
public class SuscriptorNino extends SuscriptorBase {
    private static final String EXPRESION = "id = %0 ORDER BY calidad";
    
    private final SuscriptorCamara suscriptorCam;
    private DatosNino ultDato;
        
    public SuscriptorNino(final TopicoControl controlNino, final String ninoId,
            final TopicoControl controlCam) {
        super(controlNino, EXPRESION, new String[] { "'" + ninoId + "'" });
        this.suscriptorCam = new SuscriptorCamara(controlCam, "-1", new VideoComponent());
    }

    public SuscriptorCamara getSuscriptorCamara() {
        return this.suscriptorCam;
    }
    
    @Override
    public void parar() {
        super.parar();
        this.suscriptorCam.parar();
    }
    
    @Override
    public void reanudar() {
        super.reanudar();
        this.suscriptorCam.reanudar();
    }
    
    @Override
    public void dispose() {
        super.dispose();
        this.suscriptorCam.dispose();
    }
    
    @Override
    protected void getDatos(DynamicData sample) {
        // Obtengo el dato recibido
        DatosNino datoActual = DatosNino.FromDds(sample);
        
        // Cambio la cámara si ha cambiado el ID
        if (this.ultDato == null || !this.ultDato.getCamId().equals(datoActual.getCamId())) {
            String[] params = new String[] { "'" + datoActual.getCamId() + "'" };
            this.suscriptorCam.cambioParametros(params);
        }
        
        this.ultDato = datoActual;
    }
}
