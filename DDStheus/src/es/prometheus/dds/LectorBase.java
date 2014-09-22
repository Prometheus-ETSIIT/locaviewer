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

import com.rti.dds.dynamicdata.DynamicData;
import com.rti.dds.dynamicdata.DynamicDataReader;
import com.rti.dds.dynamicdata.DynamicDataSeq;
import com.rti.dds.infrastructure.ConditionSeq;
import com.rti.dds.infrastructure.Duration_t;
import com.rti.dds.infrastructure.RETCODE_NO_DATA;
import com.rti.dds.infrastructure.RETCODE_TIMEOUT;
import com.rti.dds.infrastructure.ResourceLimitsQosPolicy;
import com.rti.dds.infrastructure.StringSeq;
import com.rti.dds.infrastructure.WaitSet;
import com.rti.dds.subscription.InstanceStateKind;
import com.rti.dds.subscription.QueryCondition;
import com.rti.dds.subscription.SampleInfo;
import com.rti.dds.subscription.SampleInfoSeq;
import com.rti.dds.subscription.SampleStateKind;
import com.rti.dds.subscription.ViewStateKind;
import java.awt.event.ActionListener;
import java.util.Arrays;

/**
 * Clase abstracta para recibir datos dinámicos de un tópico y filtrarlos según un key.
 */
public abstract class LectorBase {
    private final TopicoControl control;
    private final DynamicDataReader reader;
    private final DataCallback callback;
    private final Thread dataThread;
    
    /**
     * Crea una base de lector.
     * 
     * @param control Control de tópico actual.
     * @param expresion Expresión para la condición al discernir los datos.
     * @param params Parámetros de la expresión del filtro.
     */
    protected LectorBase(final TopicoControl control, final String expresion,
            final String[] params) {
        this.control = control;
        this.reader  = control.creaLector();
        
        // Crea el filtro de datos.
        QueryCondition condicion = reader.create_querycondition(
                SampleStateKind.ANY_SAMPLE_STATE,
                ViewStateKind.ANY_VIEW_STATE,
                InstanceStateKind.ANY_INSTANCE_STATE,
                expresion,
                new StringSeq(Arrays.asList(params)));
        
        this.callback = new DataCallback(this.reader, condicion);
        this.dataThread = new Thread(this.callback);
        this.dataThread.start();
    }
    
    /**
     * Libera los recursos del lector.
     */
    public void dispose() {
        this.dataThread.interrupt();
        this.reader.delete_contained_entities();
        this.control.eliminaLector(this.reader);
    }
    
    /**
     * Añade un listener extra que se llamará después de parsear los datos recibidos.
     * Para desactivarlo establecer a null.
     * 
     * @param listener Listener externo.
     */
    public void setExtraListener(final ActionListener listener) {
        this.callback.setExtraListener(listener);
    }
    
    /**
     * Cambia los parámetros de la expresión de filtro del lector.
     * 
     * @param params Nuevos parámetros.
     */
    public final void cambioParametros(final String[] params) {
        this.callback.cambiaParametros(params);
    }
    
    /**
     * Para de recibir datos de DDS.
     */
    public void parar() {
        this.callback.parar();
    }
    
    /**
     * Continua con la recepción de datos de DDS.
     */
    public void reanudar() {
        this.callback.reanudar();
    }
    
    /**
     * Método que se llama para sacar los datos recibidos de la muestra.
     * 
     * @param sample Muestra recibida con datos.
     */
    protected abstract void getDatos(DynamicData sample);
    
    /**
     * Clase para implementar la recepción de datos de DDS con condiciones de
     * forma síncrona.
     * Esta solución es necesaria porque usando listener, el listener de un 
     * DataReader no puede modificar otro DataReader (como sus condiciones).
     * 
     * Más información aquí:
     * http://community.rti.com/kb/how-can-i-prevent-deadlocks-while-invoking-rti-apis-listener
     * http://community.rti.com/kb/what-does-exclusive-area-error-message-mean
     */
    private class DataCallback implements Runnable {
        private final DynamicDataReader reader;
        private final QueryCondition condicion;
        private final WaitSet waitset;
        private final Duration_t duracion;
        
        private ActionListener extraListener;
        private boolean parado;
        private boolean terminar;
        
        /**
         * Crea una nueva instancia para un lector con condición dada.
         * 
         * @param reader Lector del que recibir datos.
         * @param condicion Condición a aplicar sobre los datos.
         */
        public DataCallback(final DynamicDataReader reader, final QueryCondition condicion) {
            this.parado  = true;
            this.terminar = false;
            
            this.reader    = reader;
            this.condicion = condicion;
            this.duracion  = new Duration_t(5, 0);
            this.waitset   = new WaitSet();
            this.waitset.attach_condition(condicion);
        }
        
        @Override
        public void run() {
            while (!this.terminar) {
                // Esperamos a obtener la siguiente muestra que cumpla la condición
                ConditionSeq activadas = new ConditionSeq();
                try { this.waitset.wait(activadas, duracion); }
                catch (RETCODE_TIMEOUT e) { continue; }
                
                // Si nos dicen que paremos, nosotros paramos.
                if (this.parado)
                    continue;
                
                // Procesamos los datos recibidos.
                this.processData();
            }
        }
        
        /**
         * Procesa los datos recibidos de DDS.
         */
        private void processData() {   
            // Obtiene todos los sample de DDS
            DynamicDataSeq dataSeq = new DynamicDataSeq();
            SampleInfoSeq infoSeq = new SampleInfoSeq();
            try {
                // Obtiene datos aplicandole el filtro
                this.reader.take_w_condition(
                        dataSeq,
                        infoSeq,
                        ResourceLimitsQosPolicy.LENGTH_UNLIMITED,
                        this.condicion); 

                // Procesamos todos los datos recibidos
                for (int i = 0; i < dataSeq.size(); i++) {
                    SampleInfo info = (SampleInfo)infoSeq.get(i);

                    // En caso de que sea meta-data del tópico
                    if (!info.valid_data)
                        continue;

                    // Deserializa los datos
                    DynamicData sample = (DynamicData)dataSeq.get(i);
                    getDatos(sample);

                    // Llama al listener externo
                    if (this.extraListener != null)
                        this.extraListener.actionPerformed(null);
                }
            } catch (RETCODE_NO_DATA e) {
                // No hace nada, al filtrar datos pues se da la cosa de que no haya
            } finally {
                // Es para liberar recursos del sistema.
                this.reader.return_loan(dataSeq, infoSeq);
            }
        }
        
        /**
         * Deja de procesar los datos que recibe.
         */
        public void parar() {
            this.parado = true;
        }
        
        /**
         * Comienza a procesar los datos recibidos de nuevo.
         */
        public void reanudar() {
            this.parado = false;
        }
        
        /**
         * Termina la ejecución en la próxima iteración.
         */
        public void terminar() {
            this.terminar = true;
        }
        
        /**
         * Establece un listener extra para cuando se reciben los datos.
         * 
         * @param listener Listener extra.
         */
        public void setExtraListener(final ActionListener listener) {
            this.extraListener = listener;
        }
        
        /**
         * Cambia los parámetros de la condición.
         * 
         * @param params Nuevos parámetros.
         */
        public void cambiaParametros(final String[] params) {
            this.condicion.set_query_parameters(new StringSeq(Arrays.asList(params)));
        }
    }
}
