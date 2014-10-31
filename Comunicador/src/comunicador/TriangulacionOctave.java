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

package comunicador;

import dk.ange.octave.OctaveEngine;
import dk.ange.octave.OctaveEngineFactory;
import dk.ange.octave.exception.OctaveEvalException;
import dk.ange.octave.type.Octave;
import dk.ange.octave.type.OctaveDouble;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Establece un enlace para ejecutar código de triangulación en Octave.
 */
public class TriangulacionOctave {
    private final OctaveEngine octave;
    private final String funcName;
    private List<DatosCamara> cams;
    private final double width;
    private final double length;

    private boolean alive;
    private double[] lastPosition;
    private int lastCamIdx;


    /**
     * Crea una nueva instancia inicializando el engine.
     *
     * @param scriptPath Ruta al script con las funciones de triangulación.
     * @param funcName Nombre de la función a ejecutar. Esta función tendrá
     * los siguientes argumentos en el siguiente orden:
     *  + Vector de double con posición X de los sensores.
     *  + Vector de double con posición Y de los sensores.
     *  + Valor RSSI de los sensores.
     * @param cams Datos de las cámaras disponibles.
     * @param width Ancho de la habitación.
     * @param length Largo de la habitación.
     * @param mostrarVentana Si se debe mostrar o no una ventana de prueba.
     */
    public TriangulacionOctave(final String scriptPath, final String funcName,
            final List<DatosCamara> cams, final double width, final double length,
            final boolean mostrarVentana) {
        this.octave    = new OctaveEngineFactory().getScriptEngine();
        this.funcName  = funcName;
        this.width     = width;
        this.length    = length;
        this.cams      = cams;
        this.alive     = this.initialize(scriptPath, width, length);

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
    private boolean initialize(final String scriptPath, final double width,
            final double length) {
        // Información sobre los métodos: http://goo.gl/1kbd4y

        // Lee las funciones
        String script;
        try {
            Path path = FileSystems.getDefault().getPath(scriptPath);
            script = new String(Files.readAllBytes(path), Charset.forName("ISO-8859-15"));
        } catch (IOException ex) {
            System.err.printf(
                    "[Triangulación] Error al leer el archivo: %s\n",
                    scriptPath
            );
            return false;
        }

        // Las carga en memoria
        try {
            this.octave.eval(script);
        } catch (OctaveEvalException ex) {
            System.err.printf(
                    "[Triangulación] Error al evaluar el script\n%s\n",
                    ex.getMessage()
            );
            return false;
        }

        // Obtiene un array con la posición de las cámaras y sus ángulos
        OctaveDouble camPos  = new OctaveDouble(this.cams.size(), 2);
        OctaveDouble angulos = new OctaveDouble(1, this.cams.size());
        for (int i = 0; i < this.cams.size(); i++) {
            camPos.set(this.cams.get(i).getPosX(), i + 1, 1);
            camPos.set(this.cams.get(i).getPosY(), i + 1, 2);
            angulos.set(this.cams.get(i).getAngle(), 1, i + 1);
        }

        // Lo carga en memoria
        this.octave.put("camaras", camPos);
        this.octave.put("angulos", angulos);
        this.octave.put("ancho", Octave.scalar(width));
        this.octave.put("largo", Octave.scalar(length));

        return true;
    }

    /**
     * Obtiene la lista de cámaras disponibles.
     *
     * @return Cámaras disponibles.
     */
    public List<DatosCamara> getCamaras() {
        return this.cams;
    }

    /**
     * Establece la lista de cámaras disponibles.
     *
     * @param nuevasCamaras Cámaras disponibles.
     */
    public void setCamaras(final List<DatosCamara> nuevasCamaras) {
        this.cams = nuevasCamaras;

        // Obtiene un array con la posición de las cámaras
        OctaveDouble camPos = new OctaveDouble(this.cams.size(), 2);
        OctaveDouble angulos = new OctaveDouble(1, this.cams.size());
        for (int i = 0; i < this.cams.size(); i++) {
            camPos.set(this.cams.get(i).getPosX(), i + 1, 1);
            camPos.set(this.cams.get(i).getPosY(), i + 1, 2);
            angulos.set(this.cams.get(i).getAngle(), 1, i + 1);
        }

        this.octave.put("camaras", camPos);
        this.octave.put("angulos", angulos);
    }

    /**
     * Obtiene el ancho de la habitación.
     *
     * @return Ancho de la habitación.
     */
    public double getWidth() {
        return this.width;
    }

    /**
     * Obtiene el largo de la habitación.
     *
     * @return Largo de la habitación.
     */
    public double getLength() {
        return this.length;
    }

    /**
     * Obtiene el último índice de la cámara elegida.
     *
     * @return Índice de la cámara elegida.
     */
    public int getLastCamIndex() {
        return this.lastCamIdx;
    }

    /**
     * Obtiene la última posición triangulada.
     *
     * @return Posición triangulada.
     */
    public double[] getLastPosition() {
        return this.lastPosition;
    }

    /**
     * Realiza una triangulación a partir de la señal recibida en los sensores
     * y de su posición.
     *
     * @param datos Conjunto de sensores con valores de RSSI.
     * @return Posición X e Y de la triangulación.
     */
    public String triangular(final List<DatosSensor> datos) {
        if (!this.alive) {
            System.err.println("[Triangulación] Error ¡el sistema está muerto!");
            return null;
        }

        // Crea una array con la posición de los sensores y los RSSI
        OctaveDouble bluePos = new OctaveDouble(datos.size(), 2);
        OctaveDouble rssi    = new OctaveDouble(1, datos.size());
        for (int i = 0; i < datos.size(); i++) {
            bluePos.set(datos.get(i).getPosicionSensor().getPrimero(), i + 1, 1);
            bluePos.set(datos.get(i).getPosicionSensor().getSegundo(), i + 1, 2);
            rssi.set(datos.get(i).getIntensidad(), 1, i + 1);
        }

        // Lo pone en memoria
        this.octave.put("bluePos", bluePos);
        this.octave.put("rssi", rssi);

        // Llama a la función que tiene en memoria
        try {
            String funcCall = String.format(
                    "[ninoPos, idxCam] = %s(rssi, bluePos, camaras, ancho, largo, angulos);",
                    this.funcName
            );
            this.octave.eval(funcCall);
        } catch (OctaveEvalException ex) {
            System.err.printf(
                    "[Triangulación] Error al evaluar el script\n%s\n",
                    ex.getMessage()
            );
            return null;
        }

        // Obtiene el resultado
        OctaveDouble idxCam = octave.get(OctaveDouble.class, "idxCam");
        if (idxCam != null && idxCam.size(1) == 1 && idxCam.get(1) != -1) {
            this.lastCamIdx = (int)idxCam.get(1) - 1;
            this.lastPosition = octave.get(OctaveDouble.class, "ninoPos").getData();
            return this.cams.get(this.lastCamIdx).getCamId();
        } else {
            return null;
        }
    }
}
