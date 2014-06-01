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

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import javax.swing.JFrame;
import javax.swing.JPanel;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.videosurface.CanvasVideoSurface;

/**
 * Cliente que recibe un vídeo en streaming por HTTP y muestra el reproductor.
 */
public class Cliente {
    private final static int PORT = 5556;
    
    /**
     * Inicia el programa.
     * 
     * @param args Ninguno.
     */
    public static void main(String[] args) {       
        // Inicia el servidor falso
        FakeStreaming fakeServer = new FakeStreaming(PORT);
        fakeServer.start();
        
        // Crea la ventana del reproductor
        Canvas canvas = new Canvas();
        canvas.setBackground(Color.black);

        JPanel contentPane = new JPanel();
        contentPane.setBackground(Color.black);
        contentPane.setLayout(new BorderLayout());
        contentPane.add(canvas, BorderLayout.CENTER);

        JFrame frame = new JFrame("Capture");
        frame.setContentPane(contentPane);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocation(50, 50);
        frame.setSize(800, 600);

        MediaPlayerFactory factory = new MediaPlayerFactory();
        EmbeddedMediaPlayer mediaPlayer = factory.newEmbeddedMediaPlayer();

        CanvasVideoSurface videoSurface = factory.newVideoSurface(canvas);
        mediaPlayer.setVideoSurface(videoSurface);
        
        // Reproduce el vídeo.
        frame.setVisible(true);
        mediaPlayer.playMedia("http://localhost:" + PORT);
    }
}
