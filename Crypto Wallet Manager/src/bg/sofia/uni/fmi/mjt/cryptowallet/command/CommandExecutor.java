package bg.sofia.uni.fmi.mjt.cryptowallet.command;

import bg.sofia.uni.fmi.mjt.cryptowallet.CurrencyCode;
import bg.sofia.uni.fmi.mjt.cryptowallet.User;
import bg.sofia.uni.fmi.mjt.cryptowallet.database.Database;
import bg.sofia.uni.fmi.mjt.cryptowallet.exception.FailedRequestException;
import bg.sofia.uni.fmi.mjt.cryptowallet.exception.InsufficientBalanceException;
import bg.sofia.uni.fmi.mjt.cryptowallet.response.ApiCall;
import bg.sofia.uni.fmi.mjt.cryptowallet.server.ServerLogger;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.nio.channels.SelectionKey;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CommandExecutor {

    //Messages
    private static final String LOGIN_REQUIRED =
            "Login required. Use \"login \\name\\ \\password\\\" or \"register \\name\\ \\password\\\".";
    private static final String ALREADY_LOGGED_IN = "You are already log in. Use \"logout\" first";
    private static final String IO_SERVER_EXCEPTION = "An error with the server appeared.";
    private static final String REGISTER_SUCCESSFUL = "Register successful.";
    private static final String LOGIN_SUCCESSFUL = "Login successful.";
    private static final String ACCOUNT_ALREADY_IN_USAGE = "This account is already logged in elsewhere";
    private static final String WRONG_PASSWORD_MESSAGE = "Wrong password";
    private static final String NOT_LOGGED_IN_MESSAGE = "You need to log in to use this command";
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
    private static final String FAILED_REQUEST_MESSAGE = "An error has occurred when requesting from API";
    private static final String INVALID_MONEY_AMOUNT = "Amount of money must be positive";
    private static final String ASSET_DOES_NOT_EXIST = "You are trying to buy an asset that does not exist";
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

    //Commands
    private static final String UNRECOGNISED_COMMAND = "Unrecognised command. Use \"help\" for options.";
    private static final String LOGIN_COMMAND = "login";
    private static final String LOGOUT_COMMAND = "logout";
    private static final String REGISTER_COMMAND = "register";
    private static final String DEPOSIT_COMMAND = "deposit-money";
    private static final String LIST_OFFERINGS_COMMAND = "list-offerings";
    private static final String BUY_COMMAND = "buy";
    private static final String SELL_COMMAND = "sell";
    private static final String WALLET_SUMMARY_COMMAND = "get-wallet-summary";
    private static final String WALLET_OVERALL_SUMMARY_COMMAND = "get-wallet-overall-summary";
    private static final String HELP_COMMAND = "help";

    //Constants
    private static final int DEPOSIT_ARGUMENTS = 1;
    private static final int SELL_ARGUMENTS = 1;
    private static final int BUY_ARGUMENTS = 2;
    private static final int REGISTER_ARGUMENTS = 2;
    private static final String NEW_LINE = System.lineSeparator();

    private Set<User> accounts = new HashSet<>();
    private ApiCall apiCall;
    private Database database;
    private ServerLogger logger;
    private static Set<User> currentlyUsedAccounts = new HashSet<>();

    private static final String OUTPUT_DIRECTORY = "database";
    private static final String DATABASE_FILE_NAME = "accounts.txt";
    private static final String LOG_PATH = "server.log";
    private static final Path FILE_PATH = Paths.get(OUTPUT_DIRECTORY, DATABASE_FILE_NAME);


    public CommandExecutor(String apiKey) {
        this.database = new Database(FILE_PATH);
        this.logger = new ServerLogger(LOG_PATH);
        this.accounts.addAll(database.getDatabase());
        this.apiCall = new ApiCall(HttpClient.newBuilder().build(), apiKey);
    }

    public CommandExecutor(String apiKey, Database database, ApiCall apiCall) {
        this.database = database;
        this.accounts.addAll(database.getDatabase());
        this.apiCall = apiCall;
    }

    public String execute(Command command, SelectionKey key) throws IOException {
        if (command == null || command.command() == null) {
            return UNRECOGNISED_COMMAND;
        }
        return switch (command.command()) {
            case LOGIN_COMMAND -> login(command.arguments(), key);
            case LOGOUT_COMMAND -> logout(key);
            case REGISTER_COMMAND -> register(command.arguments(), key);
            case DEPOSIT_COMMAND -> depositMoney(command.arguments(), key);
            case LIST_OFFERINGS_COMMAND -> listOfferings(key);
            case BUY_COMMAND -> buy(command.arguments(), key);
            case SELL_COMMAND -> sell(command.arguments(), key);
            case WALLET_SUMMARY_COMMAND -> getWalletSummary(key);
            case WALLET_OVERALL_SUMMARY_COMMAND -> getWalletOverallSummary(key);
            case HELP_COMMAND -> help();
            default -> UNRECOGNISED_COMMAND;
        };
    }

    private String sell(String[] input, SelectionKey key) throws IOException {
        if (input.length != SELL_ARGUMENTS) {
            return WRONG_ARGUMENTS_MESSAGE;
        }
        if (key.attachment() == null) {
            return LOGIN_REQUIRED;
        }

        CurrencyCode currency = CurrencyCode.valueOf(input[0].toUpperCase());
        Map<CurrencyCode, BigDecimal> marketChart;

        try {
            marketChart = apiCall.getMarketChart();
        } catch (FailedRequestException e) {
            return FAILED_REQUEST_MESSAGE;
        }
        if (!marketChart.containsKey(currency)) {
            return ASSET_DOES_NOT_EXIST;
        }

        User user = (User) key.attachment();
        try {
            user.getWallet().convertBalance(CurrencyCode.USD, currency);
            return SELLING_SUCCESSFUL;
        } catch (IllegalArgumentException e) {
            return WRONG_FORMAT;
        } catch (URISyntaxException | InterruptedException e) {
            return IO_SERVER_EXCEPTION;
        }
    }

    private String buy(String[] input, SelectionKey key) throws IOException {

        if (input.length != BUY_ARGUMENTS) {
            return WRONG_ARGUMENTS_MESSAGE;
        }
        if (key.attachment() == null) {
            return LOGIN_REQUIRED;
        }

        CurrencyCode currency = CurrencyCode.valueOf(input[0].toUpperCase());
        BigDecimal money = new BigDecimal(input[1]);
        Map<CurrencyCode, BigDecimal> marketChart;

        if ((money.compareTo(BigDecimal.ZERO) <= 0)) {
            return INVALID_MONEY_AMOUNT;
        }
        try {
            marketChart = apiCall.getMarketChart();
        } catch (FailedRequestException e) {
            return FAILED_REQUEST_MESSAGE;
        }
        if (!marketChart.containsKey(currency)) {
            return ASSET_DOES_NOT_EXIST;
        }

        User user = (User) key.attachment();
        try {
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


    private String depositMoney(String[] input, SelectionKey key) {
        if (input.length != DEPOSIT_ARGUMENTS) {
            return WRONG_ARGUMENTS_MESSAGE;
        }
        if (key.attachment() == null) {
            return LOGIN_REQUIRED;
        }

        try {
            BigDecimal money = new BigDecimal(input[0]);
            User current = (User) key.attachment();
            current.getWallet().deposit(money);
            return DEPOSIT_SUCCESSFUL;
        } catch (NumberFormatException e) {
            return INVALID_AMOUNT_FORMAT;
        }
    }

    private String getWalletSummary(SelectionKey key) {
        if (key.attachment() == null) {
            return LOGIN_REQUIRED;
        }
        User user = (User) key.attachment();
        return user.getWallet().currentCurrencyBalanceString();
    }

    private String getWalletOverallSummary(SelectionKey key) {
        if (key.attachment() == null) {
            return LOGIN_REQUIRED;
        }
        User user = (User) key.attachment();
        return user.getWallet().currentBalance().subtract(user.getWallet().getTotal()).toString();
    }

    private String listOfferings(SelectionKey key) {
        if (key.attachment() == null) {
            return LOGIN_REQUIRED;
        }

        StringBuilder result = new StringBuilder();
        try {
            Map<CurrencyCode, BigDecimal> map = apiCall.getMarketChart();
            for (var entry : map.entrySet()) {
                result.append(String.format("%-6s %10.4f", entry.getKey(), entry.getValue())).append(NEW_LINE);
            }

        } catch (FailedRequestException e) {
            logger.logError(FAILED_REQUEST_MESSAGE, e.getStackTrace());
            return FAILED_REQUEST_MESSAGE;
        }

        return result.toString();
    }

    private User findAccount(String username) {
        if (username == null) {
            return null;
        }
        for (User account : database.getDatabase()) {
            if (username.equals(account.getName())) {
                return account;
            }
        }
        return null;
    }

    private String login(String[] input, SelectionKey key) {
        if (key.attachment() != null) {
            return ALREADY_LOGGED_IN;
        }
        if (input.length != REGISTER_ARGUMENTS) {
            return WRONG_ARGUMENTS_MESSAGE;
        }

        String username = input[0];
        String password = input[1];
        User current = findAccount(username);

        if (!database.getDatabase().contains(current)) {
            return LOGIN_NOT_FOUND;
        }
        if (currentlyUsedAccounts.contains(current)) {
            return ACCOUNT_ALREADY_IN_USAGE;
        }
        try {
            if (!current.login(username, password)) {
                return WRONG_PASSWORD_MESSAGE;
            }
        } catch (NoSuchAlgorithmException e) {
            return REGISTER_HASHING_ERROR;
        }

        key.attach(current);
        currentlyUsedAccounts.add(current);

        return LOGIN_SUCCESSFUL;
    }

    private String logout(SelectionKey key) {
        if (key.attachment() == null) {
            return NOT_LOGGED_IN_MESSAGE;
        }

        currentlyUsedAccounts.remove((User) key.attachment());
        key.attach(null);

        return LOGOUT_SUCCESSFUL;
    }

    private String register(String[] input, SelectionKey key) {
        if (key.attachment() != null) {
            return ALREADY_LOGGED_IN;
        }
        if (input.length != REGISTER_ARGUMENTS) {
            return WRONG_ARGUMENTS_MESSAGE;
        }

        String username = input[0];
        String password = input[1];
        User current = findAccount(username);
        if (database.getDatabase().contains(current)) {
            return USER_ALREADY_EXISTS;
        }
        User newAccount;
        try {
            newAccount = new User(username, password);
        } catch (NoSuchAlgorithmException e) {
            return REGISTER_HASHING_ERROR;
        }
        if (database.getDatabase().isEmpty()) {
            newAccount.changeAdminStatus();
        }
        accounts.add(newAccount);
        database.updateData(accounts);

        return REGISTER_SUCCESSFUL;
    }

    private String help() {
        return HELP_MESSAGE;
    }
}
