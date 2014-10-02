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
import com.rti.dds.dynamicdata.DynamicDataProperty_t;
import com.rti.dds.dynamicdata.DynamicDataWriter;
import com.rti.dds.infrastructure.ByteSeq;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.infrastructure.RETCODE_ERROR;
import com.rti.dds.infrastructure.StatusKind;
import com.rti.dds.publication.DataWriter;
import com.rti.dds.publication.DataWriterAdapter;
import com.rti.dds.publication.PublicationMatchedStatus;
import es.prometheus.dds.TopicoControl;
import es.prometheus.dds.TopicoControlFactoria;
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
    private final String camId;
    private boolean parar;
    
    private TopicoControl topico;
    private DynamicDataWriter writer;
    private DynamicData instance;

    private Pipeline pipe;
    private AppSink appsink;

    /**
     * Crea una nueva instancia para obtener vídeo y publicarlo de la cámara
     * especificada.
     * 
     * @param device Ruta a la cámara deseada (Ej: /dev/video0).
     * @param camId  ID de la cámara.
     */
    public EscritorVideo(final String device, final String camId) {
        this.device = device;
        this.camId  = camId;
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
        this.writer = topico.creaEscritor();
        if (this.writer == null) {
            System.err.println("No se pudo crear el escritor -> " + this.camId);
            System.exit(1);
        }

        // DEBUG: Le añade el listener de prueba.
        this.writer.set_listener(new DataWriterListener(camId), StatusKind.STATUS_MASK_ALL);

        // Como en la estructura tenemos un campo (buffer) que puede ser mayor
        // de 64 KB, se necesita aumentar algunos límites. Más info:
        // http://community.rti.com/content/forum-topic/ddsdynamicdatasetoctetseq-returns-ddsretcodeoutofresources
        DynamicDataProperty_t propiedades = new DynamicDataProperty_t();
        propiedades.buffer_initial_size = 100;
        propiedades.buffer_max_size = 1048576;

        // Crea una estructura de datos como la que hemos definido en el XML.
        this.instance = this.writer.create_data(propiedades);
        if (this.instance == null) {
            System.err.println("No se pudo crear la instancia de datos.");
            System.exit(1);
        }
    }

    /**
     * Inicializa GStreamer.
     */
    private void iniciaGStreamer() {
        // Crea los elementos de la tubería
        // 1º Origen de vídeo, del códec v4l2
        Element videosrc = ElementFactory.make("v4l2src", null);
        videosrc.set("device", device);

        // 2º Datos del vídeo
        Element videofilter = ElementFactory.make("capsfilter", null);
        videofilter.setCaps(Caps.fromString("video/x-raw-yuv,width=640,height=480,framerate=15/1"));

        Element videorate = ElementFactory.make("videorate", null);

        Element videoconvert = ElementFactory.make("ffmpegcolorspace", null);
        Element codec = ElementFactory.make("jpegenc", null);
        Element codec2 = ElementFactory.make("multipartmux", null);

        // 3º Salida de vídeo
        this.appsink = (AppSink) ElementFactory.make("appsink", null);

        // Crea la tubería
        this.pipe = new Pipeline();
        this.pipe.addMany(videosrc, videorate, videofilter, videoconvert, codec, codec2, this.appsink);
        Element.linkMany(videosrc, videorate, videofilter, videoconvert, codec, codec2, this.appsink);

        // Configura el APPSINK
        this.appsink.setQOSEnabled(true);
        GstDebugUtils.gstDebugBinToDotFile(pipe, 0, "publicador");

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
            // NOTA: Limpiar siempre, que si no se acumulan datos en byte_seq
            // y falla porque no tiene recursos suficientes.
            this.instance.clear_all_members();

            this.instance.set_string("camId", DynamicData.MEMBER_ID_UNSPECIFIED, this.camId);
            this.instance.set_string("sala",  DynamicData.MEMBER_ID_UNSPECIFIED, "Torreón");
            this.instance.set_double("posX",  DynamicData.MEMBER_ID_UNSPECIFIED, 4.0);
            this.instance.set_double("posY",  DynamicData.MEMBER_ID_UNSPECIFIED, 3.2);
            this.instance.set_double("angle", DynamicData.MEMBER_ID_UNSPECIFIED, 90.0);

            this.instance.set_string("codecInfo", DynamicData.MEMBER_ID_UNSPECIFIED, "jpgenc");
            this.instance.set_int("width",        DynamicData.MEMBER_ID_UNSPECIFIED, 640);
            this.instance.set_int("height",       DynamicData.MEMBER_ID_UNSPECIFIED, 480);
            this.instance.set_byte_seq("buffer",  DynamicData.MEMBER_ID_UNSPECIFIED, new ByteSeq(tmp));
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
            this.writer.write(this.instance, InstanceHandle_t.HANDLE_NIL);
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

    /**
     * DEBUG: Listener del escritor de DDS.
     */
    private class DataWriterListener extends DataWriterAdapter {
        private final String id;

        public DataWriterListener(String id) {
            this.id = id;
        }

        @Override
        public void on_publication_matched(DataWriter writer, PublicationMatchedStatus status) {
            System.out.println("DataWriterListener: on_publication_matched()\n");
            if (status.current_count_change < 0) {
                System.out.println("lost a subscription" + id + "\n");
            } else {
                System.out.println("found a subscription" + id + "\n");
            }
        }
    }
}
