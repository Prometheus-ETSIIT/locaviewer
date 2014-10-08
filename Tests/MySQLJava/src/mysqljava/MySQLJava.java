

package mysqljava;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;


public class MySQLJava {


    public static void main(String[] args) {
        try {
            BaseDatos conexion = new BaseDatos();
            
            String query = "SELECT * FROM padres where padre = ? and nino= ? ";
            PreparedStatement consulta = conexion.getConnection().prepareStatement(query);
            consulta.setInt(1, 75572325);
            consulta.setInt(2, 1);
            ResultSet res = consulta.executeQuery();
            
            if(res.next()){
                String padre = res.getString("padre");
                String nino = res.getString("nino");
                String key = res.getString("key");
                String pass = res.getString("pass");
                System.out.println(padre + " "+nino+" "+" "+key+" "+pass);
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(MySQLJava.class.getName()).log(Level.SEVERE, null, ex);
        }
            
    
        
    }
    
}
