import javax.swing.*;

class Lab3GUI {
    private JFrame frame;
    private JTextArea textArea;

    public Lab3GUI(String title) {
        frame = new JFrame(title);
        textArea = new JTextArea(20, 50);
        textArea.setEditable(false);

        frame.add(new JScrollPane(textArea));
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public void appendText(String text) {
        SwingUtilities.invokeLater(() -> textArea.append(text));
    }
}