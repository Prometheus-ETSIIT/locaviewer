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
public class InfoCompra extends Mensaje {
    
    private int productoId;
    private int vendedorId;
    private Pagare[] pagares;
    
    private InfoCompra() {
    }

    public InfoCompra(final int productoId, final int vendedorId,
            final Pagare[] pagares) {
        this.productoId = productoId;
        this.vendedorId = vendedorId;
        this.pagares    = pagares;
    }
    
    public static InfoCompra Parse(final InputStream inStream) {
        DataInputStream reader = new DataInputStream(inStream);
        InfoCompra compra;
        
        try {
            compra = new InfoCompra();
            compra.productoId = reader.readInt();
            compra.vendedorId = reader.readInt();
            
            byte numPagares = reader.readByte();
            compra.pagares = new Pagare[numPagares];
            for (int i = 0; i < numPagares; i++)
                compra.pagares[i] = Pagare.Parse(inStream);
        } catch (IOException ex) {
            System.out.println("ERROR " + ex.getMessage());
            compra = null;
        }
        
        return compra;
    }

    public int getProductoId() {
        return productoId;
    }

    public int getVendedorId() {
        return vendedorId;
    }

    public Pagare[] getPagares() {
        return pagares;
    }
    
    @Override
    public void write(final OutputStream outStream) {
        DataOutputStream writer = new DataOutputStream(outStream);
        
        try {
            writer.writeInt(this.productoId);
            writer.writeInt(this.vendedorId);
            writer.writeByte(this.pagares.length);
            for (Pagare p : this.pagares)
                p.write(outStream);
        } catch (IOException ex) {
            System.out.println("ERROR " + ex.getMessage());
        }
    }
}
