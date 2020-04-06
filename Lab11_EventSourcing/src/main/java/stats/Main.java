package stats;

import database.DBConnection;
import database.DBConnectionImpl;
import org.webbitserver.WebServer;
import org.webbitserver.WebServers;

import java.io.IOException;
import java.sql.Connection;
import java.util.concurrent.ExecutionException;

public class Main {
    public static void main(String[] args) throws IOException {
        DBConnection dbConnection = new DBConnectionImpl();
        Connection connection = dbConnection.getConnection();
        StatsHandler statsHandler = new StatsHandler(new StateImpl(connection));
        WebServer webServer = WebServers.createWebServer(8082)
                .add("/day-stats", statsHandler)
                .add("/all-average-frequency", statsHandler)
                .add("/user-frequency", statsHandler)
                .add("/all-average-duration", statsHandler)
                .add("/user-average-duration", statsHandler)
                .add("/add-visit", statsHandler);
        try {
            webServer.start().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}
