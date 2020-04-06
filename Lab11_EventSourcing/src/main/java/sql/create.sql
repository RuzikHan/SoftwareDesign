create TABLE if not exists events (
    user_id INT NOT NULL,
    event_id INT NOT NULL,
    PRIMARY KEY (user_id, event_id)
);

create TABLE if not exists managerevents (
    user_id INT NOT NULL,
    event_id INT NOT NULL,
    subscriptionEnd timestamp NOT NULL,
    PRIMARY KEY (user_id, event_id),
    FOREIGN KEY (user_id, event_id) REFERENCES EVENTS(user_id, event_id)
);

create TABLE if not exists gateevents (
    user_id INT NOT NULL,
    event_id INT NOT NULL,
    event_type INT NOT NULL,
    event_time timestamp NOT NULL,
    PRIMARY KEY (user_id, event_id),
    FOREIGN KEY (user_id, event_id) REFERENCES EVENTS(user_id, event_id)
);
