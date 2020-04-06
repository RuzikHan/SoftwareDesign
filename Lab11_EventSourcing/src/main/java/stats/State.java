package stats;

import java.time.LocalDateTime;
import java.util.List;

public interface State {
    void addVisit(int userId, LocalDateTime start, LocalDateTime end);

    int getStatsByDay(LocalDateTime day);
    List<Integer> getAverageFrequency();
    List<Integer> getFrequencyForUser(int id);
    double getAverageVisitDuration();
    double getAverageVisitDurationForUser(int id);
}
