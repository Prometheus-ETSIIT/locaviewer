/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package control;

import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.DomainParticipantFactory;
import com.rti.dds.domain.DomainParticipantQos;
import com.rti.dds.infrastructure.PropertyQosPolicyHelper;
import com.rti.dds.infrastructure.StatusKind;
import com.rti.dds.topic.Topic;
import com.rti.dds.type.builtin.BytesTypeSupport;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class Dominio {    
    private final DomainParticipant participante;
    private List<Topic> topicos;
    
    public Dominio(final int id, final String[] nombreTopicos) {
        // Crea un participante de dominio
        this.participante = DomainParticipantFactory.get_instance().create_participant(
                id,
                this.creaQos(), 
                null, // listener
                StatusKind.STATUS_MASK_NONE);
        
        if (this.participante == null) {
            System.err.println("No se pudo obtener el dominio.");
            System.exit(1);
        }
        
        // Crea los tópicos (se suscribe a ellos)
        this.topicos = new ArrayList<>(nombreTopicos.length);
        for (String nombreTopico : nombreTopicos) {
            Topic topico = this.participante.create_topic(
                    nombreTopico, 
                    BytesTypeSupport.get_type_name(), 
                    DomainParticipant.TOPIC_QOS_DEFAULT, 
                    null, // listener
                    StatusKind.STATUS_MASK_NONE);
            if (topico == null)
                System.err.println("No se pudo crear el tópico: " + nombreTopico);
            else
                this.topicos.add(topico);
        }
    }
    
    private DomainParticipantQos creaQos() {
        DomainParticipantQos qos = new DomainParticipantQos();
        DomainParticipantFactory.TheParticipantFactory.get_default_participant_qos(qos);
        qos.receiver_pool.buffer_size = 65507;
        try { PropertyQosPolicyHelper.remove_property(qos.property, "dds.transport.UDPv4.builtin.parent.message_size_max"); } catch (Exception ex) { }
        try { PropertyQosPolicyHelper.remove_property(qos.property, "dds.transport.UDPv4.builtin.send_socket_buffer_size"); } catch (Exception ex) { }
        try { PropertyQosPolicyHelper.remove_property(qos.property, "dds.transport.UDPv4.builtin.recv_socket_buffer_size"); } catch (Exception ex) { }
        try { PropertyQosPolicyHelper.remove_property(qos.property, "dds.transport.shmem.builtin.parent.message_size_max"); } catch (Exception ex) { }
        try { PropertyQosPolicyHelper.remove_property(qos.property, "dds.transport.shmem.builtin.receive_buffer_size"); } catch (Exception ex) { }
        try { PropertyQosPolicyHelper.remove_property(qos.property, "dds.transport.shmem.builtin.received_message_count_max"); } catch (Exception ex) { }
        PropertyQosPolicyHelper.add_property(
                qos.property,
                "dds.transport.UDPv4.builtin.parent.message_size_max",
                "65507",
                true);
        PropertyQosPolicyHelper.add_property(
                qos.property,
                "dds.transport.UDPv4.builtin.send_socket_buffer_size",
                "2097152",
                true);
        PropertyQosPolicyHelper.add_property(
                qos.property,
                "dds.transport.UDPv4.builtin.recv_socket_buffer_size",
                "2097152",
                true);
        PropertyQosPolicyHelper.add_property(
                qos.property,
                "dds.transport.shmem.builtin.parent.message_size_max",
                "65507",
                true);
        PropertyQosPolicyHelper.add_property(
                qos.property,
                "dds.transport.shmem.builtin.receive_buffer_size",
                "2097152",
                true);
        PropertyQosPolicyHelper.add_property(
                qos.property,
                "dds.transport.shmem.builtin.received_message_count_max",
                "2048",
                true);
        PropertyQosPolicyHelper.add_property(
                qos.property,
                "dds.builtin_type.octets.max_size",
                "2097152",
                true);
        
        return qos;
    }
    
    public int getNumTopicos() {
        return this.topicos.size();
    }
    
    public Topic[] getTopicos() {
        return this.topicos.toArray(new Topic[0]);
    }
    
    public Topic getTopico(final int i) {
        return this.topicos.get(i);
    }
}
