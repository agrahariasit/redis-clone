import java.io.*;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientRequest implements Runnable{
    Socket clientSocket;
    ExecutorService executorService = Executors.newSingleThreadExecutor();



    ClientRequest(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }
    @Override
    public void run() {
        try {
            this.processCommand(clientSocket);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void processCommand(Socket clientSocket) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        String val = reader.readLine();
        while (val != null) {
            System.out.println("command received from client: "+val);
            if(val.contains("PING")) {
                writer.write("+PONG\r\n");
                writer.flush();
            }
            val = reader.readLine();
        }
    }
}
