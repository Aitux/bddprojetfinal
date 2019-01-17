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
            Connection c = DriverManager.getConnection("jdbc:oracle:thin:@oracle.fil.univ-lille1.fr:1521:filora" , "USERNAME", "PASSWORD" );
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
