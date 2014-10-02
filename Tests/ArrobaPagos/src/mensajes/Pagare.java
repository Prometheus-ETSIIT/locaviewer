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

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Benito Palacios Sánchez, Álvaro Artigas Gil
 */
public class Pagare {
    private static final SimpleDateFormat FechaFormat = new SimpleDateFormat("ddMMyyyyHHmmss");
        
    private long id;
    private short valor;
    private Date fecha;
    
    private int vendedorId;
    
    private byte compradorEdad;
    private byte compradorPais;
    
    private byte[] seguridad;
    
    private Pagare() {
    }
    
    public Pagare(final long id, final short valor, final Date fecha, 
            final int vendedorId, final byte compradorEdad,
            final byte compradorPais, final int secreto) {
        
        this.id = id;
        this.valor = valor;
        this.fecha = fecha;
        this.vendedorId = vendedorId;
        this.compradorEdad = compradorEdad;
        this.compradorPais = compradorPais;
        this.asegura(secreto);
    }
    
    public static Pagare Parse(final InputStream inStream) {
        DataInputStream reader = new DataInputStream(inStream);
        Pagare pagare = new Pagare();
        
        try {
            pagare.id    = reader.readLong();
            pagare.valor = reader.readShort();
            
            byte[] fechaBuffer = new byte[14];
            reader.readFully(fechaBuffer);
            String fechaStr = new String(fechaBuffer);
            pagare.fecha = FechaFormat.parse(fechaStr);
            
            pagare.vendedorId = reader.readInt();
            pagare.compradorEdad = reader.readByte();
            pagare.compradorPais = reader.readByte();
            pagare.seguridad = new byte[32];
            reader.readFully(pagare.seguridad);
        
        } catch (IOException | ParseException ex) {
        }
        
        return pagare;
    }

    public long getId() {
        return id;
    }

    public short getValor() {
        return valor;
    }

    public Date getFecha() {
        return fecha;
    }

    public int getVendedorId() {
        return vendedorId;
    }

    public byte getCompradorEdad() {
        return compradorEdad;
    }

    public byte getCompradorPais() {
        return compradorPais;
    }
    
    public void write(final OutputStream outStream) {
        this.write(outStream, true);
    }
    
    private void write(final OutputStream outStream, final boolean conSeguridad) {
        DataOutputStream writer = new DataOutputStream(outStream);
        
        try {
            writer.writeLong(this.id);
            writer.writeShort(this.valor);
            writer.write(FechaFormat.format(this.fecha).getBytes());
            writer.writeInt(this.vendedorId);
            writer.writeByte(this.compradorEdad);
            writer.writeByte(this.compradorPais);
            
            if (conSeguridad)
                writer.write(this.seguridad);
        } catch (IOException ex) {
        }
    }
    
    public boolean validate(final int secreto) {       
        // Calcula el código de seguridad con el secreto dado.
        byte[] seguridadNueva = this.calculaSeguridad(secreto);
        
        // Lo compara, hasta que se desmuestre lo contrario son iguales.
        boolean valido = (seguridad.length == seguridadNueva.length);
        for (int i = 0; i < seguridad.length && valido; i++)
            if (this.seguridad[i] != seguridadNueva[i])
                valido = false;
        
        return valido;
    }
    
    private void asegura(final int secreto) {
        this.seguridad = this.calculaSeguridad(secreto);
    }
    
    private byte[] calculaSeguridad(final int secreto) {
        byte[] seguridad = null;
        
        try {
            // Obtenemos un motor de cálculo de funcionas Hash (resumen)
            MessageDigest resumen = MessageDigest.getInstance("SHA-256");
            
            // Introduzco los datos del pagaré (sin el código de seguridad)
            ByteArrayOutputStream pagareBin = new ByteArrayOutputStream(30);
            this.write(pagareBin, false);
            resumen.update(pagareBin.toByteArray()); 
            
            // Le añado el secreto
            ByteArrayOutputStream secretoBin = new ByteArrayOutputStream(4);
            (new DataOutputStream(pagareBin)).writeInt(secreto);
            resumen.update(secretoBin.toByteArray());
            
            // Calcula el resumen
            seguridad = resumen.digest();
        } catch (IOException | NoSuchAlgorithmException ex) {
        }
        
        return seguridad;
    }
    
    @Override
    public String toString() {
        return String.format(
                "[ID: %08X, Valor: %d, Fecha: %s, Vendedor ID: %08X," +
                        " Edad: %d, Pais: %d]",
                this.id,
                this.valor,
                this.fecha,
                this.vendedorId,
                this.compradorEdad,
                this.compradorPais
        );
    }
}
