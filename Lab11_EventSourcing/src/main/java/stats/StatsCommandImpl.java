package stats;

import java.time.LocalDateTime;

public class StatsCommandImpl implements StatsCommand {
    public State state;

    public StatsCommandImpl(State state) {
        this.state = state;
    }

    @Override
    public void addVisit(int userId, LocalDateTime start, LocalDateTime end) {
        state.addVisit(userId, start, end);
    }
}
