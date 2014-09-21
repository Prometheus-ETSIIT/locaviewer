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

package control;

import com.rti.dds.dynamicdata.DynamicData;

/**
 * Datos enviados por el tópico de niños.
 */
public class DatosNino {
    private double calidad;
    private String id;
    private String camId;
    private String sala;
    private double posX;
    private double posY;
    private String nombre;
    private String apodo;
    
    public DatosNino() {
    }

    public DatosNino(double calidad, String id, String camId, String sala,
            double posX, double posY, String nombre, String apodo) {
        this.calidad = calidad;
        this.id = id;
        this.camId = camId;
        this.sala = sala;
        this.posX = posX;
        this.posY = posY;
        this.nombre = nombre;
        this.apodo = apodo;
    }
    
    public static DatosNino FromDds(final DynamicData sample) {
        DatosNino datos = new DatosNino();
        datos.calidad = sample.get_double("calidad", DynamicData.MEMBER_ID_UNSPECIFIED);
        datos.id      = sample.get_string("id", DynamicData.MEMBER_ID_UNSPECIFIED);
        datos.camId   = sample.get_string("camId", DynamicData.MEMBER_ID_UNSPECIFIED);
        datos.sala    = sample.get_string("sala", DynamicData.MEMBER_ID_UNSPECIFIED);
        datos.posX    = sample.get_double("posX", DynamicData.MEMBER_ID_UNSPECIFIED);
        datos.posY    = sample.get_double("posY", DynamicData.MEMBER_ID_UNSPECIFIED);
        datos.nombre  = sample.get_string("nombre", DynamicData.MEMBER_ID_UNSPECIFIED);
        datos.apodo   = sample.get_string("apodo", DynamicData.MEMBER_ID_UNSPECIFIED);
        return datos;
    }

    public double getCalidad() {
        return calidad;
    }

    public void setCalidad(double calidad) {
        this.calidad = calidad;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCamId() {
        return camId;
    }

    public void setCamId(String camId) {
        this.camId = camId;
    }

    public String getSala() {
        return sala;
    }

    public void setSala(String sala) {
        this.sala = sala;
    }

    public double getPosX() {
        return posX;
    }

    public void setPosX(double posX) {
        this.posX = posX;
    }

    public double getPosY() {
        return posY;
    }

    public void setPosY(double posY) {
        this.posY = posY;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApodo() {
        return apodo;
    }

    public void setApodo(String apodo) {
        this.apodo = apodo;
    }
}
