package bg.sofia.uni.fmi.mjt.cryptowallet.clientTest;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Client {
    private static final int SERVER_PORT = 7777;
    private static final String SERVER_HOST = "localhost";
    private static final int BUFFER_SIZE = 2048;
    private static final String DISCONNECTED_SUCCESSFULLY =
            "Disconnected successfully";
    private static final String SHUTTING_DOWN_MESSAGE =
            "Server was shut down";
    private static ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
    private static final String EXIT_COMMAND = "exit";
    private static final String EXIT_MESSAGE = "Exiting... Thanks for using the app!";


    public static void main(String[] args) {
        try (SocketChannel sc = SocketChannel.open();
             Scanner scanner = new Scanner(System.in)) {

            sc.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT));
            System.out.println("Connected to server");
            while (true) {
                String message = scanner.nextLine();

                if (message.equalsIgnoreCase(EXIT_COMMAND)) {
                    sendMessageToServer(sc, EXIT_COMMAND);
                    System.out.println(EXIT_MESSAGE);
                    break;
                }

                sendMessageToServer(sc, message);

                try {
                    String reply = handleServerResponse(sc);
                    System.out.println(reply);

                    if (reply.equals(SHUTTING_DOWN_MESSAGE) || reply.equals(DISCONNECTED_SUCCESSFULLY)) {
                        break;
                    }
                } catch (IOException e) {
                    System.out.println(SHUTTING_DOWN_MESSAGE);
                    break;
                }

            }
        } catch (IOException e) {
            System.out.println("An error has occurred, please try reconnecting to server");
        }
    }

    private static void sendMessageToServer(SocketChannel sc, String message) throws IOException {
        buffer.clear();
        buffer.put(message.getBytes());
        buffer.flip();
        sc.write(buffer);
    }

    private static String handleServerResponse(SocketChannel sc) throws IOException {

        buffer.clear();
        sc.read(buffer);
        buffer.flip();

        byte[] byteArray = new byte[buffer.remaining()];
        buffer.get(byteArray);
        return new String(byteArray, StandardCharsets.UTF_8);
    }
}
