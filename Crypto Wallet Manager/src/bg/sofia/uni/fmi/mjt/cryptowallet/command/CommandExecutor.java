package bg.sofia.uni.fmi.mjt.cryptowallet.command;

import bg.sofia.uni.fmi.mjt.cryptowallet.CurrencyCode;
import bg.sofia.uni.fmi.mjt.cryptowallet.User;
import bg.sofia.uni.fmi.mjt.cryptowallet.exception.InsufficientBalanceException;
import bg.sofia.uni.fmi.mjt.cryptowallet.exception.UserAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.cryptowallet.provider.HttpCaller;
import bg.sofia.uni.fmi.mjt.cryptowallet.provider.ObjectSerializable;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class CommandExecutor {

    private static final String LOGIN_REQUIRED = "Login required. Use \"login \\name\\ \\password\\\" or \"register \\name\\ \\password\\\".";
    private static final String IO_SERVER_EXCEPTION = "An error with the server appeared.";
    private static final String REGISTER_SUCCESSFUL = "Register successful.";
    private static final String LOGIN_SUCCESSFUL = "Login successful.";
    private static final String LOGIN_NOT_FOUND = "Login not found.";
    private static final String DEPOSIT_SUCCESSFUL = "Deposit successful.";
    private static final String BUYING_SUCCESSFUL = "Buying successful.";
    private static final String SELLING_SUCCESSFUL = "Selling successful.";
    private static final String WRONG_FORMAT = "Wrong format.";
    private static final String INSUFFICIENT_BALANCE = "Insufficient balance.";
    private static final String USER_ALREADY_EXISTS = "User already exists.";
    private static final String LOGOUT_SUCCESSFUL = "Logout successful.";
    private static final String REGISTER_HASHING_ERROR = "An error with the server appeared.";
    private static final String WRONG_ARGUMENTS_MESSAGE = "Wrong number of arguments for this command.";
    private static final String INVALID_AMOUNT_FORMAT = "Invalid amount format.";

    private static final int DEPOSIT_ARGUMENTS = 1;
    private static final int SELL_ARGUMENTS = 1;
    private static final int BUY_ARGUMENTS = 2;
    private static final int REGISTER_ARGUMENTS = 2;

    private static final String HELP_MESSAGE = """
            Available commands:
            login \\name\\ \\password\\
            register \\name\\ \\password\\
            deposit-money \\amount\\
            list-offerings
            buy \\code\\ \\amount\\
            sell \\code\\
            get-wallet-summary
            get-wallet-overall-summary
            logout
            exit
            """;

    private User user;

    public CommandExecutor() {
        user = null;
    }

    public String execute(Command command) throws IOException {
        if (command == null || command.command() == null) {
            return "Unrecognised command. Use \"help\" for options.";
        }
        return switch (command.command()) {
            case "login" -> login(command.arguments());
            case "logout" -> logout();
            case "register" -> register(command.arguments());
            case "deposit-money" -> depositMoney(command.arguments());
            case "list-offerings" -> listOfferings();
            case "buy" -> buy(command.arguments());
            case "sell" -> sell(command.arguments());
            case "get-wallet-summary" -> getWalletSummary();
            case "get-wallet-overall-summary" -> getWalletOverallSummary();
            case "help" -> help();
            default -> "Unrecognised command. Use \"help\" for options.";
        };
    }

    private boolean ensureLoggedIn() {
        if (user == null) {
            System.out.println(LOGIN_REQUIRED);
            return true;
        }
        return false;
    }

    private String sell(String[] input) throws IOException {
        if (input.length != SELL_ARGUMENTS) {
            return WRONG_ARGUMENTS_MESSAGE;
        }
        if (ensureLoggedIn()) {
            return LOGIN_REQUIRED;
        }
        try {
            CurrencyCode currency = CurrencyCode.valueOf(input[0]);
            user.getWallet().convertBalance(CurrencyCode.USD, currency);
            return SELLING_SUCCESSFUL;
        } catch (IllegalArgumentException e) {
            return WRONG_FORMAT;
        } catch (URISyntaxException | InterruptedException e) {
            return IO_SERVER_EXCEPTION;
        }
    }

    private String buy(String[] input) throws IOException {
        if (input.length != BUY_ARGUMENTS) {
            return WRONG_ARGUMENTS_MESSAGE;
        }
        if (ensureLoggedIn()) {
            return LOGIN_REQUIRED;
        }
        try {
            CurrencyCode currency = CurrencyCode.valueOf(input[0]);
            BigDecimal money = new BigDecimal(input[1]);
            user.getWallet().convertBalance(currency, CurrencyCode.USD, money);
            return BUYING_SUCCESSFUL;
        } catch (IllegalArgumentException e) {
            // Differentiate based on the cause of the exception.
            if (e instanceof NumberFormatException) {
                return INVALID_AMOUNT_FORMAT;  // Invalid number format.
            }
            return WRONG_FORMAT;  // Invalid currency code or other issues.
        } catch (InsufficientBalanceException e) {
            return INSUFFICIENT_BALANCE;
        } catch (URISyntaxException | InterruptedException e) {
            return IO_SERVER_EXCEPTION;
        }
    }


    private String depositMoney(String[] input) {
        if (input.length != DEPOSIT_ARGUMENTS) {
            return WRONG_ARGUMENTS_MESSAGE;
        }
        if (ensureLoggedIn()) {
            return LOGIN_REQUIRED;
        }
        try {
            BigDecimal money = new BigDecimal(input[0]);
            user.getWallet().deposit(money);
            return DEPOSIT_SUCCESSFUL;
        } catch (NumberFormatException e) {
            return INVALID_AMOUNT_FORMAT;
        }
    }

    private String getWalletSummary() {
        if (ensureLoggedIn()) {
            return LOGIN_REQUIRED;
        }
        return user.getWallet().currentCurrencyBalanceString();
    }

    private String getWalletOverallSummary() {
        if (ensureLoggedIn()) {
            return LOGIN_REQUIRED;
        }
        return user.getWallet().currentBalance().subtract(user.getWallet().getTotal()).toString();
    }

    private String listOfferings() throws IOException {
        if (ensureLoggedIn()) {
            return LOGIN_REQUIRED;
        }
        try {
            return HttpCaller.getPricesRateSync(CurrencyCode.USD);
        } catch (URISyntaxException | InterruptedException e) {
            return IO_SERVER_EXCEPTION;
        }
    }

    private String login(String[] input) {
        if (input.length != REGISTER_ARGUMENTS) {
            return WRONG_ARGUMENTS_MESSAGE;
        }
        try {
            List<User> users = ObjectSerializable.readUsersFromFile();
            for (User user : users) {
                if (user.login(input[0], input[1])) {
                    this.user = user;
                    return LOGIN_SUCCESSFUL;
                }
            }
        } catch (NoSuchAlgorithmException e) {
            return REGISTER_HASHING_ERROR;
        }
        return LOGIN_NOT_FOUND;
    }

    private String logout() {
        if (user != null) {
            ObjectSerializable.updateUser(user);
        }
        user = null;
        return LOGOUT_SUCCESSFUL;
    }

    private String register(String[] input) {
        if (input.length != REGISTER_ARGUMENTS) {
            return WRONG_ARGUMENTS_MESSAGE;
        }
        try {
            user = new User(input[0], input[1]);
            ObjectSerializable.writeUserToFile(user);
        } catch (NoSuchAlgorithmException e) {
            return REGISTER_HASHING_ERROR;
        } catch (UserAlreadyExistsException e) {
            return USER_ALREADY_EXISTS;
        }
        return REGISTER_SUCCESSFUL;
    }

    private String help() {
        return HELP_MESSAGE;
    }
}
