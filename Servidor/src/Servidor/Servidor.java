

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
                                    respuesta = autentificarPadre(separado[2], separado[3],writer);
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
           * registrar [id padre] [pass] [idnino] [key que se le pone al niño] [apodo]
           * borrar [id padre] [idnino]
           * modificar  [id padre] [pass] [idnino] [key que se le pone al niño] [nuevo id nino] [apodo]
           * get  [clave]
           */
          public String commands(String[] command){
              if(isAdmin){   
                  switch(command[0]){
                      case "registrar":
                          return registrarPadre(command[1],command[2],command[3],command[4],command[5]);
                      case "borrar":
                          return borrarPadre(command[1],command[2]);
                      case "modificar":
                          return modificarPadre(command[1],command[2],command[3],command[4],command[5],command[6]);
                      case "get":
                          return getNino(command[1]);
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

          
        public String getNino(String clave){
            try {
                BaseDatos conexion = new BaseDatos();
                
                String query = "SELECT nino FROM padres where clave = ?";
                PreparedStatement consulta = conexion.getConnection().prepareStatement(query);
                consulta.setString(1, clave);
                
                ResultSet res = consulta.executeQuery();
                
                if(res.next()){
                    return res.getString("key");  
                }
            } catch (SQLException ex) {
                Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            return "No encontrado";
            
        }

          public String autentificarPadre(String IDPadre, String password,DataOutputStream writer ){
           
            try {
                BaseDatos conexion = new BaseDatos();
                
                String query = "SELECT * FROM padres where padre = ? ";
                PreparedStatement consulta = conexion.getConnection().prepareStatement(query);
                consulta.setString(1, IDPadre);

                ResultSet res = consulta.executeQuery();
                System.out.println(query);
                System.out.println(consulta.toString());
                
                String pw = cifrarPass(password);
                
                while(res.next()){
                    if(res.getString("pass").equals(pw)){
                        isFather=true;
                        String respuesta = res.getString("nino")+" "+res.getString("key");
                        try {
                            writer.writeUTF(respuesta);
                        } catch (IOException ex) {
                            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
                
                
                } catch (SQLException ex) {
                Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
            }
            return "fin";
            
          }


          public String autentificarAdmin(String usuario, String pass){
              if(usuario.equals("administrador") && pass.equals("prometheus")){
                  isAdmin=true;
                  return "Bienvenido administrador";
              }
              System.out.println(usuario.equals("administrador"));
 
              return "No tiene privilegios";
          }

          public String registrarPadre(String padre, String pass, String nino, String clave, String apodo){
              try {

                  BaseDatos conexion = new BaseDatos();
                  Statement estatuto = conexion.getConnection().createStatement();
                  String passwordCifrada = cifrarPass(pass);
                  estatuto.executeUpdate("INSERT INTO padres VALUES ('"+nino+"', '"+padre+"', '"+clave+"', '"+passwordCifrada+"', '"+apodo+"');");
                  System.out.println(estatuto.toString());
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

          public String borrarPadre(String padre,String nino){
          
            try {
                BaseDatos conexion = new BaseDatos();
                String query ="DELETE FROM padres WHERE padre = ? AND nino = ?";
                
                PreparedStatement consulta = conexion.getConnection().prepareStatement(query);
                consulta.setString(1, padre);
                consulta.setString(2, nino);
                System.out.println(consulta.toString());
                consulta.executeUpdate();
                
                conexion.desconectar();
                
                return "Padre borrado correctamente";
            } catch (SQLException ex) {
                Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
                return "No se pudo padre";
            }
          }


          public String modificarPadre(String padreID, String pass, String nino, String clave, String nuevoIDnino,String apodo){
            try {
                BaseDatos conexion = new BaseDatos();
                String query;
                PreparedStatement consulta = null;
                 
                if(!pass.equals("") || !clave.equals("") || !nuevoIDnino.equals("")){
                   if(!clave.equals("")){
                          query= "UPDATE padres SET  `key` =  ? WHERE padre = ? AND nino= ? ";
                          consulta = conexion.getConnection().prepareStatement(query);
                          consulta.setString(1, clave);
                          consulta.setString(2,padreID);
                          consulta.setString(3,nino);
                          consulta.executeUpdate(); 
                   }
                   if(!nuevoIDnino.equals("")){
                          query= "UPDATE padres SET  `nino` =  ? WHERE padre = ? AND nino= ? ";
                          consulta = conexion.getConnection().prepareStatement(query);
                          consulta.setString(1, nuevoIDnino);
                          consulta.setString(2,padreID);
                          consulta.setString(3,nino);
                          consulta.executeUpdate(); 
                   }
                   if(!pass.equals("")){
                          query= "UPDATE padres SET  `pass` =  ? WHERE padre = ? AND nino= ? ";
                          consulta = conexion.getConnection().prepareStatement(query);
                          consulta.setString(1,clave);
                          consulta.setString(2,padreID);
                          consulta.setString(3,nino);
                          consulta.executeUpdate(); 
                   }
                   if(!pass.equals("")){
                          query= "UPDATE padres SET  `apodo` =  ? WHERE padre = ? AND nino= ? ";
                          consulta = conexion.getConnection().prepareStatement(query);
                          consulta.setString(1,clave);
                          consulta.setString(2,padreID);
                          consulta.setString(3,apodo);
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