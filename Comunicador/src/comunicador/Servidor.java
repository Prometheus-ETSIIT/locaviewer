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

import com.rti.dds.dynamicdata.DynamicData;
import es.prometheus.dds.DiscoveryChange;
import es.prometheus.dds.DiscoveryChangeStatus;
import es.prometheus.dds.DiscoveryData;
import es.prometheus.dds.DiscoveryListener;
import es.prometheus.dds.Escritor;
import es.prometheus.dds.LectorBase;
import es.prometheus.dds.TopicoControl;
import es.prometheus.dds.TopicoControlFactoria;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servidor de localización por habitación.
 */
public class Servidor extends Thread {
    private static final String SCRIPT_PATH = "detectarcamara.m";
    private static final String FUNC_NAME = "detectarcamara";
    private static final String VIDEO_TOPIC_NAME = "VideoDataTopic";
    private static final String CHILD_TOPIC_NAME = "ChildDataTopic";
    private static final int MAX_TIME = 20*1000;
    
    private final Map<String, ArrayList<DatosSensor>> datosNinos = new HashMap<>();
    private final List<DiscoveryData> ninoSubs = new ArrayList<>();
    private final List<DatosCamara> camaraPubs = new ArrayList<>();
    
    private final String sala;
    private final double ancho;
    private final double largo;
    
    private TriangulacionOctave triangulacion;
    private TopicoControl controlNino;
    private Escritor escritorNino;
    private DynamicData escritorData;
    private TopicoControl controlSensor;
    private LectorBase lectorSensor;
    
    public Servidor(final String sala, double ancho, double largo) {
        this.sala  = sala;
        this.ancho = ancho;
        this.largo = largo;
    }
        
    /**
     * Inicia el programa.
     * 
     * @param args Uno: el nombre de la sala.
     * Dos: Ancho de la sala.
     * Tres: Largo de la sala.
     */
    public static void main(String[] args) {
        if (args.length != 3)
            return;
        
        // Creamos el comunicador de sensor
        double ancho = Double.parseDouble(args[1]);
        double largo = Double.parseDouble(args[2]);
        Servidor servidor = new Servidor(args[0], ancho, largo);
        servidor.start();
        
        // Creamos una hebra para salidas forzosas (Control+C).
    	Runtime.getRuntime().addShutdownHook(new ShutdownThread(servidor));     
    }

    @Override
    public void run() {
        // Inicializa DDS
        this.iniciaDds();
        
        // Inicializa la triangulación
        this.triangulacion = new TriangulacionOctave(
                SCRIPT_PATH,
                FUNC_NAME,
                this.camaraPubs,
                this.ancho,
                this.largo,
                false
        );
    }
    
    /**
     * Libera recursos.
     */
    public void dispose() {
        this.lectorSensor.dispose();
        this.controlSensor.dispose();
        this.controlNino.dispose();
        this.triangulacion.close();
    }
    
    /**
     * Inicializa la entidades de DDS.
     */
    private void iniciaDds() {
        // Inicia las entidades del tópico de sensores:
        // Recopilar todos los datos de una sala (filtrado por sala).
        this.controlSensor = TopicoControlFactoria.crearControlDinamico(
                "MisParticipantes::ParticipanteServidor",
                "SensorDataTopic");
        String[] params = new String[] { "'" + this.sala + "'" };
        this.lectorSensor = new LectorBase(controlSensor, "sala =  %0", params) {
            @Override
            protected void getDatos(final DynamicData sample) {
                // Ejecuto todo en una nueva hebra para que no se bloque DDS
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        onSensorDataReceived(sample);
                    }
                });
                t.start();
            }
        };
        
        // Inicia las entidades del tópico de niños:
        // Publicar datos de triangulación de cada niño.
        this.controlNino = TopicoControlFactoria.crearControlDinamico(
                "MisParticipantes::ParticipanteServidor",
                "ChildDataTopic");
        this.escritorNino = new Escritor(this.controlNino);
        this.escritorData = this.escritorNino.creaDatos();
        this.lectorSensor.iniciar();
        
        // Añade un listener de descubridor de cámaras, filtradas por sala.
        // Actualizamos las listas por cada publicador ya existente
        for (DiscoveryData d : this.controlNino.getParticipanteControl().getDiscoveryWriterData())
            onWriterDiscovered(d, DiscoveryChangeStatus.ANADIDO);
        
        // Listener para cuando se descubra un publicador nuevo.
        this.controlNino.getParticipanteControl().addDiscoveryWriterListener(new DiscoveryListener() {
            @Override
            public void onChange(DiscoveryChange[] changes) {
                for (DiscoveryChange ch : changes)
                    onWriterDiscovered(ch.getData(), ch.getStatus());
            }
        });
        
        // Añade un listener de descubridor de lectores de niños.
        // Obtiene todos los lectores suscriptos a este escritor.
        for (DiscoveryData data : this.controlNino.getParticipanteControl().getDiscoveryReaderData())
            this.onReaderDiscovered(data, DiscoveryChangeStatus.ANADIDO);
        
        // Añade el listener para los lectores.
        this.controlNino.getParticipanteControl().addDiscoveryReaderListener(new DiscoveryListener() {
            @Override
            public void onChange(DiscoveryChange[] changes) {
                for (DiscoveryChange ch : changes)
                    onReaderDiscovered(ch.getData(), ch.getStatus());
            }
        });
    }
    
    /**
     * Actualiza las listas de cámaras a partir de los publicadores
     * descubiertos.
     * 
     * @param data Datos del publicador descubierto.
     * @param status Estado del publicador descubierto.
     */
    private void onWriterDiscovered(DiscoveryData data, DiscoveryChangeStatus status) {
        String userData = new String(data.getUserData().toArrayByte(null));
        
        // Nos centramos en las cámaras
        if (!data.getTopicName().equals(VIDEO_TOPIC_NAME))
            return;
        
        // En las cámaras de nuestra sala
        DatosCamara info = DatosCamara.FromStringSummary(userData);    
        if (!info.getSala().equals(this.sala))
            return;
        
        // Busca si ya está en la lista
        int idx = -1;
        for (int i = 0; i < this.camaraPubs.size() && idx == -1; i++)
            if (this.camaraPubs.get(i).getCamId().equals(info.getCamId()))
                idx = i;

        // Actualiza la lista
        if (idx != -1 && status == DiscoveryChangeStatus.ELIMINADO) {
            this.camaraPubs.remove(idx);
            this.triangulacion.setCamaras(this.camaraPubs);
        } else if (idx == -1 && status == DiscoveryChangeStatus.ANADIDO) {
            this.camaraPubs.add(info);
            this.triangulacion.setCamaras(this.camaraPubs);
        }
    }
    
    /**
     * Se llama cuando se recibe un dato de un sensor de la sala.
     * 
     * @param sample Dato del sensor.
     */
    private void onSensorDataReceived(DynamicData sample) {
        DatosSensor dato = DatosSensor.FromDds(sample);
        
        // Si el dato es de algún niño que no tiene suscriptor interesado, paso
        if (!this.existeSuscriptor(dato.getIDNino()))
            return;
        
        // Si ya tenemos datos del niño...
        if (this.datosNinos.containsKey(dato.getIDNino())) {
            // Busco si ya tenemos un dato de este sensor, y lo elimino
            ArrayList<DatosSensor> datosSensores = this.datosNinos.get(dato.getIDNino());
            for (int k = 0; k < datosSensores.size(); k++)
                if (datosSensores.get(k).getID().equals(dato.getID())) 
                    datosSensores.remove(k);

            // Añado el nuevo dato del sensor
            datosSensores.add(dato);

            // Si algún dato tiene más de MAX_TIME, se borra
            long date = new Date().getTime();
            for (int j = 0; j < datosSensores.size(); j++) {
                if (date - datosSensores.get(j).getCreacion() > MAX_TIME) {
                    datosSensores.remove(j);
                    j--;
                }
            }

            // Con más de 3 datos se puede triangular
            if (datosSensores.size() > 3) {
                // Triangula
                String camId = this.triangulacion.triangular(datosSensores);
                if (camId == null)
                    return;
                
                double[] pos = this.triangulacion.getLastPosition();
                
                // Eliminamos los datos usados
                datosSensores.clear();
                
                // Crea la estructura de localización y la envía
                DatosNino nino = new DatosNino();
                nino.setCalidad(0);
                nino.setId(dato.getIDNino());
                nino.setCamId(camId);
                nino.setSala(this.sala);
                nino.setSalaW(this.ancho);
                nino.setSalaL(this.largo);
                nino.setPosX(pos[0]);
                nino.setPosY(pos[1]);
                nino.escribeDds(this.escritorData);
            }
        // No teníamos datos de este niño
        } else {
            ArrayList<DatosSensor> nuevo = new ArrayList<>();
            nuevo.add(dato);
            this.datosNinos.put(dato.getIDNino(), nuevo);
        }
    }
    
    /**
     * Comprueba si existe algún lector interesado en este niño.
     * 
     * @param id Id del niño.
     * @return Si hay algún lector interesado.
     */
    private boolean existeSuscriptor(final String id) {
        for (DiscoveryData d : this.ninoSubs) {
            String currId = (String)d.getFilterParams().get(0);
            currId = currId.replaceAll("'", "");
            if (currId.equals(id))
                return true;
        }
        
        return false;
    }
    
    /**
     * Acutaliza la lista de suscriptores de niños.
     * 
     * @param data Datos del lector.
     * @param status Estado del lector.
     */
    private void onReaderDiscovered(final DiscoveryData data,  final DiscoveryChangeStatus status) {
        // Compara si coincide el tópico.
        if (!CHILD_TOPIC_NAME.equals(data.getTopicName()))
            return;
        
        // Actualizo la lista de suscriptores
        if (status == DiscoveryChangeStatus.ANADIDO)
            this.ninoSubs.add(data);
        else if (status == DiscoveryChangeStatus.ELIMINADO)
            this.ninoSubs.remove(data);
        else if (status == DiscoveryChangeStatus.CAMBIADO) {
            for (int i = 0; i < this.ninoSubs.size(); i++) {
                if (this.ninoSubs.get(i).getHandle().equals(data.getHandle())) {
                    this.ninoSubs.set(i, data);
                    break;
                }
            }
        }
        
        System.out.println("Suscriptores: " + this.ninoSubs.size());
        for (DiscoveryData d : this.ninoSubs)
            System.out.println("\t" + d.getFilterParams().get(0));
    }
    
    /**
     * Listener llamado cuando se finaliza la aplicación.
     */
    private static class ShutdownThread extends Thread {
        private final Servidor servidor;
        
        public ShutdownThread(final Servidor servidor) {
            this.servidor = servidor;
        }
        
        @Override
        public void run() {
            System.out.println("Parando. . .");
            this.servidor.dispose();
            try { this.servidor.join(5000); }
            catch (InterruptedException e) { }
        }
    }
}
