import javax.swing.*;
import java.awt.*;

public class GUI extends JFrame {
    private JTextArea consoleOutput;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private JButton startButton;
    private JScrollPane scrollPane;

    public GUI() {
        setTitle("Producer-Consumer Simulation");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel consolePanel = new JPanel(new BorderLayout());
        consolePanel.setBorder(BorderFactory.createTitledBorder("Console Output"));

        consoleOutput = new JTextArea(20, 70);
        consoleOutput.setEditable(false);
        consoleOutput.setFont(new Font("Monospaced", Font.PLAIN, 12));
        consoleOutput.setLineWrap(false);

        scrollPane = new JScrollPane(consoleOutput);
        consolePanel.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(consolePanel, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        startButton = new JButton("Start Simulation");
        startButton.setFont(new Font("Arial", Font.BOLD, 14));
        startButton.setBackground(new Color(70, 130, 180));
        startButton.setForeground(Color.WHITE);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(startButton);
        controlPanel.add(buttonPanel);

        progressBar = new JProgressBar(0, Main.TOTAL_ITEMS);
        progressBar.setStringPainted(true);
        progressBar.setString("0/" + Main.TOTAL_ITEMS);
        progressBar.setForeground(new Color(50, 205, 50));
        JPanel progressPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        progressPanel.add(progressBar);
        controlPanel.add(progressPanel);

        statusLabel = new JLabel("Ready to start simulation");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        statusPanel.add(statusLabel);
        controlPanel.add(statusPanel);

        add(mainPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);

        startButton.addActionListener(e -> startSimulation());

        pack();
        setLocationRelativeTo(null);
    }

    private void startSimulation() {
        startButton.setEnabled(false);
        consoleOutput.setText("");
        progressBar.setValue(0);
        statusLabel.setText("Starting simulation...");

        new Thread(() -> {
            try {
                Main.runSimulation(this);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
                updateStatus("Simulation interrupted!");
            } finally {
                SwingUtilities.invokeLater(() -> {
                    startButton.setEnabled(true);
                });
            }
        }).start();
    }

    public void appendToConsole(String text) {
        SwingUtilities.invokeLater(() -> {
            consoleOutput.append(text);
            consoleOutput.setCaretPosition(consoleOutput.getDocument().getLength());
        });
    }

    public void updateProgress(int value) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setValue(value);
            progressBar.setString(value + "/" + Main.TOTAL_ITEMS);
        });
    }

    public void updateStatus(String status) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("Status: " + status);
        });
    }
}