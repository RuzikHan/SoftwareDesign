package manager;

import java.time.LocalDateTime;

public class User {
    public int id;
    public LocalDateTime endSubscription;

    public User(int id, LocalDateTime endSubscription) {
        this.id = id;
        this.endSubscription = endSubscription;
    }

}
