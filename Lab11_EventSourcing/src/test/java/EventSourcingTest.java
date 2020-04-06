import database.DBConnection;
import database.DBConnectionImpl;
import gate.GateCommand;
import gate.GateCommandImpl;
import manager.*;
import org.junit.Assert;
import org.junit.Test;
import stats.StateImpl;
import stats.StatsQueryImpl;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;

public class EventSourcingTest {
    DBConnection dbConnection = new DBConnectionImpl();
    Connection connection = dbConnection.getConnection();

    @Test
    public void managerTest1() {
        drop(connection);
        create(connection);
        ManagerCommand managerCommand = new ManagerCommandImpl(connection);
        Assert.assertTrue(managerCommand.createUser(1).status);
        Assert.assertTrue(managerCommand.createUser(11).status);
        Assert.assertTrue(managerCommand.createUser(111).status);
        Assert.assertTrue(managerCommand.createUser(1111).status);
    }

    @Test
    public void managerTest2() {
        drop(connection);
        create(connection);
        ManagerCommand managerCommand = new ManagerCommandImpl(connection);
        Assert.assertTrue(managerCommand.createUser(1).status);
        Assert.assertFalse(managerCommand.createUser(1).status);
        Assert.assertTrue(managerCommand.createUser(21).status);
    }

    @Test
    public void managerTest3() {
        drop(connection);
        create(connection);
        ManagerCommand managerCommand = new ManagerCommandImpl(connection);
        managerCommand.createUser(1);
        managerCommand.createUser(2);
        Assert.assertTrue(managerCommand.renewSubscription(1, LocalDateTime.of(2020, 10, 10, 10, 10)).status);
        Assert.assertTrue(managerCommand.renewSubscription(2, LocalDateTime.of(2020, 10, 10, 10, 10)).status);
        ManagerQuery managerQuery = new ManagerQueryImpl(connection);
        Assert.assertNotNull(managerQuery.getUser(1));
        Assert.assertNull(managerQuery.getUser(3));
    }

    @Test
    public void managerTest4() {
        drop(connection);
        create(connection);
        ManagerCommand managerCommand = new ManagerCommandImpl(connection);
        managerCommand.createUser(2);
        Assert.assertFalse(managerCommand.renewSubscription(1, LocalDateTime.of(2020, 10, 10, 10, 10)).status);
        Assert.assertTrue(managerCommand.renewSubscription(2, LocalDateTime.of(2020, 10, 10, 10, 10)).status);
    }

    @Test
    public void gateTest1() {
        drop(connection);
        create(connection);
        ManagerCommand managerCommand = new ManagerCommandImpl(connection);
        managerCommand.createUser(1);
        GateCommand gateCommand = new GateCommandImpl(connection);
        Assert.assertFalse(gateCommand.enterCenter(1, LocalDateTime.now()).status);
        Assert.assertFalse(gateCommand.exitCenter(1, LocalDateTime.now()).status);
        Assert.assertFalse(gateCommand.enterCenter(11, LocalDateTime.now()).status);
    }

    @Test
    public void gateTest2() throws InterruptedException {
        drop(connection);
        create(connection);
        ManagerCommand managerCommand = new ManagerCommandImpl(connection);
        managerCommand.createUser(1);
        managerCommand.renewSubscription(1, LocalDateTime.of(2020, 10, 10, 10, 10));
        GateCommand gateCommand = new GateCommandImpl(connection);
        Assert.assertTrue(gateCommand.enterCenter(1, LocalDateTime.now()).status);
        Thread.sleep(500);
        Assert.assertTrue(gateCommand.exitCenter(1, LocalDateTime.now()).status);
        Assert.assertTrue(gateCommand.enterCenter(1, LocalDateTime.now()).status);
        Thread.sleep(500);
        Assert.assertTrue(gateCommand.exitCenter(1, LocalDateTime.now()).status);
        Assert.assertFalse(gateCommand.enterCenter(15, LocalDateTime.now()).status);
    }

    @Test
    public void gateTest3() throws InterruptedException {
        drop(connection);
        create(connection);
        GateCommand gateCommand = new GateCommandImpl(connection);
        Assert.assertFalse(gateCommand.enterCenter(2, LocalDateTime.now()).status);
        ManagerCommand managerCommand = new ManagerCommandImpl(connection);
        managerCommand.createUser(2);
        managerCommand.renewSubscription(2, LocalDateTime.of(2020, 10, 10, 10, 10));
        Assert.assertFalse(gateCommand.exitCenter(2, LocalDateTime.now()).status);
        Assert.assertTrue(gateCommand.enterCenter(2, LocalDateTime.now()).status);
        Thread.sleep(500);
        Assert.assertTrue(gateCommand.exitCenter(2, LocalDateTime.now()).status);
    }

    @Test
    public void gateTest4() {
        drop(connection);
        create(connection);
        ManagerCommand managerCommand = new ManagerCommandImpl(connection);
        managerCommand.createUser(1);
        managerCommand.renewSubscription(1, LocalDateTime.of(2020, 10, 10, 10, 10));
        GateCommand gateCommand = new GateCommandImpl(connection);
        Assert.assertFalse(gateCommand.exitCenter(1, LocalDateTime.now()).status);
        Assert.assertTrue(gateCommand.enterCenter(1, LocalDateTime.now()).status);
    }

    @Test
    public void statsTest1() throws InterruptedException, IOException {
        drop(connection);
        create(connection);
        statsSetup(connection);
        StatsQueryImpl statsQuery = new StatsQueryImpl(new StateImpl(connection));
        Assert.assertEquals(4, statsQuery.getStatsByDay(LocalDateTime.now()));
    }

    @Test
    public void statsTest2() throws InterruptedException, IOException {
        drop(connection);
        create(connection);
        statsSetup(connection);
        StatsQueryImpl statsQuery = new StatsQueryImpl(new StateImpl(connection));
        List<Integer> frequency = statsQuery.getAverageFrequency();
        Assert.assertEquals(2, (int) frequency.get(0));
    }

    @Test
    public void statsTest3() throws InterruptedException, IOException {
        drop(connection);
        create(connection);
        statsSetup(connection);
        StatsQueryImpl statsQuery = new StatsQueryImpl(new StateImpl(connection));
        List<Integer> frequency = statsQuery.getFrequencyForUser(1);
        Assert.assertEquals(3, (int) frequency.get(0));
        frequency = statsQuery.getFrequencyForUser(2);
        Assert.assertEquals(1, (int) frequency.get(0));
    }

    @Test
    public void statsTest4() throws InterruptedException, IOException {
        drop(connection);
        create(connection);
        statsSetup(connection);
        StatsQueryImpl statsQuery = new StatsQueryImpl(new StateImpl(connection));
        Assert.assertTrue(statsQuery.getAverageVisitDuration() > 0.0);
    }

    private void statsSetup(Connection connection) throws InterruptedException {
        ManagerCommand managerCommand = new ManagerCommandImpl(connection);
        managerCommand.createUser(1);
        managerCommand.renewSubscription(1, LocalDateTime.of(2020, 10, 10, 10, 10));
        managerCommand.createUser(2);
        managerCommand.renewSubscription(2, LocalDateTime.of(2020, 10, 10, 10, 10));
        GateCommand gateCommand = new GateCommandImpl(connection);
        gateCommand.enterCenter(1, LocalDateTime.now());
        Thread.sleep(500);
        gateCommand.exitCenter(1, LocalDateTime.now());
        gateCommand.enterCenter(1, LocalDateTime.now());
        Thread.sleep(500);
        gateCommand.exitCenter(1, LocalDateTime.now());
        gateCommand.enterCenter(1, LocalDateTime.now());
        Thread.sleep(500);
        gateCommand.exitCenter(1, LocalDateTime.now());
        gateCommand.enterCenter(2, LocalDateTime.now());
        Thread.sleep(500);
        gateCommand.exitCenter(2, LocalDateTime.now());
    }

    private void drop(Connection connection) {
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate("DROP TABLE events CASCADE;");
            statement.executeUpdate("DROP TABLE managerevents CASCADE;");
            statement.executeUpdate("DROP TABLE gateevents CASCADE;");
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void create(Connection connection) {
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate(
                    "create TABLE if not exists events (" +
                            "user_id INT NOT NULL," +
                            "event_id INT NOT NULL," +
                            "PRIMARY KEY (user_id, event_id)" +
                            ");");
            statement.executeUpdate(
                    "create TABLE if not exists managerevents (" +
                            "user_id INT NOT NULL," +
                            "event_id INT NOT NULL," +
                            "subscriptionEnd timestamp NOT NULL," +
                            "PRIMARY KEY (user_id, event_id)," +
                            "FOREIGN KEY (user_id, event_id) REFERENCES EVENTS(user_id, event_id)" +
                            ");");
            statement.executeUpdate(
                    "create TABLE if not exists gateevents (" +
                            "user_id INT NOT NULL," +
                            "event_id INT NOT NULL," +
                            "event_type INT NOT NULL," +
                            "event_time timestamp NOT NULL," +
                            "PRIMARY KEY (user_id, event_id)," +
                            "FOREIGN KEY (user_id, event_id) REFERENCES EVENTS(user_id, event_id)" +
                            ");");
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
