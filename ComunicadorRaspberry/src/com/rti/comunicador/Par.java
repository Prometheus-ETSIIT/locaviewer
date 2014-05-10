package com.rti.comunicador;

public class Par<tipoPrimero, tipoSegundo> {
    private tipoPrimero primero;
    private tipoSegundo segundo;

    
    public Par(tipoPrimero nuevoPrimero, tipoSegundo nuevoSegundo) {
        this.primero = nuevoPrimero;
        this.segundo = nuevoSegundo;
    }

    public void setPrimero(tipoPrimero nuevoPrimero) {
        this.primero = nuevoPrimero;
    }

    public void setSecond(tipoSegundo nuevoSegundo) {
        this.segundo = nuevoSegundo;
    }

    public tipoPrimero getPrimero() {
        return primero;
    }

    public tipoSegundo getSegundo() {
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