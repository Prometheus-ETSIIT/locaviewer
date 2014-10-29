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

package comunicador;

import es.prometheus.dds.DiscoveryChange;
import es.prometheus.dds.DiscoveryChangeStatus;
import es.prometheus.dds.DiscoveryData;
import es.prometheus.dds.DiscoveryListener;
import es.prometheus.dds.TopicoControl;
import es.prometheus.dds.TopicoControlFactoria;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Ejecuta el servidor si y sólo si este dispositivo es el más prioritario.
 */
public class ServidorLauncher extends Thread {
    private static final String CHILD_TOPIC_NAME = "ChildDataTopic";
    
    private final String sala;
    private final double ancho;
    private final double largo;
    private final int prioridad;
    
    private final SortedSet<Integer> pubList = new TreeSet<>();
    private TopicoControl topico;
    private Servidor servidor;
    
    public ServidorLauncher(String sala, double ancho, double largo, int priority) {
        this.sala  = sala;
        this.ancho = ancho;
        this.largo = largo;
        this.prioridad = priority;   
    }
    
    /**
     * Inicia el programa.
     * 
     * @param args Uno: el nombre de la sala.
     * Dos: Ancho de la sala.
     * Tres: Largo de la sala.
     * Cuatro: Prioridad.
     */
    public static void main(String[] args) {
        if (args.length != 4)
            return;
        
        // Creamos el comunicador de sensor
        double ancho = Double.parseDouble(args[1]);
        double largo = Double.parseDouble(args[2]);
        int priority = Integer.parseInt(args[3]);
        ServidorLauncher l = new ServidorLauncher(args[0], ancho, largo, priority);
        l.start();
        
        // Creamos una hebra para salidas forzosas (Control+C).
    	Runtime.getRuntime().addShutdownHook(new ShutdownThread(l));  
    }
    
    @Override
    public void run() {
        // Iniciamos el servidor para que publique su prioridad.
        this.servidor = new Servidor(this.sala, this.ancho, this.largo, this.prioridad);
        this.servidor.start();
        this.servidor.suspender();
        
        // Iniciamos DDS y que haga magia :D
        this.iniciaDds();
    }
    
    public void dispose() {
        this.topico.dispose();
        
        this.servidor.dispose();
        try { this.servidor.join(5000); }
        catch (InterruptedException e) { }
    }
    
    private void iniciaDds() {
        // Inicia el participante en el tópico del niño para monitorizar
        // los publicadores de la sala.
        this.topico = TopicoControlFactoria.crearControlDinamico(
                "MisParticipantes::ParticipanteLauncher",
                CHILD_TOPIC_NAME);
        
        // Añade un listener de descubridor de publicadores, filtradas por sala.
        // Actualizamos las listas por cada publicador ya existente
        for (DiscoveryData d : this.topico.getParticipanteControl().getDiscoveryWriterData())
            onWriterDiscovered(d, DiscoveryChangeStatus.ANADIDO);
        
        if (!this.pubList.isEmpty() && this.pubList.first() < this.prioridad)
            this.servidor.suspender();
        else
            this.servidor.reanudar();
        
        // Listener para cuando se descubra un publicador nuevo.
        this.topico.getParticipanteControl().addDiscoveryWriterListener(new DiscoveryListener() {
            @Override
            public void onChange(DiscoveryChange[] changes) {
                for (DiscoveryChange ch : changes)
                    onWriterDiscovered(ch.getData(), ch.getStatus());
                
                if (pubList.first() < prioridad)
                    servidor.suspender();
                else
                    servidor.reanudar();
            }
        });
    }
    
    /**
     * Actualiza las listas de publicadores descubiertos.
     * 
     * @param data Datos del publicador descubierto.
     * @param status Estado del publicador descubierto.
     */
    private void onWriterDiscovered(DiscoveryData data, DiscoveryChangeStatus status) {
        String userData = new String(data.getUserData().toArrayByte(null));
        
        // Nos centramos en un tópico.
        if (!data.getTopicName().equals(CHILD_TOPIC_NAME))
            return;
        
        // En los publicadores de nuestra sala
        String[] fields = userData.split("#");
        System.out.printf("[ServidorLauncher] %s -> %s\n", userData, status.name());
        String infoSala = fields[0];
        int infoPrio = Integer.parseInt(fields[1]);
        if (!infoSala.equals(this.sala))
            return;
        
        // Busca si ya está en la lista
        boolean exists = this.pubList.contains(infoPrio);

        // Actualiza la lista
        if (exists && status == DiscoveryChangeStatus.ELIMINADO) {
            this.pubList.remove(infoPrio);
        } else if (!exists && status == DiscoveryChangeStatus.ANADIDO) {
            this.pubList.add(infoPrio);
        }
    }
        
    /**
     * Listener llamado cuando se finaliza la aplicación.
     */
    private static class ShutdownThread extends Thread {
        private final ServidorLauncher servidor;
        
        public ShutdownThread(final ServidorLauncher servidor) {
            this.servidor = servidor;
        }
        
        @Override
        public void run() {
            System.out.println("[ServidorLauncher] Parando. . .");
            this.servidor.dispose();
            try { this.servidor.join(5000); }
            catch (InterruptedException e) { }
        }
    }
}
