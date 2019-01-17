package fil.coo;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;

import javax.xml.transform.Result;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.Date;

public class PDFBuilder
{
    private static String FILE = "/home/m1miage/vandeputte/Desktop/MiageBook.pdf";
    private static Font title = new Font(Font.FontFamily.TIMES_ROMAN, 42,
            Font.BOLD);

    private static Font redFont = new Font(Font.FontFamily.TIMES_ROMAN, 12,
            Font.NORMAL, BaseColor.RED);
    private static Font legend = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.NORMAL, BaseColor.GRAY);

    private static Font smallBold = new Font(Font.FontFamily.TIMES_ROMAN, 12,
            Font.BOLD);

    private static String clobToString(Clob data)
    {
        StringBuilder sb = new StringBuilder();
        try
        {
            Reader reader = data.getCharacterStream();
            BufferedReader br = new BufferedReader(reader);

            String line;
            while (null != (line = br.readLine()))
            {
                sb.append(line);
            }
            br.close();
        } catch (SQLException | IOException e)
        {
            // do smth crazy :)
            e.printStackTrace();
        }
        return sb.toString();
    }

    public static void build()
    {

        try
        {
            Document doc = new Document();
            PdfWriter.getInstance(doc, new FileOutputStream(FILE));
            doc.open();
            addMetaData(doc);
            addTitlePage(doc);
            Connection c = MyConnection.getInstance().getConnection();
            PreparedStatement stmt = c.prepareStatement("select photoid, (cv).getClobVal() as cv, rang from candidat where cv is not null AND PHOTOID is not null");
            PreparedStatement id = c.prepareStatement("SELECT id from candidat where nom=?");
            PreparedStatement note = c.prepareStatement("SELECT id_epreuve, note from notes where ID_CANDIDAT = ?");
            PreparedStatement voeux = c.prepareStatement("SELECT ide from voeux where idc=?");
            PreparedStatement admis = c.prepareStatement("select voeux.IDE, REFCANDIDAT, (select min(RANG) from preadmin where refecole = voeux.IDE) as min_rang, (select max(RANG) from preadmin where refecole = voeux.IDE) as max_rang\n" +
                    "from preadmin " +
                    "right join voeux on preadmin.REFCANDIDAT=voeux.IDC " +
                    "where IDC = ?");
            ResultSet rs = stmt.executeQuery();
            while (rs.next())
            {
                int idcand = 0;

                String str = clobToString(rs.getClob(2));
                InputStream stream = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));

                CV cv = XMLParser.parse(stream);
                Paragraph page = new Paragraph();
                Blob imageBlob = rs.getBlob(1);
                byte[] imageBytes = imageBlob.getBytes(1, (int) imageBlob.length());
                Image image = Image.getInstance(imageBytes);
                image.scaleAbsolute(125, 125);
                image.setAlignment(Element.ALIGN_RIGHT);
                page.add(image);
                Paragraph p = new Paragraph("Rang " + rs.getInt(3));
                page.add(p);
                p = printCVPage(cv);
                page.add(p);

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
                p = new Paragraph("Epreuve | Note");
                page.add(p);
                while (rs2.next())
                {
                    p = new Paragraph(rs2.getInt(1) + " | " + rs2.getDouble(2));
                    page.add(p);
                }

                voeux.setInt(1, idcand);
                ResultSet rs3 = voeux.executeQuery();
                addEmptyLine(page, 2);
                p = new Paragraph("Voeux (par ordre de priorité):");
                page.add(p);
                while (rs3.next())
                {
                    p = new Paragraph("Ecole " + rs3.getInt(1));
                    page.add(p);
                }

                admis.setInt(1,idcand);
                System.out.println(idcand);
                addEmptyLine(page, 2);
                page.add("Admission: ");
                page.add(admis(admis.executeQuery()));

                try
                {
                    doc.add(page);
                    doc.newPage();
                } catch (DocumentException e)
                {
                    e.printStackTrace();
                }
            }
            MyConnection.getInstance().close(c);
            doc.close();
            System.out.println("[DONE] PDF PROPERLY GENERATED AT {"+FILE+"}");
        } catch (Exception e)
        {
            System.out.println("[FAILURE] SOMETHING WENT WRONG");
            e.printStackTrace();
        }
    }

    private static Paragraph admis(ResultSet rs){
        try
        {
            Paragraph c = new Paragraph();
            Paragraph p = null;
            while(rs.next())
            {
                p = new Paragraph("Ecole " + rs.getInt(1));
                if (rs.getInt(2) == 0)
                    p.add(" | Non-Admis");
                else
                    p.add(" | Admis");

                p.add(" | Premier admis dans l'école: " + rs.getInt(3) + " | Dernier admis dans l'école: " + rs.getInt(4));
                c.add(p);
            }
            return c;
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    private static Paragraph printCVPage(CV c)
    {
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
        p = new Paragraph("Technologies connues: ", smallBold);
        for (String str :
                c.getTechnos())
        {
            p.add(str + " | ");
        }
        page.add(p);
        return page;
    }

    private static void addMetaData(Document document)
    {
        document.addTitle("Miage's CVs Book");
        document.addSubject("TP de conclusion de l'UE BDD");
        document.addKeywords("Java, PDF, iText, BDD, MIAGE");
        document.addAuthor("Simon Vandeputte");
        document.addCreator("Simon Vandeputte");
    }

    private static void addTitlePage(Document document)
            throws DocumentException
    {
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
        preface.add(new Paragraph("Les personnes n'ayant pas leur CV et leur photo de disponible n'apparaissent pas dans le book !", redFont));
        preface.add(new Paragraph(
                "",
                smallBold));

        addEmptyLine(preface, 8);
        document.add(preface);
        // Start a new page
        document.newPage();
    }

    private static void addEmptyLine(Paragraph paragraph, int number)
    {
        for (int i = 0; i < number; i++)
        {
            paragraph.add(new Paragraph(" "));
        }
    }
}
