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
