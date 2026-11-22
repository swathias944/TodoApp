import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Scanner;

public class Main {
    static final String FILE_NAME = "tasks.txt";

    static void sortTasks(ArrayList<Task> tasks) {
        // pending first, then High -> Low priority
        tasks.sort(Comparator.comparing((Task t) -> t.done)
                .thenComparingInt(t -> t.priority));
    }

    static void saveTasks(ArrayList<Task> tasks) {
        try (PrintWriter out = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (Task t : tasks) {
                String due = (t.dueDate == null) ? "none" : t.dueDate.toString();
                out.println(t.done + "|" + t.priority + "|" + due + "|" + t.title);
            }
        } catch (IOException e) {
            System.out.println("Could not save tasks.");
        }
    }

    static void loadTasks(ArrayList<Task> tasks) {
        File file = new File(FILE_NAME);
        if (!file.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|", 4);
                if (parts.length == 4) {
                    Task t = new Task(parts[3]);
                    t.done = Boolean.parseBoolean(parts[0]);
                    t.priority = Integer.parseInt(parts[1]);
                    if (!parts[2].equals("none")) {
                        t.dueDate = LocalDate.parse(parts[2]);
                    }
                    tasks.add(t);
                } else if (parts.length == 2) {
                    Task t = new Task(parts[1]);
                    t.done = Boolean.parseBoolean(parts[0]);
                    tasks.add(t);
                }
            }
        } catch (IOException | RuntimeException e) {
            System.out.println("Could not load tasks.");
        }
    }

    public static void main(String[] args) {
        ArrayList<Task> tasks = new ArrayList<>();
        loadTasks(tasks);
        sortTasks(tasks);
        Scanner sc = new Scanner(System.in);
        int choice = 0;

        while (choice != 6) {
            System.out.println("\n--- To-Do App ---");
            System.out.println("1. Add task");
            System.out.println("2. View tasks");
            System.out.println("3. Mark task as done");
            System.out.println("4. Delete task");
            System.out.println("5. Stats");
            System.out.println("6. Exit");
            System.out.print("Choose: ");
            choice = sc.nextInt();
            sc.nextLine();

            if (choice == 1) {
                System.out.print("Enter task: ");
                String title = sc.nextLine();

                System.out.print("Priority (1=High, 2=Medium, 3=Low): ");
                int p = 2;
                try {
                    p = Integer.parseInt(sc.nextLine().trim());
                } catch (NumberFormatException e) {
                    // keep default
                }
                if (p < 1 || p > 3) p = 2;

                System.out.print("Due date (yyyy-MM-dd) or press Enter to skip: ");
                String d = sc.nextLine().trim();
                LocalDate due = null;
                if (!d.isEmpty()) {
                    try {
                        due = LocalDate.parse(d);
                    } catch (DateTimeParseException e) {
                        System.out.println("Invalid date, skipping due date.");
                    }
                }

                tasks.add(new Task(title, p, due));
                sortTasks(tasks);
                saveTasks(tasks);
                System.out.println("Task added!");

            } else if (choice == 2) {
                sortTasks(tasks);
                if (tasks.isEmpty()) {
                    System.out.println("No tasks yet.");
                }
                for (int i = 0; i < tasks.size(); i++) {
                    Task t = tasks.get(i);
                    String mark = t.done ? "[x]" : "[ ]";
                    String line = (i + 1) + ". " + mark + " " + t.title
                            + " [" + t.priorityName() + "]";
                    if (t.dueDate != null) line += " (due " + t.dueDate + ")";
                    if (t.isOverdue()) line += " << OVERDUE";
                    System.out.println(line);
                }

            } else if (choice == 3) {
                System.out.print("Task number to mark done: ");
                int n = sc.nextInt();
                sc.nextLine();
                if (n >= 1 && n <= tasks.size()) {
                    tasks.get(n - 1).done = true;
                    saveTasks(tasks);
                    System.out.println("Marked as done!");
                } else {
                    System.out.println("Invalid number.");
                }

            } else if (choice == 4) {
                System.out.print("Task number to delete: ");
                int n = sc.nextInt();
                sc.nextLine();
                if (n >= 1 && n <= tasks.size()) {
                    tasks.remove(n - 1);
                    saveTasks(tasks);
                    System.out.println("Task deleted!");
                } else {
                    System.out.println("Invalid number.");
                }

            } else if (choice == 5) {
                int done = 0, pending = 0, overdue = 0;
                for (Task t : tasks) {
                    if (t.done) done++; else pending++;
                    if (t.isOverdue()) overdue++;
                }
                int percent = tasks.isEmpty() ? 0 : done * 100 / tasks.size();
                System.out.println("\n--- Stats ---");
                System.out.println("Total:   " + tasks.size());
                System.out.println("Done:    " + done + " (" + percent + "%)");
                System.out.println("Pending: " + pending);
                System.out.println("Overdue: " + overdue);
            }
        }
        System.out.println("Goodbye!");
    }
}