package fil.coo;

import java.sql.Connection;
import java.sql.DriverManager;

public class MyConnection
{
    private static MyConnection ourInstance = new MyConnection();

    public static MyConnection getInstance()
    {
        return ourInstance;
    }

    public Connection getConnection(){
        try
        {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            // On utilise la base de Alexandre car il manque des petites choses dans les notres.
            Connection c = DriverManager.getConnection("jdbc:oracle:thin:@oracle.fil.univ-lille1.fr:1521:filora", "devassine", "Alex09011996");
            return c;
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public void close(Connection c){
        try{
            c.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private MyConnection()
    {
    }
}
