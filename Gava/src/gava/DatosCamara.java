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

import com.rti.dds.dynamicdata.DynamicData;
import com.rti.dds.infrastructure.ByteSeq;
import es.prometheus.dds.Escritor;
import org.w3c.dom.Element;

/**
 * Representa los datos que se reciben sobre una cámara.
 */
public class DatosCamara {
    private String camId;
    private String sala;
    private double posX;
    private double posY;
    private double angle;
    
    private String codecInfo;
    private int width;
    private int height;
    private byte[] buffer;

    /**
     * Crea una nueva instancia vacía de la clase.
     */
    public DatosCamara() {
    }

    /**
     * Crea una nueva instancia de la clase a partir de los parámetros pasados.
     * 
     * @param camId ID de la cámara.
     * @param sala Sala en la que la cámara graba.
     * @param posX Coordenada X en la sala donde está la cámara.
     * @param posY Coordenada Y en la sala donde está la cámara.
     * @param angle Ángulo en la que la cámara está grabando.
     * @param codecInfo Información del códec de vídeo.
     * @param width Ancho del vídeo.
     * @param height Alto del vídeo.
     * @param buffer Buffer de datos del vídeo.
     */
    public DatosCamara(String camId, String sala, double posX, double posY,
            double angle, String codecInfo, int width, int height, byte[] buffer) {
        this.camId = camId;
        this.sala = sala;
        this.posX = posX;
        this.posY = posY;
        this.angle = angle;
        this.codecInfo = codecInfo;
        this.width = width;
        this.height = height;
        this.buffer = buffer;
    }
    
    /**
     * Crea una nueva instancia a partir de los datos recibidos en Connext DDS.
     * 
     * @param sample Datos para parsear.
     * @return Instancia de esta clase.
     */
    public static DatosCamara FromDds(final DynamicData sample) {
        DatosCamara datos = new DatosCamara();
        datos.camId = sample.get_string("camId", DynamicData.MEMBER_ID_UNSPECIFIED);
        datos.sala  = sample.get_string("sala", DynamicData.MEMBER_ID_UNSPECIFIED);
        datos.posX  = sample.get_double("posX", DynamicData.MEMBER_ID_UNSPECIFIED);
        datos.posY  = sample.get_double("posY", DynamicData.MEMBER_ID_UNSPECIFIED);
        datos.angle = sample.get_double("angle", DynamicData.MEMBER_ID_UNSPECIFIED);
        
        datos.codecInfo = sample.get_string("codecInfo", DynamicData.MEMBER_ID_UNSPECIFIED);
        datos.width  = sample.get_int("width", DynamicData.MEMBER_ID_UNSPECIFIED);
        datos.height = sample.get_int("height", DynamicData.MEMBER_ID_UNSPECIFIED);

        ByteSeq bufferSeq = new ByteSeq();
        sample.get_byte_seq(bufferSeq, "buffer", DynamicData.MEMBER_ID_UNSPECIFIED);
        datos.buffer = bufferSeq.toArrayByte(null);
        
        return datos;
    }
    
    /**
     * Crea una nueva instancia a partir de los datos que contiene una entrada
     * de XML.
     * 
     * @param el Entrada de XML con los datos.
     * @return Instancia de esta clase.
     */
    public static DatosCamara FromXml(final Element el) {
        DatosCamara datos = new DatosCamara();
        
        datos.camId = GetXmlEntryValue(el, "camId");
        datos.sala  = GetXmlEntryValue(el, "sala");
        datos.posX  = Double.parseDouble(GetXmlEntryValue(el, "posX"));
        datos.posY  = Double.parseDouble(GetXmlEntryValue(el, "posY"));
        datos.angle = Double.parseDouble(GetXmlEntryValue(el, "angle"));
        
        datos.codecInfo = GetXmlEntryValue(el, "codecInfo");
        datos.width  = Integer.parseInt(GetXmlEntryValue(el, "width"));
        datos.height = Integer.parseInt(GetXmlEntryValue(el, "height"));
        
        return datos;
    }
    
    /**
     * Shortcut to get the text in a XML entry.
     * 
     * @param el XML entry element.
     * @param name Tag name.
     * @return Value of the entry.
     */
    private static String GetXmlEntryValue(final Element el, final String name) {
        return el.getElementsByTagName(name).item(0).getTextContent();
    }

    /**
     * Obtiene el ID de la cámara.
     * 
     * @return ID de la cámara.
     */
    public String getCamId() {
        return camId;
    }

    /**
     * Establece el ID de la cámara.
     * 
     * @param camId ID de la cámara.
     */
    public void setCamId(String camId) {
        this.camId = camId;
    }

    /**
     * Obtiene la sala en la que la cámara está grabando.
     * 
     * @return Sala en la que se está grabando.
     */
    public String getSala() {
        return sala;
    }

    /**
     * Establece la sala en la que se está grabando.
     * 
     * @param sala Sala en la que se está grabando.
     */
    public void setSala(String sala) {
        this.sala = sala;
    }

    /**
     * Obtiene la coordenada X de la cámara en la sala.
     * 
     * @return coordenada X de la cáma en la sala.
     */
    public double getPosX() {
        return posX;
    }

    /**
     * Establece la coordenada X de la cámara en la sala.
     * 
     * @param posX Coordenada X de la cámara en la sala.
     */
    public void setPosX(double posX) {
        this.posX = posX;
    }

    /**
     * Obtiene la coordenada Y de la cámara en la sala.
     * 
     * @return Coordenada Y de la cámara en la sala.
     */
    public double getPosY() {
        return posY;
    }

    /**
     * Establece la coordenada Y de la cámara en la sala.
     * 
     * @param posY Coordenada Y de la cámara en la sala.
     */
    public void setPosY(double posY) {
        this.posY = posY;
    }

    /**
     * Obtiene el ángulo con el que está grabando la cámara.
     * 
     * @return Ángulo con el que está grabando.
     */
    public double getAngle() {
        return angle;
    }

    /**
     * Establece el ángulo con el que está grabando.
     * 
     * @param angle Ángulo con el que está grabando.
     */
    public void setAngle(double angle) {
        this.angle = angle;
    }

    /**
     * Obtiene la información del códec de vídeo.
     * 
     * @return Información del códec.
     */
    public String getCodecInfo() {
        return codecInfo;
    }

    /**
     * Establece la información del códec de vídeo.
     * 
     * @param codecInfo Información del códec.
     */
    public void setCodecInfo(String codecInfo) {
        this.codecInfo = codecInfo;
    }

    /**
     * Obtiene el ancho del vídeo.
     * 
     * @return Ancho del vídeo.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Establece el ancho del vídeo.
     * 
     * @param width 
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * Obtiene el alto del vídeo.
     * 
     * @return Alto del vídeo.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Establece el alto del vídeo.
     * 
     * @param height Alto del vídeo.
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * Obtiene datos del vídeo.
     * 
     * @return Datos del vídeo.
     */
    public byte[] getBuffer() {
        return buffer;
    }

    /**
     * Establece datos del vídeo.
     * 
     * @param buffer Datos del vídeo.
     */
    public void setBuffer(byte[] buffer) {
        this.buffer = buffer;
    }
    
    /**
     * Escribe los datos en una instancia reutilizable.
     * 
     * @param datos Instancia en la que escribir los datos.
     */
    public void escribeDds(DynamicData datos) {
        datos.clear_all_members();
        
        datos.set_string("camId", DynamicData.MEMBER_ID_UNSPECIFIED, this.camId);
        datos.set_string("sala",  DynamicData.MEMBER_ID_UNSPECIFIED, this.sala);
        datos.set_double("posX",  DynamicData.MEMBER_ID_UNSPECIFIED, this.posX);
        datos.set_double("posY",  DynamicData.MEMBER_ID_UNSPECIFIED, this.posY);
        datos.set_double("angle", DynamicData.MEMBER_ID_UNSPECIFIED, this.angle);

        datos.set_string("codecInfo", DynamicData.MEMBER_ID_UNSPECIFIED, this.codecInfo);
        datos.set_int("width",        DynamicData.MEMBER_ID_UNSPECIFIED, this.width);
        datos.set_int("height",       DynamicData.MEMBER_ID_UNSPECIFIED, this.height);
        datos.set_byte_seq("buffer",  DynamicData.MEMBER_ID_UNSPECIFIED, new ByteSeq(this.buffer));
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
    public DatosCamara clone() {
        DatosCamara clon = null;
        
        // JAVA: MIRA ME CAGO EN TO LA MIERDA DE OBLIGAR A CAPTURAR LAS EXCEPCIONES.
        try {
            clon = (DatosCamara)super.clone();
        } catch (CloneNotSupportedException ex) {
        }
        
        return clon;
    }
}
