package gate;

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
        GateHandler gateHandler = new GateHandler(connection);
        WebServer webServer = WebServers.createWebServer(8081)
                .add("/enter-center", gateHandler)
                .add("/exit-center", gateHandler);
        try {
            webServer.start().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}
