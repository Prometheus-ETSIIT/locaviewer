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
import com.rti.dds.dynamicdata.DynamicDataWriter;
import com.rti.dds.infrastructure.ByteSeq;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.infrastructure.RETCODE_ERROR;
import com.rti.dds.infrastructure.RETCODE_ILLEGAL_OPERATION;
import com.rti.dds.infrastructure.RETCODE_OUT_OF_RESOURCES;
import java.io.File;
import java.io.FilenameFilter;
import org.gstreamer.Buffer;
import org.gstreamer.Caps;
import org.gstreamer.ClockTime;
import org.gstreamer.Element;
import org.gstreamer.ElementFactory;
import org.gstreamer.Gst;
import org.gstreamer.Pipeline;
import org.gstreamer.State;
import org.gstreamer.elements.AppSink;

public class Publicador implements Runnable {
    private final String device;
    private final String camId;
    
    private DomainParticipant participant;
    private DynamicDataWriter writer;
    private DynamicData instance;
    
    private Pipeline pipe;
    private AppSink appsink;
    
    public Publicador(final String device, final String camId) {
        this.device = device;
        this.camId  = camId;
    }
    
    /**
     * Inicia la aplicación.
     * 
     * @param args La(s) camara(s). Ej: "/dev/video1". 
     *             Si no se pasa ningún parámetro, inicia todas.
     */
    public static void main(String[] args) {
        args = Gst.init("Gava", args);
        
        if (args.length > 0) {
            for (int i = 0; i < args.length; i++)
                new Thread(new Publicador(args[i], "test_cam_" + i)).start();
        } else {
            StartAll();
        }
    }
    
    private static void StartAll() {
        // Obtiene una lista de cámaras conectadas (/dev/video*)
        String[] cams = new File("/dev/").list(new FilenameFilter() {
            @Override
            public boolean accept(File parentDir, String filename) {
                return parentDir.getAbsolutePath().equals("/dev") &&
                       filename.startsWith("video");
            }
        });
        
        // Para cada cámara crea un publicador
        for (String cam : cams) {
            System.out.println("Iniciando cámara " + cam);
            
            // Parsea el ID
            int id = Integer.parseInt(cam.substring(5));
            
            // Crea el publicador
            new Thread(new Publicador("/dev/" + cam, "" + id)).start();
        }
    }
    
    @Override
    public void run() {
        // Inicia DDS y obtiene el escritor
        this.iniciaDds();

        // Crea los elementos de la tubería
        // 1º Origen de vídeo, del códec v4l2
        Element videosrc = ElementFactory.make("v4l2src", null);
        videosrc.set("device", device);
        
        // 2º Datos del vídeo
        Element videofilter = ElementFactory.make("capsfilter", null);
        videofilter.setCaps(Caps.fromString("video/x-raw-yuv,width=640,height=480,framerate=15/1"));
        
        Element videorate = ElementFactory.make("videorate", null);
        
        Element videoconvert = ElementFactory.make("ffmpegcolorspace", null);
        Element codec = ElementFactory.make("jpegenc", null);
        Element codec2 = ElementFactory.make("multipartmux", null);
        
        // 3º Salida de vídeo
        this.appsink = (AppSink)ElementFactory.make("appsink", null);
       
        // Crea la tubería
        this.pipe = new Pipeline();
        this.pipe.addMany(videosrc, videorate, videofilter, videoconvert, codec, codec2, this.appsink);
        Element.linkMany(videosrc, videorate, videofilter, videoconvert, codec, codec2, this.appsink);

        // Configura el APPSINK
        this.appsink.setQOSEnabled(true);
        GstDebugUtils.gstDebugBinToDotFile(pipe, 0, "publicador");        
        
        // Play!
        // Cambiar el estado puede tomar hasta 5 segundos. Comprueba errores.
        this.pipe.play();
        State retState = this.pipe.getState(ClockTime.fromSeconds(5).toSeconds());
        if (retState == State.NULL) {
            System.err.println("Error al cambiar de estado.");
            System.exit(-1);
        }
                
        // Mientras no se acabe, coje cada frame y lo envía.
        while (!appsink.isEOS()) {
            Buffer buffer = appsink.pullBuffer();            
            if (buffer == null)
                 continue;

            // Transfiere los datos a un buffer intermedio
            byte[] tmp = new byte[buffer.getSize()];
            buffer.getByteBuffer().get(tmp);
            
            // Crea la estructura de datos
            try {
                this.instance.set_string("camId", DynamicData.MEMBER_ID_UNSPECIFIED, this.camId);
                this.instance.set_string("sala", DynamicData.MEMBER_ID_UNSPECIFIED, "Torreón");
                this.instance.set_double("posX", DynamicData.MEMBER_ID_UNSPECIFIED, 4.0);
                this.instance.set_double("posY", DynamicData.MEMBER_ID_UNSPECIFIED, 3.2);
                this.instance.set_double("angle", DynamicData.MEMBER_ID_UNSPECIFIED, 90.0);

                this.instance.set_string("codecInfo", DynamicData.MEMBER_ID_UNSPECIFIED, "jpgenc");
                this.instance.set_int("width", DynamicData.MEMBER_ID_UNSPECIFIED, 640);
                this.instance.set_int("height", DynamicData.MEMBER_ID_UNSPECIFIED, 480);
                this.instance.set_byte_seq("buffer", DynamicData.MEMBER_ID_UNSPECIFIED, new ByteSeq(tmp));
            } catch (RETCODE_ILLEGAL_OPERATION | RETCODE_OUT_OF_RESOURCES e) {
                // Este error se da raramente cuando no se cierra bien la aplicación,
                // hay que ver si con esta solución deja de darlo.
                System.out.println("Reecreando estructura");
                this.writer.delete_data(instance);
                this.instance = this.writer.create_data(DynamicData.PROPERTY_DEFAULT);
            }
            
            // Publica la estructura de datos generada en DDS
            try {
                this.writer.write(this.instance, InstanceHandle_t.HANDLE_NIL);
            } catch (RETCODE_ERROR e) {
                System.out.println("! Write error:" + e.getMessage());
                System.exit(1);
            }
        }
    }
    
    private void iniciaDds() {
        // Obtiene el participante de dominio creado en el XML.
        this.participant = DomainParticipantFactory.get_instance()
                .create_participant_from_config("MyParticipantLibrary::PublicationParticipant");
        if (participant == null) {
            System.err.println("No se pudo crear el dominio.");
            System.exit(1);
        }
        
        // Obtiene el escritor creado en el XML.
        /* NOTA IMPORTANTE:
            No se pueden crear dos tópicos con el mismo nombre.
            Tampoco se pueden tener dos Writer con el mismo nombre, por ello
            para cada cámara habrá que definir un nuevo Writer en el XML, que será
            básicamente su ID.
        */
        this.writer = (DynamicDataWriter)participant
                .lookup_datawriter_by_name("MyPublisher::VideoDataWriter" + camId);
        if (this.writer == null) {
            System.err.println("No se pudo crear el escritor.");
            System.exit(1);
        }
        
        // Crea una estructura de datos como la que hemos definido en el XML.
        this.instance = this.writer.create_data(DynamicData.PROPERTY_DEFAULT);
        if (this.instance == null) {
            System.err.println("No se pudo crear la instancia de datos.");
            System.exit(1);
        }
    }
}
