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
import com.rti.dds.infrastructure.RETCODE_ERROR;
import es.prometheus.dds.Escritor;
import es.prometheus.dds.TopicoControl;
import es.prometheus.dds.TopicoControlFactoria;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.gstreamer.elements.Queue;
import org.gstreamer.Buffer;
import org.gstreamer.Caps;
import org.gstreamer.ClockTime;
import org.gstreamer.Element;
import org.gstreamer.ElementFactory;
import org.gstreamer.Pipeline;
import org.gstreamer.elements.AppSink;

/**
 * Obtiene vídeo de la cámara y lo escribe en DDS.
 */
public class EscritorVideo extends Thread {
    private final String device;
    private final DatosCamara info;
    private boolean parar;
    
    private TopicoControl topico;
    private Escritor writer;
    private DynamicData instance;

    private Pipeline pipe;
    private AppSink appsink;

    /**
     * Crea una nueva instancia para obtener vídeo y publicarlo de la cámara
     * especificada.
     * 
     * @param device Ruta a la cámara deseada (Ej: /dev/video0).
     * @param info  Información de la cámara.
     */
    public EscritorVideo(final String device, final DatosCamara info) {
        this.device = device;
        this.info   = info;
        this.parar  = false;
    }

    @Override
    public void run() {
        // Inicia DDS y obtiene el escritor
        this.iniciaDds();

        // Inicia GStreamer
        this.iniciaGStreamer();

        // Mientras no se acabe, coje cada frame y lo envía.
        while (!appsink.isEOS() && !this.parar)
            this.transmite();
        
        // Una vez terminado, paramos de transmitir.
        this.topico.dispose();
    }

    /**
     * Obtiene un participante de dominio y crea el escritor.
     */
    private void iniciaDds() {
        // Crea la clase de control de tópicos.
        this.topico = TopicoControlFactoria.crearControlDinamico(
                "MyParticipantLibrary::PublicationParticipant",
                "VideoDataTopic");

        // Crea el escritor.
        this.writer = new Escritor(this.topico);

        // Crea una estructura de datos como la que hemos definido en el XML.
        this.instance = this.writer.creaDatos();
    }

    /**
     * Inicializa GStreamer.
     */
    private void iniciaGStreamer() {
        // Crea los elementos de la tubería
        List<Element> elements = new ArrayList<>();
        
        // 1º Origen de vídeo, del códec v4l2
        Element videosrc = ElementFactory.make("v4l2src", null);
        videosrc.set("device", device);
        elements.add(videosrc);

        // 2º Datos de captura de vídeo: establecemos tamaño y framerate
        Element capsSrc = ElementFactory.make("capsfilter", null);
        capsSrc.setCaps(Caps.fromString("video/x-raw-yuv,width=640,height=480,framerate=15/1"));
        elements.add(capsSrc);
               
        //Element videoscale = ElementFactory.make("videoscale", null);
        //elements.add(videoscale);
        
        Element videorate = ElementFactory.make("videorate", null);
        elements.add(videorate);
        
        // 3º Cola que elimina paquetes en lugar de acumular
        Queue queue = (Queue)ElementFactory.make("queue", null);
        queue.set("leaky", 2);  // Drops old buffer
        queue.set("max-size-time", 50*1000*1000);   // 50 ms
        elements.add(queue);
        
        // 4º Conversor de vídeo
        Element videoconvert = ElementFactory.make("ffmpegcolorspace", null);
        elements.add(videoconvert);
        
        // 5º Codecs
        Element[] codecs = null;
        switch (this.info.getCodecInfo()) {
            case "JPEG": codecs = this.getEncJpeg(); break;
            case "VP8":  codecs = this.getEncVp8();  break;
        }
        elements.addAll(Arrays.asList(codecs));
        
        // 6º Salida de vídeo
        this.appsink = (AppSink) ElementFactory.make("appsink", null);
        this.appsink.setQOSEnabled(true);
        elements.add(appsink);

        // Crea la tubería
        this.pipe = new Pipeline();
        this.pipe.addMany(elements.toArray(new Element[0]));
        Element.linkMany(elements.toArray(new Element[0]));
        //GstDebugUtils.gstDebugBinToDotFile(pipe, 0, "publicador"); // DEBUG

        // Play!
        // Cambiar el estado puede tomar hasta 5 segundos. Comprueba errores.
        this.pipe.play();
        org.gstreamer.State retState = this.pipe.getState(ClockTime.fromSeconds(5).toSeconds());
        if (retState == org.gstreamer.State.NULL) {
            System.err.println("Error al cambiar de estado.");
            System.exit(-1);
        }
    }

    /**
     * Obtiene los elementos de la tubería para la codiciación en formato JPEG.
     * 
     * @return Codificadores JPEG.
     */
    private Element[] getEncJpeg() {        
        // Codec JPEG
        Element codec = ElementFactory.make("jpegenc", null);
        Element mux   = ElementFactory.make("multipartmux", null);
        
        return new Element[] { codec, mux };
    }
    
    /**
     * Obtiene los elementos de la tubería para la codiciación en formato VP8.
     * 
     * @return Codificadores VP8.
     */
    private Element[] getEncVp8() {
        // Codec VP8 (WebM)
        Element codec = ElementFactory.make("vp8enc", null);
	codec.set("threads", 5);

        // Caps del nuevo formato
        Element capsDst = ElementFactory.make("capsfilter", null);
        capsDst.setCaps(Caps.fromString("video/x-vp8 profile=(string)2"));
        
        return new Element[] { codec, capsDst };
    }
    
    /**
     * Obtiene un buffer y lo transmite por DDS.
     */
    private void transmite() {     
        // Obtiene el siguiente buffer a enviar
        Buffer buffer = appsink.pullBuffer();
        if (buffer == null)
            return;

        // Transfiere los datos a un buffer intermedio
        byte[] tmp = new byte[buffer.getSize()];
        buffer.getByteBuffer().get(tmp);

        // Crea la estructura de datos
        try {
            this.info.setBuffer(tmp);
            this.info.escribeDds(this.instance);
        } catch (com.rti.dds.infrastructure.RETCODE_OUT_OF_RESOURCES e) {
            // Se da cuando la estructura interna de datos no puede guardar
            // todos los bytes del buffer. Para arreglarlo hay que aumentar
            // las propiedades cuando se crea 'instance'. Ahora mismo puesto
            // a 1 MB. Si se da el error, descartamos el frame.
            System.out.println("¡Aumentar recursos! -> " + tmp.length);
            return;
        }

        // Publica la estructura de datos generada en DDS
        try {
            this.writer.escribeDatos(this.instance);
        } catch (RETCODE_ERROR e) {
            System.out.println("Write error: " + e.getMessage());
        }
    }

    /**
     * Para la transmisión de vídeo y elimina las instancias de DDS creadas.
     */
    public void parar() {
        this.parar = true;
    }
}
