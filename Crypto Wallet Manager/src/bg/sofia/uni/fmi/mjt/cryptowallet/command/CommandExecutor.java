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
import java.util.Scanner;

public class CommandExecutor {
    //commands and comments
    private static final String LOGIN = "login";
    private static final String LOGOUT = "logout";
    private static final String REGISTER = "register";
    private static final int REGISTER_ARGUMENTS = 2;
    private static final String REGISTER_ARGUMENTS_MESSAGE = "Wrong number of arguments.";
    private static final String REGISTER_HASHING_ERROR = "An error with the server appeared.";
    private static final String DEPOSIT_MONEY = "deposit-money";
    private static final String LIST_OFFERINGS = "list-offerings";
    private static final String BUY = "buy";
    private static final String SELL = "sell";
    private static final String GET_WALLET_SUMMARY = "get-wallet-summary";
    private static final String GET_WALLET_OVERALL_SUMMARY = "get-wallet-overall-summary";
    private static final String HELP = "help";
    private static final String UNRECOGNISED_COMMAND = "Unrecognised command. Use \"help\" command for options.";
    private static final String LOGIN_REQUIRED = "Login required. Use \"login \\name\\ \\password\\\"" +
            " or \"register \"\\name\\ \\password\\\".";
    private static final String IO_SERVER_EXCEPTION = "An error with the server appeared.";
    private static final String LOGOUT_SUCCESSFUL = "Logout successful.";
    private static final String REGISTER_SUCCESSFUL = "Register successful.";
    private static final String LOGIN_SUCCESSFUL = "Login successful.";
    private static final String LOGIN_NOT_FOUND = "Login not found.";
    private static final String DEPOSIT_SUCCESSFUL = "Deposit successful.";
    private static final String BUYING_SUCCESSFUL = "Buying successful.";
    private static final String SELLING_SUCCESSFUL = "Selling successful.";
    private static final int DEPOSIT_ARGUMENTS = 1;
    private static final String WRONG_FORMAT = "Wrong format.";
    private static final String INSUFFICIENT_BALANCE = "Insufficient balance.";
    private static final String USER_ALREADY_EXISTS = "User already exits.";

    private User user;

    public CommandExecutor() {
        user = null;
    }

    public String execute(Command command) {
        return switch (command.command()) {
            case LOGIN -> login(command.arguments());
            case LOGOUT -> logout();
            case REGISTER -> register(command.arguments());
            case DEPOSIT_MONEY -> depositMoney(command.arguments());
            case LIST_OFFERINGS -> listOfferings();
            case BUY -> buy(command.arguments());
            case SELL -> sell(command.arguments());
            case GET_WALLET_SUMMARY -> getWalletSummary();
            case GET_WALLET_OVERALL_SUMMARY -> getWalletOverallSummary();

            case HELP -> help();
            default -> UNRECOGNISED_COMMAND;
        };
    }

    private String getWalletOverallSummary() {
        if (user == null) {
            return LOGIN_REQUIRED;
        }
        return user.getWallet().currentBalance().subtract(user.getWallet().getTotal()).toString();
    }

    private String sell(String[] input) {
        if (input.length != DEPOSIT_ARGUMENTS) {
            return REGISTER_ARGUMENTS_MESSAGE;
        }
        if (user == null) {
            return LOGIN_REQUIRED;
        }
        try {
            user.getWallet().convertBalance(CurrencyCode.USD, CurrencyCode.valueOf(input[0]));
            return SELLING_SUCCESSFUL;
        } catch (IOException | URISyntaxException | InterruptedException e) {
            return WRONG_FORMAT;
        }
    }

    private String buy(String[] input) {
        if (input.length != REGISTER_ARGUMENTS) {
            return REGISTER_ARGUMENTS_MESSAGE;
        }
        if (user == null) {
            return LOGIN_REQUIRED;
        }
        try {
            BigDecimal money = new BigDecimal(input[1]);
            user.getWallet().convertBalance(CurrencyCode.valueOf(input[0]), CurrencyCode.USD, money);
            return BUYING_SUCCESSFUL;
        } catch (IOException | URISyntaxException | InterruptedException e) {
            return WRONG_FORMAT;
        } catch (InsufficientBalanceException e) {
            return INSUFFICIENT_BALANCE;
        }
    }

    private String getWalletSummary() {
        if (user == null) {
            return LOGIN_REQUIRED;
        }
        return user.getWallet().currentCurrencyBalanceString();
    }

    private String depositMoney(String[] input) {
        if (input.length != DEPOSIT_ARGUMENTS) {
            return REGISTER_ARGUMENTS_MESSAGE;
        }
        if (user == null) {
            return LOGIN_REQUIRED;
        }
        try {
            BigDecimal money = new BigDecimal(input[0]);
            user.getWallet().deposit(money);
            return DEPOSIT_SUCCESSFUL;
        } catch (RuntimeException e) {
            return WRONG_FORMAT;
        }
    }

    private String login(String[] input) {
        if (input.length != REGISTER_ARGUMENTS) {
            return REGISTER_ARGUMENTS_MESSAGE;
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
//        if (user != null) {
//            ObjectSerializable.updateUser(user);
//        }
        user = null;
        return LOGOUT_SUCCESSFUL;
    }

    private String register(String[] input) {
        if (input.length != REGISTER_ARGUMENTS) {
            return REGISTER_ARGUMENTS_MESSAGE;
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
        return "Available commands: " + System.lineSeparator() +
                "login \\name\\ \\password\\" + System.lineSeparator() +
                "register \\name\\ \\password\\" + System.lineSeparator() +
                "deposit-money \\amount\\" + System.lineSeparator() +
                "list-offerings" + System.lineSeparator() +
                "buy \\code\\ \\amount\\" + System.lineSeparator() +
                "sell \\code\\" + System.lineSeparator() +
                "get-wallet-summary" + System.lineSeparator() +
                "get-wallet-overall-summary" + System.lineSeparator() +
                "logout" + System.lineSeparator() +
                "exit" + System.lineSeparator();
    }

    private String listOfferings() {
        if (user == null) {
            return LOGIN_REQUIRED;
        }
        String result;
        try {
            result = HttpCaller.getPricesRateSync(CurrencyCode.USD);
        } catch (IOException | URISyntaxException | InterruptedException e) {
            return IO_SERVER_EXCEPTION;
        }
        return result;
    }

    public static void main(String[] args) {
        Scanner myObj = new Scanner(System.in);
        CommandExecutor executor = new CommandExecutor();
        while (true) {
            System.out.println("Enter command: ");

            String line = myObj.nextLine();
            String[] list = line.split(" ");
            if (list[0].equals("exit")) {
                break;
            }
            String[] input = new String[list.length - 1];
            System.arraycopy(list, 1, input, 0, list.length - 1);
            Command command = new Command(list[0], input);
            System.out.println(executor.execute(command));
        }
    }

//    public static void main(String[] args) {
//        CommandExecutor executor = new CommandExecutor();
//        Command command = new Command("register", new String[]{"pavel", "123"});
//        Command command2 = new Command("logout", null);
//        Command command3 = new Command("login", new String[]{"pavel", "123"});
//        Command command22 = new Command("logout", null);
//        Command command32 = new Command("login", new String[]{"pavel", "123"});
//        Command command4 = new Command("list-offerings", null);
//        Command command5 = new Command("deposit-money", new String[]{"5000"});
//        Command command6 = new Command(GET_WALLET_SUMMARY, null);
//        Command command7 = new Command("buy", new String[]{"BTC", "2000"});
//        Command command8 = new Command("sell", new String[]{"BTC"});
//        Command command9 = new Command(GET_WALLET_OVERALL_SUMMARY, null);
//        System.out.println(executor.execute(command));
//        System.out.println(executor.execute(command2));
//        System.out.println(executor.execute(command3));
//        //System.out.println(executor.execute(command4));
//        System.out.println(executor.execute(command5));
//        System.out.println(executor.execute(command6));
//        System.out.println(executor.execute(command7));
//        System.out.println(executor.execute(command6));
//        System.out.println(executor.execute(command8));
//        System.out.println(executor.execute(command6));
//        System.out.println(executor.execute(command9));
//    }
}