package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {
    static String url = "jdbc:mysql://localhost:3306/bookmymovies_db";
    static String username = "root";
    static String password = "toor";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }
}
