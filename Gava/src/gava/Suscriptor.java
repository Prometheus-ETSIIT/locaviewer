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

package gava;

import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.DomainParticipantFactory;
import com.rti.dds.dynamicdata.DynamicData;
import com.rti.dds.dynamicdata.DynamicDataReader;
import com.rti.dds.dynamicdata.DynamicDataSeq;
import com.rti.dds.infrastructure.ByteSeq;
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
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.gstreamer.Buffer;
import org.gstreamer.ClockTime;
import org.gstreamer.Element;
import org.gstreamer.ElementFactory;
import org.gstreamer.Format;
import org.gstreamer.Gst;
import org.gstreamer.Pipeline;
import org.gstreamer.State;
import org.gstreamer.elements.AppSrc;
import org.gstreamer.swing.VideoComponent;

public class Suscriptor extends DataReaderAdapter {
    private final Pipeline pipe;
    private final AppSrc appsrc;
    private final QueryCondition query;
    
    public Suscriptor(QueryCondition query) {
        this.query = query;
        
        // Crea los elementos de la tubería
        // 1º Origen de vídeo, simulado porque se inyectan datos.
        this.appsrc = (AppSrc)ElementFactory.make("appsrc", null);
        
        // 2º Decodificación
        Element videoconvert = ElementFactory.make("ffmpegcolorspace", null);
        Element codec = ElementFactory.make("jpegdec", null);
        
        // 3º Salida de vídeo
        VideoComponent videoComponent = new VideoComponent();
        Element videosink = videoComponent.getElement();
       
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
        
        // Crea la ventana y la muestra
        JFrame frame = new JFrame("Gava suscriptor testing");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(videoComponent, BorderLayout.CENTER);
        videoComponent.setPreferredSize(new Dimension(720, 576));
        frame.pack();
        frame.setVisible(true);
        
        // Play!
        // Cambiar el estado puede tomar hasta 5 segundos. Comprueba errores.
        this.pipe.play();
        State retState = this.pipe.getState(ClockTime.fromSeconds(5).toSeconds());
        if (retState == State.NULL) {
            System.err.println("Error al cambio de estado.");
            System.exit(1);
        }
        
        GstDebugUtils.gstDebugBinToDotFile(pipe, 0, "suscriptor");
    }
    
    /**
     * Inicia el programa
     * 
     * @param args Un argumento opcional como nombre del tópico.
     */
    public static void main(final String[] args) {
        final String key = (args.length == 0) ? "test_cam_0" : args[0];
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Gst.init("Gava", args); // Inicia GStreamer
                IniciaDds(key);      // Inicia DDS
            }
        });
    }
    
    private static void IniciaDds(final String key) {
        // Obtiene el participante de dominio creado en el XML
        DomainParticipant participant = DomainParticipantFactory.get_instance()
                .create_participant_from_config("MyParticipantLibrary::SubscriptionParticipant");
        if (participant == null) {
            System.err.println("No se pudo crear el dominio.");
            System.exit(1);
        }

        // Obtiene el lector creado en el XML
        /* NOTA IMPORTANTE:
            Cada suscriptor se puede usar sólo una vez de forma simultánea,
            es decir no se pueden usar dos instancias de este mismo, si se hace
            sólo se recibirán datos en la última, y la primera dejará de recibirlos.
            Esto se debe a que sólo puede haber un listener.
        
            Hay que crear tantos lectores en el XML como se deseen.
        */
        DynamicDataReader reader = (DynamicDataReader)participant
                .lookup_datareader_by_name("MySubscriber::VideoDataReader");
        if (reader == null) {
            System.err.println("No se pudo crear el lector.");
            System.exit(1);
        }
        
        // Añade un filtro al lector
        /* NOTA IMPORTANTE:
            El filtro se basa en una expresión SQL de este formato:
            http://community.rti.com/rti-doc/510/ndds/doc/html/api_java/group__DDSQueryAndFilterSyntaxModule.html
        
            Como se ve, el parámetro que se compara, si es un string debe ir entre
            comillas simples, pero esto tiene que suceder en queryParam, no puede ser en la
            expresión SQL directamente, de ahí que haya que crear el queryParam.
        */
        StringSeq queryParam = new StringSeq(1);
        queryParam.add("'" + key + "'");
        QueryCondition condition = reader.create_querycondition(
                SampleStateKind.ANY_SAMPLE_STATE,
                ViewStateKind.ANY_VIEW_STATE,
                InstanceStateKind.ANY_INSTANCE_STATE,
                "camId = %0",
                queryParam
                );
        
        // Le añade el listener para recibir datos.
        reader.set_listener(new Suscriptor(condition), StatusKind.STATUS_MASK_ALL);
    }
    
    /**
     * Callback que llama RTI connext cuando se recibe para datos.
     * 
     * @param reader Lector de datos
     */
    @Override
    public void on_data_available(DataReader reader) { 
        // Obtiene todos los sample de DDS
        DynamicDataReader dynamicReader = (DynamicDataReader)reader;
        DynamicDataSeq dataSeq = new DynamicDataSeq();
        SampleInfoSeq infoSeq = new SampleInfoSeq();
        try {
            // Obtiene datos aplicandole el filtro
            dynamicReader.take_w_condition(
                    dataSeq,
                    infoSeq,
                    ResourceLimitsQosPolicy.LENGTH_UNLIMITED,
                    this.query); 
            
            // Procesamos todos los datos recibidos
            for (int i = 0; i < dataSeq.size(); i++) {
                SampleInfo info = (SampleInfo)infoSeq.get(i);
                
                // En caso de que sea meta-data del tópico
                if (!info.valid_data)
                    continue;

                // Deserializa los datos
                DynamicData sample = (DynamicData)dataSeq.get(i);
                // DEBUG: sample.print(null, 0); // Para mostrarlo formateado por la consola
                String camId = sample.get_string("camId", DynamicData.MEMBER_ID_UNSPECIFIED);
                String sala  = sample.get_string("sala", DynamicData.MEMBER_ID_UNSPECIFIED);
                double posX  = sample.get_double("posX", DynamicData.MEMBER_ID_UNSPECIFIED);
                double posY  = sample.get_double("posY", DynamicData.MEMBER_ID_UNSPECIFIED);
                double angle = sample.get_double("angle", DynamicData.MEMBER_ID_UNSPECIFIED);
                String codecInfo = sample.get_string("codecInfo", DynamicData.MEMBER_ID_UNSPECIFIED);
                int width  = sample.get_int("width", DynamicData.MEMBER_ID_UNSPECIFIED);
                int height = sample.get_int("height", DynamicData.MEMBER_ID_UNSPECIFIED);
                
                // Crea el buffer de GStreamer
                ByteSeq bufferSeq = new ByteSeq();
                sample.get_byte_seq(bufferSeq, "buffer", DynamicData.MEMBER_ID_UNSPECIFIED);

                Buffer buffer = new Buffer(bufferSeq.size());
                buffer.getByteBuffer().put(bufferSeq.toArrayByte(null));

                // Lo mete en la tubería
                this.appsrc.pushBuffer(buffer);
            }
        } catch (RETCODE_NO_DATA e) {
            // No hace nada, al filtrar datos pues se da la cosa de que no haya
        } finally {
            // Es para liberar recursos del sistema.
            dynamicReader.return_loan(dataSeq, infoSeq);
        }
    }
}
