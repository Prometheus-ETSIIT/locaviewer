

package Servidor;

import java.io.DataInputStream;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

public class Servidor {   

    public static void main(String[] args) {

        char[] password = "contrasenia_servidor".toCharArray();
                
        int puerto  =6556;
        System.out.println("Iniciando servicio en puerto " + puerto);
        
        iniciarServicio(puerto, password);
    }

    private static void iniciarServicio(final int puerto, final char[] password) {
        try {
         
            SSLContext context = SSLContext.getInstance("SSL");
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");

            KeyStore ks = KeyStore.getInstance("JKS");

            ks.load(new FileInputStream("src/cert/anillo_certificado_servidor.keys"), password);
            kmf.init(ks, password);


            context.init(kmf.getKeyManagers(), null, null);
            SSLServerSocketFactory factory = context.getServerSocketFactory();
            SSLServerSocket socket = (SSLServerSocket) factory.createServerSocket(puerto);


            while (true) {

                Socket userSocket = socket.accept();

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

        private final Socket socket;
        private InputStream inStream;
        
        public Servicio(final Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                inStream  = socket.getInputStream();
                DataInputStream reader = new DataInputStream(inStream);
                String mensaje = reader.readUTF();
                System.out.println(mensaje);
                // Cierra la conexión
                try {
                    this.socket.close();
                } catch (IOException ex) {
                    System.out.println("ERROR: " + ex.getMessage());
                }
            } catch (IOException ex) {
                Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}