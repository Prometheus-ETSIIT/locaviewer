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

import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.DomainParticipantFactory;
import com.rti.dds.infrastructure.StatusKind;
import com.rti.dds.subscription.DataReader;
import com.rti.dds.subscription.DataReaderAdapter;
import com.rti.dds.subscription.SampleInfo;
import com.rti.dds.subscription.Subscriber;
import com.rti.dds.topic.Topic;
import com.rti.dds.type.builtin.Bytes;
import com.rti.dds.type.builtin.BytesDataReader;
import com.rti.dds.type.builtin.BytesTypeSupport;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Arrays;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.gstreamer.Buffer;
import org.gstreamer.Caps;
import org.gstreamer.ClockTime;
import org.gstreamer.Element;
import org.gstreamer.ElementFactory;
import org.gstreamer.Format;
import org.gstreamer.Gst;
import org.gstreamer.Pipeline;
import org.gstreamer.State;
import org.gstreamer.elements.AppSrc;
import org.gstreamer.swing.VideoComponent;

public class Suscriptor extends DataReaderAdapter {
    private final Pipeline pipe;
    private final AppSrc appsrc;
    
    public Suscriptor() {
        // Crea los elementos de la tubería
        // 1º Origen de vídeo, simulado porque se inyectan datos.
        this.appsrc = (AppSrc)ElementFactory.make("appsrc", null);
        
        // 2º Datos del vídeo
        Element videofilter = ElementFactory.make("capsfilter", null);
        videofilter.setCaps(Caps.fromString("video/x-raw-yuv,width=640,height=480,framerate=30/1"));
        
        // 3º Salida de vídeo
        VideoComponent videoComponent = new VideoComponent();
        Element videosink = videoComponent.getElement();
       
        // Crea la tubería
        this.pipe = new Pipeline();
        this.pipe.addMany(this.appsrc, videofilter, videosink);
        Element.linkMany(this.appsrc, videofilter, videosink);
        
        // Configura el APPSRC
        appsrc.setLive(true);
        appsrc.setLatency(0, 100);
        appsrc.setTimestamp(true);
        appsrc.setFormat(Format.TIME);
        appsrc.setStreamType(AppSrc.Type.STREAM);
        
        // Crea la ventana y la muestra
        JFrame frame = new JFrame("Gava suscriptor testing");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(videoComponent, BorderLayout.CENTER);
        videoComponent.setPreferredSize(new Dimension(720, 576));
        frame.pack();
        frame.setVisible(true);
        
        // Play!
        // Cambiar el estado puede tomar hasta 5 segundos. Comprueba errores.
        this.pipe.play();
        State retState = this.pipe.getState(ClockTime.fromSeconds(5).toSeconds());
        if (retState == State.NULL) {
            System.err.println("Error al cambio de estado.");
            System.exit(-1);
        }
        
        GstDebugUtils.gstDebugBinToDotFile(pipe, 0, "suscriptor");
    }
    
    public static void main(final String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Gst.init("Gava", args); // Inicia GStreamer
                IniciaDds();            // Inicia DDS
            }
        });
    }
    
    private static void IniciaDds() {
         //Dominio 0
        DomainParticipant participant = DomainParticipantFactory.get_instance().create_participant(
                1, // ID de dominio 1
                DomainParticipantFactory.PARTICIPANT_QOS_DEFAULT, 
                null, // listener
                StatusKind.STATUS_MASK_NONE);
        if (participant == null) {
            System.err.println("No se pudo obtener el dominio.");
            return;
        }

        // Crea el tópico
        Topic topic = participant.create_topic(
                "test_cam", 
                BytesTypeSupport.get_type_name(), 
                DomainParticipant.TOPIC_QOS_DEFAULT, 
                null, // listener
                StatusKind.STATUS_MASK_NONE);
        if (topic == null) {
            System.err.println("No se pudo crear el tópico");
            return;
        }

        // Crea el suscriptor
        BytesDataReader dataReader = (BytesDataReader) participant.create_datareader(
                topic, 
                Subscriber.DATAREADER_QOS_DEFAULT,
                new Suscriptor(),         // Listener
                StatusKind.DATA_AVAILABLE_STATUS);
        if (dataReader == null) {
            System.err.println("Unable to create DDS Data Reader");
            return;
        }
    }
    
    /**
     * Callback que llama RTI connext cuando se recibe para datos.
     * 
     * @param reader Lector de datos
     */
    @Override
    public void on_data_available(DataReader reader) {
        // Obtiene el sample de DDS
        BytesDataReader bytesReader = (BytesDataReader)reader;
        Bytes data = new Bytes();
        SampleInfo info = new SampleInfo();
        bytesReader.take_next_sample(data, info);           

        // Deserializa los datos
        int capLength = data.value[3] << 24 | data.value[2] << 16 | data.value[1] << 8 | data.value[0];
        byte[] capsByte = Arrays.copyOfRange(data.value, data.offset + 4, capLength);
        Caps caps = new Caps(new String(capsByte));
        
        byte[] recibido = Arrays.copyOfRange(
                data.value,
                data.offset + 4 + capLength,
                data.length - (4 + capLength)
        );

        // Crea el buffer de GStreamer
        Buffer buffer = new Buffer(recibido.length);
        buffer.getByteBuffer().put(recibido);
        //buffer.setCaps(caps);

        // Lo mete en la tubería
        this.appsrc.pushBuffer(buffer);
        
        System.out.print(". ");
    }
}
