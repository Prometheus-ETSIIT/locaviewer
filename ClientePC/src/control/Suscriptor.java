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

import com.rti.dds.infrastructure.RETCODE_ERROR;
import com.rti.dds.infrastructure.RETCODE_NO_DATA;
import com.rti.dds.infrastructure.StatusKind;
import com.rti.dds.subscription.DataReader;
import com.rti.dds.subscription.DataReaderAdapter;
import com.rti.dds.subscription.SampleInfo;
import com.rti.dds.subscription.Subscriber;
import com.rti.dds.topic.Topic;
import com.rti.dds.type.builtin.Bytes;
import com.rti.dds.type.builtin.BytesDataReader;
import java.util.Arrays;
import org.gstreamer.Buffer;
import org.gstreamer.ClockTime;
import org.gstreamer.Element;
import org.gstreamer.ElementFactory;
import org.gstreamer.Format;
import org.gstreamer.Pipeline;
import org.gstreamer.State;
import org.gstreamer.elements.AppSrc;
import org.gstreamer.swing.VideoComponent;

public class Suscriptor extends DataReaderAdapter {
    private final Topic topico;
    private final BytesDataReader dataReader;
    private final VideoComponent videocomp;
    
    private Pipeline pipe;
    private AppSrc appsrc;
    
    public Suscriptor(final Topic topico, final VideoComponent videocomp) {
        this.topico = topico;
        this.videocomp = videocomp;
        
        this.creaTuberia();
        
        // Play!
        // Cambiar el estado puede tomar hasta 5 segundos. Comprueba errores.
        this.pipe.play();
        State retState = this.pipe.getState(ClockTime.fromSeconds(5).toSeconds());
        if (retState == State.NULL) {
            System.err.println("Error al cambio de estado.");
            System.exit(1);
        }
        
        // Crea el suscriptor
        this.dataReader = (BytesDataReader)topico.get_participant().create_datareader(
                topico, 
                Subscriber.DATAREADER_QOS_DEFAULT,
                this,         // Listener
                StatusKind.DATA_AVAILABLE_STATUS);
        if (dataReader == null)
            System.err.println("No se pudo crear el suscriptor.");
    }
    
    private void creaTuberia() {
        // Crea los elementos de la tubería
        // 1º Origen de vídeo, simulado porque se inyectan datos.
        this.appsrc = (AppSrc)ElementFactory.make("appsrc", null);
        
        // 2º Decodificación
        Element videoconvert = ElementFactory.make("ffmpegcolorspace", null);
        Element codec = ElementFactory.make("jpegdec", null);
       
        // 3º Salida de vídeo
        Element videosink = this.videocomp.getElement();
        
        // Crea la tubería
        this.pipe = new Pipeline();
        this.pipe.addMany(this.appsrc, codec, videoconvert, videosink);
        Element.linkMany(this.appsrc, codec, videoconvert, videosink);
        
        // Configura el APPSRC
        appsrc.setLive(true);
        appsrc.setLatency(0, 100);
        appsrc.setTimestamp(true);
        appsrc.setFormat(Format.TIME);
        appsrc.setStreamType(AppSrc.Type.STREAM);
    }
    
    public void eliminar() {
        // Elimina el suscriptor
        this.topico.get_participant().delete_datareader(this.dataReader);
        
        // Para la tuberia
        this.pipe.stop();
        this.pipe.dispose();
    }
    
    /**
     * Callback que llama RTI connext cuando se recibe para datos.
     * 
     * @param reader Lector de datos
     */
    @Override
    public void on_data_available(DataReader reader) {   
        // Obtiene todos los sample de DDS
        BytesDataReader bytesReader = (BytesDataReader)reader;
        while (true) {
            // Intenta obtener un sample.
            Bytes data = new Bytes();
            SampleInfo info = new SampleInfo();
            try {
                bytesReader.take_next_sample(data, info);           
            } catch (RETCODE_NO_DATA noData) {
                // No hay más datos para leer, paramos
                break;
            } catch (RETCODE_ERROR e) {
                // Se produjo un error
                e.printStackTrace();
            }
            
            // En caso de que sea meta-data del tópico
            if (!info.valid_data)
                return;

            // Deserializa los datos
            byte[] recibido = Arrays.copyOfRange(
                    data.value,
                    data.offset,
                    data.length
            );

            // Crea el buffer de GStreamer
            Buffer buffer = new Buffer(recibido.length);
            buffer.getByteBuffer().put(recibido);

            // Lo mete en la tubería
            this.appsrc.pushBuffer(buffer);
        }
    }
    
    public VideoComponent getVideoComponent() {
        return this.videocomp;
    }
}
