/**
 * A class to open a connection to mysql server. If you have to make any changes to credentials,
 * do so in Configs.java class.
 */

package models.db;

import java.sql.*;

public class DBConnection {
    public Configs configs = new Configs();
    private final String url = "jdbc:mysql://" + configs.getHostName() + ":" + configs.getPort() + "/" + configs.getDbInstanceName();
    private final String username = configs.getUsername();
    private final String password = configs.getPassword();
    private Connection conn;

    /**
     * Method to open a connection to mysql server.
     * @return conn - the connection to mysql server.
     */
    public Connection getDBConnection() {
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(url, username, password);
        }
        catch(SQLException e) {
            System.out.println(e.getMessage());
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }
}
