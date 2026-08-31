public class Passenger implements Runnable {
    public enum Role {
        STUDENT,
        PROFESSOR,
        PROVOST,
        PORTER
    }

    private final Role role;
    private final int age;
    private final float weight;
    private Task task;
    private final float carryingWeight;
    private final float totalWeight;
    private volatile int currentFloor = 0;
    private volatile long entryTime;
    private volatile boolean canBoard = false;
    private String name;
    //---builder---
    public static class Builder {
        private String name;
        private Role role;
        private int age;
        private float weight;
        private Task task;
        private float carryingWeight = 0f;

        public Builder setRole(Role role) { this.role = role;
            return this;
        }
        public Builder setName(String name) { this.name = name;
            return this;
        }
        public Builder setAge(int age) { this.age = age;
            return this;
        }
        public Builder setWeight(float weight) { this.weight = weight;
            return this;
        }
        public Builder setTask(Task task) { this.task = task;
            return this;
        }
        public Builder setCarryingWeight(float carryingWeight) { this.carryingWeight = carryingWeight;
            return this;
        }
        public Passenger build() {
            return new Passenger(this);
        }
    }
    //---build---
    private Passenger(Builder builder) {
        this.name = builder.name;
        this.role = builder.role;
        this.age = builder.age;
        this.weight = builder.weight;
        this.task = builder.task;
        this.carryingWeight = builder.carryingWeight;
        this.totalWeight = this.carryingWeight + this.weight;
    }

    public void setCanBoard(boolean canBoard) {
        this.canBoard = canBoard;
    }
    public void setEntryTime(long entryTime) {
        this.entryTime = entryTime; }
    public void setCurrentFloor(int currentFloor) {
        this.currentFloor = currentFloor; }
    public float getTotalWeight() {
        return totalWeight;
    }
    public int getCurrentFloor() {
        return currentFloor;
    }
    public long getEntryTime() {
        return entryTime;
    }
    public Role getRole() {
        return role;
    }
    public int getAge() {
        return age;
    }
    public Task getTask() {
        return task;
    }
    public boolean isCanBoard() {
        return canBoard;
    }
    public String getName() {
        return name;
    }

    @Override
    public void run() {
        System.out.println("[ARRIVED] " + name + " (" + role + ") entered the m.A.A.d department.");

        int chosenElevatorID = ArythraDepartemanier.getInstance().getBestElevatorFor(this);

        System.out.println("[WAITING] " + name + " is waiting to go UP to floor " + task.getFloor());
        ArythraDepartemanier.getInstance().getFloor(0).requestElevator(this, chosenElevatorID);

        synchronized (this) {
            while (this.isCanBoard()) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {}

        this.currentFloor = task.getFloor();
        System.out.println("[TASK START] " + name + " started task " + task.getID() + " on floor " + currentFloor);

        try {
            Thread.sleep((long) task.getDuration());
        } catch (InterruptedException e) {
            System.out.println("[INTERRUPTED] " + name + " was interrupted during the task.");
            Thread.currentThread().interrupt();
            return;
        }

        ArythraDepartemanier.getInstance().registerCompletedTask(task.getID());
        System.out.println("[TASK DONE] " + name + " finished task " + task.getID());

        this.task = new Task.Builder()
                .setID(task.getID())
                .setFloor(0)
                .setPriority(task.getPriority())
                .setDuration(0)
                .build();

        System.out.println("[WAITING] " + name + " is waiting to go [DOWN] to floor 0");
        ArythraDepartemanier.getInstance().getFloor(currentFloor).requestElevator(this, chosenElevatorID);

        synchronized (this) {
            while (this.isCanBoard()) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {}

        this.currentFloor = 0;
        System.out.println("[ESCAPED!] " + name + " successfully left the m.A.A.d department!");
    }

}