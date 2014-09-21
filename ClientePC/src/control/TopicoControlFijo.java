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

import com.rti.dds.dynamicdata.DynamicDataReader;

/**
 * Crea y reutiliza lectores predefinidos en el XML.
 */
public class TopicoControlFijo extends TopicoControl {
    private final static int MAX_LECTORES = 4;
    private final DynamicDataReader[] lectoresUsados = new DynamicDataReader[MAX_LECTORES];
    private final String lectorName;
    
    /**
     * Crea una nueva instancia del control de tópico a partir de los nombres
     * del XML del participante, lector.
     * 
     * @param 
     */
    public TopicoControlFijo(final String partName,
            final String lectorName) {
        super(partName);
        this.lectorName = lectorName;
    }
    
    @Override
    public DynamicDataReader creaLector() {
        // Primero buscamos un lector que no se esté usando
        int i;
        for (i = 0; i < this.lectoresUsados.length; i++)
            if (this.lectoresUsados[i] == null)
                break;
        
        this.lectoresUsados[i] = (DynamicDataReader)this.getParticipante()
                .lookup_datareader_by_name(this.lectorName + i);
        
        if (this.lectoresUsados[i] == null)
            System.out.println("No se pudo crear el lector.");
        
        return this.lectoresUsados[i];
    }
    
    @Override
    public void eliminaLector(final DynamicDataReader reader) {
        // Lo libera de los usados
        for (int i = 0; i < this.lectoresUsados.length; i++)
            if (this.lectoresUsados[i] == reader)
                this.lectoresUsados[i] = null;
        
        // NOTA:
        // NO se puede eliminar del dominio porque si no no se podría volver a
        // recuperar. Simplemente le quitamos el listener y las condiciones.
    }
}
