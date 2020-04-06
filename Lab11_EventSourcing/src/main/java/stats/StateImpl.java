package stats;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

public class StateImpl implements State {
    Connection connection;
    Map<Integer, List<VisitTime>> data;

    public static class VisitTime {
        public LocalDateTime enterTime;
        public LocalDateTime endTime;

        public VisitTime(LocalDateTime enterTime, LocalDateTime endTime) {
            this.enterTime = enterTime;
            this.endTime = endTime;
        }
    }

    public StateImpl(Connection connection) throws IOException {
        this.connection = connection;
        data = new HashMap<>();
        getData();
    }

    private void getData() throws IOException {
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT distinct user_id from events;");
            while (resultSet.next()) {
                int id = resultSet.getInt("user_id");
                Statement visitStatement = connection.createStatement();
                ResultSet visitResultSet = visitStatement.executeQuery("SELECT * from gateevents where user_id=" + id);
                List<VisitTime> visits = new ArrayList<>();
                if (visitResultSet.next()) {
                    while (true) {
                        if (visitResultSet.getInt("event_type") == 1) {
                            LocalDateTime enterTime = visitResultSet.getTimestamp("event_time").toLocalDateTime();
                            if (visitResultSet.next()) {
                                if (visitResultSet.getInt("event_type") == 2) {
                                    LocalDateTime endTime = visitResultSet.getTimestamp("event_time").toLocalDateTime();
                                    visits.add(new VisitTime(enterTime, endTime));
                                } else {
                                    throw new IOException("Bad data in gate table");
                                }
                            } else {
                                break;
                            }
                        }
                        if (!visitResultSet.next()) {
                            break;
                        }
                    }
                }
                visitStatement.close();
                data.put(id, visits);
            }
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addVisit(int userId, LocalDateTime start, LocalDateTime end) {
        List<VisitTime> visits = data.getOrDefault(userId, new ArrayList<>());
        visits.add(new VisitTime(start, end));
        data.put(userId, visits);
    }

    private boolean checkDate(LocalDateTime dateTime1, LocalDateTime dateTime2) {
        return dateTime1.getYear() == dateTime2.getYear() && dateTime1.getDayOfYear() == dateTime2.getDayOfYear();
    }

    @Override
    public int getStatsByDay(LocalDateTime dateTime) {
        int count = 0;
        for (List<VisitTime> userVisits : data.values()) {
            for (VisitTime visit : userVisits) {
                if (checkDate(visit.enterTime, dateTime) || checkDate(dateTime, visit.endTime)) {
                    count++;
                }
            }
        }
        return count;
    }

    @Override
    public List<Integer> getAverageFrequency() {
        List<Integer> visits = new ArrayList<>(Collections.nCopies(30, 0));
        for (Integer id : data.keySet()) {
            List<Integer> userVisits = getFrequencyForUser(id);
            if (visits.size() == userVisits.size()) {
                for (int i = 0; i < visits.size(); i++) {
                    visits.set(i, visits.get(i) + userVisits.get(i));
                }
            }
        }
        visits = visits.stream().map(count -> count / data.size()).collect(Collectors.toList());
        return visits;
    }

    @Override
    public List<Integer> getFrequencyForUser(int id) {
        LocalDateTime now = LocalDateTime.now();
        List<Integer> visits = new ArrayList<>(Collections.nCopies(30, 0));
        List<VisitTime> userVisits = data.getOrDefault(id, null);
        if (userVisits == null) {
            return visits;
        }
        for (VisitTime visit : userVisits) {
            long endSeconds = visit.endTime.atZone(ZoneOffset.UTC).toEpochSecond();
            long nowSeconds = now.atZone(ZoneOffset.UTC).toEpochSecond();
            if (nowSeconds - endSeconds < 2592000) {
                int day = (int) ((nowSeconds - endSeconds) / 86400);
                if (day < visits.size()) {
                    visits.set(day, visits.get(day) + 1);
                }
            }
        }
        return visits;
    }

    @Override
    public double getAverageVisitDuration() {
        double average = 0.0;
        for (Integer id : data.keySet()) {
            average += getAverageVisitDurationForUser(id);
        }
        return average / data.size();
    }

    @Override
    public double getAverageVisitDurationForUser(int id) {
        List<VisitTime> userVisits = data.getOrDefault(id, null);
        if (userVisits == null) {
            return 0;
        }
        return userVisits
                .stream()
                .map(visit -> visit.endTime.atZone(ZoneOffset.UTC).toEpochSecond() -
                        visit.enterTime.atZone(ZoneOffset.UTC).toEpochSecond())
                .mapToInt(Math::toIntExact)
                .summaryStatistics()
                .getAverage();
    }
}
