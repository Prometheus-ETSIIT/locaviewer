/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package centroinfantil;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;         // |
import org.jdom2.Document;          // |\ Librerías
import org.jdom2.Element;    // |/ JDOM
import org.jdom2.JDOMException; // |
import org.jdom2.input.SAXBuilder;
/**
 *
 * @author iblancasa
 */
public class PasoLista {
    
    /**
     *
     * @return
     */
    static public ArrayList< ArrayList<String> > cargarXml(){
        //Se crea un SAXBuilder para poder parsear el archivo
        SAXBuilder builder = new SAXBuilder();
        File xmlFile = new File( "historial.xml" );
        ArrayList<ArrayList<String> > salida = new ArrayList<>();
        
        
        try
        {
            ArrayList<String> nino = new ArrayList<>();
            String cadena;
            
            
            //Se crea el documento a traves del archivo
            Document document = (Document) builder.build( xmlFile );

            //Se obtiene la raiz 'tables'
            Element rootNode = document.getRootElement();

            //Se obtiene la lista de hijos de la raiz 'tables'
            List list = rootNode.getChildren( "child" );
            
            System.out.println(list.size());
            //Se recorre la lista de hijos de 'tables'
            for ( int i = 0; i < list.size(); i++ )
            {
                //Se obtiene el elemento 'tabla'
                Element tabla = (Element) list.get(i);

                //Se obtiene la lista de hijos del tag 'tabla'
                List lista_campos = tabla.getChildren();

                //Se recorre la lista de campos
                for ( int j = 0; j < lista_campos.size()-1; j++ )
                {
                    //Se obtiene el elemento 'campo'
                    Element campo = (Element)lista_campos.get( j );
                    cadena = campo.getText();
                    nino.add(cadena);
                }
                
                
                Element campo = (Element)lista_campos.get(lista_campos.size()-1);
                nino.add(campo.getChildTextTrim("fecha"));
                nino.add(campo.getChildTextTrim("sala"));
                nino.add(campo.getChildTextTrim("camara"));
                nino.add(campo.getChildTextTrim("posicionX"));
                nino.add(campo.getChildTextTrim("posicionY"));
              
                salida.add(nino);
            }
        }catch ( IOException | JDOMException io ) {
            System.out.println( io.getMessage() );
        }
        return salida;
    }
}
