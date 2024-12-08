package bg.sofia.uni.fmi.mjt.cryptowallet.database;

import bg.sofia.uni.fmi.mjt.cryptowallet.User;

import java.util.Set;

public interface DatabaseAPI {
    void updateData(Set<User> accounts);

    void shutdownScheduler(Set<User> accounts);
}
