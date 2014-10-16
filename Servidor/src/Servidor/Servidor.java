

package Servidor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

public class Servidor {   

    public static void main(String[] args) {

        char[] password = "Prometheus".toCharArray();
                
        int puerto  =6556;
        System.out.println("Iniciando servicio en puerto " + puerto);
        
        iniciarServicio(puerto, password);
    }

    private static void iniciarServicio(final int puerto, final char[] password) {
        try {
         
            SSLContext context = SSLContext.getInstance("SSL");
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");

            KeyStore ks = KeyStore.getInstance("JKS");

            ks.load(new FileInputStream("src/cert/keystore.jks"), password);
            kmf.init(ks, "PrometheuS".toCharArray());

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
        private DataInputStream reader;
        
        boolean isAdmin;
        boolean isFather;

        
        public Servicio(final Socket socket) {
            this.socket = socket;
            this.isAdmin=this.isFather=false;
            
            InputStream inStream;
            try {
                inStream = this.socket.getInputStream();
                this.reader = new DataInputStream(inStream);
            } catch (IOException ex) {
                Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        @Override
        public void run() {
            try {
                DataOutputStream writer = new DataOutputStream(socket.getOutputStream());
                String respuesta, comando;
                while(true){
                    comando = reader.readUTF();
                    if(!comando.equals("exit")){
                        String [] separado = comando.split(" ");
                        if(!isFather && !isAdmin && !separado[0].equals("autentificar")){
                            respuesta = "No autentificado";
                        }
                        else if(separado[0].equals("autentificar")){
                            switch(separado[1]){
                                case "padre":
                                    int id = Integer.parseInt(separado[3]);
                                    respuesta = autentificarPadre(separado[2],id, separado[4]);
                                    break;
                                case "admin":
                                    respuesta =autentificarAdmin(separado[2],separado[3]);
                                    break;
                                default:
                                    respuesta = "No autentificado";
                                    System.out.println("No autentificado");
                                    break;
                            }
                        }
                        else{
                           respuesta = commands(separado);
                        }
                        System.out.println("Envio "+respuesta);
                        writer.writeUTF(respuesta);
                    }
                    else{
                        socket.close();
                        break;
                    }
                }
                
                
                
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

            /**
           * Comandos disponibles:
           * registrar [id padre] [pass] [idnino] [key que se le pone al niño]
           * borrar [id padre] [idnino]
           * modificar  [id padre] [pass] [idnino] [key que se le pone al niño] [nuevo id nino]
           */
          public String commands(String[] command){
              if(isAdmin){   
                  switch(command[0]){
                      case "registrar":
                          return registrarPadre(command[1],command[2],Integer.parseInt(command[3]),command[4]);
                      case "borrar":
                          return borrarPadre(command[1],Integer.parseInt(command[2]));
                      case "modificar":
                          return modificarPadre(command[1],command[2],Integer.parseInt(command[3]),command[4],Integer.parseInt(command[5]));
                  }
              }
              else{
                  switch(command[0]){
                      default:
                          return "No permitido";
                  }
              }



              return null;
          }


          public String autentificarPadre(String IDPadre, int nino, String password){
           
            try {
                BaseDatos conexion = new BaseDatos();
                
                String query = "SELECT * FROM padres where padre = ? and nino= ? ";
                PreparedStatement consulta = conexion.getConnection().prepareStatement(query);
                consulta.setString(1, IDPadre);
                consulta.setInt(2, nino);
                ResultSet res = consulta.executeQuery();
                
                if(res.next()){
                    if(res.getString("pass").equals(password)){
                        isFather=true;
                        return res.getString("key");
                    }
                }
                
                } catch (SQLException ex) {
                Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
            }
                
                return "La autentificación fallo";
            
            
          }


          public String autentificarAdmin(String usuario, String pass){
              if(usuario.equals("administrador") && pass.equals("prometheus")){
                  isAdmin=true;
                  return "Bienvenido administrador";
              }
              System.out.println(usuario.equals("administrador"));
 
              return "No tiene privilegios";
          }

          public String registrarPadre(String padre, String pass, int nino, String clave){
              try {

                  BaseDatos conexion = new BaseDatos();
                  Statement estatuto = conexion.getConnection().createStatement();
                  String passwordCifrada = cifrarPass(pass);
                  estatuto.executeUpdate("INSERT INTO padres VALUES ('"+nino+"', '"+padre+"', '"+clave+"', '"+passwordCifrada+"');");
                  conexion.desconectar();
              } catch (SQLException ex) {
                  return "No se pudo registrar";
              }
              return "Registrado satisfactoriamente";

          }


          private String cifrarPass(String pass){
              MessageDigest mDigest = null;

           
            try {
                mDigest = MessageDigest.getInstance("SHA1");
            } catch (NoSuchAlgorithmException ex) {
                Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
            }


              byte[] result = mDigest.digest(pass.getBytes());
              StringBuilder sb = new StringBuilder();
              for (int i = 0; i < result.length; i++) {
                  sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
              }

              return sb.toString();
          }

          public String borrarPadre(String padre,int nino){
          
            try {
                BaseDatos conexion = new BaseDatos();
                String query ="DELETE FROM padres WHERE padre = ? AND nino = ?";
                
                PreparedStatement consulta = conexion.getConnection().prepareStatement(query);
                consulta.setString(1, padre);
                consulta.setInt(2, nino);
                System.out.println(consulta.toString());
                consulta.executeUpdate();
                
                conexion.desconectar();
                
                return "Padre borrado correctamente";
            } catch (SQLException ex) {
                Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
                return "No se pudo padre";
            }
          }


          public String modificarPadre(String padreID, String pass, int nino, String clave, int nuevoIDnino){
            try {
                BaseDatos conexion = new BaseDatos();
                String query;
                PreparedStatement consulta = null;
                 
                if(!pass.equals("null") && !clave.equals("null") && nuevoIDnino!=0){
                   if(!clave.equals("null")){
                          query= "UPDATE padres SET  `key` =  ? WHERE padre = ? AND nino= ? ";
                          consulta = conexion.getConnection().prepareStatement(query);
                          consulta.setString(1, clave);
                          consulta.setString(2,padreID);
                          consulta.setInt(3,nino);
                          consulta.executeUpdate(); 
                   }
                   if(nuevoIDnino!=0){
                          query= "UPDATE padres SET  `nino` =  ? WHERE padre = ? AND nino= ? ";
                          consulta = conexion.getConnection().prepareStatement(query);
                          consulta.setInt(1, nuevoIDnino);
                          consulta.setString(2,padreID);
                          consulta.setInt(3,nino);
                          consulta.executeUpdate(); 
                   }
                   if(!pass.equals("null")){
                          query= "UPDATE padres SET  `pass` =  ? WHERE padre = ? AND nino= ? ";
                          consulta = conexion.getConnection().prepareStatement(query);
                          consulta.setString(1,clave);
                          consulta.setString(2,padreID);
                          consulta.setInt(3,nino);
                          consulta.executeUpdate(); 
                   }
                    

                conexion.desconectar();
                
                return "Padre modificado";
                
                
                
                }
                else{
                    return "No modificado";
                }

            } catch (SQLException ex) {
                Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
                return "No modificado";
            }

          }
        
    }
    
    
    

}