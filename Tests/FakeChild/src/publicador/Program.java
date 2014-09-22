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
import es.prometheus.dds.TopicoControlDinamico;

/**
 * Programa que simula estar localizando a un niño.
 */
public class Program {
    private static final int MAX_ITER = 200; // Máximas iteraciones => ~6.5 min
    private static final int SLEEP_TIME = 5000; // Tiempo entre iteración
    
    /**
     * Inicia el programa.
     * 
     * @param args No acepta ningún argumento.
     */
    public static void main(String[] args) {
        // Crea un escritor para el dominio
        TopicoControlDinamico topico = new TopicoControlDinamico(
                "ParticipantesPC::ParticipanteNino",
                "ChildDataTopic");
        Escritor escritor = new Escritor(topico);
        DynamicData datos = escritor.creaDatos();
        
        // Crea los datos estándar de un niño
        DatosNino nino = new DatosNino();
        nino.setCalidad(82.3);
        nino.setId("86159283");
        nino.setSala("Clase 1.A");
        nino.setNombre("Benito Palacios");
        nino.setApodo("Benii");
        
        // Versiones alteradas de los datos
        DatosNino[] valoresNino = new DatosNino[3];
        valoresNino[0] = MueveNino(nino, "0", 3.0, 1.0);
        valoresNino[1] = MueveNino(nino, "0", 2.5, 2.3);        
        valoresNino[2] = MueveNino(nino, "1", 2.0, 5.1);

        // Alterna entre los datos y los envía
        for (int i = 0; i < MAX_ITER; i++) {          
            // Alterna los datos
            DatosNino ninoActual = valoresNino[i % valoresNino.length];
            
            // Escribe los datos
            ninoActual.escribeDds(datos);
            escritor.escribeDatos(datos);
            
            // Esperamos 3 segundos antes de mandar la siguiente posición
            try { Thread.sleep(SLEEP_TIME); }
            catch (InterruptedException e) { break; }
        }
        
        // Libera recursos
        escritor.eliminaDatos(datos);
        escritor.dispose();
        topico.dispose();
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
