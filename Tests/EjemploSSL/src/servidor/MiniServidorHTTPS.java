package servidor;

import java.net.*;
import java.io.*;
import java.security.*;
import javax.net.ssl.*;

/**
 *
 * @Author jjramos 
 * bibliografía y referencia: http://docs.oracle.com/javase/1.5.0/docs/guide/security/jsse/JSSERefGuide.html
 * 
 *
 */
public class MiniServidorHTTPS {

    public final static int DEFAULT_PORT = 443;
    public final static String algorithm = "SSLv3";

    public static void main(String[] args) {

        // Se asigna como puerto de escucha el puerto por defecto,
        // a menos que se indique como parámetro el puerto deseado:
        int puerto = DEFAULT_PORT;
        if (args.length > 0) {
            puerto = Integer.parseInt(args[0]);
        }

        try {
            // Para nuestro socket seguro, inicializamos los siguiente (usar "SSL" o "TLS"):
            SSLContext context = SSLContext.getInstance("SSL");

            // Gestor de claves, para acceder al certificado del servidor
            // La implementacio'n de referencia soporta so'lo llaves con formato X.509
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");

            // El tipo de anillo a utilizar sera' del tipo SUN:
            KeyStore ks = KeyStore.getInstance("JKS");

            // Para consultar las llaves almacenadas en el anillo, es necesario dar antes
            // la contrasenia. El problema reside en que al utilizar aqui' la clave
            // en texto plano, la seguridad del anillo esta' comprometida:
            char[] password = "contrasenia_servidor".toCharArray();
            ks.load(new FileInputStream("src/cert/anillo_certificado_servidor.keys"), password);
            kmf.init(ks, password);

            // Inicializamos el motor generador de los sockets de servidor, 
            // y escuchamos en el puerto correspondiente:
            context.init(kmf.getKeyManagers(), null, null);
            SSLServerSocketFactory factory = context.getServerSocketFactory();
            SSLServerSocket server = (SSLServerSocket) factory.createServerSocket(puerto);

            // Ahora podemos trabajar como con los sockets normales:
            try {
                while (true) {
                    // Aceptamos la conexio'n,
                    Socket theConnection = server.accept();
                    BufferedReader in = new BufferedReader(new InputStreamReader(theConnection.getInputStream()));
                    PrintWriter out = new PrintWriter(theConnection.getOutputStream());
                    String linea = "";

                    // Leemos toda la petición, hasta que haya un salto de línea:
                    linea = in.readLine();
                    while (linea != null) {
                        if (linea.compareTo("") != 0) {
                            System.out.println(linea);
                             
                        } else {
                            String mensajeDar
                                    = "______           _____ _     _     _____                               \n"
                                    + "|  _  \\         /  ___| |   (_)   |_   _|                              \n"
                                    + "| | | |__ _ _ __\\ `--.| |__  _ _ __ | |_ __ ___   ___  _ __   ___ _ __ \n"
                                    + "| | | / _` | '__|`--. \\ '_ \\| | '_ \\| | '__/ _ \\ / _ \\| '_ \\ / _ \\ '__|\n"
                                    + "| |/ / (_| | |  /\\__/ / | | | | |_) | | | | (_) | (_) | |_) |  __/ |   \n"
                                    + "|___/ \\__,_|_|  \\____/|_| |_|_| .__/\\_/_|  \\___/ \\___/| .__/ \\___|_|   \n"
                                    + "                              | |                     | |              \n"
                                    + "                              |_|                     |_|              \n"
                                    + " _____                                  _____  __                      \n"
                                    + "/  ___|                                |  _  |/  |                     \n"
                                    + "\\ `--.  ___ _ ____   _____ _ __  __   _| |/' |`| |                     \n"
                                    + " `--. \\/ _ \\ '__\\ \\ / / _ \\ '__| \\ \\ / /  /| | | |                     \n"
                                    + "/\\__/ /  __/ |   \\ V /  __/ |     \\ V /\\ |_/ /_| |_                    \n"
                                    + "\\____/ \\___|_|    \\_/ \\___|_|      \\_/  \\___(_)___/                    \n"
                                    + "                                                                       \n"
                                    + "                                                        ";

                            // Si es una línea en blanco, significa que se ha terminado de recibir 
                            // mensaje de petición. En ese caso, el servidor contestará:
                            out.println("200 OK HTTP/1.1");
                            out.println("Server: DarServer 0.1");
                            out.println("Content-Length: " + mensajeDar.length());
                            out.println();
                            out.println(mensajeDar);
                            out.flush();
                            
                             out.close();
                             in.close();
                            in=null;
                            

                        }
                        
                        // Si no se ha cerrado la conexión:
                        if(in!=null){
                            linea = in.readLine();
                        } else {
                            linea=null;
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println(e);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } // end catch
        catch (KeyManagementException e) {
            e.printStackTrace();
        } // end catch
        catch (KeyStoreException e) {
            e.printStackTrace();
        } // end catch
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } // end catch
        catch (java.security.cert.CertificateException e) {
            e.printStackTrace();
        } // end catch
        catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        }
    }
}
