package bg.sofia.uni.fmi.mjt.cryptowallet;

import bg.sofia.uni.fmi.mjt.cryptowallet.exception.InvalidUserInformationException;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

public class User implements Serializable {
    private final String name;
    private final byte[] password;
    private final byte[] salt;
    private final Wallet wallet;
    private final int byteSize = 16;
    private final boolean isAdmin;
    private static final String DELIMITER = "/";

    public User(String name, String password) throws NoSuchAlgorithmException {
        if (name == null || password == null
                || name.isBlank() || password.isBlank()) {
            throw new InvalidUserInformationException();
        }
        this.name = name;

        //Implements SHA-512 algorithm and creates random salt value for the user
        SecureRandom random = new SecureRandom();
        salt = new byte[byteSize];
        random.nextBytes(salt);
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(salt);

        this.password = md.digest(password.getBytes(StandardCharsets.UTF_8));

        this.wallet = new Wallet();
        this.isAdmin = false;
    }

    //password accepted as byte array means it's already hashed!!!
    public User(String name, byte[] password, byte[] salt, Wallet wallet, boolean isAdmin) throws NoSuchAlgorithmException {
        this.name = name;
        this.password = password;
        this.salt = salt;
        this.wallet = wallet;
        this.isAdmin = isAdmin;
    }

    public String getName() {
        return name;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public boolean login(String name, String password) throws NoSuchAlgorithmException {

        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(salt);
        byte[] hashedPassword = md.digest(password.getBytes(StandardCharsets.UTF_8));

        return name.equals(this.name) && Arrays.equals(hashedPassword, this.password);
    }

    public static User fromCSV(String line) throws NoSuchAlgorithmException {
        String[] tokens = line.split(DELIMITER);

        boolean isAdmin = (Integer.parseInt(tokens[0])) != 0;

        return new User(tokens[1], tokens[2].getBytes(), tokens[3].getBytes(), Wallet.fromCSV(tokens), isAdmin);
    }

    public String toCSV() {
        StringBuilder result = new StringBuilder();

        int adminValue = isAdmin ? 1 : 0;

        result.append(adminValue).append(DELIMITER)
                .append(name).append(DELIMITER)
                .append(Arrays.toString(password))
                .append(Arrays.toString(salt));

        return result.append(wallet.toCSV()).toString();
    }
}
