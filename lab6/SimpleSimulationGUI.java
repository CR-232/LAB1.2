import javax.swing.*;
import java.awt.*;

public class SimpleSimulationGUI extends JFrame {
    private JTextArea consoleOutput;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private JButton startButton;
    private JScrollPane scrollPane;

    public SimpleSimulationGUI() {
        setTitle("Producer-Consumer Simulation");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Panel principal - doar consola
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Console output panel
        JPanel consolePanel = new JPanel(new BorderLayout());
        consolePanel.setBorder(BorderFactory.createTitledBorder("Console Output"));

        consoleOutput = new JTextArea(25, 80);
        consoleOutput.setEditable(false);
        consoleOutput.setFont(new Font("Monospaced", Font.PLAIN, 12));
        consoleOutput.setLineWrap(false);

        scrollPane = new JScrollPane(consoleOutput);
        consolePanel.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(consolePanel, BorderLayout.CENTER);

        // Control panel
        JPanel controlPanel = new JPanel(new BorderLayout());
        controlPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Top control panel with button
        JPanel topControlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        startButton = new JButton("Start Simulation");
        startButton.setFont(new Font("Arial", Font.BOLD, 14));
        startButton.setPreferredSize(new Dimension(150, 30));
        topControlPanel.add(startButton);

        // Progress bar
        progressBar = new JProgressBar(0, ProducerConsumerSimulation.TOTAL_ITEMS);
        progressBar.setStringPainted(true);
        progressBar.setString("0/" + ProducerConsumerSimulation.TOTAL_ITEMS + " items");
        progressBar.setPreferredSize(new Dimension(300, 25));

        // Status label
        statusLabel = new JLabel("Click 'Start Simulation' to begin");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));

        // Layout pentru controale
        JPanel centerControlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        centerControlPanel.add(progressBar);

        controlPanel.add(topControlPanel, BorderLayout.NORTH);
        controlPanel.add(centerControlPanel, BorderLayout.CENTER);
        controlPanel.add(statusLabel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);

        // Adaugă acțiune pentru buton
        startButton.addActionListener(e -> startSimulation());

        pack();
        setLocationRelativeTo(null);
    }

    private void startSimulation() {
        startButton.setEnabled(false);
        consoleOutput.setText(""); // Curăță consola la fiecare rulare

        // Rulează simularea într-un thread separat pentru a nu bloca GUI
        new Thread(() -> {
            try {
                ProducerConsumerSimulation.runSimulation(this);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
                updateStatus("Simulation interrupted!");
            }
        }).start();
    }

    public void appendToConsole(String text) {
        SwingUtilities.invokeLater(() -> {
            consoleOutput.append(text);
            // Auto-scroll la ultima linie
            consoleOutput.setCaretPosition(consoleOutput.getDocument().getLength());
        });
    }

    public void updateProgress(int value) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setValue(value);
            progressBar.setString(value + "/" + ProducerConsumerSimulation.TOTAL_ITEMS + " items");
        });
    }

    public void updateStatus(String status) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("Status: " + status);
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SimpleSimulationGUI gui = new SimpleSimulationGUI();
            gui.setVisible(true);
        });
    }
}