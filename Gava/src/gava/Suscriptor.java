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

package gava;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.SwingUtilities;
import org.gstreamer.Gst;

public class Suscriptor {
    /**
     * Inicia el programa
     *
     * @param args Un argumento opcional como nombre del tópico.
     */
    public static void main(final String[] args) {
        final String key = (args.length == 0) ? "test0" : args[0];

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // Inicia GStreamer
                Gst.init("Gava", args);

                // Crea el lector de vídeo.
                final LectorVideo lector = new LectorVideo(key);
                lector.iniciar();

                // A los 7 segundos cambiamos de cámara si estamos en el escenario
                // de debug
                javax.swing.Timer t = new javax.swing.Timer(7000, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent ae) {
                        System.out.println("Cambiando...");
                        lector.cambioParametros(new String[] { "'test1'" });
                    }
                });
                t.setRepeats(false);
                if (args.length == 0)
                    t.start();

                // Añade un listener que se ejecute al finalizar la aplicación (Ctl + C)
                Runtime.getRuntime().addShutdownHook(new ShutdownThread(lector));
            }
        });
    }

    /**
     * Listener llamado cuando se finaliza la aplicación.
     * Termina los thread de publicación de vídeo para eliminar el participante.
     */
    private static class ShutdownThread extends Thread {
        private final LectorVideo lector;

        public ShutdownThread(final LectorVideo lector) {
            this.lector = lector;
        }

        @Override
        public void run() {
            System.out.println("Parando. . .");
            this.lector.dispose();
        }
    }
}
