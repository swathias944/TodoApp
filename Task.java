import java.time.LocalDate;

public class Task {
    String title;
    boolean done;
    int priority;        // 1 = High, 2 = Medium, 3 = Low
    LocalDate dueDate;   // can be null

    Task(String title) {
        this(title, 2, null);
    }

    Task(String title, int priority, LocalDate dueDate) {
        this.title = title;
        this.priority = priority;
        this.dueDate = dueDate;
        this.done = false;
    }

    boolean isOverdue() {
        return !done && dueDate != null && dueDate.isBefore(LocalDate.now());
    }

    String priorityName() {
        if (priority == 1) return "High";
        if (priority == 3) return "Low";
        return "Medium";
    }
}