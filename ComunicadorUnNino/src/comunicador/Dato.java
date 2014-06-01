package comunicador;

import java.util.Date;

import comunicador.Par;

class Dato {
	private String ID;//ID del escritor del dato
	private Par<Integer,Integer> posicionSensor;//Posición del sensor bluetooth (una misma ID puede tener varios sensores)
	private String IDnino; //ID del niño
	private int intensidad;//Intensidad de la señal
	private long creacion;
	
	/*Constructor para la clase Dato
	 * @param newID		ID del sensor emisor
	 * @param posXB 	Posición X del sensor
	 * @param posYB		Posición Y del sensor
	 * @param IDnino	ID del niño
	 * @param newIntensidad		Intensidad de señal
	 * */

	public Dato(String newID, Integer posXB,Integer posYB, String newIDnino, Integer newIntensidad){
		ID=newID;
		posicionSensor = new Par<Integer, Integer>(posXB,posYB);
		IDnino=newIDnino;
		intensidad=newIntensidad;
		creacion = new Date().getTime();
	}
	
	
	/*Constructor para la clase Dato
	 * @param newID		ID del sensor emisor
	 * @param posB 		Posición del sensor
	 * @param IDnino	ID del niño
	 * @param newIntensidad		Intensidad de la señal
	 * */
	public Dato(String newID, Par<Integer,Integer> posB, String newIDnino, Integer newIntensidad){
		ID=newID;
		posicionSensor=posB;
		IDnino=newIDnino;
		intensidad=newIntensidad;
		creacion = new Date().getTime();
	}
	
	
	
	/*Constructor para la clase Dato
	 * @param newID		String con los datos de la clase
	 * */
	public Dato(String mensaje){
		String[] arr = mensaje.split(" ");
		ID=arr[0];
		Integer posXB = Integer.parseInt(arr[1]);
		Integer posYB = Integer.parseInt(arr[2]);
		posicionSensor = new Par<Integer, Integer>(posXB,posYB);
		IDnino=arr[3];
		intensidad=Integer.parseInt(arr[4]);
		creacion = Long.parseLong(arr[5]);
	}
	
	
	
	
	
	/*Devuelve la ID del sensor emisor
	 * @return	ID del sensor emisor
	 * */
	public String getID(){
		return ID;
	}
	
	
	/*Devuelve la posición del sensor
	 * @return	Posición del sensor
	 * */
	public Par<Integer,Integer> getPosicionSensor(){
		return posicionSensor;
	}
	
	
	/*Devuelve intensidad de señal
	 * @return	Intensidad de señal
	 * */
	public Integer getIntensidad(){
		return intensidad;
	}
	
	/*Devuelve la ID del niño
	 * @return	ID del niño
	 * */
	public String getIDNino(){
		return IDnino;
	}
	
	/*Devuelve el momento en el que fue creado el dato
	 * @return	Hora del dato
	 * */
	public long getCreacion(){
		return creacion;
	}
	
	
	/*Método toString sobrecargado
	 * @return Cadena de caracteres con el valor de los atributos
	 * */
	@Override
    public String toString() {
		return ID+" "+posicionSensor.getPrimero()+" "+posicionSensor.getSegundo()+" "+IDnino+" "+intensidad+" "+creacion;			
    }

	
	
}