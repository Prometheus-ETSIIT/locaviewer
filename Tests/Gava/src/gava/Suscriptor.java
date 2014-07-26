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

import org.gstreamer.Buffer;
import org.gstreamer.Caps;
import org.gstreamer.ClockTime;
import org.gstreamer.Format;
import org.gstreamer.Gst;
import org.gstreamer.Pipeline;
import org.gstreamer.State;
import org.gstreamer.elements.AppSrc;

public class Suscriptor {
    public static void main(String[] args) {
        // Inicializamos GStreamer
        args = new String[] {
        "appsrc",       // Origen de vídeo
        "video/x-raw, width=640, height=480, framerate=15/1",
        "xvimagesink"   // Destino de vídeo
        };
        args = Gst.init("Gava", args);

        // Inicia la obtención de vídeo
        Pipeline p = Pipeline.launch(args);
        AppSrc appsrc = (AppSrc)p.getSources().get(0);
        p.play();
        
        // Cambiar el estado puede tomar hasta 5 segundos. Comprueba errores.
        State retState = p.getState(ClockTime.fromSeconds(5).toSeconds());
        if (retState == State.NULL) {
            System.err.println("failed to play the file");
            System.exit(-1);
        }
        
        // Configura el APPSRC
        appsrc.setLive(true);
        appsrc.setLatency(0, 100);
        appsrc.setTimestamp(true);
        appsrc.setFormat(Format.TIME);
        appsrc.setStreamType(AppSrc.Type.STREAM);
        
        while (true) {
            byte[] recibido = null; // TODO: Recibir datos desde DDS
            Caps caps = null;       // TODO: Recibir cap del buffer desde DDS
            
            Buffer buffer = new Buffer(recibido.length);
            buffer.getByteBuffer().put(recibido);
            buffer.setCaps(caps);
            
            appsrc.pushBuffer(buffer);
            
            // FUTURE: Cuando se quiera parar...
            //appsrc.endOfStream();
            //break;
        }
    }
}
