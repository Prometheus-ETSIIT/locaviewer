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

package publicador;

import com.rti.dds.dynamicdata.DynamicData;
import es.prometheus.dds.DiscoveryChange;
import es.prometheus.dds.DiscoveryChangeStatus;
import es.prometheus.dds.DiscoveryData;
import es.prometheus.dds.DiscoveryListener;
import es.prometheus.dds.Escritor;
import es.prometheus.dds.TopicoControl;
import es.prometheus.dds.TopicoControlFactoria;
import java.util.ArrayList;
import java.util.List;

/**
 * Escribe periódicamente datos de localización de un niño.
 */
public class EscritorNino extends Thread {
    private static final String TOPIC_NAME = "ChildDataTopic";
    private static final int MAX_ITER = 200;    // Máximas iteraciones
    private static final int SLEEP_TIME = 5000; // Tiempo entre iteración

    private boolean parar;
    private DatosNino[] valoresNino;

    private final List<DiscoveryData> dataSubs = new ArrayList<>();
    private TopicoControl topico;
    private Escritor escritor;
    private DynamicData datos;

    /**
     * Inicializa una nueva instancia de la clase.
     */
    public EscritorNino() {
        this.parar = false;
    }

    @Override
    public void run() {
        // Crea los datos a enviar.
        this.creaDatos();

        // Iniciamos DDS
        this.iniciaDds();

        // Alterna entre los datos y los envía
        for (int i = 0; i < MAX_ITER && !parar; i++) {
            // Alterna los datos
            DatosNino ninoActual = valoresNino[i % valoresNino.length];

            // Comprueba que haya algún suscriptor interesado
            if (this.existeSuscriptor(ninoActual.getId())) {
                // Escribe los datos
                ninoActual.escribeDds(datos);
                escritor.escribeDatos(datos);
            }

            // Esperamos 3 segundos antes de mandar la siguiente posición
            try { Thread.sleep(SLEEP_TIME); }
            catch (InterruptedException e) { break; }
        }

        // Libera los recursos
        System.out.println("Limpiando. . .");
        this.topico.dispose();
    }

    /**
     * Pide la interrupción del escritor.
     */
    public void parar() {
        this.parar = true;
    }

    /**
     * Crea los datos a enviar.
     */
    private void creaDatos() {
        // Crea los datos estándar de un niño
        DatosNino nino = new DatosNino();
        nino.setCalidad(82.3);
        nino.setSala("Clase 1.A");
        nino.setSalaL(4.0);
        nino.setSalaW(4.0);
        nino.setNombre("Benito Palacios");
        nino.setApodo("Benii");

        // Versiones alteradas de los datos
        this.valoresNino = new DatosNino[5];
        valoresNino[0] = MueveNino(nino, "test0", 3.0, 1.0, "20:14:04:11:34:37");
        valoresNino[1] = MueveNino(nino, "test0", 2.5, 2.3, "20:14:04:11:34:37");
        valoresNino[2] = MueveNino(nino, "test1", 3.0, 2.1, "20:14:04:11:34:37");
        valoresNino[3] = MueveNino(nino, "test1", 1.0, 3.1, "42049184");
        valoresNino[4] = MueveNino(nino, "test1", 1.4, 3.2, "42049184");
    }

    /**
     * Inicializa DDS y crea las entidades.
     */
    private void iniciaDds() {
        // Crea un escritor para el dominio
        this.topico = TopicoControlFactoria.crearControlDinamico(
                "ParticipantesPC::ParticipanteNino",
                "ChildDataTopic");

        // Obtiene todos los lectores suscriptos a este escritor.
        for (DiscoveryData data : this.topico.getParticipanteControl().getDiscoveryReaderData())
            this.updateNumSubs(data, DiscoveryChangeStatus.ANADIDO);

        // Añade el listener para los lectores.
        this.topico.getParticipanteControl().addDiscoveryReaderListener(new DiscoveryListener() {
            @Override
            public void onChange(DiscoveryChange[] changes) {
                for (DiscoveryChange ch : changes)
                    updateNumSubs(ch.getData(), ch.getStatus());
            }
        });

        this.escritor = new Escritor(topico);
        this.datos = escritor.creaDatos();
    }

    /**
     * Comprueba si existe algún lector interesado en este niño.
     *
     * @param id Id del niño.
     * @return Si hay algún lector interesado.
     */
    private boolean existeSuscriptor(final String id) {
        for (DiscoveryData d : this.dataSubs) {
            if (d.getFilterParams().isEmpty())
                continue;

            String currId = (String)d.getFilterParams().get(0);
            currId = currId.replaceAll("'", "");
            if (currId.equals(id))
                return true;
        }

        return false;
    }

    /**
     * Acutaliza la lista de suscriptores.
     *
     * @param data Datos del lector.
     * @param status Estado del lector.
     */
    private void updateNumSubs(final DiscoveryData data,  final DiscoveryChangeStatus status) {
        // Compara si coincide el tópico.
        if (!TOPIC_NAME.equals(data.getTopicName()))
            return;

        // Actualizo la lista de suscriptores
        if (status == DiscoveryChangeStatus.ANADIDO)
            this.dataSubs.add(data);
        else if (status == DiscoveryChangeStatus.ELIMINADO)
            this.dataSubs.remove(data);
        else if (status == DiscoveryChangeStatus.CAMBIADO) {
            for (int i = 0; i < this.dataSubs.size(); i++) {
                if (this.dataSubs.get(i).getHandle().equals(data.getHandle())) {
                    this.dataSubs.set(i, data);
                    break;
                }
            }
        }

        System.out.println("Suscriptores: " + this.dataSubs.size());
        for (DiscoveryData d : this.dataSubs)
            if (!d.getFilterParams().isEmpty())
                System.out.println("\t" + d.getFilterParams().get(0));
    }

    /**
     * Crea un nuevo dato alterando uno ya existente.
     *
     * @param nino Dato base.
     * @param camId Nuevo ID de cámara que le está enfocando.
     * @param x Nueva coordenada X de la posición del niño.
     * @param y Nueva coordenada Y de la posición del niño.
     * @return
     */
    private static DatosNino MueveNino(DatosNino nino, String camId, double x,
            double y, String id) {
        DatosNino nuevo = nino.clone();
        nuevo.setCamId(camId);
        nuevo.setPosX(x);
        nuevo.setPosY(y);
        nuevo.setId(id);
        return nuevo;
    }
}
