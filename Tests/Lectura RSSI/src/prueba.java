import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;


public class prueba {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			Runtime.getRuntime().exec("sudo python sensor.py 1 4554");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	
	 try {
		DatagramSocket socketServidor = new DatagramSocket(4554);
		 byte [] bufer = new byte [256];
		 while(true){
		 DatagramPacket paquete = new DatagramPacket(bufer,bufer.length);
		 socketServidor.receive(paquete);
		 String peticion = new String (paquete.getData());
		 System.out.println(peticion);
		 }
	} catch (SocketException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	 
	

	 
	 

	}

}
