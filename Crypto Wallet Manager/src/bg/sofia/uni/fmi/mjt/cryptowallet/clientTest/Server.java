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
import java.nio.file.Paths;
import java.util.Iterator;

public class Server {
    private static final String OUTPUT_DIRECTORY = "database";
    private static final String DATABASE_FILE_NAME = "accounts.txt";
    private static final String LOG_PATH = "server.log";
    private static final Path FILE_PATH = Paths.get(OUTPUT_DIRECTORY, DATABASE_FILE_NAME);
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
                try {
                    if (selector.select() == 0) {
                        continue; // No channels are ready
                    }

                    Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                    while (keyIterator.hasNext()) {
                        SelectionKey key = keyIterator.next();
                        keyIterator.remove(); // Remove the key to prevent re-processing

                        try {
                            if (key.isAcceptable()) {
                                accept(serverSocketChannel);
                            } else if (key.isReadable()) {
                                processCommand(key);
                            }
                        } catch (IOException e) {
                            System.err.println("Error handling client: " + e.getMessage());
                            closeClient(key);
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Error in selector loop: " + e.getMessage());
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
            System.out.println("Client disconnected: " + clientChannel.getRemoteAddress());
            closeClient(key);
            return;
        }

        buffer.flip();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        String clientInput = new String(bytes, StandardCharsets.UTF_8).trim();
        System.out.println("Received command: " + clientInput);

        Command command = CommandCreator.newCommand(clientInput);
        String response;
        try {
            response = commandExecutor.execute(command, key);
        } catch (IllegalArgumentException e) {
            response = "Error: " + e.getMessage();
        } catch (RuntimeException e) {
            response = "Internal server error. Please try again.";
        }

        sendResponse(clientChannel, response);
    }

    private void sendResponse(SocketChannel clientChannel, String response) throws IOException {
        buffer.clear();
        buffer.put(response.getBytes(StandardCharsets.UTF_8));
        buffer.flip();
        clientChannel.write(buffer);
    }

    private void closeClient(SelectionKey key) {
        try {
            if (key.channel() instanceof SocketChannel) {
                key.channel().close(); // Close the channel
            }
        } catch (IOException e) {
            System.err.println("Error closing client channel: " + e.getMessage());
        } finally {
            key.cancel(); // Remove the key from the selector
        }
    }

    public static void main(String[] args) {
        try {
            String apiKey = "E92452D2-DF77-4022-8A8A-E2A4CFB06D51";
            Database database = new Database(FILE_PATH);
            ApiCall apiCall = new ApiCall(HttpClient.newHttpClient(), apiKey);

            CommandExecutor commandExecutor = new CommandExecutor(apiKey, database, apiCall);
            Server server = new Server(commandExecutor);

            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}