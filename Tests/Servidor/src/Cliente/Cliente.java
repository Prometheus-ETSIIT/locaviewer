

package Cliente;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.Security;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class Cliente {
    
    
    public static void main(String[] args) {
        try {
  
            Socket socket = creaSocketSeguro("localhost", 6556);
            DataOutputStream writer = new DataOutputStream(socket.getOutputStream());
            writer.writeUTF("hasdasd");
            
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
        }

           
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
