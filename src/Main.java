import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.Random;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Random random = new Random();
        boolean canGoForward = true ;
        int totalFloors = 0;
        int totalElevators = 0;
        System.out.println("====== WELCOME TO THE m.A.A.d DEPARTMENT ======");
        while(canGoForward) {
            System.out.print("Enter total number of floors (N): ");
            try {
                totalFloors = scanner.nextInt();
            } catch (InputMismatchException e) {
                System.out.println("Please enter an integer");
                scanner.next();
                continue;
            }


            System.out.print("Enter total number of elevators (M): ");
            try{
                totalElevators = scanner.nextInt();
            } catch (InputMismatchException e) {
                System.out.println("Please enter an integer");
                scanner.next();
                continue;
            }
            if (totalElevators <= 0 || totalFloors <= 1) {
                System.out.println("INVALID INPUT. TRY AGAIN");
            }
            else {
                canGoForward = false;
            }
        }
        ArythraDepartemanier controller = ArythraDepartemanier.getInstance();
        controller.initializeBuilding(totalFloors, totalElevators);

        System.out.println("Fleet deployed. Generating and entering passengers...\n");

        int passengerCount = 6;
        String[] names = {"Dr.Shahshahani", "Eng. Khazaei", "Mmd Mehrani", "Student Dude", "Big Mike (Porter)", "Dr. Rezaei"};
        Passenger.Role[] roles = {Passenger.Role.PROFESSOR, Passenger.Role.PROVOST, Passenger.Role.STUDENT, Passenger.Role.STUDENT, Passenger.Role.PORTER, Passenger.Role.PROFESSOR};

        for (int i = 0; i < passengerCount; i++) {
            int randomAge = 18 + random.nextInt(62);
            float randomWeight = 60f + random.nextFloat() * 40f;
            int targetFloor = 1 + random.nextInt(totalFloors - 1);

            Task.Priority priority = Task.Priority.values()[random.nextInt(Task.Priority.values().length)];

            float randomDuration = 1000f + random.nextFloat() * 3000f;

            Task passengerTask = new Task.Builder()
                    .setID("TASK-ID-" + (100 + i))
                    .setFloor(targetFloor)
                    .setPriority(priority)
                    .setDuration(randomDuration)
                    .build();

            Passenger passenger = new Passenger.Builder()
                    .setName(names[i])
                    .setAge(randomAge)
                    .setWeight(randomWeight)
                    .setRole(roles[i])
                    .setTask(passengerTask)
                    .build();

            controller.enterPassenger(passenger);

            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        try {
            System.out.println("\nSimulation running...\n");
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        controller.shutdownSystem();

        scanner.close();
    }
}