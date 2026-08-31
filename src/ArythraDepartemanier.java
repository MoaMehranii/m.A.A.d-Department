import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ArythraDepartemanier {

    private FloorMonitor[] floors;
    private List<Elevator> elevators;
    private List<Thread> elevatorThreads;
    private List<Thread> passengerThreads;
    private final List<String> completedTaskIDs = Collections.synchronizedList(new ArrayList<>());

    private static class Helper {
        private static final ArythraDepartemanier INSTANCE = new ArythraDepartemanier();
    }

    public static ArythraDepartemanier getInstance() {
        return Helper.INSTANCE;
    }

    public void initializeBuilding(int totalFloors, int totalElevatorsCount) {
        this.elevators = new ArrayList<>(totalElevatorsCount);
        this.elevatorThreads = new ArrayList<>(totalElevatorsCount);
        this.passengerThreads = new ArrayList<>();

        this.floors = new FloorMonitor[totalFloors];
        for (int i = 0; i < totalFloors; i++) {
            floors[i] = new FloorMonitor(i, totalElevatorsCount);
        }

        for (int i = 0; i < totalElevatorsCount; i++) {

            Elevator.Type type;
            if (i % 3 == 0) {
                type = Elevator.Type.VIP;
            } else if (i % 3 == 1) {
                type = Elevator.Type.PUBLIC;
            } else {
                type = Elevator.Type.CARRIAGE;
            }

            float maxWeight;

            if (i % 3 == 2) {
                maxWeight = 1000f;
            } else {
                maxWeight = 500f;
            }

            Elevator elevator = new Elevator.Builder()
                    .setID(i)
                    .setType(type)
                    .setMaxWeight(maxWeight)
                    .setTransitionTime(2000)
                    .setCurrentFloor(0)
                    .setIsOnAFloor(true)
                    .setDepartmentFloors(totalFloors - 1)
                    .build();

            elevator.setisTurnedOn(true);
            elevators.add(elevator);

            Thread t = new Thread(elevator, "Elevator-Thread-" + i);
            elevatorThreads.add(t);
            t.start();
        }
    }

    public FloorMonitor getFloor(int floorNumber) {
        if (floorNumber < 0 || floorNumber >= floors.length) return floors[0];
        return floors[floorNumber];
    }

    public void registerCompletedTask(String taskID) {
        completedTaskIDs.add(taskID);
    }

    public void enterPassenger(Passenger passenger) {
        Thread t = new Thread(passenger, passenger.getName());
        synchronized (passengerThreads) {
            passengerThreads.add(t);
        }
        t.start();
    }

    public void shutdownSystem() {
        System.out.println("\n ... shutting down the m.A.A.d department...");

        synchronized (passengerThreads) {
            for (Thread t : passengerThreads) {
                try {
                    if (t.isAlive()) {
                        t.join();
                    }
                } catch (InterruptedException e) {
                    System.out.println("[SHUTDOWN] Interrupted while waiting for passengers to escape.");
                }
            }
        }

        for (Elevator elevator : elevators) {
            elevator.setisTurnedOn(false);
        }

        for (FloorMonitor floor : floors) {
            synchronized (floor) {
                floor.notifyAll();
            }
        }

        for (Thread t : elevatorThreads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                System.out.println("[SHUTDOWN] Interrupted while waiting for elevators to stop.");
            }
        }

        renderFinalReport();
    }

    private void renderFinalReport() {
        System.out.println("-----------------------------------------------");
        System.out.println("ARYTHRA'S REPORTS:");
        System.out.println("-----------------------------------------------");
        for (Elevator e : elevators) {
            System.out.println("Elevator " + e.getID() + " with the type of " + e.getType() + " total movement time: " + e.getTotalMoveTime() + " ms");
        }

        System.out.println("\n" + "SUCCESSFUL TASKS REGISTERED!:");
        if (completedTaskIDs.isEmpty()) {
            System.out.println("   No tasks were completed today.");
        }
        else {
            for (String id : completedTaskIDs) {
                System.out.println("    Task ID: " + id);
            }
        }
        System.out.println("-----------------------------------------------");
    }
    public int getBestElevatorFor(Passenger p) {
        for (Elevator e : elevators) {
            if (p.getTotalWeight() > e.getMaxWeight())
                continue;

            if (e.getType() == Elevator.Type.VIP &&
                    (p.getRole() == Passenger.Role.PROFESSOR || p.getRole() == Passenger.Role.PROVOST)) {
                return e.getID();
            }
            if (e.getType() == Elevator.Type.CARRIAGE && p.getRole() == Passenger.Role.PORTER) {
                return e.getID();
            }
            if (e.getType() == Elevator.Type.PUBLIC && p.getRole() != Passenger.Role.PORTER) {
                return e.getID();
            }
        }
        return 0;
    }
}