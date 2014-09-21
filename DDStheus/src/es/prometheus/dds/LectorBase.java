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
import com.rti.dds.infrastructure.RETCODE_NO_DATA;
import com.rti.dds.infrastructure.ResourceLimitsQosPolicy;
import com.rti.dds.infrastructure.StatusKind;
import com.rti.dds.infrastructure.StringSeq;
import com.rti.dds.subscription.DataReader;
import com.rti.dds.subscription.DataReaderAdapter;
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
public abstract class LectorBase extends DataReaderAdapter {
    private final TopicoControl control;
    private final DynamicDataReader reader;
    private final QueryCondition condition;
    
    private ActionListener extraListener;
    private boolean parado;
    
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
        this.parado  = true;
        
        // Crea el filtro de datos.
        this.condition = reader.create_querycondition(
                SampleStateKind.ANY_SAMPLE_STATE,
                ViewStateKind.ANY_VIEW_STATE,
                InstanceStateKind.ANY_INSTANCE_STATE,
                expresion,
                new StringSeq(Arrays.asList(params)));
    }
    
    /**
     * Libera los recursos del lector.
     */
    public void dispose() {
        this.reader.set_listener(null, StatusKind.STATUS_MASK_NONE);
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
        this.extraListener = listener;
    }
    
    /**
     * Cambia los parámetros de la expresión de filtro del lector.
     * 
     * @param params Nuevos parámetros.
     */
    public final void cambioParametros(final String[] params) {
        // Paro la recepción para poder cambiar el parámetro.
        this.parar();
        this.condition.set_query_parameters(new StringSeq(Arrays.asList(params)));
        this.reanudar();
    }
    
    /**
     * Para de recibir datos de DDS.
     */
    public void parar() {
        if (this.parado)
            return;

        // Le quita el listener luego no recibe datos.
        reader.set_listener(null, StatusKind.STATUS_MASK_NONE);
        this.parado = true;
    }
    
    /**
     * Continua con la recepción de datos de DDS.
     */
    public void reanudar() {
        if (!this.parado)
            return;
        
        // Le añade el listener para recibir datos.
        reader.set_listener(this, StatusKind.STATUS_MASK_ALL);
        this.parado = false;
    }
    
    /**
     * Método que se llama para sacar los datos recibidos de la muestra.
     * 
     * @param sample Muestra recibida con datos.
     */
    protected abstract void getDatos(DynamicData sample);
    
    /**
     * Callback que llama RTI connext cuando se recibe para datos.
     * 
     * @param dataReader Lector de datos
     */
    @Override
    public void on_data_available(DataReader dataReader) {
        // Obtiene todos los sample de DDS
        DynamicDataReader dynamicReader = (DynamicDataReader)dataReader;
        DynamicDataSeq dataSeq = new DynamicDataSeq();
        SampleInfoSeq infoSeq = new SampleInfoSeq();
        try {
            // Obtiene datos aplicandole el filtro
            dynamicReader.take_w_condition(
                    dataSeq,
                    infoSeq,
                    ResourceLimitsQosPolicy.LENGTH_UNLIMITED,
                    this.condition); 
            
            // Procesamos todos los datos recibidos
            for (int i = 0; i < dataSeq.size(); i++) {
                SampleInfo info = (SampleInfo)infoSeq.get(i);
                
                // En caso de que sea meta-data del tópico
                if (!info.valid_data)
                    continue;

                // Deserializa los datos
                DynamicData sample = (DynamicData)dataSeq.get(i);
                this.getDatos(sample);
                
                // Llama al listener externo
                if (this.extraListener != null)
                    this.extraListener.actionPerformed(null);
            }
        } catch (RETCODE_NO_DATA e) {
            // No hace nada, al filtrar datos pues se da la cosa de que no haya
        } finally {
            // Es para liberar recursos del sistema.
            dynamicReader.return_loan(dataSeq, infoSeq);
        }
    }
}
