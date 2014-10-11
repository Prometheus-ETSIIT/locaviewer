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
import es.prometheus.dds.LectorBase;
import es.prometheus.dds.TopicoControlFactoria;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JFrame;
import org.gstreamer.Buffer;
import org.gstreamer.Caps;
import org.gstreamer.ClockTime;
import org.gstreamer.Element;
import org.gstreamer.ElementFactory;
import org.gstreamer.Format;
import org.gstreamer.Pipeline;
import org.gstreamer.elements.AppSrc;
import org.gstreamer.swing.VideoComponent;

/**
 * Obtiene vídeo de DDS y lo muestra en una nueva ventana.
 */
public class LectorVideo extends LectorBase {
    private static final String EXPRESION = "camId = %0";
    
    private String codecName;
    private Pipeline pipe;
    private AppSrc appsrc;
    private JFrame frame;
    
    /**
     * Crea una nueva instancia del lector a partir del ID de la cámara a ver.
     * 
     * @param camId 
     */
    public LectorVideo(final String camId) {
        // Inicia DDS creando un control de tópico dinámico
        super(
            TopicoControlFactoria.crearControlDinamico(
                "MyParticipantLibrary::PublicationParticipant", "VideoDataTopic"),
            EXPRESION,
            new String[] { "'" + camId + "'" }
        );
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
    }
    
    /**
     * Crea la tubería de GStreamer.
     */
    private void iniciaGStreamer() {
        // Crea los elementos de la tubería
        List<Element> elements = new ArrayList<>();
        
        // 1º Origen de vídeo, simulado porque se inyectan datos.
        this.appsrc = (AppSrc)ElementFactory.make("appsrc", null);
        this.appsrc.setLive(true);
        //this.appsrc.setLatency(0, 100000000);
        this.appsrc.setTimestamp(true);
        this.appsrc.setFormat(Format.TIME);
        this.appsrc.setStreamType(AppSrc.Type.STREAM);
        elements.add(this.appsrc);
    
        // 2º Códec
        Element[] codecs = null;
        switch (this.codecName) {
            case "JPEG": codecs = this.getDecJpeg(); break;
            case "VP8":  codecs = this.getDecVp8();  break;
        }
        elements.addAll(Arrays.asList(codecs));
        
        // 3º Salida de vídeo
        VideoComponent videoComponent = new VideoComponent();
        Element videosink = videoComponent.getElement();
        elements.add(videosink);
        
        // Crea la tubería
        this.pipe = new Pipeline();
        this.pipe.addMany(elements.toArray(new Element[0]));
        Element.linkMany(elements.toArray(new Element[0]));
        //GstDebugUtils.gstDebugBinToDotFile(pipe, 0, "suscriptor"); // DEBG
        
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
    }

    /**
     * Obtiene los elementos de la tubería para la decodiciación en formato JPEG.
     * 
     * @return Decodificadores JPEG.
     */
    private Element[] getDecJpeg() {
        // Codec JPEG
        Element codec = ElementFactory.make("jpegdec", null);
        
        return new Element[] { codec };
    }
    
    /**
     * Obtiene los elementos de la tubería para la decodiciación en formato VP8.
     * 
     * @return Decodificadores VP8.
     */
    private Element[] getDecVp8() {
        // Codec VP8
        String caps = "video/x-vp8, width=(int)640, height=(int)480, framerate=25/1";
        Element capsSrc = ElementFactory.make("capsfilter", null);
        capsSrc.setCaps(Caps.fromString(caps));
        
        Element queue = ElementFactory.make("queue2", null);
        
        Element codec = ElementFactory.make("vp8dec", null);
        
        Element convert = ElementFactory.make("ffmpegcolorspace", null);
        
        return new Element[] { capsSrc, queue, codec, convert };
    }
    
    @Override
    public void getDatos(DynamicData sample) {
        // Deserializa los datos
        // DEBUG: sample.print(null, 0); // Para mostrarlo formateado por la consola
        DatosCamara datos = DatosCamara.FromDds(sample);

        // Inicializamos GStreamer si es la primera vez
        if (this.pipe == null) {
            this.codecName = datos.getCodecInfo();
            this.iniciaGStreamer();
        }
        
        Buffer buffer = new Buffer(datos.getBuffer().length);
        buffer.getByteBuffer().put(datos.getBuffer());
        
        // Lo mete en la tubería
        if (this.appsrc != null)
            this.appsrc.pushBuffer(buffer);
    }
}
