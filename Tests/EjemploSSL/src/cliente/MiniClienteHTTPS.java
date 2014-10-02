package cliente;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.security.Security;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 *
 * @author jjramos
 */
public class MiniClienteHTTPS {
       
 public static void main(String[] args){
    
        int port = 443; // puerto https por defecto    
        String host;    // Nombre o dirección IP  del servidor
        
        // Al menos hay que indicar el nombre/dirección IP del servidor:
        if (args.length < 1){
            System.out.println("Usage: java ClienteSSL <host> [<puerto>]");
            return;
        }
        
        // Asignamos el nombre del servidor:
        host = args[0];
        // Y el puerto, si se pasa como argumento:
        if(args.length>=2){
            port = Integer.parseInt(args[1]);
        }
        
        
        // Abrimos un cliente SSl para la conexión con el servidor:
        try
        {
            // Un socket seguro (SSL) con la configuracio'n por defecto:
            Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider(  ));
            SSLSocketFactory factory
                    = (SSLSocketFactory) SSLSocketFactory.getDefault(  );
            SSLSocket socket = (SSLSocket) factory.createSocket(host, port);
            // A partir de aqui' trabajamos igual que en el caso de Socket y ServerSocket!!
            // Le indicamos de aque' anillo obtener las claves pu'blicas fiables
            //de autoridades de certificacio'n:
            //System.setProperty("javax.net.ssl.trustStore","/tmp/autoridades_certificadoras_fiables.keys");
            
            //socket.setEnabledCipherSuites(socket.getSupportedCipherSuites());
            //{
            //     String []cifra=socket.getSupportedCipherSuites();
            //   for(int i=0;i<cifra.length;i++){
            //     System.out.println(cifra[i]);
            //}
            // System.out.println("------------");
            // cifra=socket.getEnabledCipherSuites();
            // for(int i=0;i<cifra.length;i++){
            //       System.out.println(cifra[i]);
            //}
            //}
            
            // Obtenemos el stream de salida, y lo encapsulamos con PrintWriter,
            // para enviar cómodamente mensajes de texto:
            PrintWriter out = new PrintWriter(socket.getOutputStream(  ));
            
            // Escribimos un mensaje, y el servidor lo mostrara'. Funciona con un
            // servidor web real!
            out.println("GET / HTTP/1.1");
            out.println("Host: "+host);
            out.println("User-Agent: Dar-mini-Browser 0.1");
            out.println();
            out.flush();
            
            // Leemos la respuesta línea a línea:
            String linea="";
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(  )));
            
            while ((linea = in.readLine(  )) != null)
            {
                System.out.println(linea);
            }
            
            out.close();
            in.close();
            socket.close();
        }
        catch (IOException e){             
            System.err.println(e);
        }
    }
}
