/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Prometheus
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
        if (args.length != 4) {
            System.err.println("[ServidorLauncher] Número de argumentos inválido.");
            return;
        }

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

                if (!pubList.isEmpty() && pubList.first() < prioridad)
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
