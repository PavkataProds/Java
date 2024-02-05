package bg.sofia.uni.fmi.mjt.cryptowallet.provider;

import bg.sofia.uni.fmi.mjt.cryptowallet.User;
import bg.sofia.uni.fmi.mjt.cryptowallet.exception.UserAlreadyExistsException;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ObjectSerializable {
    private static final String DESTINATION = "users.bin";

    public static void writeUserToFile(User newUser) {
        List<User> oldList = new ArrayList<>();
        try {
            oldList = readUsersFromFile();
        } catch (IllegalStateException ignored) {
            //the list is empty
        }
        try (var objectOutputStream = new ObjectOutputStream(Files.newOutputStream(Path.of(DESTINATION)))) {
            for (User user : oldList) {
                objectOutputStream.writeObject(user);
                objectOutputStream.flush();
            }
            for (User user : oldList) {
                if (user.getName().equals(newUser.getName())) {
                    throw new UserAlreadyExistsException();
                }
            }
            objectOutputStream.writeObject(newUser);
            objectOutputStream.flush();
        } catch (IOException e) {
            throw new IllegalStateException("A problem occurred while writing to a file", e);
        }
    }

    public static void updateUser(User userUpdate) {
        List<User> oldList = new ArrayList<>();
        try {
            oldList = readUsersFromFile();
        } catch (IllegalStateException ignored) {
            //the list is empty
        }
        try (var objectOutputStream = new ObjectOutputStream(Files.newOutputStream(Path.of(DESTINATION)))) {
            for (User user : oldList) {
                if (user.getName().equals(userUpdate.getName())) {
                    objectOutputStream.writeObject(userUpdate);
                } else {
                    objectOutputStream.writeObject(user);
                }
                objectOutputStream.flush();
            }
        } catch (IOException e) {
            throw new IllegalStateException("A problem occurred while writing to a file", e);
        }
    }

    public static List<User> readUsersFromFile() {
        List<User> result = new ArrayList<>();
        try (var objectInputStream = new ObjectInputStream(Files.newInputStream(Path.of(DESTINATION)))) {
            Object userObject;
            while ((userObject = objectInputStream.readObject()) != null) {
                result.add((User) userObject);
            }

        } catch (EOFException e) {
            // EMPTY BODY
        } catch (FileNotFoundException e) {
            throw new IllegalStateException("The files does not exist", e);
        } catch (IOException e) {
            throw new IllegalStateException("A problem occurred while reading from a file", e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
//    public static void main(String... args) throws NoSuchAlgorithmException {
//        User firstUsere = new User("Gosho", "123");
//        User firstUser = new User("Stamat", "123");
//
//        writeUserToFile(firstUser);
//        List<User> list = readUsersFromFile();
//        assert list != null;
//        for (User user : list) {
//            System.out.println(user);
//        }
//    }
}
