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

package encapsuladocam;

import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.DomainParticipantFactory;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.infrastructure.StatusKind;
import com.rti.dds.publication.Publisher;
import com.rti.dds.topic.Topic;
import com.rti.dds.type.builtin.BytesDataWriter;
import com.rti.dds.type.builtin.BytesTypeSupport;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Clase para redireccionar un streaming HTTP de vídeo por DDS.
 */
public class VlcToDds {
    
    private final String srcHost;
    private final int srcPort;
    private BytesDataWriter writer;
    
    /**
     * Crea una nueva instancia con los datos de redirección.
     * 
     * @param srcHost Host del servidor de streaming.
     * @param srcPort Puerto del servidor de streaming.
     */
    public VlcToDds(final String srcHost, final int srcPort) {
        this.srcHost = srcHost;
        this.srcPort = srcPort;
        this.iniciaDds();
    }
    
    /**
     * Comienza a redireccionar los datos del streaming.
     */
    public void start() {
        try {
            // Crea el socket.
            Socket conn = new Socket(this.srcHost, this.srcPort);
            
            // Envía la petición GET
            PrintWriter socketWriter = new PrintWriter(conn.getOutputStream());
            socketWriter.append("GET / HTTP/1.1\r\n");
            socketWriter.append("Host: " + this.srcHost + ":" + this.srcPort + "\r\n");
            socketWriter.append("User-Agent: VLC/2.1.4 LibVLC/2.1.4\r\n");
            socketWriter.append("Range: bytes=0-\r\n");
            socketWriter.append("Connection: close\r\n");
            socketWriter.append("Icy-MetaData: 1\r\n");
            socketWriter.append("\r\n");
            socketWriter.flush();

            // Comienza a recibir los datos del streaming y los reenvía por DDS.
            InputStream inputStream = conn.getInputStream();
            int bytesRead;
            byte[] buffer = new byte[1024];
            while (true) {
                bytesRead = inputStream.read(buffer);
                if (bytesRead != -1)
                    this.writer.write(buffer, 0, bytesRead, InstanceHandle_t.HANDLE_NIL);
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
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
        
        writer = (BytesDataWriter)participant.create_datawriter(
                topic, 
                Publisher.DATAWRITER_QOS_DEFAULT,
                null, // listener
                StatusKind.STATUS_MASK_NONE);
        if (writer == null) {
            System.err.println("No se pudo crear el escritor");
        }
    }
}
