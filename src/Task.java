public class Task{
    public enum Priority{
        LOW,
        MEDIUM,
        HIGH;
    }
    private final String ID;
    private final int floor;
    private final Priority priority;
    private final float duration;

    //---builder---
    public static class Builder{
        private String ID;
        private int floor;
        private Task.Priority priority;
        private float duration;
        public Builder setID(String ID){
            this.ID = ID;
            return this;
        }
        public Builder setFloor(int floor){
            this.floor = floor;
            return this;
        }
        public Builder setPriority(Task.Priority priority){
            this.priority = priority;
            return this;
        }
        public Builder setDuration(float duration){
            this.duration = duration;
            return this;
        }
        public Task build() {
            return new Task(this);
        }
    }
    private Task(Task.Builder builder){
        this.ID = builder.ID;
        this.floor = builder.floor;
        this.priority = builder.priority;
        this.duration = builder.duration;
    }

    //---get functions---
    public float getDuration() {
        return duration;
    }

    public int getFloor() {
        return floor;
    }

    public Priority getPriority() {
        return priority;
    }

    public String getID() {
        return ID;
    }
}
