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

import com.rti.dds.subscription.DataReader;
import com.rti.dds.subscription.DataReaderAdapter;
import com.rti.dds.subscription.SampleInfo;
import com.rti.dds.type.builtin.Bytes;
import com.rti.dds.type.builtin.BytesDataReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Crea un servidor falso que simule ser VLC haciendo Streaming.
 */
public class FakeStreaming extends DataReaderAdapter {
    private final List<Socket> clients;
    private final int port;

    /**
     * Crea una nueva instancia del servidor falso.
     *
     * @param port Puerto en el que se iniciará el streaming falso.
     */
    public FakeStreaming(final int port) {
        this.clients = new ArrayList<>();
        this.port    = port;
        this.start();
    }

    /**
     * Inicia el servidor.
     */
    private void start() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() { serverLoop(); }
        });
        t.start();
    }

    /**
     * Bucle principal del servidor que escucha nuevas conexiones.
     */
    private void serverLoop() {
        try {
            ServerSocket server = new ServerSocket(this.port);
            while (true) {
                Socket socket = server.accept();
                sendHttpOk(socket);
                this.clients.add(socket);
            }
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }

    /**
     * Envía un mensaje de 200 OK al socket dado.
     *
     * @param socket Socket al que enviar los datos.
     */
    private void sendHttpOk(final Socket socket) {
        try {
            PrintWriter writer = new PrintWriter(socket.getOutputStream());
            writer.append("HTTP/1.0 200 OK\r\n");
            writer.append("Content-type: application/octet-stream\r\n");
            writer.append("Cache-Control: no-cache\r\n");
            writer.append("\r\n");
            writer.flush();
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }

    /**
     * Callback que llama RTI connext cuando se recibe para datos.
     *
     * @param reader Lector de datos
     */
    @Override
    public void on_data_available(DataReader reader) {
        BytesDataReader bytesReader = (BytesDataReader)reader;
        try {
            Bytes data = new Bytes();
            SampleInfo info = new SampleInfo();
            bytesReader.take_next_sample(data, info);
            for (Socket socket : this.clients)
                socket.getOutputStream().write(data.value, data.offset, data.length);
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }
}
