import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;

public class TodoGUI extends JFrame {
    static final String FILE_NAME = "tasks.txt";

    ArrayList<Task> tasks = new ArrayList<>();
    DefaultListModel<String> listModel = new DefaultListModel<>();
    JList<String> taskList = new JList<>(listModel);
    JTextField titleField = new JTextField(12);
    JComboBox<String> priorityBox =
            new JComboBox<>(new String[]{"High", "Medium", "Low"});
    JTextField dateField = new JTextField(8);

    public TodoGUI() {
        setTitle("To-Do App");
        setSize(700, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel top = new JPanel();
        top.add(new JLabel("Task:"));
        top.add(titleField);
        top.add(new JLabel("Priority:"));
        top.add(priorityBox);
        top.add(new JLabel("Due (yyyy-MM-dd):"));
        top.add(dateField);
        JButton addButton = new JButton("Add");
        top.add(addButton);

        JPanel bottom = new JPanel();
        JButton doneButton = new JButton("Mark done");
        JButton deleteButton = new JButton("Delete");
        bottom.add(doneButton);
        bottom.add(deleteButton);
        JButton statsButton = new JButton("Stats");
        bottom.add(statsButton);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(taskList), BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        addButton.addActionListener(e -> addTask());
        doneButton.addActionListener(e -> markDone());
        deleteButton.addActionListener(e -> deleteTask());
        statsButton.addActionListener(e -> showStats());

        taskList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list,
                                                          Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                if (index >= 0 && index < tasks.size()
                        && tasks.get(index).isOverdue() && !isSelected) {
                    c.setForeground(Color.RED);
                }
                return c;
            }
        });


        loadTasks();
        refreshList();
        setVisible(true);
    }

    void addTask() {
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a task.");
            return;
        }
        int priority = priorityBox.getSelectedIndex() + 1;
        LocalDate due = null;
        String d = dateField.getText().trim();
        if (!d.isEmpty()) {
            try {
                due = LocalDate.parse(d);
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "Invalid date. Use yyyy-MM-dd.");
                return;
            }
        }
        tasks.add(new Task(title, priority, due));
        saveTasks();
        refreshList();
        titleField.setText("");
        dateField.setText("");
    }

    void markDone() {
        int i = taskList.getSelectedIndex();
        if (i == -1) {
            JOptionPane.showMessageDialog(this, "Select a task first.");
            return;
        }
        tasks.get(i).done = true;
        saveTasks();
        refreshList();
    }

    void deleteTask() {
        int i = taskList.getSelectedIndex();
        if (i == -1) {
            JOptionPane.showMessageDialog(this, "Select a task first.");
            return;
        }
        tasks.remove(i);
        saveTasks();
        refreshList();
    }
    void showStats() {
        int done=0,pending=0,overdue=0;
            for (Task t : tasks) {
                if (t.done) done++; else pending++;
                if (t.isOverdue()) overdue++;
            }
            int percent = tasks.isEmpty() ? 0 : done * 100 / tasks.size();
            JOptionPane.showMessageDialog(this,
                    "Total: " + tasks.size()
                            + "\nDone: " + done + " (" + percent + "%)"
                            + "\nPending: " + pending
                            + "\nOverdue: " + overdue,
                    "Stats", JOptionPane.INFORMATION_MESSAGE);
        }
    
    void refreshList() {
        tasks.sort(Comparator.comparing((Task t) -> t.done)
                .thenComparingInt(t -> t.priority));
        listModel.clear();
        for (Task t : tasks) {
            String line = (t.done ? "[x] " : "[ ] ") + t.title
                    + " [" + t.priorityName() + "]";
            if (t.dueDate != null) line += " (due " + t.dueDate + ")";
            if (t.isOverdue()) line += " << OVERDUE";
            listModel.addElement(line);
        }
    }

    void saveTasks() {
        try (PrintWriter out = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (Task t : tasks) {
                String due = (t.dueDate == null) ? "none" : t.dueDate.toString();
                out.println(t.done + "|" + t.priority + "|" + due + "|" + t.title);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Could not save tasks.");
        }
    }

    void loadTasks() {
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
                }
            }
        } catch (IOException | RuntimeException e) {
            JOptionPane.showMessageDialog(this, "Could not load tasks.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TodoGUI::new);
    }
}