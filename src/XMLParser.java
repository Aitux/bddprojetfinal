import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class XMLParser {
    private static void printNote(NodeList nodeList) {

        for (int count = 0; count < nodeList.getLength(); count++) {

            Node tempNode = nodeList.item(count);

            // make sure it's element node.
            if (tempNode.getNodeType() == Node.ELEMENT_NODE) {

                // get node name and value
                System.out.println("\nNode Name =" + tempNode.getNodeName() + " [OPEN]");
                System.out.println("Node Value =" + tempNode.getTextContent());

                if (tempNode.hasAttributes()) {

                    // get attributes names and values
                    NamedNodeMap nodeMap = tempNode.getAttributes();

                    for (int i = 0; i < nodeMap.getLength(); i++) {

                        Node node = nodeMap.item(i);
                        System.out.println("attr name : " + node.getNodeName());
                        System.out.println("attr value : " + node.getNodeValue());

                    }

                }

                if (tempNode.hasChildNodes()) {

                    // loop again if has child nodes
                    printNote(tempNode.getChildNodes());

                }

                System.out.println("Node Name =" + tempNode.getNodeName() + " [CLOSE]");

            }

        }

    }

    public static String readFile(String path, Charset encoding)
            throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }
    public static void main(String[] args) {

        try {

            File file = new File("/home/aitux/Bureau/cvoce.xml");
            InputStream stream = new ByteArrayInputStream(readFile("/home/aitux/Bureau/cvoce.xml", StandardCharsets.UTF_8).getBytes(StandardCharsets.UTF_8));
            parse(stream);


        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    public static CV parse(InputStream xml) {
        CV c = new CV();
        try {
            DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder();

            Document doc = dBuilder.parse(xml);

            System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
            NodeList nl = doc.getElementsByTagName("donnees-personnelles");

            for(int i = 0 ; i < nl.getLength(); i++){
                Node node = nl.item(i);
                if(node.getNodeType() == Node.ELEMENT_NODE){
                    Element e = (Element) node;
                    c.setNom(e.getElementsByTagName("nom").item(0).getTextContent());
                    c.setAge(Integer.parseInt(e.getElementsByTagName("age").item(0).getTextContent()));
                    c.setPrenom(e.getElementsByTagName("prenom").item(0).getTextContent());
                    c.setTel(e.getElementsByTagName("telephone").item(0).getTextContent());
                    String adresse = e.getElementsByTagName("adresse").item(0).getTextContent();
                    adresse += " " + e.getElementsByTagName("ville").item(0).getTextContent();
                    adresse += ", " + e.getElementsByTagName("ville").item(0).getAttributes().item(0).getTextContent();
                    c.setAdresse(adresse);
                    c.setDob(e.getElementsByTagName("date-de-naissance").item(0).getTextContent());
                }
            }

            nl = doc.getElementsByTagName("environnement-technique");
            for(int i = 0 ; i < nl.getLength(); i++){
                Node node = nl.item(i);
                System.out.println(node.getNodeType() == Node.ELEMENT_NODE);
                if(node.getNodeType() == Node.ELEMENT_NODE){
                    Element e = (Element) node;
                    for(int k = 0 ; k < e.getElementsByTagName("technologie").getLength(); k++){
                        c.addTechnos(e.getElementsByTagName("technologie").item(k).getAttributes().item(0).getTextContent());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return c;
    }

}
