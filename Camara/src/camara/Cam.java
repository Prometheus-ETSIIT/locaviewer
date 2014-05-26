package camara;

import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.DomainParticipantFactory;
import com.rti.dds.infrastructure.StatusKind;
import com.rti.dds.publication.Publisher;
import com.rti.dds.topic.Topic;
import com.rti.dds.type.builtin.StringDataWriter;
import com.rti.dds.type.builtin.StringTypeSupport;

public class Cam {
	  public static final void main(String[] args) {
		  	if(args.length==0){
		  		System.err.println("Inserte ID de la cámara");
		  	}
		  
	        //Dominio 1
	        DomainParticipant participant = DomainParticipantFactory.get_instance().create_participant(
	                1, // Domain ID = 0
	                DomainParticipantFactory.PARTICIPANT_QOS_DEFAULT, 
	                null, // listener
	                StatusKind.STATUS_MASK_NONE);
	        if (participant == null) {
	            System.err.println("No se pudo crear");
	            return;
	        }

	       //Creación del tópico
	        Topic topic = participant.create_topic(
	                args[0], 
	                StringTypeSupport.get_type_name(), 
	                DomainParticipant.TOPIC_QOS_DEFAULT, 
	                null, // listener
	                StatusKind.STATUS_MASK_NONE);
	        if (topic == null) {
	            System.err.println("Unable to create topic.");
	            return;
	        }


	        StringDataWriter dataWriter =
	            (StringDataWriter) participant.create_datawriter(
	                topic, 
	                Publisher.DATAWRITER_QOS_DEFAULT,
	                null, // listener
	                StatusKind.STATUS_MASK_NONE);
	        if (dataWriter == null) {
	            System.err.println("No se pudo crear el escritor");
	            return;
	        }

	        
            while (true) {
             //Lectura y envío de vídeo

            }
	    }
}
