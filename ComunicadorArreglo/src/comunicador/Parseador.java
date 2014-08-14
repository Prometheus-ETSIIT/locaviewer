package comunicador;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Parseador {

    String fichero;
    ArrayList<CamaraPos> posiciones;
    File archivo;
    
    public Parseador(String file){
            fichero=file;
            posiciones = new ArrayList<>();

    }
    
    
    public ArrayList<CamaraPos> parse(){
        try {
            archivo = new File(fichero);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(archivo);
            doc.getDocumentElement().normalize();



            NodeList nodes = doc.getElementsByTagName("camara");

            CamaraPos posicion;
            int x,y;
            
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);

                if (node.getNodeType() == Node.ELEMENT_NODE) {

                    Element element = (Element) node;
                    String ID = getValue("ID", element);
                    String posicionX =  getValue("posicionX", element);
                    String posicionY = getValue("posicionY", element);
                    System.out.println("oasdad");
                    x=Integer.parseInt(posicionX);
                    y=Integer.parseInt(posicionY);
                    
                    posicion = new CamaraPos(x,y,ID);
                    
                    posiciones.add(posicion);
                }
            }
        } catch (IOException | ParserConfigurationException | SAXException ex) {

        }
    
        return posiciones;
    }

    private String getValue(String tag, Element element) {
            NodeList nodes = element.getElementsByTagName(tag).item(0).getChildNodes();
            Node node = (Node) nodes.item(0);
            return node.getNodeValue();
    }
}