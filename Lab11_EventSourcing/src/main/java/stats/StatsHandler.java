package stats;

import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class StatsHandler implements HttpHandler {
    State state;

    public StatsHandler(State state) {
        this.state = state;
    }

    @Override
    public void handleHttpRequest(HttpRequest httpRequest, HttpResponse httpResponse, HttpControl httpControl) throws Exception {
        String request = httpRequest.uri();
        String sResponse;
        try {
            StatsQuery statsQuery = new StatsQueryImpl(state);
            if (request.startsWith("/day-stats")) {
                String[] dateParts = httpRequest.queryParam("date").split("-");
                Integer[] date = Arrays.stream(dateParts).map(Integer::parseInt).toArray(Integer[]::new);
                String[] timeParts = httpRequest.queryParam("time").split(":");
                Integer[] time = Arrays.stream(timeParts).map(Integer::parseInt).toArray(Integer[]::new);
                int visits = statsQuery.getStatsByDay(LocalDateTime.of(date[0], date[1], date[2], time[0], time[1]));
                sResponse = visits + " visits";
            } else if (request.startsWith("/all-average-frequency")) {
                List<Integer> visits = statsQuery.getAverageFrequency();
                Collections.reverse(visits);
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Visits in last 30 days").append("\n");
                for (Integer count : visits) {
                    stringBuilder.append(count).append(" ");
                }
                sResponse = stringBuilder.toString();
            } else if (request.startsWith("/user-average-frequency")) {
                int user_id = Integer.parseInt(httpRequest.queryParam("user_id"));
                List<Integer> visits = statsQuery.getAverageFrequencyForUser(user_id);
                Collections.reverse(visits);
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Visits in last 30 days for user with id = ").append(user_id).append("\n");
                for (Integer count : visits) {
                    stringBuilder.append(count).append(" ");
                }
                sResponse = stringBuilder.toString();
            } else if (request.startsWith("/all-average-duration")) {
                double duration = statsQuery.getAverageVisitDuration();
                int hours = (int) duration / 3600;
                int seconds = (int) duration - hours * 3600;
                int minutes = seconds / 60;
                seconds = seconds - minutes * 60;
                sResponse = "Agerage visit time: " + hours + " hours, " + minutes + " minutes " + seconds + " seconds";
            } else if (request.startsWith("/user-average-duration")) {
                int user_id = Integer.parseInt(httpRequest.queryParam("user_id"));
                double duration = statsQuery.getAverageVisitDurationForUser(user_id);
                int hours = (int) duration / 3600;
                int seconds = (int) duration - hours * 3600;
                int minutes = seconds / 60;
                seconds = seconds - minutes * 60;
                sResponse = "Agerage visit time for user with id = " + user_id + ": " + hours + " hours, " + minutes
                        + " minutes " + seconds + " seconds";
            } else if (request.startsWith("/add-visit")) {
                StatsCommand statsCommand = new StatsCommandImpl(state);
                int user_id = Integer.parseInt(httpRequest.queryParam("user_id"));
                String[] dateParts = httpRequest.queryParam("startdate").split("-");
                Integer[] date = Arrays.stream(dateParts).map(Integer::parseInt).toArray(Integer[]::new);
                String[] timeParts = httpRequest.queryParam("starttime").split(":");
                Integer[] time = Arrays.stream(timeParts).map(Integer::parseInt).toArray(Integer[]::new);
                LocalDateTime startTime = LocalDateTime.of(date[0], date[1], date[2], time[0], time[1]);
                dateParts = httpRequest.queryParam("enddate").split("-");
                date = Arrays.stream(dateParts).map(Integer::parseInt).toArray(Integer[]::new);
                timeParts = httpRequest.queryParam("endtime").split(":");
                time = Arrays.stream(timeParts).map(Integer::parseInt).toArray(Integer[]::new);
                LocalDateTime endTime = LocalDateTime.of(date[0], date[1], date[2], time[0], time[1]);
                statsCommand.addVisit(user_id, startTime, endTime);
                sResponse = "Successfully add info to stats";
            } else {
                sResponse = "Error: incorrect command";
            }
        } catch (NumberFormatException e) {
            sResponse = "Error: user id must be a number";
        } catch (NullPointerException e) {
            sResponse = "Error: missing arguments";
        }
        httpResponse.content(sResponse).end();
    }
}
