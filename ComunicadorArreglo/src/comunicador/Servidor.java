package comunicador;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.DomainParticipantFactory;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.infrastructure.RETCODE_ERROR;
import com.rti.dds.infrastructure.RETCODE_NO_DATA;
import com.rti.dds.infrastructure.StatusKind;
import com.rti.dds.publication.Publisher;
import com.rti.dds.subscription.DataReader;
import com.rti.dds.subscription.DataReaderAdapter;
import com.rti.dds.subscription.SampleInfo;
import com.rti.dds.subscription.Subscriber;
import com.rti.dds.topic.Topic;
import com.rti.dds.type.builtin.StringDataReader;
import com.rti.dds.type.builtin.StringDataWriter;
import com.rti.dds.type.builtin.StringTypeSupport;

public class Servidor extends DataReaderAdapter{
		static Map<String,ArrayList<Dato>> datosNinos = new HashMap<String,ArrayList<Dato>>(); 
		static ArrayList<CamaraPos> posiciones  = new ArrayList<CamaraPos>();
		static TriangulacionOctave triangulacion;
		static StringDataWriter dataWriter;



		
		
        public static final void main(String[] args) {
    		Parseador pars = new Parseador("posicionesCamaras.xml");
    		
    		//posiciones = pars.parse();
    		CamaraPos hola = new CamaraPos(3,0,"Camara 1");

    		posiciones.add(hola);
    		 hola = new CamaraPos(3,6,"Camara 2");
     		posiciones.add(hola);
    		 hola = new CamaraPos(0,3,"Camara 3");
     		posiciones.add(hola);
    		 hola = new CamaraPos(6,3,"Camara 4");
     		posiciones.add(hola);
    		
		  triangulacion = new TriangulacionOctave(
		             "detectarcamara.m",
		             "detectarcamara",
		             posiciones,
		             6,
		             6,
		             false
		        );
     		
    		System.out.println(posiciones.get(0).getPosX()+" "+posiciones.get(0).getPosY());
    		
            DomainParticipant participant = DomainParticipantFactory.get_instance().create_participant(
                    0, 
                    DomainParticipantFactory.PARTICIPANT_QOS_DEFAULT, 
                    null, 
                    StatusKind.STATUS_MASK_NONE);
            if (participant == null) {
                System.err.println("Unable to create domain participant");
                return;
            }

           
            Topic topic = participant.create_topic(
            		"1", 
                    StringTypeSupport.get_type_name(), 
                    DomainParticipant.TOPIC_QOS_DEFAULT, 
                    null, // listener
                    StatusKind.STATUS_MASK_NONE);
            if (topic == null) {
                System.err.println("Unable to create topic.");
                return;
            }
            try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
            Topic clasificadorPadres = participant.create_topic(
            		"clasificador", 
                    StringTypeSupport.get_type_name(), 
                    DomainParticipant.TOPIC_QOS_DEFAULT, 
                    null, // listener
                    StatusKind.STATUS_MASK_NONE);
            if (clasificadorPadres == null) {
                System.err.println("Unable to create topic.");
                return;
            }
            
            //Escritor
            dataWriter =
                (StringDataWriter) participant.create_datawriter(
                    topic, 
                    Publisher.DATAWRITER_QOS_DEFAULT,
                    null, // listener
                    StatusKind.STATUS_MASK_NONE);
            if (dataWriter == null) {
                System.err.println("Unable to create data writer\n");
                return;
            }

            //Lector
            StringDataReader dataReader =
                (StringDataReader) participant.create_datareader(
                    topic, 
                    Subscriber.DATAREADER_QOS_DEFAULT,
                    new Servidor(),         // Listener
                    StatusKind.DATA_AVAILABLE_STATUS);
            if (dataReader == null) {
                System.err.println("Unable to create DDS Data Reader");
                return;
            }
            
 
    		
    		
            while(true){//Para que el programa no finalice
            	try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
            }
        }
        

        public void on_data_available(DataReader reader) {
            StringDataReader stringReader = (StringDataReader) reader;
            SampleInfo info = new SampleInfo();
            for (;;) {

                    String sample = stringReader.take_next_sample(info);
                    if (info.valid_data) {
                    	Dato datoNuevo = new Dato(sample);
                    	if(datosNinos.containsKey(datoNuevo.getIDNino())){
                    		ArrayList<Dato> datos = datosNinos.get(datoNuevo.getIDNino());
                    		datos.add(datoNuevo);
                    		if(datos.size()>2){
                    			String camId = triangulacion.triangular(datos);
                    			datos.clear();
                    			
                    			System.out.println(camId+" "+datoNuevo.getIDNino());
            					dataWriter.write(camId, InstanceHandle_t.HANDLE_NIL); //Al padre correspondiente
                    		}
                    	}
                    	else{
                    		ArrayList<Dato> nuevo = new ArrayList<Dato>();
                    		nuevo.add(datoNuevo);
                    		datosNinos.put(datoNuevo.getIDNino(), nuevo);
                    	}
                    	
               
                    	}
                    
            }
        }
}
