package manager;

import utils.Response;

import java.time.LocalDateTime;

public interface ManagerCommand {
    Response createUser(int id);
    Response renewSubscription(int id, LocalDateTime endTime);
}
