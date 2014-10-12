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

package publicador;

import com.rti.dds.dynamicdata.DynamicData;
import es.prometheus.dds.Escritor;
import es.prometheus.dds.TopicoControl;
import es.prometheus.dds.TopicoControlFactoria;

/**
 * Escribe periódicamente datos de localización de un niño.
 */
public class EscritorNino extends Thread {
    private static final int MAX_ITER = 200;    // Máximas iteraciones
    private static final int SLEEP_TIME = 5000; // Tiempo entre iteración
    
    private boolean parar;
    
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
        // Iniciamos DDS
        this.iniciaDds();
        
        // Crea los datos estándar de un niño
        DatosNino nino = new DatosNino();
        nino.setCalidad(82.3);
        nino.setId("86159283");
        nino.setSala("Clase 1.A");
        nino.setNombre("Benito Palacios");
        nino.setApodo("Benii");
        
        // Versiones alteradas de los datos
        DatosNino[] valoresNino = new DatosNino[3];
        valoresNino[0] = MueveNino(nino, "test0", 3.0, 1.0);
        valoresNino[1] = MueveNino(nino, "test0", 2.5, 2.3);        
        valoresNino[2] = MueveNino(nino, "test1", 2.0, 5.1);
        
        // Alterna entre los datos y los envía
        for (int i = 0; i < MAX_ITER && !parar; i++) {          
            // Alterna los datos
            DatosNino ninoActual = valoresNino[i % valoresNino.length];
            
            // Escribe los datos
            ninoActual.escribeDds(datos);
            escritor.escribeDatos(datos);
            
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
     * Inicializa DDS y crea las entidades.
     */
    private void iniciaDds() {
        // Crea un escritor para el dominio
        this.topico = TopicoControlFactoria.crearControlDinamico(
                "ParticipantesPC::ParticipanteNino",
                "ChildDataTopic");
        this.escritor = new Escritor(topico);
        this.datos = escritor.creaDatos();
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
            double y) {
        DatosNino nuevo = nino.clone();
        nuevo.setCamId(camId);
        nuevo.setPosX(x);
        nuevo.setPosY(y);
        return nuevo;
    }
}
