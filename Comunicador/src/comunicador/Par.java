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
