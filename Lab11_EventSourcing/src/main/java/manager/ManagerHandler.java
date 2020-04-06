package manager;

import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;
import utils.Response;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.Arrays;

public class ManagerHandler implements HttpHandler {
    Connection connection;

    public ManagerHandler(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void handleHttpRequest(HttpRequest httpRequest, HttpResponse httpResponse, HttpControl httpControl) throws Exception {
        String request = httpRequest.uri();
        String sResponse;
        try {
            int user_id = Integer.parseInt(httpRequest.queryParam("user_id"));
            if (request.startsWith("/create-user")) {
                ManagerCommand managerCommand = new ManagerCommandImpl(connection);
                Response response = managerCommand.createUser(user_id);
                sResponse = response.comment;
            } else if (request.startsWith("/renew-subscription")) {
                String[] dateParts = httpRequest.queryParam("date").split("-");
                Integer[] date = Arrays.stream(dateParts).map(Integer::parseInt).toArray(Integer[]::new);
                String[] timeParts = httpRequest.queryParam("time").split(":");
                Integer[] time = Arrays.stream(timeParts).map(Integer::parseInt).toArray(Integer[]::new);
                ManagerCommand managerCommand = new ManagerCommandImpl(connection);
                Response response = managerCommand.renewSubscription(user_id, LocalDateTime.of(date[0], date[1], date[2], time[0], time[1]));
                sResponse = response.comment;
            } else if (request.startsWith("/user-info")) {
                ManagerQuery managerQuery = new ManagerQueryImpl(connection);
                User user = managerQuery.getUser(user_id);
                if (user != null) {
                    sResponse = "User { id = " + user.id + ", subscriptionEnd = " + user.endSubscription.toString() + " }";
                } else {
                    sResponse = "User with id = " + user_id + " doesn't exists";
                }
            } else {
                sResponse = "Error: incorrect command/query";
            }
        } catch (NumberFormatException e) {
            sResponse = "Error: user id must be a number";
        }
        httpResponse.content(sResponse).end();
    }
}
