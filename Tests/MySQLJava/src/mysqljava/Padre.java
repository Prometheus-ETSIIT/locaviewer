

package mysqljava;

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
public class Padre {
    private String ID=null;
    private int IDnino=0;
    private String key=null;
    
    private Padre(String padre, String password, int nino){
        try {
            BaseDatos conexion = new BaseDatos();
            
            String query = "SELECT * FROM padres where padre = ? and nino= ? ";
            PreparedStatement consulta = conexion.getConnection().prepareStatement(query);
            consulta.setString(1, padre);
            consulta.setInt(2, nino);
            ResultSet res = consulta.executeQuery();
            
            if(res.next()){
                if(res.getString("pass").equals(password)){
                    ID=padre;
                    IDnino=nino;
                    key = res.getString("key");
                }               
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(MySQLJava.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    Padre (String padre, String pass, int nino, String clave){
        try {
            ID=padre;
            IDnino=nino;
            key=clave;
            
            BaseDatos conexion = new BaseDatos();
            Statement estatuto = conexion.getConnection().createStatement();
            String passwordCifrada = cifrarPass(pass);
            estatuto.executeUpdate("INSERT INTO padres VALUES ('"+nino+"', '"+padre+"', '"+clave+"', '"+passwordCifrada+"');");
        } catch (SQLException ex) {
            Logger.getLogger(Padre.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private String cifrarPass(String pass){
        MessageDigest mDigest = null;
        try {
            mDigest = MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Padre.class.getName()).log(Level.SEVERE, null, ex);
        }
        byte[] result = mDigest.digest(pass.getBytes());
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < result.length; i++) {
            sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
        }
         
        return sb.toString();
    }
    
    public Padre getInstanceOf(String padre, String pass,int nino){
        Padre p = new Padre(padre,pass,nino);
        if(p.getID()==null){
            return null;
        }
        else{
            return p;
        }
    }
    
    
    public String getID(){
        return ID;
    }
    
    public int getIDnino(){
        return IDnino;
    }
    
    public String getKey(){
        return key;
    }
    
}
