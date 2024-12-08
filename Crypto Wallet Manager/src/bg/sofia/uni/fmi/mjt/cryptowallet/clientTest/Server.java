package bg.sofia.uni.fmi.mjt.cryptowallet.clientTest;

import bg.sofia.uni.fmi.mjt.cryptowallet.command.Command;
import bg.sofia.uni.fmi.mjt.cryptowallet.command.CommandCreator;
import bg.sofia.uni.fmi.mjt.cryptowallet.command.CommandExecutor;
import bg.sofia.uni.fmi.mjt.cryptowallet.database.Database;
import bg.sofia.uni.fmi.mjt.cryptowallet.response.ApiCall;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Iterator;

public class Server {
    private static final int BUFFER_SIZE = 2048;
    private static final String HOST = "localhost";
    private static final int PORT = 7777;

    private final Selector selector;
    private final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
    private final CommandExecutor commandExecutor;

    public Server(CommandExecutor commandExecutor) throws IOException {
        this.commandExecutor = commandExecutor;
        this.selector = Selector.open();
    }

    public void start() throws IOException {
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            serverSocketChannel.bind(new InetSocketAddress(HOST, PORT));
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("Server started on port " + PORT);

            while (true) {
                if (selector.select() == 0) {
                    continue;
                }

                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    if (key.isAcceptable()) {
                        accept(serverSocketChannel);
                    } else if (key.isReadable()) {
                        processCommand(key);
                    }
                    keyIterator.remove();
                }
            }
        }
    }

    private void accept(ServerSocketChannel serverSocketChannel) throws IOException {
        SocketChannel clientChannel = serverSocketChannel.accept();
        clientChannel.configureBlocking(false);
        clientChannel.register(selector, SelectionKey.OP_READ);
        System.out.println("New connection: " + clientChannel.getRemoteAddress());
    }

    private void processCommand(SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        buffer.clear();

        int readBytes = clientChannel.read(buffer);
        if (readBytes <= 0) {
            clientChannel.close();
            return;
        }

        buffer.flip();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        String clientInput = new String(bytes, StandardCharsets.UTF_8).trim();

        Command command = CommandCreator.newCommand(clientInput);
        String response;
        try {
            response = commandExecutor.execute(command, key);
        } catch (IllegalArgumentException e) {
            response = "Error: " + e.getMessage(); // Handle bad input
        } catch (RuntimeException e) {
            response = "Internal server error. Please try again."; // Handle unexpected errors
        }

        sendResponse(clientChannel, response);
    }

    private void sendResponse(SocketChannel clientChannel, String response) throws IOException {
        buffer.clear();
        buffer.put(response.getBytes(StandardCharsets.UTF_8));
        buffer.flip();
        clientChannel.write(buffer);
    }

    public static void main(String[] args) {
        try {
            String apiKey = "E92452D2-DF77-4022-8A8A-E2A4CFB06D51";
            Database database = new Database(Path.of("accounts.txt"));
            ApiCall apiCall = new ApiCall(HttpClient.newHttpClient(), apiKey);

            CommandExecutor commandExecutor = new CommandExecutor(apiKey, database, apiCall);
            Server server = new Server(commandExecutor);

            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
