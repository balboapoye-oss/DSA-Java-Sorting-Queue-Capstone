import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

public class Main {

    // ---------- Data storage (parallel ArrayLists) ----------
    static ArrayList<Integer> regNumbers = new ArrayList<>();   // unique registration number
    static ArrayList<String> names = new ArrayList<>();         // participant name
    static ArrayList<String> regDates = new ArrayList<>();      // registration date (yyyy-MM-dd)
    static ArrayList<String> statuses = new ArrayList<>();      // Registered / In Queue / Checked In

    // ---------- Check-in queue (FIFO) ----------
    static Queue<Integer> checkInQueue = new LinkedList<>();

    static int nextRegNumber = 1001;
    static Scanner input = new Scanner(System.in);

    // Sort field constants
    static final int BY_NAME = 1;
    static final int BY_REG_NUMBER = 2;
    static final int BY_DATE = 3;

    // =====================================================
    //                        MAIN
    // =====================================================
    public static void main(String[] args) {
        boolean running = true;

        System.out.println("=======================================");
        System.out.println("  EVENT REGISTRATION & CHECK-IN SYSTEM ");
        System.out.println("=======================================");

        while (running) {
            showMenu();
            int choice = readInt("Enter your choice: ");

            switch (choice) {
                case 1:
                    registerParticipant();
                    break;
                case 2:
                    displayRecords();
                    break;
                case 3:
                    searchParticipant();
                    break;
                case 4:
                    sortRecords();
                    break;
                case 5:
                    enqueueParticipant();
                    break;
                case 6:
                    processNext();
                    break;
                case 7:
                    peekNext();
                    break;
                case 8:
                    displayQueue();
                    break;
                case 9:
                    checkQueueEmpty();
                    break;
                case 0:
                    running = false;
                    System.out.println("\nThank you for using the system. Goodbye!");
                    break;
                default:
                    System.out.println("\n[!] Invalid choice. Please enter a number from the menu (0-9).");
            }
        }
        input.close();
    }

    // =====================================================
    //                        MENU
    // =====================================================
    static void showMenu() {
        System.out.println("\n------------- MAIN MENU -------------");
        System.out.println(" 1. Register a participant");
        System.out.println(" 2. Display all registered participants");
        System.out.println(" 3. Search for a participant");
        System.out.println(" 4. Sort participant records");
        System.out.println(" 5. Add participant to check-in queue (Enqueue)");
        System.out.println(" 6. Process next participant (Dequeue / Check in)");
        System.out.println(" 7. Peek at next participant in queue");
        System.out.println(" 8. Display check-in waiting list");
        System.out.println(" 9. Check if the queue is empty");
        System.out.println(" 0. Exit");
        System.out.println("-------------------------------------");
    }

    // =====================================================
    //                 RECORD MANAGEMENT
    // =====================================================


    static void registerParticipant() {
        System.out.println("\n--- Register Participant ---");
        String name = readNonEmptyString("Enter full name: ");
        String date = readDate("Enter registration date (yyyy-MM-dd) or press Enter for today: ");

        int regNo = nextRegNumber++;
        regNumbers.add(regNo);
        names.add(name);
        regDates.add(date);
        statuses.add("Registered");

        System.out.println("[OK] " + name + " registered successfully. Registration No.: " + regNo);
    }

    static void displayRecords() {
        System.out.println("\n--- Registered Participants ---");
        if (names.isEmpty()) {
            System.out.println("[!] No participants registered yet.");
            return;
        }
        printTableHeader();
        for (int i = 0; i < names.size(); i++) {
            printRecord(i);
        }
        System.out.println("Total participants: " + names.size());
    }

    static void printTableHeader() {
        System.out.printf("%-10s %-25s %-12s %-12s%n", "Reg No.", "Name", "Reg Date", "Status");
        System.out.println("------------------------------------------------------------");
    }

    static void printRecord(int i) {
        System.out.printf("%-10d %-25s %-12s %-12s%n",
                regNumbers.get(i), names.get(i), regDates.get(i), statuses.get(i));
    }


    static void searchParticipant() {
        System.out.println("\n--- Search Participant ---");
        if (names.isEmpty()) {
            System.out.println("[!] No participants registered yet. Nothing to search.");
            return;
        }
        System.out.println(" 1. Search by registration number");
        System.out.println(" 2. Search by name");
        int option = readInt("Choose search type: ");

        if (option == 1) {
            int target = readInt("Enter registration number: ");
            int index = findIndexByRegNo(target);
            if (index == -1) {
                System.out.println("[!] No participant found with registration number " + target + ".");
            } else {
                printTableHeader();
                printRecord(index);
            }
        } else if (option == 2) {
            String keyword = readNonEmptyString("Enter name (or part of a name): ").toLowerCase();
            boolean found = false;
            for (int i = 0; i < names.size(); i++) {
                if (names.get(i).toLowerCase().contains(keyword)) {
                    if (!found) {
                        printTableHeader();
                        found = true;
                    }
                    printRecord(i);
                }
            }
            if (!found) {
                System.out.println("[!] No participant found matching \"" + keyword + "\".");
            }
        } else {
            System.out.println("[!] Invalid search option.");
        }
    }

    static int findIndexByRegNo(int regNo) {
        for (int i = 0; i < regNumbers.size(); i++) {
            if (regNumbers.get(i) == regNo) {
                return i;
            }
        }
        return -1;
    }


    static void sortRecords() {
        System.out.println("\n--- Sort Participant Records ---");
        if (names.isEmpty()) {
            System.out.println("[!] No participants to sort.");
            return;
        }

        System.out.println("Sort by:");
        System.out.println(" 1. Name");
        System.out.println(" 2. Registration number");
        System.out.println(" 3. Registration date");
        int field = readInt("Choose field: ");
        if (field < 1 || field > 3) {
            System.out.println("[!] Invalid field. Sorting cancelled.");
            return;
        }

        System.out.println("Order:");
        System.out.println(" 1. Ascending");
        System.out.println(" 2. Descending");
        int orderChoice = readInt("Choose order: ");
        if (orderChoice != 1 && orderChoice != 2) {
            System.out.println("[!] Invalid order. Sorting cancelled.");
            return;
        }
        boolean ascending = (orderChoice == 1);

        System.out.println("Algorithm:");
        System.out.println(" 1. Bubble Sort    - simple; stops early when the list is already ordered");
        System.out.println(" 2. Insertion Sort - efficient for small / nearly sorted lists (new sign-ups)");
        System.out.println(" 3. Selection Sort - fewest swaps; consistent performance");
        int algo = readInt("Choose algorithm: ");
        if (algo < 1 || algo > 3) {
            System.out.println("[!] Invalid algorithm. Sorting cancelled.");
            return;
        }

        System.out.println("\nBEFORE sorting:");
        printTableHeader();
        for (int i = 0; i < names.size(); i++) {
            printRecord(i);
        }

        if (algo == 1) {
            bubbleSort(field, ascending);
        } else if (algo == 2) {
            insertionSort(field, ascending);
        } else {
            selectionSort(field, ascending);
        }

        System.out.println("\nAFTER sorting:");
        printTableHeader();
        for (int i = 0; i < names.size(); i++) {
            printRecord(i);
        }
        System.out.println("[OK] Records sorted. The check-in queue order was not affected.");
    }


    static int compareRecords(int a, int b, int field, boolean ascending) {
        int result;
        if (field == BY_NAME) {
            result = names.get(a).compareToIgnoreCase(names.get(b));
        } else if (field == BY_REG_NUMBER) {
            result = Integer.compare(regNumbers.get(a), regNumbers.get(b));
        } else {
            result = regDates.get(a).compareTo(regDates.get(b)); // yyyy-MM-dd sorts correctly as text
        }
        return ascending ? result : -result;
    }


    static void swapRecords(int i, int j) {
        int tempReg = regNumbers.get(i);
        regNumbers.set(i, regNumbers.get(j));
        regNumbers.set(j, tempReg);

        String tempName = names.get(i);
        names.set(i, names.get(j));
        names.set(j, tempName);

        String tempDate = regDates.get(i);
        regDates.set(i, regDates.get(j));
        regDates.set(j, tempDate);

        String tempStatus = statuses.get(i);
        statuses.set(i, statuses.get(j));
        statuses.set(j, tempStatus);
    }


    static void bubbleSort(int field, boolean ascending) {
        int n = names.size();
        for (int pass = 0; pass < n - 1; pass++) {
            boolean swapped = false;
            for (int i = 0; i < n - 1 - pass; i++) {
                if (compareRecords(i, i + 1, field, ascending) > 0) {
                    swapRecords(i, i + 1);
                    swapped = true;
                }
            }
            if (!swapped) {
                break; // already sorted
            }
        }
    }

    static void insertionSort(int field, boolean ascending) {
        int n = names.size();
        for (int i = 1; i < n; i++) {
            int j = i;
            while (j > 0 && compareRecords(j - 1, j, field, ascending) > 0) {
                swapRecords(j - 1, j);
                j--;
            }
        }
    }


    static void selectionSort(int field, boolean ascending) {
        int n = names.size();
        for (int i = 0; i < n - 1; i++) {
            int minIndex = i;
            for (int j = i + 1; j < n; j++) {
                if (compareRecords(minIndex, j, field, ascending) > 0) {
                    minIndex = j;
                }
            }
            if (minIndex != i) {
                swapRecords(i, minIndex);
            }
        }
    }




    static void enqueueParticipant() {
        System.out.println("\n--- Add to Check-In Queue ---");
        if (names.isEmpty()) {
            System.out.println("[!] No participants registered yet. Register someone first.");
            return;
        }
        int regNo = readInt("Enter registration number to queue: ");
        int index = findIndexByRegNo(regNo);

        if (index == -1) {
            System.out.println("[!] No participant found with registration number " + regNo + ".");
            return;
        }
        if (statuses.get(index).equals("Checked In")) {
            System.out.println("[!] " + names.get(index) + " has already checked in.");
            return;
        }
        if (checkInQueue.contains(regNo)) {
            System.out.println("[!] " + names.get(index) + " is already in the queue.");
            return;
        }

        checkInQueue.add(regNo);
        statuses.set(index, "In Queue");
        System.out.println("[OK] " + names.get(index) + " added to the queue. Position: " + checkInQueue.size());
    }


    static void processNext() {
        System.out.println("\n--- Process Next Participant ---");
        if (checkInQueue.isEmpty()) {
            System.out.println("[!] The check-in queue is empty. No one to process.");
            return;
        }
        int regNo = checkInQueue.poll();
        int index = findIndexByRegNo(regNo);
        statuses.set(index, "Checked In");
        System.out.println("[OK] Now checking in: " + names.get(index) + " (Reg No. " + regNo + ")");
        System.out.println("     Participants still waiting: " + checkInQueue.size());
    }


    static void peekNext() {
        System.out.println("\n--- Next in Line ---");
        if (checkInQueue.isEmpty()) {
            System.out.println("[!] The check-in queue is empty.");
            return;
        }
        int regNo = checkInQueue.peek();
        int index = findIndexByRegNo(regNo);
        System.out.println("Next participant: " + names.get(index) + " (Reg No. " + regNo + ")");
    }


    static void displayQueue() {
        System.out.println("\n--- Check-In Waiting List ---");
        if (checkInQueue.isEmpty()) {
            System.out.println("[!] The check-in queue is empty.");
            return;
        }
        System.out.printf("%-6s %-10s %-25s%n", "Pos.", "Reg No.", "Name");
        System.out.println("---------------------------------------------");
        int position = 1;
        for (int regNo : checkInQueue) { // iterates front -> back without removing items
            int index = findIndexByRegNo(regNo);
            System.out.printf("%-6d %-10d %-25s%n", position, regNo, names.get(index));
            position++;
        }
        System.out.println("Total waiting: " + checkInQueue.size());
    }

    static void checkQueueEmpty() {
        System.out.println();
        if (checkInQueue.isEmpty()) {
            System.out.println("The check-in queue is EMPTY.");
        } else {
            System.out.println("The check-in queue is NOT empty. Waiting: " + checkInQueue.size());
        }
    }



 
    static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = input.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("[!] Invalid input. Please enter a whole number.");
            }
        }
    }


    static String readNonEmptyString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = input.nextLine().trim();
            if (!line.isEmpty()) {
                return line;
            }
            System.out.println("[!] Input cannot be empty.");
        }
    }


    static String readDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = input.nextLine().trim();
            if (line.isEmpty()) {
                return LocalDate.now().toString();
            }
            try {
                LocalDate parsed = LocalDate.parse(line); // rejects impossible dates like 2026-02-31
                return parsed.toString();
            } catch (Exception e) {
                System.out.println("[!] Invalid date. Use the format yyyy-MM-dd (e.g., 2026-10-29).");
            }
        }
    }
}