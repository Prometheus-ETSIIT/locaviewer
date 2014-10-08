

package mysqljava;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author iblancasa
 */
public class BaseDatos {
   static String bd = "prueba";
   static String login = "root";
   static String password = "prometheus";
   static String url = "jdbc:mysql://localhost/"+bd;
    Connection connection = null;
    
    public BaseDatos() {
      try{
         //obtenemos el driver de para mysql
         Class.forName("com.mysql.jdbc.Driver");
         //obtenemos la conexión
         connection = DriverManager.getConnection(url,login,password);
 
         if (connection!=null){
            System.out.println("Conectado");
         }
      }
      catch(SQLException e){
         System.out.println(e);
      }catch(ClassNotFoundException e){
         System.out.println(e);
      }catch(Exception e){
         System.out.println(e);
      }
   }
   /**Permite retornar la conexió
     * @return n*/
   public Connection getConnection(){
      return connection;
   }
 
   public void desconectar(){
      connection = null;
   }
}
