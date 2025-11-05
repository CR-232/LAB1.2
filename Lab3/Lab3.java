import javax.swing.SwingUtilities;
import java.util.Random;

public class Lab3 {
    private static Lab3GUI gui;
    private static final Object lock = new Object();

    public Lab3(String title) {
        gui = new Lab3GUI(title);
    }

    public static void main(String[] args) throws InterruptedException {
        int[] mas = new int[100];
        Lab3 lab3 = new Lab3("Laborator PCD - CR_232");

        gui.appendText("Tablou generat (10 linii x 10 coloane):\n");
        Random rand = new Random();

        for (int i = 0; i < mas.length; i++) {
            mas[i] = rand.nextInt(1448) + 120;

            if (mas[i] < 1000) {
                gui.appendText(" " + mas[i] + " ");
            } else {
                gui.appendText(mas[i] + " ");
            }

            if ((i + 1) % 10 == 0) {
                gui.appendText("\n");
            }
        }
        gui.appendText("\n");

        ThreadCalcule thread3 = new ThreadCalcule(0, 49, mas, "Th3", lab3.gui);
        ThreadCalcule thread4 = new ThreadCalcule(50, 99, mas, "Th4", lab3.gui);

        thread3.start();
        thread4.start();

        thread3.join();
        thread4.join();

        String message = "Laborator efectuat de elevii din grupa CR-232: Cruc Maxim si Cotoman Vadim la disciplina Programarea concurenta si distribuita\n";
        for (char c : message.toCharArray()) {
            lab3.appendText(String.valueOf(c));
            Thread.sleep(100);
        }
    }

    public void appendText(String s) {
        synchronized (lock) {
            SwingUtilities.invokeLater(() -> gui.appendText(s));
        }
    }

    public static void appendTextWithLock(String s) {
        synchronized (lock) {
            SwingUtilities.invokeLater(() -> gui.appendText(s));
        }
    }
}

class ThreadCalcule extends Thread {
    int startIndex, endIndex;
    int[] mas;
    String nameThread;
    Lab3GUI gui;
    private static final Object printLock = new Object();
    private static final Object calculationLock = new Object();

    public ThreadCalcule(int startIndex, int endIndex, int[] mas, String nameThread, Lab3GUI gui) {
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.mas = mas;
        this.nameThread = nameThread;
        this.gui = gui;
    }

    @Override
    public void run() {
        int S1 = 0, S2 = 0, k = 0;
        int count = 0;

        printToGUI(nameThread + " a început execuția pe intervalul [" + startIndex + ", " + endIndex + "]\n");

        for (int i = startIndex; i <= endIndex; i++) {
            try {
                synchronized (printLock) {
                    Thread.sleep((int) (Math.random() * 40 + 10));
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            synchronized (calculationLock) {
                if (mas[i] >= 120 && mas[i] <= 690 && mas[i] % 2 == 0) {
                    if (k == 0) {
                        S1 = mas[i];
                        k++;
                    } else {
                        S2 = mas[i];
                        int S = S1 + S2;
                        count++;
                        printToGUI(nameThread + " -> Suma " + count + ": " + S1 + " + " + S2 + " = " + S +
                                " (poziții: " + findFirstPosition(S1, i) + ", " + i + ")\n");
                        S1 = S2 = 0;
                        k = 0;
                    }
                }
            }
        }

        synchronized (printLock) {
            if (k == 1) {
                printToGUI(nameThread + " -> Valoare pară rămasă singură: " + S1 + " (poziție: " + findFirstPosition(S1, endIndex) + ")\n");
            }
        }

        Lab3.appendTextWithLock(nameThread + " -> Total sume calculate: " + count + "\n");
        printToGUI(nameThread + " a terminat execuția.\n");
    }

    private void printToGUI(String text) {
        if (Math.random() > 0.5) {
            synchronized (printLock) {
                SwingUtilities.invokeLater(() -> gui.appendText(text));
            }
        } else {
            Lab3.appendTextWithLock(text);
        }
    }

    private int findFirstPosition(int value, int currentIndex) {
        synchronized (printLock) {
            for (int i = startIndex; i <= currentIndex; i++) {
                if (mas[i] == value) {
                    return i;
                }
            }
            return currentIndex - 1;
        }
    }
}