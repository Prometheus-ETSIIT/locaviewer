package comunicador;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;


import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.DomainParticipantFactory;
import com.rti.dds.infrastructure.RETCODE_ERROR;
import com.rti.dds.infrastructure.RETCODE_NO_DATA;
import com.rti.dds.infrastructure.StatusKind;
import com.rti.dds.subscription.DataReader;
import com.rti.dds.subscription.DataReaderAdapter;
import com.rti.dds.subscription.SampleInfo;
import com.rti.dds.subscription.Subscriber;
import com.rti.dds.topic.Topic;
import com.rti.dds.type.builtin.StringDataReader;
import com.rti.dds.type.builtin.StringTypeSupport;

public class Servidor extends DataReaderAdapter{

		static Map<String,ArrayList<Dato>> datosNinos = new HashMap<String, ArrayList<Dato>>(); ;

        public static final void main(String[] args) {
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
            
        	Iterator<Entry<String, ArrayList<Dato>>> it = datosNinos.entrySet().iterator();//Iterar en el mapa
    		ArrayList<Dato> datosActual;//Datos del niño actual
    		long min,actual;
    		
            while(true){
				while(it.hasNext()){//Se recorren las claves del mapa
					
					Map.Entry entrada = (Map.Entry)it.next();
					datosActual=(ArrayList<Dato>) entrada.getValue();
					
					
					min=0;
					
					for(int i=0;i<datosActual.size();i++){
						if(datosActual.get(i).getCreacion()>min){//Tomamos de la última medición
							min=datosActual.get(i).getCreacion();
						}
					}
					
					actual = new Date().getTime(); 
					
					if((actual-min)/1000>10){//Si han pasado más de 10 segundos
						datosNinos.remove(entrada.getKey());//Desechamos los datos de ese niño
						it = datosNinos.entrySet().iterator();//Hay que regenerar el iterador
					}
					else if(datosActual.size()>3){//Si hay más de 3 datos
						ArrayList<Dato> datosNino =  (ArrayList<Dato>) entrada.getValue();
						//PROCESAR
					}
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
                    	System.out.println(sample);
                    	Dato datoNuevo = new Dato(sample);
                    	ArrayList<Dato> datos = datosNinos.get(datoNuevo.getIDNino());
                    	if(datos==null){//No hay datos del niño
                    		datos = new ArrayList<Dato>();
                    		datos.add(datoNuevo);
                    	}
                    	else{
                    		for(int i=0;i<datos.size();i++){
                    			if(datos.get(i).getID()==datoNuevo.getID()){
                    				datos.remove(i);
                    				datos.add(datoNuevo);
                    				break;
                    			}
                    		}
                    	}
                    }
                } catch (RETCODE_NO_DATA noData) {
                    break;
                } catch (RETCODE_ERROR e) {
                    e.printStackTrace();
                }
            }
        }
}
