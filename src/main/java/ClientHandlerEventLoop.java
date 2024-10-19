
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class ClientHandlerEventLoop {

    BlockingQueue<Runnable> queue;
    Boolean running = false;

    public ClientHandlerEventLoop() {
        queue = new LinkedBlockingDeque<>();
        boolean running = true;
    }

    public void submit(Runnable task) {
        queue.offer(task);
    }

    public void start() {
        while (running) {
            try {
                Runnable task = queue.take();
                task.run();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void stop() {
        running = false;
    }

}
