import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ThreadGUI {
    private static JTextArea outputArea;
    private static JFrame frame;

    public static void showGUI() {
        frame = new JFrame("Rezultate Thread-uri");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        outputArea = new JTextArea(20, 60);
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        outputArea.append("Rezultate executie thread-uri:\n");
        outputArea.append("=".repeat(50) + "\n\n");

        JScrollPane scrollPane = new JScrollPane(outputArea);

        JButton startButton = new JButton("Pornește Thread-uri");
        JButton clearButton = new JButton("Șterge Rezultate");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(startButton);
        buttonPanel.add(clearButton);

        frame.add(buttonPanel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);

        startButton.addActionListener((ActionEvent e) -> {
            outputArea.append("Pornesc toate thread-urile...\n\n");
            Main.startAllThreads();
        });

        clearButton.addActionListener((ActionEvent e) -> {
            outputArea.setText("Rezultate executie thread-uri:\n" + "=".repeat(50) + "\n\n");
        });

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        outputArea.append("Porneste Thread-urile !!!\n\n");
    }

    public static void addResult(String text) {
        if (outputArea != null) {
            SwingUtilities.invokeLater(() -> {
                outputArea.append(text + "\n");
                outputArea.setCaretPosition(outputArea.getDocument().getLength());
            });
        }
    }
}