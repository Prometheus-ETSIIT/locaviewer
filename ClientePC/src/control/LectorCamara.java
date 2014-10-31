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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.gstreamer.Buffer;
import org.gstreamer.Caps;
import org.gstreamer.ClockTime;
import org.gstreamer.Element;
import org.gstreamer.ElementFactory;
import org.gstreamer.Format;
import org.gstreamer.Pipeline;
import org.gstreamer.StateChangeReturn;
import org.gstreamer.elements.AppSrc;
import org.gstreamer.elements.Queue;
import org.gstreamer.swing.VideoComponent;

/**
 * Suscriptor de tópico de cámaras.
 * Recibe datos de DDS y va actualizando el componente de vídeo de GStreamer.
 */
public class LectorCamara extends LectorBase {
    private static final String EXPRESION = "camId = %0";
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
        super(control, EXPRESION, new String[] { "'" + key + "'" });
        this.videocomp = videocomp;
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
        this.appsrc.setLatency(0, 100000000);
        this.appsrc.setTimestamp(true);
        this.appsrc.setFormat(Format.TIME);
        this.appsrc.setStreamType(AppSrc.Type.STREAM);
        elements.add(this.appsrc);

        Queue queue = (Queue)ElementFactory.make("queue", null);
        queue.set("leaky", 2);  // Drops old buffer
        queue.set("max-size-time", 50*1000*1000);   // 50 ms
        elements.add(queue);

        // 2º Códec
        Element[] codecs = null;
        switch (this.ultDatos.getCodecInfo()) {
            case "JPEG": codecs = this.getDecJpeg(); break;
            case "VP8":  codecs = this.getDecVp8();  break;
        }
        elements.addAll(Arrays.asList(codecs));

        // 3º Salida de vídeo
        Element videosink = this.videocomp.getElement();
        elements.add(videosink);

        // Crea la tubería
        this.pipe = new Pipeline();
        this.pipe.addMany(elements.toArray(new Element[0]));
        Element.linkMany(elements.toArray(new Element[0]));
        //GstDebugUtils.gstDebugBinToDotFile(pipe, 0, "suscriptor"); // DEBG

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
        String caps = "video/x-vp8, width=(int)320, height=(int)240, framerate=15/1";
        Element capsSrc = ElementFactory.make("capsfilter", null);
        capsSrc.setCaps(Caps.fromString(caps));

        Element queue = ElementFactory.make("queue2", null);

        Element codec = ElementFactory.make("vp8dec", null);

        Element convert = ElementFactory.make("ffmpegcolorspace", null);

        return new Element[] { capsSrc, queue, codec, convert };
    }

    @Override
    public void dispose() {
        super.dispose();

        if (this.pipe != null) {
            StateChangeReturn retState = this.pipe.stop();
            if (retState == StateChangeReturn.FAILURE)
                System.err.println("Error al parar.");

            this.pipe.dispose();
            this.appsrc.dispose();
        }
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
        if (this.pipe == null)
            this.iniciaGStreamer();

        Buffer buffer = new Buffer(this.ultDatos.getBuffer().length);
        buffer.getByteBuffer().put(this.ultDatos.getBuffer());

        // Lo mete en la tubería
        if (this.appsrc != null)
            this.appsrc.pushBuffer(buffer);
    }
}
