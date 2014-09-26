/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package xmlcam;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.jdom2.Document;         // |
import org.jdom2.Element;          // |\ Librerías
import org.jdom2.JDOMException;    // |/ JDOM
import org.jdom2.input.SAXBuilder; // |

/**
 *
 * @author iblancasa
 */
public class XMLCam {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        cargarXML();
    }
    
    private static void cargarXML(){
        //Se crea un SAXBuilder para poder parsear el archivo
            SAXBuilder builder = new SAXBuilder();
            File xmlFile = new File( "posicionesCamaras.xml" );
            try
            {
                //Se crea el documento a traves del archivo
                Document document = (Document) builder.build( xmlFile );

                //Se obtiene la raiz
                Element rootNode = document.getRootElement();

                //Se obtiene la lista de hijos de la raiz
                List camaras = rootNode.getChildren();

                for (Object camara : camaras) {
                    //Se obtiene el elemento 'campo'
                    Element campo = (Element) camara;

                    String ID = campo.getChildTextTrim("ID");
                    String posicionX = campo.getChildTextTrim("posicionX");
                    String posicionY = campo.getChildTextTrim("posicionY");
                    String habitacion = campo.getChildTextTrim("nombrehabitacion");
                    
                    System.out.println("ID: " +ID);
                    System.out.println("\tPosición X: " +posicionX);
                    System.out.println("\tPosición Y: " +posicionY);
                    System.out.println("\tNombre habitación: " +habitacion);
                }

            }catch ( IOException | JDOMException io ) {
                System.out.println( io.getMessage() );
            }

    }
    
}
