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

package tienda;

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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import mensajes.CompraRespuesta;
import mensajes.InfoCompra;
import mensajes.Mensaje;
import mensajes.Pagare;

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
        args = new String[]{ "9091" };

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
        private static final int VendedorId = 0xCCAAFFE;
        private static final int[] Secretos = {
            0xCCCCCCCC, 0xAAAAAAAA,
            0xFFFFFFFF, 0xEEEEEEEE
        };
        private static final Map<Integer, Short> Productos = CreaListaProductos();
        private static final List<Long> PagaresRecibidos = new ArrayList<>();
        
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
            Mensaje peticion = InfoCompra.Parse(this.inStream);
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
            InfoCompra solicitud = (InfoCompra)mensaje;
            
            // Valida el mensaje y genera la respuesta
            byte resultado = ValidaCompra(solicitud);
            CompraRespuesta respuesta = new CompraRespuesta(resultado, solicitud.getProductoId());
            
            // En caso de error le devuelvo todo su dinero
            if (resultado != 0) {
                for (Pagare p : solicitud.getPagares())
                    respuesta.addPagare(p);
                return respuesta;
            }
            
            // Obtiene todo el dinero que nos da y lo marco como usado
            short dinero = 0;
            for (Pagare p : solicitud.getPagares()) {
                dinero += p.getValor();
                PagaresRecibidos.add(p.getId());
            }
            
            // Obtiene otros parámetros adicionales para generar pagarés
            byte edad = solicitud.getPagares()[0].getCompradorEdad();
            byte pais = solicitud.getPagares()[0].getCompradorPais();
            
            // En caso de que no haya habido errores, generar pagarés de vuelta
            short vuelta = (short)(dinero - Productos.get(solicitud.getProductoId()));
            while (vuelta > 0) {
                // Obtiene el valor que tendrá este pagaré y actualiza la var.
                short valorActual = (vuelta > MaxCantidad) ? MaxCantidad : vuelta;
                vuelta -= valorActual;
                
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
                        edad,
                        pais,
                        GetSecreto(id)
                );
                
                // Lo añade a la respuesta
                respuesta.addPagare(pagare);
            }
            
            return respuesta;
        }
        
        private static int GetSecreto(final long id) {
            return Secretos[(int)(id % Secretos.length)];
        }
        
        private static byte ValidaCompra(final InfoCompra msg) {
            if (msg.getVendedorId() != VendedorId)
                return 1;
            
            if (!Productos.containsKey(msg.getProductoId()))
                return 2;
            
            // Comprueba que haya suficiente dinero
            int dinero = 0;
            for (Pagare p : msg.getPagares())
                dinero += p.getValor();
            if (dinero < Productos.get(msg.getProductoId()))
                return 3;
            
            // Compruebo que no haya repetidos
            for (Pagare p : msg.getPagares())
                if (PagaresRecibidos.contains(p.getId()))
                    return 4;
            
            // Valida los pagaré
            for (Pagare p : msg.getPagares())
                if (!ValidaPagare(p))
                    return 5;
            
            return 0;
        }
        
        private static boolean ValidaPagare(final Pagare p) {
            return p.validate(GetSecreto(p.getId()));
        }
        
        private static Map<Integer, Short> CreaListaProductos() {
            Map<Integer, Short> productos = new HashMap<>();
            
            productos.put(0x11111111, (short)437);
            productos.put(0x12538219, (short)10000);
            
            return productos;
        }
    }
}