package manager;

import java.sql.*;

public class ManagerQueryImpl implements ManagerQuery {
    Connection connection;

    public ManagerQueryImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public User getUser(int id) {
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * from managerevents where event_id = (" +
                    "SELECT max(event_id) from managerevents where user_id=" + id + ");");
            if (resultSet.next()) {
                User user = new User(id, resultSet.getTimestamp("subscriptionEnd").toLocalDateTime());
                statement.close();
                return user;
            } else {
                System.out.println("User does not exists");
                statement.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
