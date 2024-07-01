import java.time.Duration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

public class ServingBooth extends Thread {
    public BlockingQueue<Customer> queue = new LinkedBlockingQueue<>();
    private ReentrantLock lock;
    private volatile boolean running = true;
    int boothNo;
    private int totalTimeToServe;

    public ServingBooth(int no, ReentrantLock lock, int totalTimeToServe) {
        boothNo = no;
        this.lock = lock;
    }
    public long getId(){
        return boothNo;
    }
    public void stopServing() {
        running = false;
        this.interrupt(); // In case it's blocked on queue.take()
    }
    public int getTotalTimeToServe(){
        return totalTimeToServe;
    }

    @Override
    public void run() {
        while (running) {
            try {
                Customer customer = queue.take();
                customer.updateServeTime();
                int servedTime = (int) Duration.between(customer.getArrivalTime(), customer.getServeTime()).toMillis();
                totalTimeToServe = servedTime;
                System.out.println("Customer " + customer.getId() + " is served at " + customer.getServeTime() + " at booth " + boothNo);
                Thread.sleep(600); // Simulate serving time
            } catch (InterruptedException e) {
                if (!running) {
                    break; // Exit the loop if the thread is stopped
                }
                Thread.currentThread().interrupt(); // Restore the interrupted status
            }
        }
    }
}
