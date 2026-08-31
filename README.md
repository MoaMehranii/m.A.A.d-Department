# m.A.A.d Department — Elevator Simulation

A concurrent Java simulation of an intelligent elevator system designed to manage passengers with different roles, priorities, weights, and tasks.

The simulation models multiple elevators operating concurrently across several floors. Passengers are scheduled using priority queues with a custom scoring system that combines role, age, task priority, and waiting time.

The project focuses on **multithreading, synchronization, priority scheduling, starvation prevention, and object-oriented design**.

## Features

### 🛗 Multi-Elevator Simulation

- Supports an arbitrary number of floors
- Supports multiple elevators running concurrently
- Each elevator operates in its own thread
- Elevators independently pick up and transport passengers
- Tracks total movement time for each elevator

### 👥 Passenger Management

Each passenger has:

- Name
- Role
- Age
- Weight
- Current floor
- Assigned task
- Task priority
- Task duration

Supported passenger roles:

```text
STUDENT
PROFESSOR
PROVOST
PORTER
````

### 🚦 Elevator Types

The simulation includes three elevator types:

```text
VIP
PUBLIC
CARRIAGE
```

Each type has different passenger preferences.

| Elevator Type | Preferred Passengers             |
| ------------- | -------------------------------- |
| VIP           | Professors / Provosts            |
| PUBLIC        | Students / Professors / Provosts |
| CARRIAGE      | Porters                          |

Elevators also have maximum weight capacities.

### ⚖️ Priority-Based Scheduling

Passengers waiting for an elevator are stored in a `PriorityQueue`.

The scheduling algorithm calculates a score using:

* Passenger age
* Passenger role
* Task priority
* Waiting time

Conceptually:

```text
Passenger Score =
    Age Score
  + Role Priority
  + Task Priority
  + Aging Score
```

Higher-scoring passengers are served first.

### ⏳ Starvation Prevention

The scheduling system implements an **aging mechanism**.

As a passenger waits longer, their scheduling score increases:

```text
Waiting Time
     │
     ▼
Aging Score Increases
     │
     ▼
Priority Increases
     │
     ▼
Passenger Eventually Gets Served
```

This prevents low-priority passengers from waiting indefinitely behind higher-priority passengers.

The concept is inspired by scheduling techniques commonly used in operating systems.

### ⚖️ Weight Constraints

Passengers cannot be assigned to an elevator whose maximum capacity is lower than their total weight.

A passenger's total weight is calculated as:

```text
Total Weight = Body Weight + Carrying Weight
```

### 📋 Task System

Each passenger is assigned a task containing:

* Task ID
* Target floor
* Priority
* Duration

Supported priorities:

```text
LOW
MEDIUM
HIGH
```

After reaching the target floor, the passenger performs their task and then requests an elevator to return to the ground floor.

### 📊 Final System Report

When the simulation ends, the system generates a final report containing:

* Total movement time for each elevator
* Successfully completed task IDs

Example:

```text
-----------------------------------------------
ARYTHRA'S REPORTS:
-----------------------------------------------
Elevator 0 with the type of VIP total movement time: 12000 ms
Elevator 1 with the type of PUBLIC total movement time: 8000 ms
Elevator 2 with the type of CARRIAGE total movement time: 14000 ms

SUCCESSFUL TASKS REGISTERED!:
    Task ID: TASK-ID-100
    Task ID: TASK-ID-101
    Task ID: TASK-ID-102
-----------------------------------------------
```

## Architecture

The system is organized around several concurrent components:

```text
                         ┌─────────────────────┐
                         │        Main         │
                         └──────────┬──────────┘
                                    │
                                    ▼
                       ┌────────────────────────┐
                       │ ArythraDepartemanier   │
                       │       Controller       │
                       └───────────┬────────────┘
                                   │
                  ┌────────────────┼────────────────┐
                  │                │                │
                  ▼                ▼                ▼
          ┌─────────────┐  ┌─────────────┐  ┌─────────────┐
          │  Elevator   │  │  Elevator   │  │  Elevator   │
          │   Thread    │  │   Thread    │  │   Thread    │
          └──────┬──────┘  └──────┬──────┘  └──────┬──────┘
                 │                │                │
                 └────────────────┼────────────────┘
                                  │
                                  ▼
                         ┌─────────────────┐
                         │  FloorMonitor   │
                         │                 │
                         │ PriorityQueues  │
                         └────────┬────────┘
                                  │
                                  ▼
                            ┌───────────┐
                            │ Passenger │
                            │  Threads  │
                            └───────────┘
```

## Concurrency Model

The simulation uses multiple independent threads.

### Elevator Threads

Every elevator is represented by an `Elevator` object implementing `Runnable`.

```text
Elevator 0 → Elevator-Thread-0
Elevator 1 → Elevator-Thread-1
Elevator 2 → Elevator-Thread-2
...
```

Each elevator continuously:

1. Checks its current floor
2. Looks for a waiting passenger
3. Selects a passenger from its queue
4. Moves toward the target floor
5. Drops the passenger
6. Becomes available for another passenger

### Passenger Threads

Every passenger also runs in its own thread.

A passenger:

1. Enters the department
2. Selects a suitable elevator
3. Joins the elevator queue
4. Waits until boarding is allowed
5. Travels to the target floor
6. Performs their assigned task
7. Requests an elevator to return
8. Returns to the ground floor
9. Leaves the department

## Synchronization

Because elevators and passengers operate concurrently, shared state must be synchronized.

The project uses Java synchronization mechanisms such as:

```java
synchronized
wait()
notifyAll()
volatile
Collections.synchronizedList()
```

### Passenger ↔ Elevator Coordination

A passenger waits until an elevator selects them:

```text
Passenger
    │
    ▼
PriorityQueue
    │
    │ wait()
    ▼
Elevator selects passenger
    │
    │ notifyAll()
    ▼
Passenger boards
```

The same mechanism is used when passengers arrive at their destination and wait for the elevator to complete the trip.

### Thread-Safe Task Registration

Completed tasks are stored using:

```java
Collections.synchronizedList(...)
```

This allows multiple passenger threads to register completed tasks safely.

## Scheduling Algorithm

The elevator queues use a custom comparator.

The priority score is based on:

```text
Score =
    age × 30
  + rolePriority × 200
  + taskPriority × 50
  + agingScore
```

The aging component increases over time:

```text
agingScore = (waitingTime / 200) × 10
```

This means that even a passenger with a relatively low initial priority can eventually move ahead in the queue if they have waited long enough.

### Example

```text
Passenger A
Role: STUDENT
Priority: LOW
Waiting: 10 seconds

Passenger B
Role: PROFESSOR
Priority: HIGH
Waiting: 1 second
```

Initially:

```text
B > A
```

But as A continues waiting:

```text
Aging Score ↑
      │
      ▼
A's Total Score ↑
      │
      ▼
A Eventually Receives Service
```

This is a practical implementation of **aging-based priority scheduling**.

## Elevator Selection

Before requesting an elevator, passengers are assigned an elevator according to their role and weight.

The system considers:

1. Passenger weight
2. Passenger role
3. Elevator type

Selection rules include:

```text
Professor / Provost
        │
        ▼
      VIP

Student / non-Porter
        │
        ▼
     PUBLIC

Porter
        │
        ▼
    CARRIAGE
```

If the passenger cannot use the preferred elevator because of its weight capacity, other suitable elevators are considered.

## Design Patterns

### Singleton

`ArythraDepartemanier` uses the initialization-on-demand holder idiom:

```java
private static class Helper {
    private static final ArythraDepartemanier INSTANCE =
        new ArythraDepartemanier();
}
```

This provides a single central controller for the simulation.

### Builder

`Passenger`, `Task`, and `Elevator` use the Builder pattern.

Example:

```java
Passenger passenger = new Passenger.Builder()
        .setName("Student Dude")
        .setAge(22)
        .setWeight(75)
        .setRole(Passenger.Role.STUDENT)
        .setTask(task)
        .build();
```

This simplifies construction of objects with multiple parameters.

### Strategy-like Scheduling

Passenger selection logic is encapsulated in a custom `Comparator`, allowing the queue to determine which passenger should be served next based on a calculated priority score.

## Project Structure

```text
src/
├── Main.java
├── ArythraDepartemanier.java
├── Elevator.java
├── Passenger.java
├── FloorMonitor.java
└── Task.java
```

### `Main`

Entry point of the application.

Responsibilities:

* Read simulation configuration
* Initialize the building
* Generate passengers
* Start the simulation
* Shut down the system
* Display the final report

### `ArythraDepartemanier`

Central controller of the simulation.

Responsibilities:

* Initialize floors and elevators
* Manage elevator threads
* Manage passenger threads
* Register completed tasks
* Select suitable elevators
* Coordinate system shutdown
* Generate the final report

### `Elevator`

Represents an elevator and implements `Runnable`.

Responsibilities:

* Run the elevator thread
* Pick up passengers
* Move between floors
* Track movement time
* Drop passengers at their destination

### `Passenger`

Represents a passenger and implements `Runnable`.

Responsibilities:

* Request elevators
* Wait for boarding
* Perform assigned tasks
* Return to the ground floor
* Track passenger state

### `FloorMonitor`

Manages passenger queues for a specific floor.

Responsibilities:

* Maintain one priority queue per elevator
* Add passengers to queues
* Select passengers for elevators
* Coordinate waiting passengers and elevators
* Apply priority scheduling

### `Task`

Represents work assigned to a passenger.

Contains:

```text
Task ID
Target Floor
Priority
Duration
```

## Simulation Flow

A typical passenger lifecycle looks like this:

```text
        Passenger Enters
               │
               ▼
       Select Best Elevator
               │
               ▼
        Join Elevator Queue
               │
               ▼
          Wait for Pickup
               │
               ▼
          Board Elevator
               │
               ▼
       Travel to Target Floor
               │
               ▼
          Perform Task
               │
               ▼
       Register Task Result
               │
               ▼
        Request Elevator
               │
               ▼
       Return to Floor 0
               │
               ▼
       Leave Department
```

## Running the Project

### Requirements

* Java JDK 8+
* No external libraries required

### Run

Compile the source files and execute:

```bash
javac src/*.java
java -cp src Main
```

The program asks for:

```text
Enter total number of floors (N):
Enter total number of elevators (M):
```

For example:

```text
Enter total number of floors (N): 5
Enter total number of elevators (M): 4
```

The simulation then automatically generates passengers and starts the elevator system.

## Example Output

```text
====== WELCOME TO THE m.A.A.d DEPARTMENT ======

Enter total number of floors (N): 5
Enter total number of elevators (M): 4

[SYSTEM] Elevator 0 with the type of: VIP is active.
[SYSTEM] Elevator 1 with the type of: PUBLIC is active.
[SYSTEM] Elevator 2 with the type of: CARRIAGE is active.

Fleet deployed. Generating and entering passengers...

[ARRIVED] Dr.Shahshahani (PROFESSOR) entered the m.A.A.d department.
[WAITING] Dr.Shahshahani is waiting to go UP to floor 3
[QUEUED] Dr.Shahshahani joined queue for Elevator 0 on floor 0
[BOARDED] Elevator 0 picked up Dr.Shahshahani on floor 0

[MOVING] Elevator 0 reached floor 1
[MOVING] Elevator 0 reached floor 2
[MOVING] Elevator 0 reached floor 3

[TASK START] Dr.Shahshahani started task TASK-ID-100 on floor 3
[TASK DONE] Dr.Shahshahani finished task TASK-ID-100

[ESCAPED!] Dr.Shahshahani successfully left the m.A.A.d department!
```

## Concepts Demonstrated

This project demonstrates practical implementations of:

* Java multithreading
* `Runnable`
* Thread lifecycle management
* Thread synchronization
* `wait()` / `notifyAll()`
* `volatile`
* Thread-safe collections
* Priority queues
* Custom comparators
* Priority scheduling
* Aging / starvation prevention
* Builder pattern
* Singleton pattern
* Object-oriented design
* Simulation design
* Resource constraints
* Graceful thread shutdown

## Learning Goals

The project was developed to explore concepts commonly encountered in:

* Operating systems
* Concurrent programming
* Scheduling algorithms
* Object-oriented programming
* Thread synchronization
* Resource management

## Future Improvements

Potential improvements include:

* Dynamic elevator reassignment
* More sophisticated elevator selection
* Real-time passenger injection
* Elevator direction optimization
* Multiple passengers per elevator trip
* Better starvation guarantees
* Configurable scheduling weights
* Simulation statistics and visualization
* GUI-based monitoring dashboard

## License

This project is an educational/personal project.

