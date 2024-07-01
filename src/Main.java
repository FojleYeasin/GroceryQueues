import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

public class Main {

    public static void main(String[] args) {
        int totalCustomer = 0;
        int totalServedCustomer = 0;
        int totalTimeToServe = 0;
        ReentrantLock lock = new ReentrantLock();
        BlockingQueue<Customer> queue = new LinkedBlockingQueue<>();

        int queueLength = 5;

        int numberOfBooths = 2;
        List<ServingBooth> booths = new ArrayList<>();
        for (int i = 1; i <= numberOfBooths; i++) {
            ServingBooth booth = new ServingBooth(i, lock, totalTimeToServe);
            booths.add(booth);
            booth.start();
        }
        int customerId = 1;
        LocalTime startTime = LocalTime.now();
        while (Duration.between(startTime, LocalTime.now()).getSeconds() < 20) {
            try {
                // Simulate the arrival of new customers
                Thread.sleep(200); // Time between arrivals

                // Add a new customer to the queue
                Customer customer = new Customer(customerId++);
                totalCustomer++;
                customer.updateArrivalTime();
                System.out.println("Customer " + customer.getId() + " arrived at " + customer.getArrivalTime());
                boolean isServed = false;
                for(ServingBooth booth: booths){
                    if(booth.queue.size() < queueLength){
                        lock.lock();
                        try {
                            System.out.println("Booth Size of booth " + booth.getId() + "is: " + booth.queue.size());
                            booth.queue.put(customer);
                            totalServedCustomer++;
                            isServed = true;
                            customer.updateIsServed();
                            System.out.println("Customer " + customer.getId() + " is served");
                            break;
                        } catch (InterruptedException err) {
                            System.out.println(err);
                        } finally {
                            lock.unlock();
                        }


                    }
                }
                if(!isServed){
                    System.out.println("Customer " + customer.getId() + " is not served");
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Customer arrival interrupted.");
                break;
            }
        }

        // Stop all serving booths
        for (ServingBooth booth : booths) {
            totalTimeToServe += booth.getTotalTimeToServe();
            booth.stopServing();
        }

        // Wait for all serving booths to finish
        for (ServingBooth booth : booths) {
            try {
                booth.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Total Number of Customer: " + totalCustomer);
        System.out.println("Total Number of Served Customer: " + totalServedCustomer);
        System.out.println("Total Number of leaved Customer: " + (totalCustomer - totalServedCustomer));
        System.out.println("Average time : " + ((float)totalTimeToServe / totalCustomer));
    }
}
