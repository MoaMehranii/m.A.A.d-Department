public class Elevator implements Runnable {
    public enum Type { VIP, PUBLIC, CARRIAGE }

    private final int ID;
    private final float maxWeight;
    private final long transitionTime;
    private final int departmentFloors;
    private final Type type;
    private long totalMoveTime = 0;
    private volatile boolean isTurnedOn;
    private volatile int targetFloor = 0;
    private volatile int currentFloor = 0;
    private volatile boolean isOnAFloor = true;
    private volatile Passenger currentPassenger = null;


    public static class Builder {
        private int ID;
        private Type type;
        private float maxWeight;
        private long transitionTime;
        private int currentFloor;
        private boolean isOnAFloor;
        private int departmentFloors;

        public Builder setID(int ID) {
            this.ID = ID;
            return this;
        }
        public Builder setType(Type type) {
            this.type = type;
            return this;
        }
        public Builder setMaxWeight(float maxWeight) {
            this.maxWeight = maxWeight;
            return this;
        }
        public Builder setTransitionTime(long transitionTime) {
            this.transitionTime = transitionTime;
            return this;
        }
        public Builder setCurrentFloor(int currentFloor) {
            this.currentFloor = currentFloor;
            return this;
        }
        public Builder setIsOnAFloor(boolean isOnAFloor) {
            this.isOnAFloor = isOnAFloor;
            return this;
        }
        public Builder setDepartmentFloors(int departmentFloors) {
            this.departmentFloors = departmentFloors;
            return this; }
        public Elevator build() {
            return new Elevator(this);
        }
    }

    Elevator(Builder builder) {
        this.ID = builder.ID;
        this.type = builder.type;
        this.maxWeight = builder.maxWeight;
        this.transitionTime = builder.transitionTime;
        this.currentFloor = builder.currentFloor;
        this.isOnAFloor = builder.isOnAFloor;
        this.departmentFloors = builder.departmentFloors;
    }

    public void setisTurnedOn(boolean isTurnedOn) {
        this.isTurnedOn = isTurnedOn;
    }
    public int getID() {
        return ID;
    }
    public float getMaxWeight() {
        return maxWeight;
    }
    public long getTransitionTime() {
        return transitionTime;
    }
    public int getCurrentFloor() {
        return currentFloor;
    }
    public Type getType() {
        return type;
    }
    public long getTotalMoveTime() {
        return totalMoveTime;
    }

    @Override
    public void run() {
        System.out.println("[SYSTEM] Elevator " + ID + " with the type of: " + type + " is active.");
        while (isTurnedOn) {
            isOnAFloor = true;

            if (currentPassenger == null) {
                currentPassenger = ArythraDepartemanier.getInstance()
                        .getFloor(currentFloor)
                        .getPassengerForElevator(this);
            }

            if (currentPassenger != null) {
                isOnAFloor = false;
                targetFloor = currentPassenger.getTask().getFloor();

                while (currentFloor != targetFloor && isTurnedOn) {
                    try {
                        Thread.sleep(transitionTime);
                        totalMoveTime += transitionTime;
                    } catch (InterruptedException e) {
                        System.out.println("[INTERRUPTED] Elevator " + ID + " interrupted.");
                        Thread.currentThread().interrupt();
                        break;
                    }
                    if (currentFloor < targetFloor) currentFloor++;
                    else currentFloor--;

                    System.out.println("[MOVING] Elevator " + ID + " reached floor " + currentFloor);
                }

                if (currentFloor == targetFloor && isTurnedOn) {
                    isOnAFloor = true;
                    Passenger p = currentPassenger;
                    currentPassenger = null;
                    ArythraDepartemanier.getInstance().getFloor(currentFloor).dropPassenger(p);
                }
            }
        }
        System.out.println("[SHUTDOWN] Elevator " + ID + " stopped.");
    }
}