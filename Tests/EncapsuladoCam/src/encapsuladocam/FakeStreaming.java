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