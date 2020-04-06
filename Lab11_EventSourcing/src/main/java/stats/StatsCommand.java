package stats;

import java.time.LocalDateTime;

public interface StatsCommand {
    void addVisit(int userId, LocalDateTime start, LocalDateTime end);
}
