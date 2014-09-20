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

package control;

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

/**
 * Clase abstracta para recibir datos de un tópico y filtrarlos según un key.
 */
public abstract class SuscriptorBase extends DataReaderAdapter {
    private final TopicoControl control;
    private final DynamicDataReader reader;
    private final QueryCondition condition;
    private final String key;
    
    /**
     * Crea una base de suscriptor.
     * 
     * @param control Control de tópico actual.
     * @param key Parámetro key para distiguir los datos.
     */
    protected SuscriptorBase(final TopicoControl control, final String key) {
        this.control = control;
        this.reader  = control.creaLector();
        this.key     = key;
                
        // Crea el filtro de datos.
        StringSeq queryParams = new StringSeq(1);
        queryParams.add("'" + key + "'");
        
        this.condition = reader.create_querycondition(
                SampleStateKind.ANY_SAMPLE_STATE,
                ViewStateKind.ANY_VIEW_STATE,
                InstanceStateKind.ANY_INSTANCE_STATE,
                "camId = %0",
                queryParams);

        // Le añade el listener para recibir datos.
        reader.set_listener(this, StatusKind.STATUS_MASK_ALL);
    }
    
    /**
     * Libera los recursos del suscriptor.
     */
    public void dispose() {
        this.reader.set_listener(null, StatusKind.STATUS_MASK_NONE);
        this.reader.delete_contained_entities();
        this.control.eliminaLector(this.reader);
    }
    
    /**
     * Obtiene la clave que está discerniendo los datos en este lector.
     * 
     * @return Clave del lector. 
     */
    public String getKey() {
        return this.key;
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
            }
        } catch (RETCODE_NO_DATA e) {
            // No hace nada, al filtrar datos pues se da la cosa de que no haya
        } finally {
            // Es para liberar recursos del sistema.
            dynamicReader.return_loan(dataSeq, infoSeq);
        }
    }
}
