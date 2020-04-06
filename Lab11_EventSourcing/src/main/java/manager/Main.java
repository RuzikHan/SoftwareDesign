package manager;

import database.DBConnection;
import database.DBConnectionImpl;
import org.webbitserver.WebServer;
import org.webbitserver.WebServers;

import java.sql.Connection;
import java.util.concurrent.ExecutionException;

public class Main {
    public static void main(String[] args) {
        DBConnection dbConnection = new DBConnectionImpl();
        Connection connection = dbConnection.getConnection();
        ManagerHandler managerHandler = new ManagerHandler(connection);
        WebServer webServer = WebServers.createWebServer(8080)
                .add("/create-user", managerHandler)
                .add("/renew-subscription", managerHandler)
                .add("/user-info", managerHandler);
        try {
            webServer.start().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}
