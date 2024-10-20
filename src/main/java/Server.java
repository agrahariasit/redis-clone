import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Locale;

public class Server {
    final int  PORT;
    Selector selector;


    public Server(int port) {
        PORT = port;
    }

    private ServerSocketChannel initializeServerChannel() throws IOException {
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.socket().bind(new InetSocketAddress("0.0.0.0", PORT));
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("Server started on port 6379");
        return serverChannel;
    }


    private void acceptClientConnection(ServerSocketChannel serverChannel) {
        try {
            // Accept the connection
            SocketChannel clientChannel = serverChannel.accept();
            clientChannel.configureBlocking(false); // Set client to non-blocking mode
            clientChannel.register(selector, SelectionKey.OP_READ); // Register for read events
        }
        catch (IOException exception) {
            System.out.println("Error in accepting connection");
        }
    }

    private String readFromClient(SelectionKey key)  {
        String responseMessage;
        try {
            // Read data from the client
            SocketChannel clientChannel = (SocketChannel) key.channel();
            ByteBuffer buffer = ByteBuffer.allocate(1024);

            // Read the incoming data
            int bytesRead = clientChannel.read(buffer);
            if (bytesRead == -1) {
                // Client has closed the connection
                clientChannel.close();
                System.out.println("Client disconnected");
                return null;
            }
            else {
                buffer.flip();
                byte[] bytes = new byte[bytesRead];
                buffer.get(bytes);
                String message = new String(bytes);
                if (message.toLowerCase().contains("ping")) {
                    responseMessage = "PONG";
                } else if (message.toLowerCase().contains("echo")) {
                    responseMessage = message.split("\n")[4];
                } else {
                    responseMessage = message;
                }

                String response = "Server received: " + message;
                System.out.println(response);
                clientChannel.register(selector, SelectionKey.OP_WRITE); // Register for write events

                return "+"+responseMessage.replace("\n","").replace("\r","")+"\r\n";
            }
        }
        catch (IOException exception) {
            exception.printStackTrace();
            return null;
        }
    }

    private void writeToClient(String message, SelectionKey key) {
        SocketChannel clientChannel = (SocketChannel) key.channel();

        try {
            clientChannel.write(ByteBuffer.wrap(message.getBytes()));
            clientChannel.register(selector, SelectionKey.OP_READ); // Switch back to read events
        } catch (IOException exception) {
            try {
                System.out.println("Error while writing to client. Closing connection.");
                clientChannel.close();
            }
            catch (IOException exception1) {
                System.out.println("Error while closing client");
            }
        }

    }

    public void start() {
        try {
            // Create a Selector
            selector = Selector.open();

            ServerSocketChannel serverChannel = initializeServerChannel();

            if (serverChannel == null) {
                System.out.println("Shutting down...");
                return;
            }
            String message = "null";

            while (true) {
                // Wait for events
                if(selector.select()==0) {
                    continue;
                }

                // Iterate through the selected keys
                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove();

                    // Accept connection key
                    if (key.isAcceptable()) {
                        acceptClientConnection(serverChannel);
                    }
                    // Read key
                    else if (key.isReadable()) {
                        message = readFromClient(key);
                    }
                    // Write key
                    else if (key.isWritable()) {
                        assert message != null;
                        writeToClient(message, key);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
