import javax.swing.*;
import java.awt.*;

public class Lab3GUI extends JFrame {
    private JTextArea textArea;

    public Lab3GUI(String title) {
        setTitle(title);
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        textArea = new JTextArea();
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        textArea.setEditable(false);
        textArea.setBackground(new Color(240, 240, 240));
        textArea.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        add(scrollPane);
        setVisible(true);
    }

    public void appendText(String text) {
        SwingUtilities.invokeLater(() -> {
            textArea.append(text);
            // Auto-scroll to bottom
            textArea.setCaretPosition(textArea.getDocument().getLength());
        });
    }
}