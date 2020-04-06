package gate;

import utils.Response;

import java.time.LocalDateTime;

public interface GateCommand {
    Response enterCenter(int id, LocalDateTime time);
    Response exitCenter(int id, LocalDateTime time);
}
