package stats;

import java.time.LocalDateTime;
import java.util.List;

public class StatsQueryImpl implements StatsQuery {
    private State state;

    public StatsQueryImpl(State state) {
        this.state = state;
    }

    @Override
    public int getStatsByDay(LocalDateTime day) {
        return state.getStatsByDay(day);
    }

    @Override
    public List<Integer> getAverageFrequency() {
        return state.getAverageFrequency();
    }

    @Override
    public List<Integer> getAverageFrequencyForUser(int id) {
        return state.getAverageFrequencyForUser(id);
    }

    @Override
    public double getAverageVisitDuration() {
        return state.getAverageVisitDuration();
    }

    @Override
    public double getAverageVisitDurationForUser(int id) {
        return state.getAverageVisitDurationForUser(id);
    }
}
