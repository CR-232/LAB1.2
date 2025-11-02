import javax.swing.*;
import java.awt.*;

public class ThreadGUI extends JFrame {
    private JTextArea outputArea;

    public ThreadGUI() {
        setTitle("Structura Fire - Varianta 3");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(null);
        setupInterface();
    }

    private void setupInterface() {
        JPanel panel = new JPanel(new BorderLayout());

        outputArea = new JTextArea();
        outputArea.setFont(new Font("Arial", Font.PLAIN, 14));
        JScrollPane scroll = new JScrollPane(outputArea);
        panel.add(scroll, BorderLayout.CENTER);

        JButton showButton = new JButton("Arata Structura");
        JButton startButton = new JButton("Porneste Firele");

        showButton.addActionListener(e -> showStructure());
        startButton.addActionListener(e -> startThreads());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(showButton);
        buttonPanel.add(startButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(panel);
    }

    private void showStructure() {
        outputArea.setText("=== STRUCTURA VARIANTA 3 ===\n\n");

        outputArea.append("Main {\n");
        outputArea.append("  GN {\n");
        outputArea.append("    GH {\n");
        outputArea.append("      Tha(4)\n");
        outputArea.append("      Thb(3)\n");
        outputArea.append("      Thc(6)\n");
        outputArea.append("      Thd(3)\n");
        outputArea.append("    }\n");
        outputArea.append("    ThA(3)\n");
        outputArea.append("  }\n");
        outputArea.append("  GM {\n");
        outputArea.append("    Th1(2)\n");
        outputArea.append("    Th2(3)\n");
        outputArea.append("    Th3(3)\n");
        outputArea.append("  }\n");
        outputArea.append("  Th1(8)\n");
        outputArea.append("  Th2(3)\n");
        outputArea.append("}\n");
    }

    private void startThreads() {
        outputArea.append("\n=== FIRELE SE EXECUTA ===\n");

        ThreadGroup gnGroup = new ThreadGroup("GN");
        ThreadGroup ghGroup = new ThreadGroup(gnGroup, "GH");
        ThreadGroup gmGroup = new ThreadGroup("GM");

        createThread(ghGroup, "Tha", 4);
        createThread(ghGroup, "Thb", 3);
        createThread(ghGroup, "Thc", 6);
        createThread(ghGroup, "Thd", 3);
        createThread(gnGroup, "ThA", 3);
        createThread(gmGroup, "Th1", 2);
        createThread(gmGroup, "Th2", 3);
        createThread(gmGroup, "Th3", 3);
        createThread(Thread.currentThread().getThreadGroup(), "Th1", 8);
        createThread(Thread.currentThread().getThreadGroup(), "Th2", 3);
    }

    private void createThread(ThreadGroup group, String name, int priority) {
        Thread thread = new Thread(group, name) {
            public void run() {
                String output = "Fir: " + getName() + " | Grup: " + getThreadGroup().getName() + " | Prioritate: " + getPriority();
                System.out.println(output);

                SwingUtilities.invokeLater(() -> {
                    outputArea.append(output + "\n");
                });
            }
        };

        thread.setPriority(priority);
        thread.start();

        outputArea.append("Pornit: " + name + " (prioritate " + priority + ")\n");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ThreadGUI().setVisible(true);
        });
    }
}
