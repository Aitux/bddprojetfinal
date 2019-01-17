package fil.coo;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.Date;

public class Main {

    private static String FILE = "/home/aitux/Bureau/MiageBook.pdf";
    private static Font title = new Font(Font.FontFamily.TIMES_ROMAN, 42,
            Font.BOLD);
    private static Font catFont = new Font(Font.FontFamily.TIMES_ROMAN, 18,
            Font.BOLD);
    private static Font redFont = new Font(Font.FontFamily.TIMES_ROMAN, 12,
            Font.NORMAL, BaseColor.RED);
    private static Font legend = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.NORMAL, BaseColor.GRAY);
    private static Font subFont = new Font(Font.FontFamily.TIMES_ROMAN, 16,
            Font.BOLD);
    private static Font smallBold = new Font(Font.FontFamily.TIMES_ROMAN, 12,
            Font.BOLD);

    private static String clobToString(Clob data) {
        StringBuilder sb = new StringBuilder();
        try {
            Reader reader = data.getCharacterStream();
            BufferedReader br = new BufferedReader(reader);

            String line;
            while (null != (line = br.readLine())) {
                sb.append(line);
            }
            br.close();
        } catch (SQLException e) {
            // handle this exception
        } catch (IOException e) {
            // handle this exception
        }
        return sb.toString();
    }

    public static void main(String[] args) {

        try {
            Document doc = new Document();
            PdfWriter.getInstance(doc, new FileOutputStream(FILE));
            doc.open();
            addMetaData(doc);
            addTitlePage(doc);
            Class.forName("oracle.jdbc.driver.OracleDriver");
            Connection c = DriverManager.getConnection("jdbc:oracle:thin:@oracle.fil.univ-lille1.fr:1521:filora", "vandeputte", "e94a0dc724");
            PreparedStatement stmt = c.prepareStatement("select (cv).getClobVal() as cv from candidats where cv is not null");
            PreparedStatement id = c.prepareStatement("SELECT idcand from candidats where nom=?");
            PreparedStatement note = c.prepareStatement("SELECT id_epreuve, note from notes where idcand = ?");
            PreparedStatement voeux = c.prepareStatement("SELECT ide from voeux where idc=?");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int idcand = 0;
                String str = clobToString(rs.getClob(1));
                InputStream stream = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));
                CV cv = XMLParser.parse(stream);
                Paragraph page = printCVPage(cv);
                String nom = cv.getNom().toLowerCase();
                char[] stringArray = nom.trim().toCharArray();
                stringArray[0] = Character.toUpperCase(stringArray[0]);
                nom = new String(stringArray);
                cv.setNom(nom);
                id.setString(1, cv.getNom());
                ResultSet rs1 = id.executeQuery();
                if (rs1.next())
                    idcand = rs1.getInt(1);
                note.setInt(1, idcand);
                ResultSet rs2 = note.executeQuery();
                Paragraph p = new Paragraph("Epreuve | Note");
                page.add(p);
                while(rs2.next()) {
                    p = new Paragraph(rs2.getInt(1) + " | " + rs2.getDouble(2));
                    page.add(p);
                }

                voeux.setInt(1,idcand);
                ResultSet rs3 = voeux.executeQuery();
                addEmptyLine(page,2);
                p = new Paragraph("Voeux (par ordre de priorité):");
                page.add(p);
                while(rs3.next()){
                     p = new Paragraph("Ecole " + rs3.getInt(1));
                     page.add(p);
                }

                try {
                    doc.add(page);
                    doc.newPage();
                } catch (DocumentException e) {
                    e.printStackTrace();
                }
            }
            doc.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Paragraph printCVPage(CV c) {
        Paragraph page = new Paragraph();
        addEmptyLine(page, 1);
        Paragraph p = new Paragraph(c.getPrenom() + " " + c.getNom(), title);
        p.setAlignment(Element.ALIGN_CENTER);
        page.add(p);
        p = new Paragraph(c.getAge() + " ans", smallBold);
        page.add(p);
        addEmptyLine(page, 2);
        p = new Paragraph("Date de naissance: " + c.getDob(), smallBold);
        page.add(p);
        p = new Paragraph("adresse: " + c.getAdresse(), smallBold);
        page.add(p);
        p = new Paragraph("tel: " + c.getTel(), smallBold);
        page.add(p);
        addEmptyLine(page, 2);
        p = new Paragraph("Technolgies connues: ", smallBold);
        for (String str :
                c.getTechnos()) {
            p.add(str + " | ");
        }
        page.add(p);
        return page;


    }

    private static void addMetaData(Document document) {
        document.addTitle("Miage's CVs Book");
        document.addSubject("TP de conclusion de l'UE BDD");
        document.addKeywords("Java, PDF, iText, BDD, MIAGE");
        document.addAuthor("Simon Vandeputte");
        document.addCreator("Simon Vandeputte");
    }

    private static void addTitlePage(Document document)
            throws DocumentException {
        Paragraph preface = new Paragraph();
        // We add one empty line
        addEmptyLine(preface, 1);
        // Lets write a big header
        Paragraph p = new Paragraph("Miage's CVs Book", title);
        p.setAlignment(Element.ALIGN_CENTER);
        preface.add(p);

        addEmptyLine(preface, 1);
        // Will create: Report generated by: _name, _date
        preface.add(new Paragraph(
                "PDF généré par Simon Vandeputte, le " + new Date().toLocaleString(), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                legend));
        addEmptyLine(preface, 3);
        preface.add(new Paragraph(
                "",
                smallBold));

        addEmptyLine(preface, 8);
        document.add(preface);
        // Start a new page
        document.newPage();
    }

    private static void addEmptyLine(Paragraph paragraph, int number) {
        for (int i = 0; i < number; i++) {
            paragraph.add(new Paragraph(" "));
        }
    }
}
