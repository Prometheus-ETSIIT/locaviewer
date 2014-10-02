/*
 * Copyright (C) 2014 Benito Palacios Sánchez, Álvaro Artigas Gil
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

package mensajes;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author Benito Palacios Sánchez, Álvaro Artigas Gil
 */
public class Solicitud extends Mensaje {

    private int    vendedorId;
    private short  valor;
    
    private String nombre;
    private String apellidos;
    private byte   edad;
    private byte   pais;
    
    private String tarjetaNum;
    private byte   tarjetaMes;
    private byte   tarjetaAnio;
    private String tarjetaSeg;
    
    private Solicitud() {
    }

    public Solicitud(int vendedorId, short valor, String nombre, String apellidos,
            byte edad, byte pais, String tarjetaNum, byte tarjetaMes, 
            byte tarjetaAnio, String tarjetaSeg) {
        this.vendedorId = vendedorId;
        this.valor     = valor;
        this.nombre    = nombre;
        this.apellidos = apellidos;
        this.edad = edad;
        this.pais = pais;
        this.tarjetaNum  = tarjetaNum;
        this.tarjetaMes  = tarjetaMes;
        this.tarjetaAnio = tarjetaAnio;
        this.tarjetaSeg  = tarjetaSeg;
    }
    
    
    
    public static Solicitud Parse(final InputStream inStream) {
        DataInputStream reader = new DataInputStream(inStream);
        Solicitud solicitud = new Solicitud();
        
        try {
            solicitud.vendedorId = reader.readInt();
            solicitud.valor      = reader.readShort();
            
            // Info comprador
            solicitud.nombre    = reader.readUTF();
            solicitud.apellidos = reader.readUTF();
            solicitud.edad      = reader.readByte();
            solicitud.pais      = reader.readByte();
            
            // Info tarjeta
            byte[] tarjetaNumBytes = new byte[16];
            reader.readFully(tarjetaNumBytes);
            solicitud.tarjetaNum = new String(tarjetaNumBytes);
            
            solicitud.tarjetaMes = reader.readByte();
            solicitud.tarjetaAnio = reader.readByte();
            solicitud.tarjetaSeg = "" + (char)reader.readByte() + 
                    (char)reader.readByte() + (char)reader.readByte();
            
        } catch (IOException ex) {
            System.out.println("ERROR: " + ex.getMessage());
            solicitud = null;
        }
        
        return solicitud;
    }

    public int getVendedorId() {
        return vendedorId;
    }

    public short getValor() {
        return valor;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public byte getEdad() {
        return edad;
    }

    public byte getPais() {
        return pais;
    }

    public String getTarjetaNum() {
        return tarjetaNum;
    }

    public byte getTarjetaMes() {
        return tarjetaMes;
    }

    public byte getTarjetaAnio() {
        return tarjetaAnio;
    }

    public String getTarjetaSeg() {
        return tarjetaSeg;
    }
    
    @Override
    public void write(final OutputStream outStream) {
        DataOutputStream writer = new DataOutputStream(outStream);
        
        try {
            writer.writeInt(this.vendedorId);
            writer.writeShort(this.valor);
            
            writer.writeUTF(this.nombre);
            writer.writeUTF(this.apellidos);
            writer.writeByte(this.edad);
            writer.writeByte(this.pais);
            
            writer.writeBytes(this.tarjetaNum);
            writer.writeByte(this.tarjetaMes);
            writer.writeByte(this.tarjetaAnio);
            writer.writeBytes(this.tarjetaSeg);
        } catch (IOException ex) {
        }
    }
}
