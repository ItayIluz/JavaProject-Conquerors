package managers;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class UserManager {

    private final Set<String> users;

    public UserManager() {
        users = Collections.synchronizedSet(new HashSet<>());
    }

    public synchronized Set<String> getUsers() {
        return users;
    }

    public boolean isUserExists(String username) {
        return users.contains(username);
    }
}