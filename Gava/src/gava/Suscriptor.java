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
                LectorVideo lector = new LectorVideo(key);
                lector.iniciar();
                
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
