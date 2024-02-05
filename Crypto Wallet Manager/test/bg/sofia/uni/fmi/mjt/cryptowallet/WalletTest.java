package bg.sofia.uni.fmi.mjt.cryptowallet;

import bg.sofia.uni.fmi.mjt.cryptowallet.command.Command;
import bg.sofia.uni.fmi.mjt.cryptowallet.command.CommandExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WalletTest {

    private CommandExecutor commandExecutor;

    @BeforeEach
    public void setUp() {
        commandExecutor = new CommandExecutor();
    }

    @Test
    public void testRegisterCommand() {
        Command registerCommand = new Command("register", new String[]{"user", "pass"});

        String result = commandExecutor.execute(registerCommand);

        assertEquals("User already exits.", result);
    }

    @Test
    public void testLoginCommand() {
        Command registerCommand = new Command("register", new String[]{"user", "pass"});
        Command loginCommand = new Command("login", new String[]{"user", "pass"});

        commandExecutor.execute(registerCommand);
        String result = commandExecutor.execute(loginCommand);

        assertEquals("Login successful.", result);
    }

    @Test
    public void testLogoutCommand() {
        Command registerCommand = new Command("register", new String[]{"user", "pass"});
        Command loginCommand = new Command("login", new String[]{"user", "pass"});
        Command logoutCommand = new Command("logout", null);

        commandExecutor.execute(registerCommand);
        commandExecutor.execute(loginCommand);
        String result = commandExecutor.execute(logoutCommand);

        assertEquals("Logout successful.", result);
    }

    @Test
    public void testDepositMoneyCommand() {
        Command registerCommand = new Command("register", new String[]{"user", "pass"});
        Command loginCommand = new Command("login", new String[]{"user", "pass"});
        Command depositCommand = new Command("deposit-money", new String[]{"5000"});

        commandExecutor.execute(registerCommand);
        commandExecutor.execute(loginCommand);
        String result = commandExecutor.execute(depositCommand);

        assertEquals("Deposit successful.", result);
    }

    @Test
    public void testBuyCommand() {
        Command registerCommand = new Command("register", new String[]{"user", "pass"});
        Command loginCommand = new Command("login", new String[]{"user", "pass"});
        Command depositCommand = new Command("deposit-money", new String[]{"5000"});
        Command buyCommand = new Command("buy", new String[]{"BTC", "2000"});
        Command listCommand = new Command("list-offerings", null);

        commandExecutor.execute(registerCommand);
        commandExecutor.execute(loginCommand);
        commandExecutor.execute(depositCommand);
        String result = commandExecutor.execute(buyCommand);

        assertEquals("Buying successful.", result);
        StringBuilder res = new StringBuilder(commandExecutor.execute(listCommand));
        String result2 = res.substring(0, 3);
        assertEquals(result2, "ADA");
    }

    @Test
    public void testSellCommand() {
        Command registerCommand = new Command("register", new String[]{"user", "pass"});
        Command loginCommand = new Command("login", new String[]{"user", "pass"});
        Command depositCommand = new Command("deposit-money", new String[]{"5000"});
        Command sellCommand = new Command("sell", new String[]{"BTC"});

        commandExecutor.execute(registerCommand);
        commandExecutor.execute(loginCommand);
        String result = commandExecutor.execute(depositCommand);
        String result2 = commandExecutor.execute(sellCommand);

        assertEquals("Deposit successful.", result);
        assertEquals("Selling successful.", result2);
    }

    @Test
    public void testGetWalletSummaryCommand() {
        Command registerCommand = new Command("register", new String[]{"user", "pass"});
        Command loginCommand = new Command("login", new String[]{"user", "pass"});
        Command depositCommand = new Command("deposit-money", new String[]{"5000"});
        Command summaryCommand = new Command("get-wallet-summary", null);

        commandExecutor.execute(registerCommand);
        commandExecutor.execute(loginCommand);
        commandExecutor.execute(depositCommand);
        String result = commandExecutor.execute(summaryCommand);

        assertTrue(result.contains("USD: 5000"));
    }

    @Test
    public void testGetWalletOverallSummaryCommand() {
        Command registerCommand = new Command("register", new String[]{"user", "pass"});
        Command loginCommand = new Command("login", new String[]{"user", "pass"});
        Command depositCommand = new Command("deposit-money", new String[]{"5000"});
        Command overallSummaryCommand = new Command("get-wallet-overall-summary", null);

        commandExecutor.execute(registerCommand);
        commandExecutor.execute(loginCommand);
        commandExecutor.execute(depositCommand);
        String result = commandExecutor.execute(overallSummaryCommand);

        assertEquals("0", result);
    }

    @Test
    public void testInvalidCommand() {
        Command invalidCommand = new Command("invalid", null);

        String result = commandExecutor.execute(invalidCommand);

        assertEquals("Unrecognised command. Use \"help\" command for options.", result);
    }
}
