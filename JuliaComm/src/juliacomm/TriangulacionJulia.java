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

package juliacomm;

/**
 * Clase para la ejecución del script de triangulación
 * 
 * @author Benito Palacios
 */
public class TriangulacionJulia {

    static {
        System.loadLibrary("jltriang");
    }
    
    /**
     * Realiza una triangulación a partir de la señal recibida en los sensores
     * y de su posición.
     * 
     * @param sensorX Vector con la posición en el eje X de los sensores.
     * @param sensorY Vector con la posición en el eje Y de los sensores.
     * @param rssi Vector con el nivel de señal recibida en los sensores.
     * @return Posición X e Y de la triangulación.
     */
    public native double[] triangular(double[] sensorX, double[] sensorY, double[] rssi);
    
    /**
     * Prueba a ejecutar el script en Julia de triangulación
     * 
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // Prueba la clase
        double[] sensorX = new double[] { 5.0, 4.1 };
        double[] sensorY = new double[] { 1.4, 2.5 };
        double[] rssi    = new double[] { -54, -12 };
        
        TriangulacionJulia julia = new TriangulacionJulia();
        double[] resultado = julia.triangular(sensorX, sensorY, rssi);
        System.out.println("X: " + resultado[0] + " Y: " + resultado[1]);
    }
    
}
