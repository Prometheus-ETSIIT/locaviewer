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
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Benito Palacios Sánchez, Álvaro Artigas Gil
 */
public class CompraRespuesta extends Mensaje {
    
    private byte tipo;
    private int compraId;
    private final List<Pagare> pagares;
    
    private CompraRespuesta() {
        this.pagares = new ArrayList<>();
    }

    public CompraRespuesta(byte tipo, int compraId) {
        this.tipo = tipo;
        this.compraId = compraId;
        this.pagares = new ArrayList<>();
    }

    public static CompraRespuesta Parse(final InputStream inStream) {
        DataInputStream reader = new DataInputStream(inStream);
        CompraRespuesta respuesta;
        
        try {
            respuesta = new CompraRespuesta();
            respuesta.tipo = reader.readByte();
            respuesta.compraId = reader.readInt();
            
            byte numPagares = reader.readByte();
            for (int i = 0; i < numPagares; i++)
                respuesta.pagares.add(Pagare.Parse(inStream));
        } catch (IOException ex) {
            System.out.println("ERROR " + ex.getMessage());
            respuesta = null;
        }
        
        return respuesta;
    }
    
    public byte getTipo() {
        return tipo;
    }

    public int getCompraId() {
        return compraId;
    }

    public Pagare[] getPagares() {
        return (Pagare[])this.pagares.toArray(new Pagare[this.pagares.size()]);
    }
        
    public void addPagare(final Pagare p) {
        this.pagares.add(p);
    }
    
    @Override
    public void write(OutputStream outStream) {
        DataOutputStream writer = new DataOutputStream(outStream);
        try {
            writer.writeByte(this.tipo);
            writer.writeInt(this.compraId);
            writer.writeByte(this.pagares.size());
            for (Pagare p : this.pagares)
                p.write(outStream);
        } catch (IOException ex) {
            System.out.println("ERROR " + ex.getMessage());
        }
    }
}
