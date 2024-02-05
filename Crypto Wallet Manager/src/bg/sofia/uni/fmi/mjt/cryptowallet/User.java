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
}
