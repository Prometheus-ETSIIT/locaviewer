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
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.infrastructure.StatusKind;
import com.rti.dds.publication.Publisher;
import com.rti.dds.topic.Topic;
import com.rti.dds.type.builtin.BytesDataWriter;
import com.rti.dds.type.builtin.BytesTypeSupport;
import org.gstreamer.Buffer;
import org.gstreamer.ClockTime;
import org.gstreamer.Gst;
import org.gstreamer.Pipeline;
import org.gstreamer.State;
import org.gstreamer.elements.AppSink;

public class Publicador {
    private BytesDataWriter writer;
    private Pipeline pipe;
    private AppSink appsink;
    
    public Publicador() {
    }
    
    /**
     * Inicia la aplicación.
     * 
     * @param args Ninguno.
     */
    public static void main(String[] args) {
        Gst.init("Gava", args);
        Publicador pub = new Publicador();
        pub.start();
    }
    
    public void start() {
        // Inicia DDS y obtiene el escritor
        this.iniciaDds();

        // Inicializamos GStreamer
        String[] args = new String[] {
        "v4lsrc device=/dev/video0",       // Origen de vídeo
        "video/x-raw, width=640, height=480, framerate=15/1",
        "appsink"   // Destino de vídeo
        };

        // Inicia la obtención de vídeo
        this.pipe = Pipeline.launch(args);
        this.appsink = (AppSink)this.pipe.getSinks().get(0);
        this.pipe.play();

        // Cambiar el estado puede tomar hasta 5 segundos. Comprueba errores.
        State retState = this.pipe.getState(ClockTime.fromSeconds(5).toSeconds());
        if (retState == State.NULL) {
            System.err.println("Error al cambiar de estado.");
            System.exit(-1);
        }
        
        // Configura el APPSINK
        this.appsink.setQOSEnabled(true);
        
        // Mientras no se acabe, coje cada frame y lo envía.
        while (!appsink.isEOS()) {
            Buffer buffer = appsink.pullBuffer();

            if (buffer == null)
                 continue;

            // Lo envía por DDS
            byte[] toSend = buffer.getByteBuffer().array();
            String caps   = buffer.getCaps().toString();
            byte[] capsLength = new byte[] {
                (byte)(caps.getBytes().length & 0xFF),
                (byte)((caps.getBytes().length >> 8) & 0xFF),
                (byte)((caps.getBytes().length >> 16) & 0xFF),
                (byte)((caps.getBytes().length >> 24) & 0xFF)
            };
            
            this.writer.write(capsLength, 0, 4, InstanceHandle_t.HANDLE_NIL);
            this.writer.write(caps.getBytes(), 0, caps.getBytes().length, InstanceHandle_t.HANDLE_NIL);
            this.writer.write(toSend, 0, toSend.length, InstanceHandle_t.HANDLE_NIL);
            
            // TODO: Los caps son todos iguales y se podría solo enviar uno que
            // se obtiene desde
            //String caps = appsink.getCaps().toString();
            // Hay que mirar como enviar un dato sólo una vez al inicio.
        }
    }
    
    private void iniciaDds() {
        //Dominio 1
        DomainParticipant participant = DomainParticipantFactory.get_instance().create_participant(
                1, // Domain ID = 0
                DomainParticipantFactory.PARTICIPANT_QOS_DEFAULT, 
                null, // listener
                StatusKind.STATUS_MASK_NONE);
        if (participant == null) {
            System.err.println("No se pudo crear el dominio.");
            return;
        }

       //Creación del tópico
        Topic topic = participant.create_topic(
                "test_cam", 
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
