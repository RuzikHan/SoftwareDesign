package gate;

import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;
import utils.Response;

import java.sql.Connection;
import java.time.LocalDateTime;

public class GateHandler implements HttpHandler {
    Connection connection;

    public GateHandler(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void handleHttpRequest(HttpRequest httpRequest, HttpResponse httpResponse, HttpControl httpControl) throws Exception {
        String request = httpRequest.uri();
        String sResponse;
        try {
            int user_id = Integer.parseInt(httpRequest.queryParam("user_id"));
            GateCommand gateCommand = new GateCommandImpl(connection);
            if (request.startsWith("/enter-center")) {
                Response response = gateCommand.enterCenter(user_id, LocalDateTime.now());
                sResponse = response.comment;
            } else if (request.startsWith("/exit-center")) {
                Response response = gateCommand.exitCenter(user_id, LocalDateTime.now());
                sResponse = response.comment;
            } else {
                sResponse = "Error: incorrect command";
            }
        } catch (NumberFormatException e) {
            sResponse = "Error: user id must be a number";
        }
        httpResponse.content(sResponse).end();
    }
}
