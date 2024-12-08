package bg.sofia.uni.fmi.mjt.cryptowallet.database;

import bg.sofia.uni.fmi.mjt.cryptowallet.User;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Database implements DatabaseAPI {
    private static final int PERIOD_OF_SAVING = 5;
    private static final String NEW_LINE = System.lineSeparator();
    private final Path filePath;

    private final Set<User> database = new HashSet<>();
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public Database(Path filePath) {
        this.filePath = filePath;
        initDatabase();
    }

    public Set<User> getDatabase() {
        return Collections.unmodifiableSet(database);
    }

    private void initDatabase() {
        loadData();
        scheduler.schedule(() -> updateData(database), PERIOD_OF_SAVING, TimeUnit.SECONDS);
    }

    private void loadData() {
        if (!Files.exists(filePath)) {
            return;
        }

        try (var bufferedReader = Files.newBufferedReader(filePath)) {

            String line;

            while ((line = bufferedReader.readLine()) != null) {
                User account = User.fromCSV(line);
                database.add(account);
            }

        } catch (IOException e) {
            throw new UncheckedIOException("An error has occurred while loading from file", e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("An error has occurred while reading from file", e);
        }
    }

    public void updateData(Set<User> accounts) {
        database.addAll(accounts);
        saveData(database);
        scheduler.schedule(() -> updateData(database), PERIOD_OF_SAVING, TimeUnit.SECONDS);
    }

    private void saveData(Set<User> database) {
        try {
            StringBuilder result = new StringBuilder();

            for (User acc : database) {
                result.append(acc.toCSV()).append(NEW_LINE);
            }

            if (!Files.exists(filePath.getParent())) {
                Files.createDirectory(filePath.getParent());
            }
            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
            }

            Files.writeString(filePath, result.toString());

        } catch (IOException e) {
            throw new UncheckedIOException("An error has occurred while saving to file", e);
        }
    }

    public void shutdownScheduler(Set<User> accounts) {
        scheduler.shutdownNow();
        saveData(accounts);
    }
}
