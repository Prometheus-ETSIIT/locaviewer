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

import java.util.logging.Level;
import java.util.logging.Logger;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.headless.HeadlessMediaPlayer;

/**
 * Captura vídeo de la cámara y crea un streaming por HTTP.
 */
public class HttpStreaming {

    private static final int DELAY = 20000;
    private static final int PORT  = 5555;

    /**
     * Inicia el programa.
     *
     * @param args MRL al medio que se va a transmitir (v4l2:///dev/video0).
     */
    public static void main(String[] args) {
        // Comprueba los argumentos.
        if(args.length != 1) {
            System.out.println("USO: HttpStreaming MRL");
            System.exit(1);
        }

        String media = args[0];

        // Crea el reproductor
        MediaPlayerFactory mediaPlayerFactory = new MediaPlayerFactory();
        HeadlessMediaPlayer mediaPlayer = mediaPlayerFactory.newHeadlessMediaPlayer();

        // Crea la opciones y comienza a reproducir (capturar y streaming).
        String options = formatHttpStream("127.0.0.1", PORT);
        mediaPlayer.playMedia(media, options);

        // Espera un tiempo para iniciar el streaming.
        try {
            Thread.sleep(DELAY);
        } catch (InterruptedException ex) {
            Logger.getLogger(HttpStreaming.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Redirección
        VlcToDds redireccion = new VlcToDds("127.0.0.1", PORT);
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
