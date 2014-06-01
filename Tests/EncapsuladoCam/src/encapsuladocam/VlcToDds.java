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

import java.io.FileOutputStream;
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
    
    /**
     * Crea una nueva instancia con los datos de redirección.
     * 
     * @param srcHost Host del servidor de streaming.
     * @param srcPort Puerto del servidor de streaming.
     */
    public VlcToDds(final String srcHost, final int srcPort) {
        this.srcHost = srcHost;
        this.srcPort = srcPort;
    }
    
    /**
     * Comienza a redireccionar los datos del streaming.
     */
    public void start() {
        try {
            Socket conn = new Socket(this.srcHost, this.srcPort);
            
            // Envía la petición GET
            PrintWriter writer = new PrintWriter(conn.getOutputStream());
            writer.append("GET / HTTP/1.1\r\n");
            writer.append("Host: " + this.srcHost + ":" + this.srcPort + "\r\n");
            writer.append("User-Agent: VLC/2.1.4 LibVLC/2.1.4\r\n");
            writer.append("Range: bytes=0-\r\n");
            writer.append("Connection: close\r\n");
            writer.append("Icy-MetaData: 1\r\n");
            writer.append("\r\n");
            writer.flush();

            // Comienza a recibir los datos del streaming y los guarda.
            InputStream inputStream = conn.getInputStream();
            FileOutputStream outputStream = new FileOutputStream("test.mpg");

            int bytesRead;
            byte[] buffer = new byte[1024];
            while (true) {
                bytesRead = inputStream.read(buffer);
                if (bytesRead != -1)
                    outputStream.write(buffer, 0, bytesRead);
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }
}
