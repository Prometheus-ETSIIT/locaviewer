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
import org.gstreamer.Buffer;
import org.gstreamer.ClockTime;
import org.gstreamer.Element;
import org.gstreamer.ElementFactory;
import org.gstreamer.Format;
import org.gstreamer.Pipeline;
import org.gstreamer.State;
import org.gstreamer.elements.AppSrc;
import org.gstreamer.swing.VideoComponent;

/**
 * Suscriptor de tópico de cámaras.
 * Recibe datos de DDS y va actualizando el componente de vídeo de GStreamer.
 */
public class LectorCamara extends LectorBase {
    private final VideoComponent videocomp;
    private DatosCamara ultDatos;
    
    private Pipeline pipe;
    private AppSrc appsrc;
    
    /**
     * Crea una nueva instancia del suscriptor de cámara.
     * 
     * @param control Control de tópico.
     * @param key Clave para discernir los datos en el tópico.
     * @param videocomp Componente de vídeo a actualizar.
     */
    public LectorCamara(final TopicoControl control, final String key,
            final VideoComponent videocomp) {
        super(control, "camId = %0", new String[] { "'" + key + "'" });
        this.videocomp = videocomp;
        
        this.creaTuberia();
        
        // Play!
        // Cambiar el estado puede tomar hasta 5 segundos. Comprueba errores.
        this.pipe.play();
        State retState = this.pipe.getState(ClockTime.fromSeconds(5).toSeconds());
        if (retState == State.NULL) {
            System.err.println("Error al cambio de estado.");
            System.exit(1);
        }
    }
    
    /**
     * Crea la tubería (pipe) de GStreamer.
     */
    private void creaTuberia() {
        // Crea los elementos de la tubería
        // 1º Origen de vídeo, simulado porque se inyectan datos.
        this.appsrc = (AppSrc)ElementFactory.make("appsrc", null);
        
        // 2º Decodificación
        Element videoconvert = ElementFactory.make("ffmpegcolorspace", null);
        Element codec = ElementFactory.make("jpegdec", null);
       
        // 3º Salida de vídeo
        Element videosink = this.videocomp.getElement();
        
        // Crea la tubería
        this.pipe = new Pipeline();
        this.pipe.addMany(this.appsrc, codec, videoconvert, videosink);
        Element.linkMany(this.appsrc, codec, videoconvert, videosink);
        
        // Configura el APPSRC
        appsrc.setLive(true);
        appsrc.setLatency(0, 100);
        appsrc.setTimestamp(true);
        appsrc.setFormat(Format.TIME);
        appsrc.setStreamType(AppSrc.Type.STREAM);
    }
    
    @Override
    public void dispose() {
        super.dispose();
        
        // Para la tuberia
        this.pipe.stop();
        this.pipe.dispose();
    }
    
    /**
     * Obtiene el último dato recibido en el tópico.
     * 
     * @return Dato recibido.
     */
    public DatosCamara getUltimoDato() {
        return this.ultDatos;
    }
    
    /**
     * Obtiene el componente para ver el vídeo.
     * 
     * @return Componente de vídeo.
     */
    public VideoComponent getVideoComponent() {
        return this.videocomp;
    }
    
    @Override
    protected void getDatos(DynamicData sample) {
        this.ultDatos = DatosCamara.FromDds(sample);
        
        Buffer buffer = new Buffer(this.ultDatos.getBuffer().length);
        buffer.getByteBuffer().put(this.ultDatos.getBuffer());

        // Lo mete en la tubería
        this.appsrc.pushBuffer(buffer);
    }
}
