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

package cliente;

import java.io.IOException;
import java.net.Socket;
import java.security.Security;
import java.util.Scanner;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import mensajes.*;

/**
 *
 * @author Benito Palacios Sánchez, Álvaro Artigas Gil
 */
public class Cliente {
    
    private static final String DefaultNombre    = "Benito";
    private static final String DefaultApellidos = "Palacios Sánchez";
    private static final String DefaultEdad      = "20";
    private static final String DefaultPais      = "12";
    
    private static final String DefaultVendedor  = "CCAAFFE";
    private static final String DefaultTarjeta   = "1234567890123456";
    private static final String DefaultMes       = "12";
    private static final String DefaultAnio      = "15";
    private static final String DefaultSeg       = "123";
    private static final String DefaultValor     = "833";
    
    private static final String DefaultProducto  = "11111111";
    
    public static void main(String[] args) {
        // Nos saluda
        Scanner scanner = new Scanner(System.in);
        System.out.println("¡Bienvenido a comprator 3000!");
        System.out.println();
        
        try {
            // Genera solicitud
            Solicitud solicitud = generaSolicitud(scanner);
            System.out.println();
            
            // Envía solicitud
            Socket socket = creaSocketSeguro("localhost", 9090);
            solicitud.write(socket.getOutputStream());

            // Recibe respuesta
            SolicitudRespuesta soliRespuesta = SolicitudRespuesta.Parse(socket.getInputStream());
            socket.close();

            byte tipo = soliRespuesta.getTipo();
            if (tipo != 0x00) {
                System.out.println("Error de respuesta a solicitud: " + tipo);
                return;
            }
            
            // Obtiene los pagarés
            System.out.println("Pagares recibidos:");
            Pagare[] moneeyyy = soliRespuesta.getPagares();
            for (Pagare p : moneeyyy)
                System.out.println(p);    
            
            // Genera la petición de compra
            InfoCompra compra = generaCompra(moneeyyy, scanner);
            System.out.println();
            
            // La envía
            socket = creaSocketSeguro("localhost", 9091);
            compra.write(socket.getOutputStream());
            
            // Recibe la respuesta
            CompraRespuesta compraRespuesta = CompraRespuesta.Parse(socket.getInputStream());
            socket.close();
            
            tipo = compraRespuesta.getTipo();
            if (tipo != 0x00) {
                System.out.println("Error de respuesta a compra: " + tipo);
            } else {
                System.out.println("Compra realizada con éxito.");
                System.out.println("Vuelta:");
                for (Pagare p : compraRespuesta.getPagares())
                    System.out.println(p);
            }
        } catch (IOException ex) {
        }
    }
    
    private static Solicitud generaSolicitud(final Scanner scanner) {
        // Pide los datos del usuario
        System.out.println("# Introduzca sus datos personales");
        String nombre = pideDato("Nombre", DefaultNombre, scanner);
        String apellidos = pideDato("Apellidos", DefaultApellidos, scanner);
        byte edad = Byte.parseByte(pideDato("Edad", DefaultEdad, scanner));
        byte pais = Byte.parseByte(pideDato("Código país", DefaultPais, scanner));
        
        // Pide datos de compra
        System.out.println("# Introduzca los datos de compra");
        int vendedorId = Integer.parseInt(
                pideDato("ID de vendedor (hex)", DefaultVendedor, scanner),
                16
        );
        String tarjetaNum = pideDato("Nº tarjeta", DefaultTarjeta, scanner);
        byte tarjetaMes   = Byte.parseByte(pideDato("Mes caducidad", DefaultMes, scanner));
        byte tarjetaAnio  = Byte.parseByte(pideDato("Año caducidad", DefaultAnio, scanner));
        String tarjetaSeg = pideDato("Nº seguridad", DefaultSeg, scanner);
        short valor = Short.parseShort(pideDato("Euros", DefaultValor, scanner));
        
        // Genera solicitud
        Solicitud solicitud = new Solicitud(
                vendedorId,
                valor,
                nombre,
                apellidos,
                edad,
                pais,
                tarjetaNum,
                tarjetaMes,
                tarjetaAnio,
                tarjetaSeg
        );   
        
        return solicitud;
    }
    
    private static InfoCompra generaCompra(final Pagare[] pagares, 
            final Scanner scanner) {
        System.out.println("# Introduzca los datos de la compra a realizar");
        int productoId = Integer.parseInt(
                pideDato("Id producto", DefaultProducto, scanner),
                16
        );
        int vendedorId = Integer.parseInt(
                pideDato("Id vendedor", DefaultVendedor, scanner),
                16
        );
        
        InfoCompra compra = new InfoCompra(productoId, vendedorId, pagares);
        return compra;
    }
    
    private static String pideDato(final String campo, final String defecto,
            final Scanner scanner) {
        System.out.print("\t" + campo + " [" + defecto + "]: ");
        
        String valor = scanner.nextLine();
        if (valor.isEmpty())
            valor = defecto;
        
        return valor;
    }
    
    private static Socket creaSocketSeguro(final String host, final int puerto) {
        SSLSocket socket = null;
        
        try {
            // Le indicamos de qué anillo obtener las claves públicas fiables
            // de autoridades de certificación:
            System.setProperty(
                    "javax.net.ssl.trustStore",
                    "./src/cert/autoridades_certificadoras_fiables.keys"
            );
            
            // Un socket seguro (SSL) con la configuración por defecto:
            Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
            SSLSocketFactory factory = (SSLSocketFactory)SSLSocketFactory.getDefault();
            socket = (SSLSocket)factory.createSocket(host, puerto);

            socket.setEnabledCipherSuites(socket.getSupportedCipherSuites());
        } catch (IOException ex) {
            System.out.println("ERROR: " + ex.getMessage());
        }
        
        return socket;
    }
}
