package fil.coo;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.Date;

public class PDFBuilder
{

    private static String FILE = "/home/m1miage/pruvost/Documents/MiageBook.pdf";
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
            PreparedStatement stmt = c.prepareStatement("select photoid, (cv).getClobVal() as cv from candidat where cv is not null AND PHOTOID is not null");
            PreparedStatement id = c.prepareStatement("SELECT id from candidat where nom=?");
            PreparedStatement note = c.prepareStatement("SELECT id_epreuve, note from notes where ID_CANDIDAT = ?");
            PreparedStatement voeux = c.prepareStatement("SELECT ide from voeux where idc=?");

            ResultSet rs = stmt.executeQuery();
            while (rs.next())
            {
                int idcand = 0;
                String str = clobToString(rs.getClob(2));
                InputStream stream = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));
                CV cv = XMLParser.parse(stream);
                Paragraph page = new Paragraph();

                page.add(getImageBlob(rs.getBlob(1)));
                page.add(printCVPage(cv));

                String nom = cv.getNom().toLowerCase();
                char[] stringArray = nom.trim().toCharArray();
                stringArray[0] = Character.toUpperCase(stringArray[0]);

                cv.setNom(new String(stringArray));
                id.setString(1, cv.getNom());

                ResultSet rs1 = id.executeQuery();
                if (rs1.next())
                    idcand = rs1.getInt(1);

                note.setInt(1, idcand);
                ResultSet rs2 = note.executeQuery();

                page.add(new Paragraph("Epreuve | Note"));

                while (rs2.next())
                    page.add(new Paragraph(rs2.getInt(1) + " | " + rs2.getDouble(2)));

                voeux.setInt(1, idcand);
                ResultSet rs3 = voeux.executeQuery();
                addEmptyLine(page, 2);

                page.add(new Paragraph("Voeux (par ordre de priorité):"));
                while (rs3.next())
                    page.add(new Paragraph("Ecole " + rs3.getInt(1)));

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
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private static Image getImageBlob(Blob blob) throws IOException, BadElementException, SQLException
    {
        Blob imageBlob = blob;
        byte[] imageBytes = imageBlob.getBytes(1, (int) imageBlob.length());
        Image image = Image.getInstance(imageBytes);
        image.scaleAbsolute(125, 125);
        image.setAlignment(Element.ALIGN_RIGHT);
        return image;
    }


    private static Paragraph printCVPage(CV c)
    {
        Paragraph page = new Paragraph();
        addEmptyLine(page, 1);
        Paragraph p = new Paragraph(c.getPrenom() + " " + c.getNom(), title);
        p.setAlignment(Element.ALIGN_CENTER);
        page.add(p);
        page.add( new Paragraph(c.getAge() + " ans", smallBold));
        addEmptyLine(page, 2);
        page.add(new Paragraph("Date de naissance: " + c.getDob(), smallBold));
        page.add(new Paragraph("adresse: " + c.getAdresse(), smallBold));
        page.add(new Paragraph("tel: " + c.getTel(), smallBold));
        addEmptyLine(page, 2);
        p = new Paragraph("Technologies connues: ", smallBold);
        for (String str : c.getTechnos())
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
            paragraph.add(new Paragraph(" "));
    }
}
