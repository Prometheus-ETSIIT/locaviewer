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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Benito Palacios Sánchez, Álvaro Artigas Gil
 */
public class SolicitudRespuesta extends Mensaje {
    private byte tipo;
    private final List<Pagare> pagares;
    
    public SolicitudRespuesta () {
        this.pagares = new ArrayList<>();
    }
    
    public SolicitudRespuesta(final byte tipo) {
        this.tipo = tipo;
        this.pagares = new ArrayList<>();
    }
    
    public static SolicitudRespuesta Parse(final InputStream inStream) {
        SolicitudRespuesta respuesta = new SolicitudRespuesta();
        
        try {
            respuesta.tipo = (byte)inStream.read();
            
            byte numPagares = (byte)inStream.read();
            for (int i = 0; i < numPagares; i++)
                respuesta.addPagare(Pagare.Parse(inStream));
        } catch (IOException ex) {
        }
        
        return respuesta;
    }
    
    public byte getTipo() {
        return this.tipo;
    }
    
    public Pagare[] getPagares() {
        return this.pagares.toArray(new Pagare[this.pagares.size()]);
    }
    
    public void addPagare(final Pagare p) {
        this.pagares.add(p);
    }
    
    @Override
    public void write(final OutputStream outStream) {
        try {
            outStream.write(this.tipo);
            outStream.write(this.pagares.size());
        } catch (IOException ex) {
            System.out.println("ERROR: " + ex.getMessage());
        }
        
        for (Pagare p : pagares)
            p.write(outStream);
    }
    
}
