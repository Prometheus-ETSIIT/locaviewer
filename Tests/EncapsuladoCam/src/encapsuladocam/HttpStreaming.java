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

import java.util.logging.Level;
import java.util.logging.Logger;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.headless.HeadlessMediaPlayer;

/**
 * Captura vídeo de la cámara y crea un streaming por HTTP.
 */
public class HttpStreaming {

    private static final int DELAY = 20000;
    
    /**
     * Inicia el programa.
     * 
     * @param args Primer elemento: MRL al medio que se va a transmitir (v4l2:///dev/video0). 
     * Segundo elemento: puerto de transmisión.
     */
    public static void main(String[] args) {
        // Comprueba los argumentos.
        if(args.length != 2) {
            System.out.println("USO: HttpStreaming MRL puerto");
            System.exit(1);
        }

        String media = args[0];
        int port = Integer.parseInt(args[1]);

        // Crea el reproductor
        MediaPlayerFactory mediaPlayerFactory = new MediaPlayerFactory();
        HeadlessMediaPlayer mediaPlayer = mediaPlayerFactory.newHeadlessMediaPlayer();
        
        // Crea la opciones y comienza a reproducir (capturar y streaming).
        String options = formatHttpStream("0.0.0.0", port);
        mediaPlayer.playMedia(media, options);

        // Espera un tiempo para iniciar el streaming.
        try {
            Thread.sleep(DELAY);
        } catch (InterruptedException ex) {
            Logger.getLogger(HttpStreaming.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // Redirección
        VlcToDds redireccion = new VlcToDds("127.0.0.1", 5555);
        redireccion.start();
        
        try {
            // Don't exit
            Thread.currentThread().join();
        } catch (InterruptedException ex) {
            System.err.println(ex.getMessage());
        }
    }

    /**
     * Crea una cadena de caracteres con opciones de streaming HTTP.
     * 
     * @param serverAddress Dirección del servidor.
     * @param serverPort Puerto del servidor.
     * @return Opciones con streaming HTTP.
     */
    private static String formatHttpStream(String serverAddress, int serverPort) {
        StringBuilder sb = new StringBuilder(60);
        sb.append(":sout=#");
        
        // Añade la codificación del video
        sb.append("transcode{");
        sb.append("vcodec=mp4v,vb=4096,scale=1,acodec=mpga,ab=128,");
        sb.append("channels=2,samplerate=44100,fps=59.9");
        sb.append("}");
        
        // Añade la transmisión por streaming HTTP.
        sb.append(":duplicate{dst=std{access=http,mux=ts,");
        sb.append("dst=");
        sb.append(serverAddress);
        sb.append(':');
        sb.append(serverPort);
        sb.append("}}");

        return sb.toString();
    }
}
