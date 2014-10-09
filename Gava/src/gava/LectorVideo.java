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

package gava;

import com.rti.dds.dynamicdata.DynamicData;
import com.rti.dds.infrastructure.ByteSeq;
import es.prometheus.dds.LectorBase;
import es.prometheus.dds.TopicoControlFactoria;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JFrame;
import org.gstreamer.Buffer;
import org.gstreamer.ClockTime;
import org.gstreamer.Element;
import org.gstreamer.ElementFactory;
import org.gstreamer.Format;
import org.gstreamer.Pipeline;
import org.gstreamer.elements.AppSrc;
import org.gstreamer.swing.VideoComponent;

/**
 * 
 */
public class LectorVideo extends LectorBase {
    private static final String EXPRESION = "camId = %0";
    
    private Pipeline pipe;
    private AppSrc appsrc;
    private JFrame frame;
    
    public LectorVideo(final String camId) {
        // Inicia DDS creando un control de tópico dinámico
        super(
            TopicoControlFactoria.crearControlDinamico(
                "MyParticipantLibrary::PublicationParticipant", "VideoDataTopic"),
            EXPRESION,
            new String[] { "'" + camId + "'" }
        );
        
        this.iniciaGStreamer();
    }
    
    @Override
    public void dispose() {
        super.dispose();
        this.getTopicoControl().dispose();
        
        org.gstreamer.StateChangeReturn retState = this.pipe.stop();
        if (retState == org.gstreamer.StateChangeReturn.FAILURE)
            System.err.println("Error al parar.");
        
        this.pipe.dispose();
        this.appsrc.dispose();
        
        this.frame.setVisible(false);
        this.frame.dispose();
    }
    
    private void iniciaGStreamer() {
        // Crea los elementos de la tubería
        // 1º Origen de vídeo, simulado porque se inyectan datos.
        this.appsrc = (AppSrc)ElementFactory.make("appsrc", null);
        
        // 2º Decodificación
        Element videoconvert = ElementFactory.make("ffmpegcolorspace", null);
        Element codec = ElementFactory.make("jpegdec", null);
        
        // 3º Salida de vídeo
        VideoComponent videoComponent = new VideoComponent();
        Element videosink = videoComponent.getElement();
       
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
        
        // Crea la ventana y la muestra
        this.frame = new JFrame("Gava suscriptor testing");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(videoComponent, BorderLayout.CENTER);
        videoComponent.setPreferredSize(new Dimension(720, 576));
        frame.pack();
        frame.setVisible(true);
        
        // Play!
        // Cambiar el estado puede tomar hasta 5 segundos. Comprueba errores.
        this.pipe.play();
        org.gstreamer.State retState = this.pipe.getState(ClockTime.fromSeconds(5).toSeconds());
        if (retState == org.gstreamer.State.NULL) {
            System.err.println("Error al cambio de estado.");
            System.exit(1);
        }
        
        //GstDebugUtils.gstDebugBinToDotFile(pipe, 0, "suscriptor");
    }

    @Override
    public void getDatos(DynamicData sample) {
        // Deserializa los datos
        // DEBUG: sample.print(null, 0); // Para mostrarlo formateado por la consola
        String camId = sample.get_string("camId", DynamicData.MEMBER_ID_UNSPECIFIED);
        String sala  = sample.get_string("sala", DynamicData.MEMBER_ID_UNSPECIFIED);
        double posX  = sample.get_double("posX", DynamicData.MEMBER_ID_UNSPECIFIED);
        double posY  = sample.get_double("posY", DynamicData.MEMBER_ID_UNSPECIFIED);
        double angle = sample.get_double("angle", DynamicData.MEMBER_ID_UNSPECIFIED);
        String codecInfo = sample.get_string("codecInfo", DynamicData.MEMBER_ID_UNSPECIFIED);
        int width  = sample.get_int("width", DynamicData.MEMBER_ID_UNSPECIFIED);
        int height = sample.get_int("height", DynamicData.MEMBER_ID_UNSPECIFIED);

        // Crea el buffer de GStreamer
        ByteSeq bufferSeq = new ByteSeq();
        sample.get_byte_seq(bufferSeq, "buffer", DynamicData.MEMBER_ID_UNSPECIFIED);

        Buffer buffer = new Buffer(bufferSeq.size());
        buffer.getByteBuffer().put(bufferSeq.toArrayByte(null));

        // Lo mete en la tubería
        if (this.appsrc != null)
            this.appsrc.pushBuffer(buffer);
    }
}
