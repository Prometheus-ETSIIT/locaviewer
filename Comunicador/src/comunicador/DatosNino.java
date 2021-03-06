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

import com.rti.dds.dynamicdata.DynamicData;
import es.prometheus.dds.Escritor;

/**
 * Datos enviados por el tópico de niños.
 */
public class DatosNino implements Cloneable {
    private double calidad;
    private String id;
    private String camId;
    private String sala;
    private double salaW;
    private double salaL;
    private double posX;
    private double posY;
    private String nombre;
    private String apodo;

    /**
     * Crea una instancia vacía de los datos.
     */
    public DatosNino() {
    }

    /**
     * Crea una instancia a partir de los valores pasados.
     *
     * @param calidad Fiabilidad de los datos.
     * @param id ID del niño.
     * @param camId ID de la mejor cámara que apunta al niño.
     * @param sala Habitación en la que se encuentra el niño.
     * @param salaW Ancho de la habitación.
     * @param salaL Largo de la habitación.
     * @param posX Coordenada X de la posición del niño.
     * @param posY Coordenada Y de la posición del niño.
     * @param nombre Nombre del niño.
     * @param apodo Apodo del niño.
     */
    public DatosNino(double calidad, String id, String camId, String sala,
            double salaW, double salaL, double posX, double posY,
            String nombre, String apodo) {
        this.calidad = calidad;
        this.id = id;
        this.camId = camId;
        this.sala = sala;
        this.salaW = salaW;
        this.salaL = salaL;
        this.posX = posX;
        this.posY = posY;
        this.nombre = nombre;
        this.apodo = apodo;
    }

    /**
     * Crea una instancia de la estructura leyendo datos recibidos por DDS.
     *
     * @param sample Estructura de datos recibida.
     * @return Nueva instancia.
     */
    public static DatosNino FromDds(final DynamicData sample) {
        DatosNino datos = new DatosNino();
        datos.calidad = sample.get_double("calidad", DynamicData.MEMBER_ID_UNSPECIFIED);
        datos.id      = sample.get_string("id",      DynamicData.MEMBER_ID_UNSPECIFIED);
        datos.camId   = sample.get_string("camId",   DynamicData.MEMBER_ID_UNSPECIFIED);
        datos.sala    = sample.get_string("sala",    DynamicData.MEMBER_ID_UNSPECIFIED);
        datos.salaW   = sample.get_double("salaW",   DynamicData.MEMBER_ID_UNSPECIFIED);
        datos.salaL   = sample.get_double("salaL",   DynamicData.MEMBER_ID_UNSPECIFIED);
        datos.posX    = sample.get_double("posX",    DynamicData.MEMBER_ID_UNSPECIFIED);
        datos.posY    = sample.get_double("posY",    DynamicData.MEMBER_ID_UNSPECIFIED);
        datos.nombre  = sample.get_string("nombre",  DynamicData.MEMBER_ID_UNSPECIFIED);
        datos.apodo   = sample.get_string("apodo",   DynamicData.MEMBER_ID_UNSPECIFIED);
        return datos;
    }

    /**
     * Crea una instancia de la estructura a partir de un resumen.
     *
     * @param summary Resumen de datos.
     * @return Nueva instancia.
     */
    public static DatosNino FromSummary(final String summary) {
        DatosNino datos = new DatosNino();
        String[] fields = summary.split(",");
        datos.id     = fields[0];
        datos.nombre = fields[1];
        datos.apodo  = fields[2];
        return datos;
    }

    /**
     * Obtiene la calidad de los datos recibidos.
     *
     * @return Calidad de los datos.
     */
    public double getCalidad() {
        return calidad;
    }

    /**
     * Establece la calidad de los datos a enviar.
     *
     * @param calidad Calidad de los datos.
     */
    public void setCalidad(double calidad) {
        this.calidad = calidad;
    }

    /**
     * Obtiene el ID del niño.
     *
     * @return ID del niño.
     */
    public String getId() {
        return id;
    }

    /**
     * Establece el ID del niño.
     *
     * @param id ID del niño.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Obtiene el ID de la cámara que enfoca al niño.
     *
     * @return ID de la cámara.
     */
    public String getCamId() {
        return camId;
    }

    /**
     * Establece el ID de la cámara que enfoca al niño.
     *
     * @param camId ID de la cámara.
     */
    public void setCamId(String camId) {
        this.camId = camId;
    }

    /**
     * Obtiene la habitación en la que se encuentra el niño.
     *
     * @return Habitación en la que se encuentra el niño.
     */
    public String getSala() {
        return sala;
    }

    /**
     * Establece la habitación en la que se encuentra el niño.
     *
     * @param sala Habitación en la que se encuentra el niño.
     */
    public void setSala(String sala) {
        this.sala = sala;
    }

    /**
     * Obtiene el ancho de la sala.
     *
     * @return Ancho de la sala.
     */
    public double getSalaW() {
        return this.salaW;
    }

    /**
     * Establece el ancho de la sala.
     *
     * @param width Ancho de la sala.
     */
    public void setSalaW(double width) {
        this.salaW = width;
    }

    /**
     * Obtiene la longitud de la sala.
     *
     * @return Longitud de la sala.
     */
    public double getSalaL() {
        return this.salaL;
    }

    /**
     * Establece la longitud de la sala.
     *
     * @param length Longitud de la sala.
     */
    public void setSalaL(double length) {
        this.salaL = length;
    }

    /**
     * Obtiene la coordenada X de la posición del niño.
     *
     * @return Coordenada X.
     */
    public double getPosX() {
        return posX;
    }

    /**
     * Establece la coordenada X de la posición del niño.
     *
     * @param posX Coordenada X.
     */
    public void setPosX(double posX) {
        this.posX = posX;
    }

    /**
     * Obtiene la coordenada Y de la posición del niño.
     *
     * @return Coordenada Y.
     */
    public double getPosY() {
        return posY;
    }

    /**
     * Establece la coordenada Y de la posición del niño.
     *
     * @param posY Coordenada Y.
     */
    public void setPosY(double posY) {
        this.posY = posY;
    }

    /**
     * Obtiene el nombre del niño.
     *
     * @return Nombre del niño.
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Establece el nombre del niño.
     *
     * @param nombre Nombre del niño.
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Obtiene el apodo del niño.
     *
     * @return Apodo del niño.
     */
    public String getApodo() {
        return apodo;
    }

    /**
     * Establece el apodo del niño.
     *
     * @param apodo Apodo del niño.
     */
    public void setApodo(String apodo) {
        this.apodo = apodo;
    }

    /**
     * Obtiene un resumen de los datos del niño a partir de aquellos datos
     * que no cambian.
     * Usado para los metadatos del publicador.
     *
     * @return Resumen de datos del niño.
     */
    public String getSummary() {
        return this.id + "," + this.nombre + "," + this.apodo;
    }

    /**
     * Escribe los datos en una instancia reutilizable.
     *
     * @param datos Instancia en la que escribir los datos.
     */
    public void escribeDds(DynamicData datos) {
        datos.clear_all_members();
        datos.set_double("calidad", DynamicData.MEMBER_ID_UNSPECIFIED, this.calidad);
        datos.set_string("id",      DynamicData.MEMBER_ID_UNSPECIFIED, this.id);
        datos.set_string("camId",   DynamicData.MEMBER_ID_UNSPECIFIED, this.camId);
        datos.set_string("sala",    DynamicData.MEMBER_ID_UNSPECIFIED, this.sala);
        datos.set_double("salaW",   DynamicData.MEMBER_ID_UNSPECIFIED, this.salaW);
        datos.set_double("salaL",   DynamicData.MEMBER_ID_UNSPECIFIED, this.salaL);
        datos.set_double("posX",    DynamicData.MEMBER_ID_UNSPECIFIED, this.posX);
        datos.set_double("posY",    DynamicData.MEMBER_ID_UNSPECIFIED, this.posY);
        datos.set_string("nombre",  DynamicData.MEMBER_ID_UNSPECIFIED, this.nombre);
        datos.set_string("apodo",   DynamicData.MEMBER_ID_UNSPECIFIED, this.apodo);
    }

    /**
     * Escribe los datos de esta estructura en una instancia de un sólo uso.
     *
     * @param escritor Escritor del que generar la instancia y enviar.
     */
    public void escribeDds(Escritor escritor) {
        DynamicData datos = escritor.creaDatos();
        this.escribeDds(datos);
        escritor.escribeDatos(datos);
        escritor.eliminaDatos(datos);
    }

    @Override
    public DatosNino clone() {
        DatosNino clon = null;

        // JAVA: MIRA ME CAGO EN TO LA MIERDA DE OBLIGAR A CAPTURAR LAS EXCEPCIONES.
        try {
            clon = (DatosNino)super.clone();
        } catch (CloneNotSupportedException ex) {
        }

        return clon;
    }
}
