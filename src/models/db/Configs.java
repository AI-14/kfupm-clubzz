/**
 * A class that stores all the credentials of AWS RDS Mysql cloud database.
 * To configure - write all the details in the contructor.
 * CAUTION: Configure the credentials with caution and do not make them public to avoid
 * any misconduct or misuse.
 * NOTE: When you push it to github, replace all the credentials with empty strings and integer 0.
 */

package models.db;

import java.util.Objects;

public class Configs {
    private String hostName;
    private int port;
    private String dbInstanceName;
    private String username;
    private String password;

    public Configs() {
        //AWS RDS details is with the admin.

        // Details for localhost. Some fields are kept hidden.
        this.hostName = "localhost";
        this.port = 3306;
        this.dbInstanceName = "kcdbs";
        this.username = "root";
        this.password = "";
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getDbInstanceName() {
        return dbInstanceName;
    }

    public void setDbInstanceName(String dbInstanceName) {
        this.dbInstanceName = dbInstanceName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "Configs{" +
                "hostName='" + hostName + '\'' +
                ", port=" + port +
                ", dbInstanceName='" + dbInstanceName + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Configs configs = (Configs) o;
        return port == configs.port &&
                Objects.equals(hostName, configs.hostName) &&
                Objects.equals(dbInstanceName, configs.dbInstanceName) &&
                Objects.equals(username, configs.username) &&
                Objects.equals(password, configs.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hostName, port, dbInstanceName, username, password);
    }
}
