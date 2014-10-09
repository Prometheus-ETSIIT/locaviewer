

package mysqljava;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author iblancasa
 */
public class Facade {
    boolean isAdmin;
    private DataInputStream reader;
    
    private String padre=null;
    private int ninoID=0;
    private String key=null;
    
    
    public Facade(InputStream inStream){
        isAdmin=false;
        reader = new DataInputStream(inStream);
    
    }
    
    /**
     * Es una consola de comandos
     */
    public void ejecutar(){
        String respuesta;
        while(true){
            try {
                String comando = reader.readUTF();
                if(!comando.equals("exit")){
                    String [] separado = comando.split(" ");
                    
                    if(separado[0].equals("autentificar") || padre==null){
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
                                break;
                        }
                    }
                    else{
                       respuesta = commands(separado);
                    }
                }
                else{
                    break;
                }
            } catch (IOException ex) {
                Logger.getLogger(Facade.class.getName()).log(Level.SEVERE, null, ex);
            }
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
                    padre=IDPadre;
                    ninoID=nino;
                    key = res.getString("key");
                }
                return "Bienvenido "+padre;
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(MySQLJava.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "La autentificación fallo";
    }
    
    
    public String autentificarAdmin(String usuario, String pass){
        if(usuario.equals("administrador") && pass.equals(cifrarPass("prometheus"))){
            isAdmin=true;
            return "Bienvenido administrador";
        }
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
            Logger.getLogger(Facade.class.getName()).log(Level.SEVERE, null, ex);
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
            String query ="DELETE FROM padres  WHERE padre = ? AND padre = ?";
     
            PreparedStatement consulta = conexion.getConnection().prepareStatement(query);
            consulta.setString(1, padre);
            consulta.setInt(2, nino);
            consulta.executeQuery();
            
            conexion.desconectar();
        } catch (SQLException ex) {
            return "No se pudo borrar al padre";
        }
        return "Padre borrado correctamente";
    }
    
    
    public String modificarPadre(String padreID, String pass, int nino, String clave, int nuevoIDnino){
      try {
            
            BaseDatos conexion = new BaseDatos();
            String query = "UPDATE padres SET  pass = ?, nino= ?, clave = ?  WHERE  nino = ? AND padre = ?;";

            PreparedStatement consulta = conexion.getConnection().prepareStatement(query);
            
            consulta.setString(1, pass);
            consulta.setInt(2, nuevoIDnino);
            consulta.setString(3, clave);
            consulta.setInt(4, nino);
            consulta.setString(5, padreID);
            
            consulta.executeQuery();
            
            conexion.desconectar();
        } catch (SQLException ex) {
            return "No se pudo modificar al padre";
        }
        return "Padre modificado";
    
    }
}
