/*
 * Copyright (C) 2014 Benito Palacios Sánchez, Álvaro Artigas Gil
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

package broker;

import mensajes.Mensaje;
import mensajes.SolicitudRespuesta;
import mensajes.Solicitud;
import mensajes.Pagare;
import java.io.Console;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

/**
 *
 * @author Benito Palacios Sánchez, Álvaro Artigas Gil
 */
public class Servidor {   
    /**
     * @param args Argumentos pasados por la línea de comandos
     */
    public static void main(String[] args) {
        // DEBUG:
        args = new String[]{ "9090" };

        if (args.length != 1) {
            System.out.println("Argumentos inválidos.");
            System.out.println();
            System.out.println("USO: Servidor puerto");
            return;
        }

        /*
        // Pregunta por la contraseña, es mejor preguntarla porque
        // se ocultarán los caracteres escritos y no quedará constancia
        // si que quiere automatizar se podría mandar desde un fichero
        // con el operador '<' desde al consola.
        // contrasenia_servidor
        Console console = System.console();
        if (console == null) {
            System.out.println("Console es nulo");
            System.out.println("¿Está ejecutando el programa desde el IDE?");
            return;
        }
        
        char[] password = console.readPassword("Contraseña: ");
        */
        char[] password = "contrasenia_servidor".toCharArray();
                
        int puerto  = Integer.parseInt(args[0]);
        System.out.println("Iniciando servicio en puerto " + puerto);
        
        iniciarServicio(puerto, password);
    }

    private static void iniciarServicio(final int puerto, final char[] password) {
        try {
            // Para nuestro socket seguro, inicializamos los siguiente (usar "SSL" o "TLS"):
            SSLContext context = SSLContext.getInstance("SSL");

            // Gestor de claves, para acceder al certificado del servidor
            // La implementación de referencia soporta solo llaves con formato X.509
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");

            // El tipo de anillo a utilizar sera del tipo SUN:
            KeyStore ks = KeyStore.getInstance("JKS");

            // Para consultar las llaves almacenadas en el anillo, es necesario dar antes
            // la contrasenia. El problema reside en que al utilizar aquí la clave
            // en texto plano, la seguridad del anillo está comprometida:
            ks.load(new FileInputStream("src/cert/anillo_certificado_servidor.keys"), password);
            kmf.init(ks, password);

            // Inicializamos el motor generador de los sockets de servidor, 
            // y escuchamos en el puerto correspondiente:
            context.init(kmf.getKeyManagers(), null, null);
            SSLServerSocketFactory factory = context.getServerSocketFactory();
            SSLServerSocket socket = (SSLServerSocket) factory.createServerSocket(puerto);


            while (true) {
                // Espero a recibir una nueva petición del cliente.
                Socket userSocket = socket.accept();

                // Para cada cliente nuevo, creo una nueva hebra que tendrá
                // el socket para realizar la comunicación y la base de datos
                // con todos los usuarios existentes en el servicio.
                Servicio serv = new Servicio(userSocket);
                serv.start();
            }

        } catch (NoSuchAlgorithmException | KeyStoreException |
                CertificateException | UnrecoverableKeyException | 
                KeyManagementException | IOException ex) {
            System.out.println("ERROR: " + ex.getMessage());
        }
    }
    
    private static class Servicio extends Thread {
        private static final int MaxCantidad = 500; // 5 €
        private static final Map<Integer, Integer[]> Secretos = CreaSecretos();
        
        private final Socket socket;
        
        private InputStream inStream;
        private OutputStream outStream;
        
        public Servicio(final Socket socket) {
            this.socket = socket;

            try {
                this.inStream  = this.socket.getInputStream();
                this.outStream = this.socket.getOutputStream();
            } catch (IOException ex) {
                System.out.println("ERROR: " + ex.getMessage());
            }
        }

        @Override
        public void run() {
            // Es un servidor sencillo que responde cada vez que se le hace
            // una petición. No puede recibir más de un tipo de mensajes y no
            // llega a crear una sesión, no tiene que guardar datos del cliente.
            Mensaje peticion = Solicitud.Parse(this.inStream);
            if (peticion != null) {     
                // Procesa el mensaje y genera una respuesta
                Mensaje respuesta = ProcesaMensaje(peticion);

                // Envía la respuesta
                respuesta.write(outStream);
            }
            
            // Cierra la conexión
            try {
                this.socket.close();
            } catch (IOException ex) {
                System.out.println("ERROR: " + ex.getMessage());
            }
        }

        private static Mensaje ProcesaMensaje(final Mensaje mensaje) {
            Solicitud solicitud = (Solicitud)mensaje;
            
            // Valida el mensaje y genera la respuesta
            byte resultado = ValidaSolicitud(solicitud);
            SolicitudRespuesta respuesta = new SolicitudRespuesta(resultado);
            if (resultado != 0)
                return respuesta;
            
            // En caso de que no haya habido errores, generar pagarés
            short valor = solicitud.getValor();
            while (valor > 0) {
                // Obtiene el valor que tendrá este pagaré y actualiza la var.
                short valorActual = (valor > MaxCantidad) ? MaxCantidad : valor;
                valor -= valorActual;
                
                // Fecha de validez del pagaré: un año desde ahora.
                Calendar fecha = Calendar.getInstance();
                fecha.add(Calendar.YEAR, 1);
                
                // Genera un ID aleatorio y de ahí sacará el secreto
                // Vale, odio Java, ¿por qué no tiene valores unsigned?
                long id = Math.abs(new Random().nextLong());
                
                // Genera el pagaré
                Pagare pagare = new Pagare(
                        id,
                        valorActual,
                        fecha.getTime(),
                        solicitud.getVendedorId(),
                        solicitud.getEdad(),
                        solicitud.getPais(),
                        GetSecreto(solicitud.getVendedorId(), id)
                );
                
                // Lo añade a la respuesta
                respuesta.addPagare(pagare);
            }
            
            return respuesta;
        }
        
        private static int GetSecreto(final int vendedorId, final long id) {
            Integer[] secretos = Secretos.get(vendedorId);
            return secretos[(int)(id % secretos.length)];
        }
        
        private static byte ValidaSolicitud(final Solicitud solicitud) {           
            if (!Secretos.containsKey(solicitud.getVendedorId()))
                return 1;
            
            if (solicitud.getEdad() < 18)
                return 2;
            
            // TODO: Hacer algo para validad la tarjeta
            
            return 0;
        }
        
        /**
         * Crea el mapa de secretos.
         * Cada entrada representa el ID del vendedor, a la cual tendrá
         * asociada un vector de secretos.
         * 
         * @return 
         */
        private static Map<Integer, Integer[]> CreaSecretos() {
            Map<Integer, Integer[]> secretos = new HashMap<>();
            
            // Tienda de compra-venta de tapones de botellas de agua.
            secretos.put(
                    0x1F1F1F1F,
                    new Integer[] {
                        0x12345678, 0x77654321,
                        0x12563478, 0x77435421
                    }
            );
            
            // Tienda de la asociación de afectados por la cafeína
            secretos.put(
                    0x0CCAAFFE,
                    new Integer[] {
                        0x0CCCCCCC, 0x0AAAAAAA,
                        0x0FFFFFFF, 0x0EEEEEEE
                    }
            );
            
            // Tienda de bebidas para bebes.
            secretos.put(
                    0x0BEEBBEE,
                    new Integer[] {
                        0x00000001, 0x0FFFFFFE,
                        0x77777777, 0x74747474
                    }
            );
            
            // Tienda de servidores AAA
            secretos.put(
                    0x0AAAAAAA,
                    new Integer[] {
                        0x58217492, 0x172E84BA,
                        0x9372EFEF, 0x0FEFEFEF
                    }
            );

            return secretos;
        }
    }
}