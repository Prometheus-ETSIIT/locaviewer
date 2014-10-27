/*
 * Copyright (C) 2014 Prometheus
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package es.prometheus.dds;

import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.DomainParticipantFactory;
import com.rti.dds.domain.DomainParticipantFactoryQos;
import com.rti.dds.domain.DomainParticipantQos;
import com.rti.dds.infrastructure.ConditionSeq;
import com.rti.dds.infrastructure.Duration_t;
import com.rti.dds.infrastructure.PropertyQosPolicyHelper;
import com.rti.dds.infrastructure.RETCODE_NO_DATA;
import com.rti.dds.infrastructure.RETCODE_TIMEOUT;
import com.rti.dds.infrastructure.ResourceLimitsQosPolicy;
import com.rti.dds.infrastructure.WaitSet;
import com.rti.dds.publication.builtin.PublicationBuiltinTopicData;
import com.rti.dds.publication.builtin.PublicationBuiltinTopicDataDataReader;
import com.rti.dds.publication.builtin.PublicationBuiltinTopicDataSeq;
import com.rti.dds.publication.builtin.PublicationBuiltinTopicDataTypeSupport;
import com.rti.dds.subscription.DataReader;
import com.rti.dds.subscription.InstanceStateKind;
import com.rti.dds.subscription.SampleInfo;
import com.rti.dds.subscription.SampleInfoSeq;
import com.rti.dds.subscription.SampleStateKind;
import com.rti.dds.subscription.Subscriber;
import com.rti.dds.subscription.ViewStateKind;
import com.rti.dds.subscription.builtin.SubscriptionBuiltinTopicData;
import com.rti.dds.subscription.builtin.SubscriptionBuiltinTopicDataDataReader;
import com.rti.dds.subscription.builtin.SubscriptionBuiltinTopicDataSeq;
import com.rti.dds.subscription.builtin.SubscriptionBuiltinTopicDataTypeSupport;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Participante del dominio DDS.
 */
public class Participante {
    private final static Map<String, Participante> Instancias = new HashMap<>();
    private final static Map<Participante, Integer> CountInstancias = new HashMap<>();
    
    private DomainParticipant participante;
    private final DataReader discovReader;
    private final DiscoveryReaders discovReaderListener;
    private final Thread discovReaderThread;
    private final DataReader discovWriter;
    private final DiscoveryWriters discovWriterListener;
    private final Thread discovWriterThread;
    
    /**
     * Crea una nueva instancia de participante en tópico.
     * 
     * @param partName Nombre del participante en el XML
     *  (BibliotecaParticipantes::NombreParticipante).
     */
    private Participante(final String name) {
        // Buscamos si ya está creado el participante para recuperarlo.
        // Aunque esto tiene poco sentido en este entorno, puede que DDS
        // por algún mecanismo lo haya creado previamente (según la conf. de XML).
        String simpleName = name.substring(name.indexOf("::") + 2);
        this.participante = DomainParticipantFactory.get_instance()
                .lookup_participant_by_name(simpleName);
        
        // No ha sido creado previamente -> Crea un participante de dominio.
        if (this.participante == null) {        
            // Lo creamos deshabilitado para que a los listener lleguen todos los datos.
            DomainParticipantFactoryQos qos = new DomainParticipantFactoryQos();
            DomainParticipantFactory.get_instance().get_qos(qos);
            qos.entity_factory.autoenable_created_entities = false;
            DomainParticipantFactory.get_instance().set_qos(qos);
                        
            // Creamos el participante
            this.participante = DomainParticipantFactory.get_instance()
                        .create_participant_from_config(name);

            // No ha habido forma de crearlo :(
            if (this.participante == null) {
                System.err.println("[DDStheus::Participante] No se pudo crear.");
                System.exit(1);
            }

            // Modicamos el QoS por defecto para aumentar el tamaño de USER_DATA
            // y configurar RTI WAN Server.
            DomainParticipantQos partQos = new DomainParticipantQos();
            this.participante.get_qos(partQos);
            
            // Aumentamos el tamaño para USER_DATA
            partQos.resource_limits.participant_user_data_max_length = 256;
            partQos.resource_limits.reader_user_data_max_length = 256;
            partQos.resource_limits.writer_user_data_max_length = 256;
            
            // Configura RTI WAN Server   
            String activate_wan = System.getenv("ACTIVATE_RTI_WAN_SERVER");
            System.out.println("var: " + activate_wan);
            if (activate_wan != null && activate_wan.equals("true"))
                ConfiguraRtiWanServer(partQos);
            
            this.participante.set_qos(partQos);
            
            // Volvemos a habilitarlo para dejarlo en su valor por defecto.
            qos.entity_factory.autoenable_created_entities = true;
            DomainParticipantFactory.get_instance().set_qos(qos);
        }
        
        // Establece los discovery listener
        Subscriber spSubs = this.participante.get_builtin_subscriber();
        
        this.discovWriter = spSubs.lookup_datareader(PublicationBuiltinTopicDataTypeSupport.PUBLICATION_TOPIC_NAME);
        this.discovWriterListener = new DiscoveryWriters(this.discovWriter);
        this.discovWriterThread = new Thread(this.discovWriterListener);
        this.discovWriterThread.start();
        
        this.discovReader = spSubs.lookup_datareader(SubscriptionBuiltinTopicDataTypeSupport.SUBSCRIPTION_TOPIC_NAME);
        this.discovReaderListener = new DiscoveryReaders(this.discovReader);
        this.discovReaderThread = new Thread(this.discovReaderListener);
        this.discovReaderThread.start();
        
        // Finalmente ya lo podemos habilitar
        this.participante.enable();
    }
    
    /**
     * Configura RTI WAN Server sobre el QoS de un participante.
     * 
     * @param qos QoS para configurar.
     */
    private static void ConfiguraRtiWanServer(DomainParticipantQos qos) {
        String WAN_SERVER = "37.252.96.104";
        String WAN_PORT = "5555";
        String WAN_ID = UUID.randomUUID().toString();
        String archName = System.getProperty("os.name").toLowerCase();
        String WAN_LIB = archName.contains("win") ? 
                "nddstransportwan.dll" : "libnddstransportwan.so";

        /* Set up property QoS to load plugin */
        PropertyQosPolicyHelper.add_property(qos.property, 
            "dds.transport.load_plugins", "dds.transport.wan_plugin.wan", false);

        /* library */
        PropertyQosPolicyHelper.add_property(qos.property, 
            "dds.transport.wan_plugin.wan.library",
            WAN_LIB, false);

        /* create function */
        PropertyQosPolicyHelper.add_property(qos.property, 
            "dds.transport.wan_plugin.wan.create_function", "NDDS_Transport_WAN_create", false);

        /* plugin properties */
        PropertyQosPolicyHelper.add_property(qos.property, 
            "dds.transport.wan_plugin.wan.server", WAN_SERVER, false);

        PropertyQosPolicyHelper.add_property(qos.property,
                "dds.transport.wan_plugin.wan.server_port", WAN_PORT, false);
        
        PropertyQosPolicyHelper.add_property(qos.property, 
            "dds.transport.wan_plugin.wan.transport_instance_id", WAN_ID, false);
    }
    
    /**
     * Devuelve la instancia asociada a ese nombre.
     * 
     * @param name Nombre del participante.
     * @return Instancia de participante.
     */
    public static Participante GetInstance(final String name) {
        if (!Instancias.containsKey(name))
            Instancias.put(name, new Participante(name));
          
        // Obtiene la instancia
        Participante p = Instancias.get(name);
        
        // Añade un uso
        int num = CountInstancias.containsKey(p) ? CountInstancias.get(p) : 0;
        CountInstancias.put(p, num + 1);
        
        return p;
    }
    
    /**
     * Libera recursos del sistema.
     */
    public void dispose() {
        int num = CountInstancias.get(this);
        CountInstancias.put(this, --num);
        
        // Si ya nadie lo está usando, lo eliminamos
        // TODO: Esto está fallando
        if (num == 0) {
            // Paramos de recibir datos
            try {
                this.discovWriterListener.terminar();
                this.discovWriterThread.join(5000);
                
                this.discovReaderListener.terminar();
                this.discovReaderThread.join(5000);
            } catch (InterruptedException e) { 
                System.err.println("TimeOver!");
            }
            
            this.discovReader.delete_contained_entities();
            this.discovWriter.delete_contained_entities();
            this.participante.get_builtin_subscriber().delete_datareader(discovReader);
            this.participante.get_builtin_subscriber().delete_datareader(discovWriter);
            this.participante.get_builtin_subscriber().delete_contained_entities();
            this.participante.delete_contained_entities();
            DomainParticipantFactory.get_instance().delete_participant(this.participante);
        }
    }
    
    /**
     * Obtiene el objeto participante de DDS.
     * 
     * @return Participante DDS.
     */
    public DomainParticipant getParticipante() {
        return this.participante;
    }
    
    /**
     * Obtiene los lectores en acción.
     * 
     * @return Lectores descubiertas.
     */
    public DiscoveryData[] getDiscoveryReaderData() {
        return this.discovReaderListener.getData();
    }
    
    /**
     * Añade un listener de cambio al descubridor de lectores.
     * 
     * @param l Listener de cambio en descubrimiento.
     */
    public void addDiscoveryReaderListener(DiscoveryListener l) {
        this.discovReaderListener.addListener(l);
    }
    
    /**
     * Elimina un listener de cambio al descubridor de lectores.
     * 
     * @param l Listener de cambio en descubrimiento.
     */
    public void removeDiscoveryReaderListener(DiscoveryListener l) {
        this.discovReaderListener.removeListener(l);
    }
    
    /**
     * Obtiene los escritores en acción.
     * 
     * @return Escritores descubiertos.
     */
    public DiscoveryData[] getDiscoveryWriterData() {
        return this.discovWriterListener.getData();
    }
    
    /**
     * Añade un listener de cambio al descubridor de escritores.
     * 
     * @param l Listener de cambio en descubrimiento.
     */
    public void addDiscoveryWriterListener(DiscoveryListener l) {
        this.discovWriterListener.addListener(l);
    }
    
    /**
     * Elimina un listener de cambio al descubridor de escritores.
     * 
     * @param l Listener de cambio en descubrimiento.
     */
    public void removeDiscoveryWriterListener(DiscoveryListener l) {
        this.discovWriterListener.removeListener(l);
    }
    
    /**
     * Listener de descubridor genérico de entidades T.
     * 
     * @param <T> Entidad a descubrir.
     */
    private static abstract class DiscoveryAdapter<T> implements Runnable {
        private static final int MAX_TIME_SEC  = 3;
        private static final int MAX_TIME_NANO = 0;
       
        protected final DataReader reader;
        private final WaitSet waitset;
        private final Duration_t duracion;
        private boolean terminar;
        
        protected final List<DiscoveryListener> listeners = new ArrayList<>();
        protected final List<DiscoveryData> data = new ArrayList<>();
        protected final List<DiscoveryChange> changes = new ArrayList<>();
        
        protected DiscoveryAdapter(final DataReader reader) {
            this.terminar = false;
            this.reader   = reader;
            this.duracion = new Duration_t(MAX_TIME_SEC, MAX_TIME_NANO);
            this.waitset  = new WaitSet();
            this.waitset.attach_condition(reader.get_statuscondition());
        }
        
        @Override
        public void run() {
            while (!this.terminar) {
                // Esperamos a obtener la siguiente muestra que cumpla la condición
                ConditionSeq activadas = new ConditionSeq();
                try { this.waitset.wait(activadas, duracion); }
                catch (RETCODE_TIMEOUT e) { continue; }
                
                // Procesamos los datos recibidos.
                this.processData();
            }
        }
        
        /**
         * Termina la ejecución en la próxima iteración.
         */
        public void terminar() {
            this.terminar = true;
        }
        
        /**
         * Procesa los datos obtenidos de DDS.
         */
        protected abstract void processData();
        
        /**
         * Obtiene las entidades en acción.
         * 
         * @return Entidades en acción.
         */
        public DiscoveryData[] getData() {
            return this.data.toArray(new DiscoveryData[0]);
        }
        
        /**
         * Añade un listener de cambio en descubrimiento.
         * 
         * @param l Listener de cambio en descubrimiento.
         */
        public synchronized void addListener(DiscoveryListener l) {            
            if (!this.listeners.contains(l))
                this.listeners.add(l);
        }
        
        /**
         * Elimina un listener de cambio en descubrimiento.
         * 
         * @param l Listener de cambio en descubrimiento.
         */
        public synchronized void removeListener(DiscoveryListener l) {
            if (this.listeners.contains(l))
                this.listeners.remove(l);
        }
        
        /**
         * Notifica a todos los listener un cambio.
         */
        protected void notifyListeners() {
            DiscoveryChange[] ch = this.changes.toArray(new DiscoveryChange[0]);
            for (DiscoveryListener listener : this.listeners)
                listener.onChange(ch);
            this.changes.clear();
        }
        
        /**
         * Añade una entidad a la lista.
         * 
         * @param datum Datos de la entidad.
         * @param info Información de la muestra de la entidad.
         */
        protected void addElement(T datum, SampleInfo info) {            
            DiscoveryData dd = this.convertData(datum, info);
            if (dd.getTopicName().startsWith("rti/"))   // Fuera estadísticas de RTI
                return;
            
            // Comprueba si ya existe
            int idx = -1;
            for (int i = 0; i < this.data.size() && idx == -1; i++)
                if (data.get(i).getHandle().equals(dd.getHandle()))
                    idx = i;
            
            // Si existe lo 
            if (idx != -1) {
                this.data.set(idx, dd);
                this.changes.add(new DiscoveryChange(dd, DiscoveryChangeStatus.CAMBIADO));
            } else {            
                this.data.add(dd);
                this.changes.add(new DiscoveryChange(dd, DiscoveryChangeStatus.ANADIDO));
            }
        }
        
        /**
         * Elimina una entidad de la lista.
         * 
         * @param info Información de la muestra de la entidad.
         */
        protected void removeElement(SampleInfo info) {
            DiscoveryData toRemove = null;
            for (DiscoveryData dd : this.data) {
                if (dd.getHandle().equals(info.instance_handle))
                    toRemove = dd;
            }
            
            if (toRemove == null)
                return;
            
            this.data.remove(toRemove);
            this.changes.add(new DiscoveryChange(toRemove, DiscoveryChangeStatus.ELIMINADO));
        }
        
        /**
         * Convierte de muestra e información a DiscoveryData.
         * Es dependiente del tipo de entidad.
         * 
         * @param datum Muestra.
         * @param info Información de la muestra.
         * @return DiscoveryData con valores.
         */
        protected abstract DiscoveryData convertData(T datum, SampleInfo info);
    }
    
    /**
     * Listener de descubridor de lectores.
     */
    private static class DiscoveryReaders extends DiscoveryAdapter<SubscriptionBuiltinTopicData> {
        /**
         * Crea una nueva instancia a partir del lector dado.
         * 
         * @param reader Lector a usar.
         */
        public DiscoveryReaders(final DataReader reader) {
            super(reader);
        }
        
        @Override
        protected DiscoveryData convertData(SubscriptionBuiltinTopicData datum,
                SampleInfo info) {
           return new DiscoveryData(
                   datum.topic_name,
                   datum.user_data.value,
                   datum.content_filter_property.expression_parameters,
                   info.instance_handle);
        }
        
        @Override
        public void processData() {
            SampleInfoSeq infoSeq = new SampleInfoSeq();
            SubscriptionBuiltinTopicDataSeq sampleSeq = 
                    new SubscriptionBuiltinTopicDataSeq();
            
            SubscriptionBuiltinTopicDataDataReader builtinReader = 
                    (SubscriptionBuiltinTopicDataDataReader)this.reader;
            
            try {
                // Lee las muestras
                builtinReader.take(
                        sampleSeq,
                        infoSeq,
                        ResourceLimitsQosPolicy.LENGTH_UNLIMITED,
                        SampleStateKind.ANY_SAMPLE_STATE,
                        ViewStateKind.ANY_VIEW_STATE,
                        InstanceStateKind.ANY_INSTANCE_STATE
                );

                // Procesa cada muestra
                for (int i = 0; i < sampleSeq.size(); i++) {
                    SampleInfo info = (SampleInfo)infoSeq.get(i);
                    SubscriptionBuiltinTopicData sample = 
                            (SubscriptionBuiltinTopicData)sampleSeq.get(i);

                    if (!info.valid_data)
                        this.removeElement(info);
                    else
                        this.addElement(sample, info);
                }
            } catch (RETCODE_NO_DATA e) {
                // No hace nada, al filtrar datos pues se da la cosa de que no haya
            } finally {
                // Es para liberar recursos del sistema.
                builtinReader.return_loan(sampleSeq, infoSeq);
                this.notifyListeners();
            }
        }
    }
    
    /**
     * Listener de descubridor de escritores.
     */
    private static class DiscoveryWriters extends DiscoveryAdapter<PublicationBuiltinTopicData> {
        /**
         * Crea una nueva instancia a partir del lector dado.
         * 
         * @param reader Lector a usar.
         */
        public DiscoveryWriters(final DataReader reader) {
            super(reader);
        }
        
        @Override
        protected DiscoveryData convertData(PublicationBuiltinTopicData datum,
                SampleInfo info) {
           return new DiscoveryData(
                   datum.topic_name,
                   datum.user_data.value,
                   null,
                   info.instance_handle);
        }
        
        @Override
        public void processData() {
            SampleInfoSeq infoSeq = new SampleInfoSeq();
            PublicationBuiltinTopicDataSeq sampleSeq = 
                    new PublicationBuiltinTopicDataSeq();
            
            PublicationBuiltinTopicDataDataReader builtinReader = 
                    (PublicationBuiltinTopicDataDataReader)reader;
            
            try {
                // Lee las muestras
                builtinReader.take(                 
                        sampleSeq,
                        infoSeq,
                        ResourceLimitsQosPolicy.LENGTH_UNLIMITED,
                        SampleStateKind.ANY_SAMPLE_STATE,
                        ViewStateKind.ANY_VIEW_STATE,
                        InstanceStateKind.ANY_INSTANCE_STATE
                );

                // Procesa cada muestra
                for (int i = 0; i < sampleSeq.size(); i++) {
                    SampleInfo info = (SampleInfo)infoSeq.get(i);
                    PublicationBuiltinTopicData sample = 
                            (PublicationBuiltinTopicData)sampleSeq.get(i);

                    if (!info.valid_data)
                        this.removeElement(info);
                    else
                        this.addElement(sample, info);
                }
            } catch (RETCODE_NO_DATA e) {
                // No hace nada, al filtrar datos pues se da la cosa de que no haya
            } finally {
                // Es para liberar recursos del sistema.
                builtinReader.return_loan(sampleSeq, infoSeq);
                this.notifyListeners();
            }
        }
    }
}
