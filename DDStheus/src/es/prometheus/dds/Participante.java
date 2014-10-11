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
import com.rti.dds.infrastructure.ResourceLimitsQosPolicy;
import com.rti.dds.infrastructure.StatusKind;
import com.rti.dds.publication.builtin.PublicationBuiltinTopicData;
import com.rti.dds.publication.builtin.PublicationBuiltinTopicDataDataReader;
import com.rti.dds.publication.builtin.PublicationBuiltinTopicDataSeq;
import com.rti.dds.publication.builtin.PublicationBuiltinTopicDataTypeSupport;
import com.rti.dds.subscription.DataReader;
import com.rti.dds.subscription.DataReaderAdapter;
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

/**
 * Participante del dominio DDS.
 */
public class Participante {
    private final static Map<String, Participante> Instancias = new HashMap<>();
    private final static Map<DomainParticipant, Integer> CountInstancias = new HashMap<>();
    
    private DomainParticipant participante;
    private DataReader discovReader;
    private DiscoveryReaders discovReaderListener;
    private DataReader discovWriter;
    private DiscoveryWriters discovWriterListener;
    
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
        if (this.participante != null)
            return;
        
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
        
        // Volvemos a habilitarlo para dejarlo en su valor por defecto.
        qos.entity_factory.autoenable_created_entities = true;
        DomainParticipantFactory.get_instance().set_qos(qos);
        
        // Establece los discovery listener
        Subscriber spSubs = this.participante.get_builtin_subscriber();
        
        this.discovWriterListener = new DiscoveryWriters();
        this.discovReader = spSubs.lookup_datareader(PublicationBuiltinTopicDataTypeSupport.PUBLICATION_TOPIC_NAME);
        this.discovReader.set_listener(this.discovWriterListener, StatusKind.DATA_AVAILABLE_STATUS);
        
        this.discovReaderListener = new DiscoveryReaders();
        this.discovWriter = spSubs.lookup_datareader(SubscriptionBuiltinTopicDataTypeSupport.SUBSCRIPTION_TOPIC_NAME);
        this.discovWriter.set_listener(this.discovReaderListener, StatusKind.DATA_AVAILABLE_STATUS);
        
        // Finalmente ya lo podemos habilitar
        this.participante.enable();
        
        // Añade la instancia a la cuenta
        int num = CountInstancias.containsKey(participante) ? CountInstancias.get(participante) : 0;
        CountInstancias.put(participante, num + 1);
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
        
        return Instancias.get(name);
    }
    
    /**
     * Libera recursos del sistema.
     */
    public void dispose() {
        int num = CountInstancias.get(participante);
        CountInstancias.put(participante, --num);
        
        // Si ya nadie lo está usando, lo eliminamos
        // TODO: Esto está fallando
        if (num == 0) {
            discovReader.delete_contained_entities();
            discovWriter.delete_contained_entities();
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
    private static abstract class DiscoveryAdapter<T> extends DataReaderAdapter {
        protected final List<DiscoveryListener> listeners = new ArrayList<>();
        protected final List<DiscoveryData> data = new ArrayList<>();
        protected final List<DiscoveryChange> changes = new ArrayList<>();
        
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
        public void addListener(DiscoveryListener l) {
            System.out.println(l == null);
            if (!this.listeners.contains(l))
                this.listeners.add(l);
        }
        
        /**
         * Elimina un listener de cambio en descubrimiento.
         * 
         * @param l Listener de cambio en descubrimiento.
         */
        public void removeListener(DiscoveryListener l) {
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
            this.data.add(dd);
            this.changes.add(new DiscoveryChange(dd, DiscoveryChangeStatus.ANADIDO));
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
        @Override
        protected DiscoveryData convertData(SubscriptionBuiltinTopicData datum,
                SampleInfo info) {
           return new DiscoveryData(
                   datum.topic_name,
                   datum.user_data.value,
                   info.instance_handle);
        }
        
        @Override
        public void on_data_available(DataReader reader) {
            SampleInfoSeq infoSeq = new SampleInfoSeq();
            SubscriptionBuiltinTopicDataSeq sampleSeq = 
                    new SubscriptionBuiltinTopicDataSeq();
            
            SubscriptionBuiltinTopicDataDataReader builtinReader = 
                    (SubscriptionBuiltinTopicDataDataReader)reader;
            
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
            
            builtinReader.return_loan(sampleSeq, infoSeq);
            this.notifyListeners();
        }
    }
    
    /**
     * Listener de descubridor de escritores.
     */
    private static class DiscoveryWriters extends DiscoveryAdapter<PublicationBuiltinTopicData> {
        @Override
        protected DiscoveryData convertData(PublicationBuiltinTopicData datum,
                SampleInfo info) {
           return new DiscoveryData(
                   datum.topic_name,
                   datum.user_data.value,
                   info.instance_handle);
        }
        
        @Override
        public void on_data_available(DataReader reader) {
            SampleInfoSeq infoSeq = new SampleInfoSeq();
            PublicationBuiltinTopicDataSeq sampleSeq = 
                    new PublicationBuiltinTopicDataSeq();
            
            PublicationBuiltinTopicDataDataReader builtinReader = 
                    (PublicationBuiltinTopicDataDataReader)reader;
            
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
            
            builtinReader.return_loan(sampleSeq, infoSeq);
            this.notifyListeners();
        }
    }
}
