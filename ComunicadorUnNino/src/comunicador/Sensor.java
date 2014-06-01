package comunicador;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.DomainParticipantFactory;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.infrastructure.StatusKind;
import com.rti.dds.publication.Publisher;
import com.rti.dds.subscription.DataReaderAdapter;
import com.rti.dds.topic.Topic;
import com.rti.dds.type.builtin.StringDataWriter;
import com.rti.dds.type.builtin.StringTypeSupport;

public class Sensor extends DataReaderAdapter{
	

	
	
    public static final void main(String[] args) throws InterruptedException {
    	
    	
    	/*Escritor en el Dominio 0, tópico 1*/
     	
        //Creación del participante en el dominio 0
        DomainParticipant participante = DomainParticipantFactory.get_instance().create_participant(
                0,
                DomainParticipantFactory.PARTICIPANT_QOS_DEFAULT, 
                null,
                StatusKind.STATUS_MASK_NONE);
        
        if (participante == null) {//Si falló la creación
            System.err.println("No se pudo crear un participante en el dominio");
            return;
        }
        
        //Creación del tópico 1 (escribir potencias)
        Topic topic = participante.create_topic(
                "1", 
                StringTypeSupport.get_type_name(), 
                DomainParticipant.TOPIC_QOS_DEFAULT, 
                null, // listener
                StatusKind.STATUS_MASK_NONE);
        
        if (topic == null) {//Si falló la creación
            System.err.println("No se pudo crear el topico");
            return;
        }

        
        //Creación del escritor
        StringDataWriter dataWriter =
            (StringDataWriter) participante.create_datawriter(
                topic, 
                Publisher.DATAWRITER_QOS_DEFAULT,
                null, // listener
                StatusKind.STATUS_MASK_NONE);
        
        if (dataWriter == null) {//Si algo falló
            System.err.println("No se puedo crear el escritor");
            return;
        }

        
        /*GENERANDO UN PAQUETE PARA ENVIAR*/
        
    	
    	//Obteniendo la MAC
    	byte[] direccionmac;
    	String MAC = null;
		try {
		    Enumeration<NetworkInterface> networks = NetworkInterface.getNetworkInterfaces();
		    while(networks.hasMoreElements()) {
		      NetworkInterface network = networks.nextElement();
		      direccionmac = network.getHardwareAddress();

		      if(direccionmac != null) {
		        StringBuilder sb = new StringBuilder();
		        for (int i = 0; i < direccionmac.length; i++) {
		          sb.append(String.format("%02X%s", direccionmac[i], (i < direccionmac.length - 1) ? "-" : ""));
		        }
		        MAC=sb.toString();
		      }
		    }
		  } catch (SocketException e){
		    e.printStackTrace();
		  }
		
		//Niño a
	/*	Dato prueba = new Dato(MAC,0,0,"75572325",-89);
		Dato prueba2 = new Dato(MAC,6,0,"75572325",-82);
		Dato prueba3 = new Dato(MAC,0,6,"75572325",-85);
		*/
		
		//Lectura
		/*try {
		//	Runtime.getRuntime().exec("sudo python sensor.py 1 4554");
		} catch (IOException e) {
			e.printStackTrace();
		}*/
		
	
		DatagramSocket socketServidor;

		 try {
			 socketServidor = new DatagramSocket(5445);
     	while(true){
     			 byte [] bufer = new byte [256];
 				 DatagramPacket paquete = new DatagramPacket(bufer,bufer.length);
 				 socketServidor.receive(paquete);
 				 String peticion = new String (paquete.getData());
 				 String [] recibido = peticion.split("\\s+");
 				 int numEntero = Integer.parseInt(recibido[1].substring(0, 3));
 				 
 				 Dato prueba = new Dato (recibido[0],0,0,"75572325",numEntero);
 	     		 dataWriter.write(prueba.toString(), InstanceHandle_t.HANDLE_NIL);
 	     		 
 				 prueba = new Dato (recibido[0],6,0,"75572325",numEntero);
 	     		 dataWriter.write(prueba.toString(), InstanceHandle_t.HANDLE_NIL);
 	     		 
 				 prueba = new Dato (recibido[0],0,6,"75572325",numEntero);
 	     		 dataWriter.write(prueba.toString(), InstanceHandle_t.HANDLE_NIL);
 	     		 
     		}
     		} catch (SocketException e1) {
     			e1.printStackTrace();
     		} catch (IOException e) {
     			e.printStackTrace();
     		}
     	
    }
    
    
}
