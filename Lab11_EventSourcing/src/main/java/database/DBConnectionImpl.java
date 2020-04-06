package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnectionImpl implements DBConnection {
    String login;
    String password;

    public DBConnectionImpl() {
        login = "rzkhn";
        password = "12345";
    }

    @Override
    public Connection getConnection() {
        String url = "jdbc:postgresql:eventsourcing";
        try {
            return DriverManager.getConnection(url, login, password);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
