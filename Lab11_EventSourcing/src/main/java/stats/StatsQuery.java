package stats;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsQuery {
    int getStatsByDay(LocalDateTime day);
    List<Integer> getAverageFrequency();
    List<Integer> getFrequencyForUser(int id);
    double getAverageVisitDuration();
    double getAverageVisitDurationForUser(int id);
}
