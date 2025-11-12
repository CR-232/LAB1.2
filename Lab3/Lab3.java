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

        ThreadCalc thread1 = new ThreadCalc(0, 49, mas, "Th1", lab3.gui);
        ThreadCalc thread2 = new ThreadCalc(50, 99, mas, "Th2", lab3.gui);

        ThreadCalcule thread3 = new ThreadCalcule(0, 49, mas, "Th3", lab3.gui);
        ThreadCalcule thread4 = new ThreadCalcule(50, 99, mas, "Th4", lab3.gui);

        thread1.start();
        thread2.start();
        thread3.start();
        thread4.start();

        thread1.join();
        thread2.join();
        thread3.join();
        thread4.join();

        displayInfoWithDelay();
    }

    private static void displayInfoWithDelay() throws InterruptedException {

        String numeStudent = "Th2 -> Nume student: Cruc,Cotoman\n";
        for (char c : numeStudent.toCharArray()) {
            appendTextWithLock(String.valueOf(c));
            Thread.sleep(100);
        }

        String grupa = "Th4 -> Grupa: CR-232\n";
        for (char c : grupa.toCharArray()) {
            appendTextWithLock(String.valueOf(c));
            Thread.sleep(100);
        }

        String prenumeStudent = "Th1 -> Prenume student: Maxim,Vadim\n";
        for (char c : prenumeStudent.toCharArray()) {
            appendTextWithLock(String.valueOf(c));
            Thread.sleep(100);
        }

        String disciplina = "Th3 -> Disciplina: Programarea concurentă și distribuită\n";
        for (char c : disciplina.toCharArray()) {
            appendTextWithLock(String.valueOf(c));
            Thread.sleep(100);
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
                Thread.yield();
                Thread.sleep((int) (Math.random() * 40 + 10));
            } catch (InterruptedException e) {
                if (Thread.interrupted()) {
                    printToGUI(nameThread + " a fost întrerupt și se încheie.\n");
                    return;
                }
                e.printStackTrace();
            }

            if (isThreadActive()) {
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

        if (k == 1) {
            printToGUI(nameThread + " -> Valoare pară rămasă singură: " + S1 + " (poziție: " + findFirstPosition(S1, endIndex) + ")\n");
        }

        Lab3.appendTextWithLock(nameThread + " -> Total sume calculate: " + count + "\n");
        printToGUI(nameThread + " a terminat execuția.\n");
    }

    private boolean isThreadActive() {
        Thread currentThread = Thread.currentThread();
        return currentThread.isAlive() && !currentThread.isInterrupted();
    }

    private void printToGUI(String text) {
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if (Math.random() > 0.5) {
            SwingUtilities.invokeLater(() -> gui.appendText(text));
        } else {
            Lab3.appendTextWithLock(text);
        }
    }

    private int findFirstPosition(int value, int currentIndex) {
        for (int i = startIndex; i <= currentIndex; i++) {
            Thread.yield();
            if (mas[i] == value) {
                return i;
            }
        }
        return currentIndex - 1;
    }
}

class ThreadCalc extends Thread {
    int startIndex, endIndex;
    int[] mas;
    String nameThread;
    Lab3GUI gui;

    public ThreadCalc(int startIndex, int endIndex, int[] mas, String nameThread, Lab3GUI gui) {
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
                Thread.yield();
                Thread.sleep((int) (Math.random() * 40 + 10));
            } catch (InterruptedException e) {
                if (Thread.interrupted()) {
                    printToGUI(nameThread + " a fost întrerupt.\n");
                    return;
                }
                e.printStackTrace();
            }

            if (isThreadValid()) {
                if (mas[i] >= 1000 && mas[i] <= 1567 && mas[i] % 2 == 0) {
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

        if (k == 1) {
            printToGUI(nameThread + " -> Valoare pară rămasă singură: " + S1 + " (poziție: " + findFirstPosition(S1, endIndex) + ")\n");
        }

        Lab3.appendTextWithLock(nameThread + " -> Total sume calculate: " + count + "\n");
        printToGUI(nameThread + " a terminat execuția.\n");
    }

    private boolean isThreadValid() {
        return Thread.currentThread().isAlive();
    }

    private void printToGUI(String text) {
        try {
            Thread.sleep(2);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if (Math.random() > 0.5) {
            SwingUtilities.invokeLater(() -> gui.appendText(text));
        } else {
            Lab3.appendTextWithLock(text);
        }
    }

    private int findFirstPosition(int value, int currentIndex) {
        for (int i = startIndex; i <= currentIndex; i++) {
            Thread.yield();
            if (mas[i] == value) {
                return i;
            }
        }
        return currentIndex - 1;
    }
}