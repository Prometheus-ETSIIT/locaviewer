package com.rti.comunicador;

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


import dk.ange.octave.OctaveEngine;
import dk.ange.octave.OctaveEngineFactory;
import dk.ange.octave.exception.OctaveEvalException;
import dk.ange.octave.type.OctaveDouble;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Establece un enlace para ejecutar código de triangulación en Octave.
 */
public class TriangulacionOctave {
    private final OctaveEngine octave;
    private final String funcName;
    private boolean alive;

    /**
     * Crea una nueva instancia inicializando el engine.
     * 
     * @param scriptPath Ruta al script con las funciones de triangulación.
     * @param funcName Nombre de la función a ejecutar. Esta función tendrá
     * los siguientes argumentos en el siguiente orden:
     *  + Vector de double con posición X de los sensores.
     *  + Vector de double con posición Y de los sensores.
     *  + Valor RSSI de los sensores.
     */
    public TriangulacionOctave(final String scriptPath, final String funcName) {
        this.octave   = new OctaveEngineFactory().getScriptEngine();
        this.funcName = funcName;
        this.alive = this.initialize(scriptPath);
    }
    
    /**
     * Obtiene si el sistema está en funcionamiento.
     * 
     * @return Estado de vida del sistema.
     */
    public boolean isAlive() {
        return this.alive;
    }
    
    /**
     * Apaga el sistema...
     */
    public void close() {
        this.octave.close();
        this.alive = false;
    }
    
    /**
     * Inicializa el sistema poniendo el script en memoria.
     * 
     * @param scriptPath Ruta al archivo con el script de triangulación.
     * @return Devuelve si la operación se llevó a cabo con éxito.
     */
    private boolean initialize(final String scriptPath) {
        // Información sobre los métodos: http://goo.gl/1kbd4y
        
        // Lee las funciones
        String script;
        try {
            Path path = FileSystems.getDefault().getPath(scriptPath);
            script = new String(Files.readAllBytes(path));
        } catch (IOException ex) {
            System.err.printf(
                    "[JAVA] Error al leer el archivo: %s\n",
                    scriptPath
            );
            return false;
        }
        
        // Las carga en memoria
        try {
            this.octave.eval(script);
        } catch (OctaveEvalException ex) {
            System.err.printf(
                    "[JAVA] Error al evaluar el script\n%s\n",
                    ex.getMessage()
            );
            return false;
        }

        return true;
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
    public double[] triangular(double[] sensorX, double[] sensorY, double[] rssi) {
        if (!this.alive) {
            System.err.println("[JAVA] Error ¡el sistema está muerto!");
            return null;
        }
        
        // Convierte los valores a tipo Octave y los pone en memoria
        this.putDoubleArray(sensorX, "sensorX");
        this.putDoubleArray(sensorY, "sensorY");
        this.putDoubleArray(rssi,    "rssi");

        // Llama a la función que tiene en memoria
        String funcCall = String.format("pos = %s(sensorX, sensorY, rssi);", this.funcName);
        this.octave.eval(funcCall);
 
        // Obtiene el resultado
        OctaveDouble posOct = octave.get(OctaveDouble.class, "pos");
        if (posOct != null && posOct.size(1) == 2)
            return new double[] { posOct.get(1), posOct.get(2) };
        else
            return null;
    }
    
    /**
     * Pone un vector de tipo de double en la memoria de Octave.
     * 
     * @param array Array a establecer.
     * @param name Nombre de la variable que lo contendrá.
     */
    private void putDoubleArray(final double[] array, final String name) {
        OctaveDouble arrayOct  = new OctaveDouble(array, 1, array.length);
        this.octave.put(name, arrayOct);
    }
    
    /**
     * Prueba a ejecutar el script en Octave de triangulación
     * 
     * @param args Ninguno por el momento.
     */
    public static void main(String[] args) {
        // Prueba la clase
        double[] sensorX  = new double[] { 0, 3, 3 };
        double[] sensorY  = new double[] { 0, 0, 3 };
        double[] rssi     = new double[] { 2, 2, 2 };
        String scriptPath = "../../Localizacion/Triangulacion/PONER_NOMBRE_ARCHIVO.m";
        String funcName   = "PONER_NOMBRE_FUNCION";
        
        TriangulacionOctave octave = new TriangulacionOctave(scriptPath, funcName);
        double[] resultado = octave.triangular(sensorX, sensorY, rssi);
        System.out.printf("[JAVA] X: %.2f | Y: %.2f\n", resultado[0], resultado[1]);
    }
}