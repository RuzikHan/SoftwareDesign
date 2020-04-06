package manager;

import utils.Response;

import java.sql.*;
import java.time.LocalDateTime;

public class ManagerCommandImpl implements ManagerCommand {
    Connection connection;

    public ManagerCommandImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Response createUser(int id) {
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * from events where user_id=" + id + ";");
            if (!resultSet.next()) {
                statement.executeUpdate("INSERT into events VALUES (" + id + ", 1);");
                statement.executeUpdate("INSERT into managerevents VALUES (" + id + ", 1, '" + Timestamp.valueOf(LocalDateTime.now()) + "');");
                statement.close();
                return new Response(true, "Successfully create user with id = " + id);
            }
            statement.close();
            return new Response(false, "User with id = " + id + " already exists");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new Response(false, "An error occurred while executing command \"create-user\"");
    }

    @Override
    public Response renewSubscription(int id, LocalDateTime endTime) {
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * from events where event_id = (" +
                    "SELECT max(event_id) from events where user_id=" + id + ");");
            if (resultSet.next()) {
                int next_event_id = resultSet.getInt("event_id") + 1;
                statement.executeUpdate("INSERT into events VALUES (" + id + ", " + next_event_id + ");");
                statement.executeUpdate("INSERT into managerevents VALUES (" + id + ", " + next_event_id +
                        ", '" + Timestamp.valueOf(endTime) + "');");
                statement.close();
                return new Response(true, "Successfully renew subscription for user with id = " + id);
            }
            statement.close();
            return new Response(false, "User with id = " + id + " doesn't exists");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new Response(false, "An error occurred while executing command \"renew-subscription\"");
    }
}
