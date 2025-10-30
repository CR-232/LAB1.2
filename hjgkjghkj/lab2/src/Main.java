import javax.swing.*;
import java.awt.*;

public class Main {
    static class MyThread extends Thread {
        private final JTextArea outputArea;
        private final int executionOrder;

        public MyThread(ThreadGroup group, String name, int priority, JTextArea outputArea, int executionOrder) {
            super(group, name);
            this.setPriority(priority);
            this.outputArea = outputArea;
            this.executionOrder = executionOrder;
        }

        @Override
        public void run() {
            SwingUtilities.invokeLater(() -> outputArea.append(
                    executionOrder + ". Thread: " + getName() +
                            " | Group: " + getThreadGroup().getName() +
                            " | Priority: " + getPriority() + "\n"
            ));
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                SwingUtilities.invokeLater(() ->
                        outputArea.append(getName() + " a fost întrerupt.\n"));
            }
        }
    }

    static class ThreadGUI extends JFrame {
        private final JTextArea outputArea;

        public ThreadGUI() {
            setTitle("Structura Firelor de Execuție — Cu ordine de execuție");
            setSize(600, 500);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLayout(new BorderLayout(10, 10));

            JLabel title = new JLabel("Firele vor rula în ordinea specificată în structură", SwingConstants.CENTER);
            title.setFont(new Font("Arial", Font.BOLD, 15));

            JButton startButton = new JButton("Start Threads");
            outputArea = new JTextArea();
            outputArea.setEditable(false);
            outputArea.setFont(new Font("Consolas", Font.PLAIN, 13));

            JScrollPane scrollPane = new JScrollPane(outputArea);
            add(title, BorderLayout.NORTH);
            add(scrollPane, BorderLayout.CENTER);
            add(startButton, BorderLayout.SOUTH);

            startButton.addActionListener(e -> runThreads());

            setVisible(true);
        }

        private void runThreads() {
            outputArea.setText("Pornire fire în ordine:\n\n");

            ThreadGroup mainGroup = Thread.currentThread().getThreadGroup();

            ThreadGroup GE = new ThreadGroup(mainGroup, "GE");

            ThreadGroup GH = new ThreadGroup(GE, "GH");

            MyThread Th1_main = new MyThread(mainGroup, "Th1", 3, outputArea, 1);

            MyThread Tha = new MyThread(GH, "Tha", 4, outputArea, 2);
            MyThread Thb = new MyThread(GH, "Thb", 3, outputArea, 3);
            MyThread Thc = new MyThread(GH, "Thc", 2, outputArea, 4);
            MyThread Thd = new MyThread(GH, "Thd", 1, outputArea, 5);

            MyThread ThA = new MyThread(GE, "ThA", 3, outputArea, 6);

            ThreadGroup GK = new ThreadGroup(mainGroup, "GK");
            MyThread Th1_GK = new MyThread(GK, "Th1_GK", 3, outputArea, 7);
            MyThread Th2_GK = new MyThread(GK, "Th2_GK", 6, outputArea, 8);
            MyThread Th3_GK = new MyThread(GK, "Th3_GK", 3, outputArea, 9);

            MyThread Th2_main = new MyThread(mainGroup, "Th2", 7, outputArea, 10);

            startThreadInOrder(Th1_main, 1);
            startThreadInOrder(Tha, 2);
            startThreadInOrder(Thb, 3);
            startThreadInOrder(Thc, 4);
            startThreadInOrder(Thd, 5);
            startThreadInOrder(ThA, 6);
            startThreadInOrder(Th1_GK, 7);
            startThreadInOrder(Th2_GK, 8);
            startThreadInOrder(Th3_GK, 9);
            startThreadInOrder(Th2_main, 10);

            new Thread(() -> {
                try {
                    Thread.sleep(2500);
                } catch (InterruptedException ignored) {}
                SwingUtilities.invokeLater(() -> {
                    outputArea.append("\n--- Structura completă ThreadGroup ---\n");
                    listGroups(mainGroup, "  ");
                });
            }).start();
        }

        private void startThreadInOrder(MyThread thread, int order) {
            new Thread(() -> {
                try {
                    Thread.sleep((order - 1) * 250);
                    thread.start();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }

        private void listGroups(ThreadGroup group, String indent) {
            int numGroups = group.activeGroupCount();
            int numThreads = group.activeCount();

            ThreadGroup[] groups = new ThreadGroup[numGroups];
            Thread[] threads = new Thread[numThreads];

            group.enumerate(groups, false);
            group.enumerate(threads, false);

            outputArea.append(indent + "Grup: " + group.getName() + "\n");
            for (Thread t : threads) {
                if (t != null)
                    outputArea.append(indent + "  └── Fir: " + t.getName() +
                            " (Prioritate: " + t.getPriority() + ")\n");
            }

            for (ThreadGroup g : groups) {
                if (g != null)
                    listGroups(g, indent + "  ");
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ThreadGUI::new);
    }
}
