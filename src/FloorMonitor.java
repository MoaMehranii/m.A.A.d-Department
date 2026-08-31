import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class FloorMonitor {
    private final int floor;
    private final List<PriorityQueue<Passenger>> passengersQueue;

    public FloorMonitor(int floor, int totalElevatorsCount) {
        this.floor = floor;
        this.passengersQueue = new ArrayList<>(totalElevatorsCount);
        for (int i = 0; i < totalElevatorsCount; i++) {
            passengersQueue.add(new PriorityQueue<>(new GoodComparator()));
        }
    }

    public void requestElevator(Passenger p, int elevatorID) {
        PriorityQueue<Passenger> queue = passengersQueue.get(elevatorID);

        // We save the entrance time, to use the mechanism aging. Which prevents a passenger waiting forever
        synchronized (queue) {
            p.setEntryTime(System.currentTimeMillis());
            queue.add(p);
            System.out.println("[QUEUED] " + p.getName() + " joined queue for Elevator " + elevatorID + " on floor " + floor);
            queue.notifyAll();

            // passenger sleeps until the elevator can board
            while (!p.isCanBoard()) {
                try {
                    queue.wait();
                } catch (InterruptedException e) {
                    queue.remove(p);
                    Thread.currentThread().interrupt();
                    System.out.println("[CANCEL] " + p.getName() + " left queue due to interrupt.");
                    return;
                }
            }
        }
    }


    public Passenger getPassengerForElevator(Elevator elevator) {
        int elevatorID = elevator.getID();
        PriorityQueue<Passenger> queue = passengersQueue.get(elevatorID);

        synchronized (queue) {
            // elevator sleeps until a passenger wants to board
            while (queue.isEmpty()) {
                try {
                    queue.wait(1000); 
                    if (queue.isEmpty()) {
                        return null;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            }

            Passenger LUCKYPASSENGER = queue.poll();
            if (LUCKYPASSENGER != null) {
                LUCKYPASSENGER.setCanBoard(true);
                System.out.println("[BOARDED] Elevator " + elevatorID + " picked up " + LUCKYPASSENGER.getName() + " on floor " + floor);
                queue.notifyAll();
            }
            return LUCKYPASSENGER;
        }
    }

    public void dropPassenger(Passenger p) {
        System.out.println("[DEBOARDED] " + p.getName() + " left the elevator at floor " + floor);
        synchronized (p) {
            p.setCanBoard(false);
            p.notifyAll();
        }
    }


    // --- Comparator for advanced justice ---
    private static class GoodComparator implements Comparator<Passenger> {
        @Override
        public int compare(Passenger o1, Passenger o2) {
            return Float.compare(score(o2), score(o1));
        }
    }

    private static float score(Passenger passenger) {
        long waitingTimeMs = System.currentTimeMillis() - passenger.getEntryTime();
        float agingScore = (waitingTimeMs / 200f) * 10f;
        //this stops a passenger from waiting forever. Idea taken from OS scheduling systems.

        return (passenger.getAge() * 30f
                + (passenger.getRole().ordinal() + 1) * 200f
                + (passenger.getTask().getPriority().ordinal() + 1) * 50f
                + agingScore
        );
    }
}