package com.chat.app.config;

import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Component
public class ChatRoom {
    // Use a thread-safe Set to store usernames
    private final Set<String> users = Collections.synchronizedSet(new HashSet<>());

    public void addUser(String username) {
        users.add(username);
    }

    public void removeUser(String username) {
        users.remove(username);
    }

    public Set<String> getUsers() {
        return Collections.unmodifiableSet(users);
    }

}


