/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mysqljava;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
