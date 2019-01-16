import java.util.ArrayList;
import java.util.List;

public class CV {
    private String nom;
    private String prenom;
    private int age;
    private String dob;
    private String adresse;
    private List<String> technos = new ArrayList<>();

    public CV(String nom, String prenom, int age, String dob, String adresse) {
        this.nom = nom;
        this.prenom = prenom;
        this.age = age;
        this.dob = dob;
        this.adresse = adresse;
    }

    public CV() {
    }

    public void addTechnos(String str) {
        technos.add(str);
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }
}
