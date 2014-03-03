package comunicador;

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
		Dato prueba = new Dato(MAC,10,10,"75572325",10);
		Dato prueba2 = new Dato(MAC,10,10,"75572325",15);
		Dato prueba3 = new Dato(MAC,10,10,"75572325",20);
		
		//Niño b
		Dato prueba4 = new Dato(MAC,10,10,"24298911",10);
		Dato prueba5 = new Dato(MAC,10,10,"24298911",15);
		Dato prueba6 = new Dato(MAC,10,10,"24298911",20);
        int numero=0;
     	while(true){
	        dataWriter.write(prueba.toString(), InstanceHandle_t.HANDLE_NIL);
	        if(numero<5){
		        dataWriter.write(prueba4.toString(), InstanceHandle_t.HANDLE_NIL);
		        Thread.sleep(100);
		        dataWriter.write(prueba2.toString(), InstanceHandle_t.HANDLE_NIL);
		        dataWriter.write(prueba5.toString(), InstanceHandle_t.HANDLE_NIL);
		        Thread.sleep(1000);
		        dataWriter.write(prueba3.toString(), InstanceHandle_t.HANDLE_NIL);
		        dataWriter.write(prueba6.toString(), InstanceHandle_t.HANDLE_NIL);
		        Thread.sleep(100);
		        numero++;
	        }
     	}
    }
    
    
}
