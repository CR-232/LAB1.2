import Lab1GUI.Lab1GUI;
import java.util.Random;

public class Lab1 {
    public static void main(String[] args) throws InterruptedException {
        int[] mas = new int[100];
        Lab1GUI gui = new Lab1GUI("Laborator PCD - CR_232");


        gui.appendText("Tablou generat:\n");
        Random rand = new Random();
        for (int i = 0; i < 100; i++) {
            mas[i] = rand.nextInt(100) + 1;
            gui.appendText(mas[i] + " ");
            if (i == 49) gui.appendText("\n");
        }
        gui.appendText("\n");


        ThreadCalc thread1 = new ThreadCalc(0, 49, mas, "Th1", gui);
        ThreadCalc thread2 = new ThreadCalc(50, 99, mas, "Th2", gui);
        ThreadCalcule thread3 = new ThreadCalcule(0, 49, mas, "Th3", gui);
        ThreadCalcule thread4 = new ThreadCalcule(50, 99, mas, "Th4", gui);

        thread1.start();
        thread2.start();
        thread3.start();
        thread4.start();

        thread1.join();
        thread2.join();
        thread3.join();
        thread4.join();

        String message = "Laborator efectuat de elevii din grupa CR-232: Cruc Maxim si Cotoman Vadim\n";
        for (char c : message.toCharArray()) {
            gui.appendText(String.valueOf(c));
            Thread.sleep(100);
        }
    }
}

class ThreadCalc extends Thread {
    int from, to;
    int[] mas;
    String nameThread;
    Lab1GUI gui;

    public ThreadCalc(int from, int to, int[] mas, String nameThread, Lab1GUI gui) {
        this.from = from;
        this.to = to;
        this.mas = mas;
        this.nameThread = nameThread;
        this.gui = gui;
    }

    @Override
    public void run() {
        int S1 = 0, S2 = 0, k = 0;

        for (int i = to; i >= from; i--) {
            if (mas[i] % 2 == 0) {
                if (k == 0) S1 = mas[i];
                else S2 = mas[i];
                k++;
                if (k == 2) {
                    int S = S1 + S2;
                    gui.appendText(nameThread + " -> (descrescător) Suma a două valori pare: " + S +
                            " (valori: " + S1 + ", " + S2 + ")\n");
                    S1 = S2 = 0;
                    k = 0;
                }
            }
        }
        if (k == 1) {
            gui.appendText(nameThread + " -> (descrescător) Valoare pară rămasă singură: " + S1 + "\n");
        }
    }
}

class ThreadCalcule extends Thread {
    int from, to;
    int[] mas;
    String nameThread;
    Lab1GUI gui;

    public ThreadCalcule(int from, int to, int[] mas, String nameThread, Lab1GUI gui) {
        this.from = from;
        this.to = to;
        this.mas = mas;
        this.nameThread = nameThread;
        this.gui = gui;
    }

    @Override
    public void run() {
        int S1 = 0, S2 = 0, k = 0;

        for (int i = from; i <= to; i++) {
            if (mas[i] % 2 == 0) {
                if (k == 0) S1 = mas[i];
                else S2 = mas[i];
                k++;
                if (k == 2) {
                    int S = S1 + S2;
                    gui.appendText(nameThread + " -> (crescător) Suma a două valori pare: " + S +
                            " (valori: " + S1 + ", " + S2 + ")\n");
                    S1 = S2 = 0;
                    k = 0;
                }
            }
        }
        if (k == 1) {
            gui.appendText(nameThread + " -> (crescător) Valoare pară rămasă singură: " + S1 + "\n");
        }
    }
}
