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
import com.rti.dds.domain.DomainParticipantQos;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.infrastructure.PropertyQosPolicyHelper;
import com.rti.dds.infrastructure.StatusKind;
import com.rti.dds.publication.DataWriterQos;
import com.rti.dds.publication.Publisher;
import com.rti.dds.topic.Topic;
import com.rti.dds.type.builtin.BytesDataWriter;
import com.rti.dds.type.builtin.BytesTypeSupport;
import java.io.File;
import java.io.FilenameFilter;
import org.gstreamer.Buffer;
import org.gstreamer.Caps;
import org.gstreamer.ClockTime;
import org.gstreamer.Element;
import org.gstreamer.ElementFactory;
import org.gstreamer.Gst;
import org.gstreamer.Pipeline;
import org.gstreamer.State;
import org.gstreamer.elements.AppSink;

public class Publicador implements Runnable {
    private final String device;
    private final String camId;
    
    private BytesDataWriter writer;
    private Pipeline pipe;
    private AppSink appsink;
    
    public Publicador(final String device, final String camId) {
        this.device = device;
        this.camId  = camId;
    }
    
    /**
     * Inicia la aplicación.
     * 
     * @param args La(s) camara(s). Ej: "/dev/video1". 
     *             Si no se pasa ningún parámetro, inicia todas.
     */
    public static void main(String[] args) {
        args = Gst.init("Gava", args);
        
        if (args.length > 0) {
            for (int i = 0; i < args.length; i++)
                new Thread(new Publicador(args[i], "test_cam_" + i)).start();
        } else {
            StartAll();
        }
    }
    
    private static void StartAll() {
        // Obtiene una lista de cámaras conectadas (/dev/video*)
        String[] cams = new File("/dev/").list(new FilenameFilter() {
            @Override
            public boolean accept(File parentDir, String filename) {
                return parentDir.getAbsolutePath().equals("/dev") &&
                       filename.startsWith("video");
            }
        });
        
        // Para cada cámara crea un publicador
        for (String cam : cams) {
            System.out.println("Iniciando cámara " + cam);
            
            // Parsea el ID
            int id = Integer.parseInt(cam.substring(5));
            
            // Crea el publicador
            new Thread(new Publicador("/dev/" + cam, "test_cam_" + id)).start();
        }
    }
    
    @Override
    public void run() {
        // Inicia DDS y obtiene el escritor
        this.iniciaDds(camId);

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
        this.appsink = (AppSink)ElementFactory.make("appsink", null);
       
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
        State retState = this.pipe.getState(ClockTime.fromSeconds(5).toSeconds());
        if (retState == State.NULL) {
            System.err.println("Error al cambiar de estado.");
            System.exit(-1);
        }
                
        // Mientras no se acabe, coje cada frame y lo envía.
        while (!appsink.isEOS()) {
            Buffer buffer = appsink.pullBuffer();            
            if (buffer == null)
                 continue;

            // Lo envía por DDS
            byte[] toSend = new byte[buffer.getSize()];
            buffer.getByteBuffer().get(toSend, 0, toSend.length);
            this.writer.write(toSend, 0, toSend.length, InstanceHandle_t.HANDLE_NIL);
        }
    }
    
    private void iniciaDds(final String topico) {    
        DomainParticipantQos qos = new DomainParticipantQos();
        DomainParticipantFactory.TheParticipantFactory.get_default_participant_qos(qos);
        qos.receiver_pool.buffer_size = 65507;
        try { PropertyQosPolicyHelper.remove_property(qos.property, "dds.transport.UDPv4.builtin.parent.message_size_max"); } catch (Exception ex) { }
        try { PropertyQosPolicyHelper.remove_property(qos.property, "dds.transport.UDPv4.builtin.send_socket_buffer_size"); } catch (Exception ex) { }
        try { PropertyQosPolicyHelper.remove_property(qos.property, "dds.transport.UDPv4.builtin.recv_socket_buffer_size"); } catch (Exception ex) { }
        try { PropertyQosPolicyHelper.remove_property(qos.property, "dds.transport.shmem.builtin.parent.message_size_max"); } catch (Exception ex) { }
        try { PropertyQosPolicyHelper.remove_property(qos.property, "dds.transport.shmem.builtin.receive_buffer_size"); } catch (Exception ex) { }
        try { PropertyQosPolicyHelper.remove_property(qos.property, "dds.transport.shmem.builtin.received_message_count_max"); } catch (Exception ex) { }
        PropertyQosPolicyHelper.add_property(
                qos.property,
                "dds.transport.UDPv4.builtin.parent.message_size_max",
                "65507",
                true);
        PropertyQosPolicyHelper.add_property(
                qos.property,
                "dds.transport.UDPv4.builtin.send_socket_buffer_size",
                "2097152",
                true);
        PropertyQosPolicyHelper.add_property(
                qos.property,
                "dds.transport.UDPv4.builtin.recv_socket_buffer_size",
                "2097152",
                true);
        PropertyQosPolicyHelper.add_property(
                qos.property,
                "dds.transport.shmem.builtin.parent.message_size_max",
                "65507",
                true);
        PropertyQosPolicyHelper.add_property(
                qos.property,
                "dds.transport.shmem.builtin.receive_buffer_size",
                "2097152",
                true);
        PropertyQosPolicyHelper.add_property(
                qos.property,
                "dds.transport.shmem.builtin.received_message_count_max",
                "2048",
                true);
        PropertyQosPolicyHelper.add_property(
                qos.property,
                "dds.builtin_type.octets.max_size",
                "2097152",
                true);
                
        //Dominio 0
        DomainParticipant participant = DomainParticipantFactory.TheParticipantFactory.create_participant(
                1, // Domain ID = 1
                qos,
                null, // listener
                StatusKind.STATUS_MASK_NONE);
        if (participant == null) {
            System.err.println("No se pudo crear el dominio.");
            return;
        }

       //Creación del tópico
        Topic topic = participant.create_topic(
                topico, 
                BytesTypeSupport.get_type_name(),
                DomainParticipant.TOPIC_QOS_DEFAULT, 
                null, // listener
                StatusKind.STATUS_MASK_NONE);
        if (topic == null) {
            System.err.println("Unable to create topic.");
            return;
        }

        this.writer = (BytesDataWriter)participant.create_datawriter(
                topic, 
                Publisher.DATAWRITER_QOS_DEFAULT,
                null, // listener
                StatusKind.STATUS_MASK_NONE);
        if (this.writer == null) {
            System.err.println("No se pudo crear el escritor");
        }
    }
}
