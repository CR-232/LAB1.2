package Lab3.java;

import javax.swing.*;
import java.awt.*;

public class Lab3GUI extends JFrame {

    private JTextArea textArea;

    public Lab3GUI(String title) {
        setTitle(title);
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        textArea = new JTextArea();
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        textArea.setEditable(false);

        add(new JScrollPane(textArea));
        setVisible(true);
    }

    public void appendText(String text) {
        SwingUtilities.invokeLater(() -> textArea.append(text));
    }
}