
public class Main {
  public static void main(String[] args) {
      int PORT = 6379;
      Server server = new Server(PORT);
      server.start();
  }
}
