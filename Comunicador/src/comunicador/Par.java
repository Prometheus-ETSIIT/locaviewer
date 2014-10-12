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

package comunicador;

/**
 * Par de datos.
 * 
 * @param <T> Tipo del primer dato.
 * @param <E> Tipo del segundo dato.
 */
public class Par<T, E> {
    private T primero;
    private E segundo;

    /**
     * Crea una nueva instancia de la clase.
     * 
     * @param nuevoPrimero Primer dato.
     * @param nuevoSegundo Segundo dato.
     */
    public Par(T nuevoPrimero, E nuevoSegundo) {
        this.primero = nuevoPrimero;
        this.segundo = nuevoSegundo;
    }

    /**
     * Establece el primer dato.
     * 
     * @param nuevoPrimero Primer dato.
     */
    public void setPrimero(T nuevoPrimero) {
        this.primero = nuevoPrimero;
    }

    /**
     * Establece el segundo dato.
     * 
     * @param nuevoSegundo Segundo dato.
     */
    public void setSecond(E nuevoSegundo) {
        this.segundo = nuevoSegundo;
    }

    /**
     * Obtiene el primer dato.
     * 
     * @return Primer dato.
     */
    public T getPrimero() {
        return primero;
    }

    /**
     * Obtiene el segundo dato.
     * 
     * @return Segundo dato.
     */
    public E getSegundo() {
        return segundo;
    }
    
    @Override
    public boolean equals(Object o) {
      if (o == null) return false;
      if (!(o instanceof Par)) return false;
      @SuppressWarnings("unchecked")
	Par<Integer, Integer> par = (Par<Integer, Integer>) o;
	return primero.equals(par.getPrimero()) && segundo.equals(par.getSegundo());
    }
}