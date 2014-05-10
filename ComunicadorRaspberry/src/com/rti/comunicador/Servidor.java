package com.rti.comunicador;

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
import com.rti.dds.subscription.DataReader;
import com.rti.dds.subscription.DataReaderAdapter;
import com.rti.dds.subscription.SampleInfo;
import com.rti.dds.subscription.Subscriber;
import com.rti.dds.topic.Topic;
import com.rti.dds.type.builtin.StringDataReader;
import com.rti.dds.type.builtin.StringDataWriter;
import com.rti.dds.type.builtin.StringTypeSupport;



class Servidor extends DataReaderAdapter implements Runnable {
	private int tipo;
	private static HashMap<String,ArrayList<Dato>> datos = new HashMap<String, ArrayList<Dato> >();;
	private String ninoProcesar;

	private static DomainParticipant participante = DomainParticipantFactory.get_instance().create_participant(
            0,
            DomainParticipantFactory.PARTICIPANT_QOS_DEFAULT, 
            null, // listener
            StatusKind.STATUS_MASK_NONE);
	
	
	
	private static Topic topicoLeer;
	private static StringDataReader lector;
	private static StringDataWriter escritor;
	
	/*Constructor
	 * @param tip		Define el tipo de la hebra
	 * @param nino		Niño a procesar
	 * */
	public Servidor(int tip, String nino){
		tipo=tip;
		ninoProcesar=nino;
	}
	
	
	
	@Override
	public void run() {
		switch(tipo){
			case 0://Lee los datos
				leerDatos();
				break;
			case 1://Lee el mapa
				leer();
				break;
			
			case 2://Procesa los datos
				procesar();
				break;
		}	
	}
	
	
	
	private void leerDatos(){
		
		//Al crear aquí el tópico de lectura, se obliga a que se ejecute desde esta hebra el método on_data_available
	    topicoLeer = participante.create_topic(
                "1", 
                StringTypeSupport.get_type_name(), 
                DomainParticipant.TOPIC_QOS_DEFAULT, 
                null, // listener
                StatusKind.STATUS_MASK_NONE);
		
	     lector =
		            (StringDataReader) participante.create_datareader(
		                topicoLeer, 
		                Subscriber.DATAREADER_QOS_DEFAULT,
		                new HelloSubscriber(),         // Listener
		                StatusKind.DATA_AVAILABLE_STATUS);
	}
	
	/*Método main. Llama a iniciar dos hebras
	 * 
	 * */
    public static final void main(String[] args){
		Runnable lectora = new Servidor(0,null);//Lectora de datos
		Runnable lectoraMapa = new Servidor(1,null);//Lectora de datos conseguidos
		new Thread(lectora).start();
	//	new Thread(lectoraMapa).start();
    }
	
    
    /*Método llamado por DDS cuando hay novedades
     * 
     * */
	public void on_data_available(DataReader reader) {
        StringDataReader stringReader = (StringDataReader) reader;
        SampleInfo info = new SampleInfo();
        ArrayList<Dato> datosNino;
        
        for (;;) {
            try {
                String sample = stringReader.take_next_sample(info);
                if (info.valid_data) {
                	Dato nuevoDato = new Dato(sample);
                	datosNino=datos.get(nuevoDato.getIDNino());
                	
                	if(datosNino==null){//No hay datos para esa clave
                		datosNino = new ArrayList<Dato>();
                		datosNino.add(nuevoDato);
                		datos.put(nuevoDato.getIDNino(), datosNino);
                	}
                	else{
                		boolean anadido=false;
                		for(int i=0;i<datosNino.size();i++){
                			if(datosNino.get(i).getID().equals(nuevoDato.getID())){//Se busca de la misma MAC
                				datosNino.remove(i);
                				datosNino.add(nuevoDato);
                				anadido=true;
                			}
                		}
                		if(!anadido){
                			datosNino.add(nuevoDato);
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
	
	
	
	
	
	/*Usado por la hebra que procesa. Procesa los datos y los publica*/
	private void procesar(){
		ArrayList<Dato> datosNino =  datos.get(ninoProcesar);//Se toman los datos y se eliminan del mapa
		datos.remove(ninoProcesar);
		
		
		try {
			Thread.sleep(1000);//SIMULACION DE PROCESADO
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	
	
	
	
	/*Usado por la hebra lectora*/
	@SuppressWarnings("unchecked")
	private void leer(){
		Iterator<Entry<String, ArrayList<Dato>>> it = datos.entrySet().iterator();//Iterar en el mapa
		ArrayList<Dato> datosActual;//Datos del niño actual
		long min,actual;
		
		
		while(true){
			while(it.hasNext()){//Se recorren las claves del mapa
				@SuppressWarnings("rawtypes")
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
					datos.remove(entrada.getKey());//Desechamos los datos de ese niño
					it = datos.entrySet().iterator();//Hay que regenerar el iterador
				}
				else if(datosActual.size()>3){//Si hay más de 3 datos
					Runnable procesadora = new Servidor(2,(String)entrada.getKey());
					new Thread(procesadora).start();//Procesar
				}
			}
		}
	}

}
