

package centroinfantil;


import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.security.Security;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class ClienteServidor {
    
    
    public static void main(String[] args) {
        try {
            InputStreamReader leer = new InputStreamReader(System.in);
            BufferedReader buff = new BufferedReader(leer);
            
         
            Socket socket = creaSocketSeguro("localhost", 6556);
            DataOutputStream writer = new DataOutputStream(socket.getOutputStream());
            InputStream inStream = socket.getInputStream();
            DataInputStream reader = new DataInputStream(inStream);
            
            
            String comando="";
            String respuesta;
            
            while(!comando.equals("exit")){
                System.out.print("Siguiente comando ");
                comando = buff.readLine();
                writer.writeUTF(comando);
                respuesta  =reader.readUTF();
                System.out.println(respuesta);
            }
            
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(ClienteServidor.class.getName()).log(Level.SEVERE, null, ex);
        }

           
    }
    
    
    
    private static Socket creaSocketSeguro(final String host, final int puerto) {
        SSLSocket socket = null;
        
        try {
            // Le indicamos de qué anillo obtener las claves públicas fiables
            // de autoridades de certificación:
            System.setProperty(
                    "javax.net.ssl.trustStore",
                    "./src/cert/cacerts.jks"
            );
            
          
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
