package fil.coo;

import fil.coo.CV;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

public class XMLParser {

    public static String readFile(String path, Charset encoding)
            throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }


    public static CV parse(InputStream xml) {
        CV c = new CV();
        try {
            DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder();

            Document doc = dBuilder.parse(xml);
            NodeList nl = doc.getElementsByTagName("donnees-personnelles");

            for (int i = 0; i < nl.getLength(); i++) {
                Node node = nl.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element e = (Element) node;
                    c.setNom(e.getElementsByTagName("nom").item(0).getTextContent());
                    c.setAge(Integer.parseInt(e.getElementsByTagName("age").item(0).getTextContent()));
                    c.setPrenom(e.getElementsByTagName("prenom").item(0).getTextContent());
                    c.setTel(e.getElementsByTagName("telephone").item(0).getTextContent());
                    String adresse = e.getElementsByTagName("adresse").item(0).getTextContent();
                    try {
                        adresse += " " + e.getElementsByTagName("ville").item(0).getTextContent();
                        adresse += ", " + e.getElementsByTagName("ville").item(0).getAttributes().item(0).getTextContent();
                    }catch (NullPointerException nu){
                        adresse += " N/A";
                    }
                    c.setAdresse(adresse);
                    c.setDob(e.getElementsByTagName("date-de-naissance").item(0).getTextContent());
                }
            }

            nl = doc.getElementsByTagName("environnement-technique");
            for (int i = 0; i < nl.getLength(); i++) {
                Node node = nl.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element e = (Element) node;
                    for (int k = 0; k < e.getElementsByTagName("technologie").getLength(); k++) {
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
