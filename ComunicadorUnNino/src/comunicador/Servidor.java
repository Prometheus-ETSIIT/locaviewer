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
		static ArrayList<Dato> datos = new ArrayList<Dato>();


        public static final void main(String[] args) {
    		Parseador pars = new Parseador("posicionesCamaras.xml");
    		ArrayList<CamaraPos> posiciones  = new ArrayList<CamaraPos>();
    		//posiciones = pars.parse();
    		CamaraPos hola = new CamaraPos(3,0,"Camara 1");

    		posiciones.add(hola);
    		 hola = new CamaraPos(3,6,"Camara 2");
     		posiciones.add(hola);
    		 hola = new CamaraPos(0,3,"Camara 3");
     		posiciones.add(hola);
    		 hola = new CamaraPos(6,3,"Camara 4");
     		posiciones.add(hola);
    		
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
            StringDataWriter dataWriter =
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
            
        	Iterator<Entry<String, ArrayList<Dato>>>it;
    		ArrayList<Dato> datosActual;//Datos del niño actual
    		long min,actual, max;
    		Topic topicoNino;
    		
    		TriangulacionOctave triangulacion = new TriangulacionOctave(
                    "detectarcamara.m",
                    "detectarcamara",
                    posiciones,
                    6,
                    6,
                    false
            );
            while(true){
            	if(datos.size()>2){
					String camId = triangulacion.triangular(datos);
					datos.clear();//Se limpian los datos antiguos
					String toWrite = camId;
					System.out.println(toWrite+" Niño 1");
					//dataWriter.write(toWrite, InstanceHandle_t.HANDLE_NIL); Al padre correspondiente
				
            	}
            }
        }

        public void on_data_available(DataReader reader) {
            StringDataReader stringReader = (StringDataReader) reader;
            SampleInfo info = new SampleInfo();
            for (;;) {
                try {
                    String sample = stringReader.take_next_sample(info);
                    if (info.valid_data) {
                 
                    	Dato datoNuevo = new Dato(sample);
                    	datos.add(datoNuevo);
                    }
                } catch (RETCODE_NO_DATA noData) {
                    break;
                } catch (RETCODE_ERROR e) {
                    e.printStackTrace();
                }
            }
        }
}
