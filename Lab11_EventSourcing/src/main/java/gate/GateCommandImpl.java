package gate;

import utils.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.*;
import java.time.LocalDateTime;

public class GateCommandImpl implements GateCommand {
    Connection connection;

    public GateCommandImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Response enterCenter(int id, LocalDateTime time) {
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT event_type from gateevents where event_id = (" +
                    "SELECT max(event_id) from gateevents where user_id=" + id + ");");
            if (resultSet.next()) {
                if (resultSet.getInt("event_type") == 1) {
                    statement.close();
                    return new Response(false, "User with id = " + id + " already inside center");
                }
            }
            resultSet = statement.executeQuery("SELECT * from managerevents where event_id = (" +
                    "SELECT max(event_id) from managerevents where user_id=" + id + ");");
            String comment;
            if (resultSet.next()) {
                LocalDateTime endTime = resultSet.getTimestamp("subscriptionEnd").toLocalDateTime();
                if (time.isBefore(endTime)) {
                    resultSet = statement.executeQuery("SELECT * from events where event_id = (" +
                            "SELECT max(event_id) from events where user_id=" + id + ");");
                    if (resultSet.next()) {
                        int next_event_id = resultSet.getInt("event_id") + 1;
                        statement.executeUpdate("INSERT into events VALUES (" + id + ", " + next_event_id + ");");
                        statement.executeUpdate("INSERT into gateevents VALUES (" + id + ", " + next_event_id +
                                ", 1, '" + Timestamp.valueOf(time) + "');");
                        return new Response(true, "User with id = " + id + " successfully enter the center");
                    } else {
                        comment = "An error occurred while executing command \"enter-center\"";
                    }
                } else {
                    comment = "User with id = " + id + " has an expired subscription";
                }
            } else {
                comment = "User with id = " + id + "doesn't have a subscription";
            }
            statement.close();
            return new Response(false, comment);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new Response(false, "An error occurred while executing command \"enter-center\"");
    }

    @Override
    public Response exitCenter(int id, LocalDateTime time) {
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT event_type, event_time from gateevents where event_id = (" +
                    "SELECT max(event_id) from gateevents where user_id=" + id + ");");
            String comment;
            if (resultSet.next()) {
                if (resultSet.getInt("event_type") == 1) {
                    LocalDateTime startTime = resultSet.getTimestamp("event_time").toLocalDateTime();
                    resultSet = statement.executeQuery("SELECT * from events where event_id = (" +
                            "SELECT max(event_id) from events where user_id=" + id + ");");
                    if (resultSet.next()) {
                        int next_event_id = resultSet.getInt("event_id") + 1;
                        statement.executeUpdate("INSERT into events VALUES (" + id + ", " + next_event_id + ");");
                        statement.executeUpdate("INSERT into gateevents VALUES (" + id + ", " + next_event_id +
                                ", 2, '" + Timestamp.valueOf(time) + "');");
                        String sendToStats = sendRequest("http://localhost:8082/add-visit?user_id=" + id + "&startdate="
                                + startTime.getYear() + "-" + startTime.getMonthValue() + "-" + startTime.getDayOfMonth()
                                + "&starttime=" + startTime.getHour() + ":" + startTime.getMinute() + "&enddate="
                                + time.getYear() + "-" + time.getMonthValue() + "-" + time.getDayOfMonth() + "&endtime="
                                + time.getHour() + ":" + time.getMinute());
                        if (sendToStats == null) {
                            return new Response(true, "User with id = " + id + " successfully exit the center\n" +
                                    "But date didn't send to stats server, because it doesn't respond");
                        } else {
                            return new Response(true, "User with id = " + id + " successfully exit the center " +
                                    "and data successfully send to stats server");
                        }
                    } else {
                        comment = "An error occurred while executing command \"exit-center\"";
                    }
                } else {
                    comment = "User with id = " + id + " currently not in the center";
                }
            } else {
                comment = "User with id = " + id + " currently not in the center";
            }
            statement.close();
            return new Response(false, comment);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new Response(false, "An error occurred while executing command \"exit-center\"");
    }

    private String sendRequest(String request) {
        try {
            URL url = new URL(request);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            int status = con.getResponseCode();
            assert (status == 200);
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            con.disconnect();
            return content.toString();
        } catch (IOException ignored) {

        }
        return null;
    }
}
