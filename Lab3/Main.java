import javax.swing.*;
import java.util.Random;

public class Main {

    private static final String NUME_STUDENT = "Cruc,Cotoman";
    private static final String PRENUME_STUDENT = "Maxim,Vadim";
    private static final String GRUPA = "CR-232";
    private static final String DISCIPLINA = "Programarea concurenta si distribuita";

    private static int[] array;
    private static Lab3GUI gui;
    private static Random rand = new Random();

    private static boolean th1Done = false;
    private static boolean th2Done = false;
    private static boolean th3Done = false;
    private static boolean th4Done = false;

    private static boolean th2CanPrint = false;
    private static boolean th4CanPrint = false;
    private static boolean th1CanPrint = false;
    private static boolean th3CanPrint = false;

    static Thread Th1 = new ThreadSumaPareInceput();
    static Thread Th2 = new ThreadSumaPareSfarsit();
    static Thread Th3 = new ThreadParcurgereCrescator();
    static Thread Th4 = new ThreadParcurgereDescrescator();

    public static void main(String[] args) {
        gui = new Lab3GUI("Laborator PCD - Varianta 2");
        gui.appendText("=== LABORATOR PCD - VARIANTA 2 ===\n\n");

        array = new int[100];
        gui.appendText("Array generat (valori 120-1567):\n");

        for (int i = 0; i < array.length; i++) {
            array[i] = rand.nextInt(1448) + 120;
            gui.appendText(array[i] + " ");
            if ((i + 1) % 10 == 0) gui.appendText("\n");
        }
        gui.appendText("\n=== PORNIRE FIRE DE EXECUȚIE ===\n");

        Th1.setName("Th1");
        Th2.setName("Th2");
        Th3.setName("Th3");
        Th4.setName("Th4");

        Th1.start();
        Th2.start();
        Th3.start();
        Th4.start();
    }

    public static void afisare(String text) {
        try {
            for (char c : text.toCharArray()) {
                Thread.sleep(100);
                gui.appendText(String.valueOf(c));
            }
            gui.appendText("\n");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static class ThreadSumaPareInceput extends Thread {
        public void run() {
            gui.appendText("Th1 - Suma pozițiilor numerelor pare două câte două de la început\n");

            int S = 0, C = 0, s1 = 0, s2 = 0, pair = 0;
            int numar1 = 0, numar2 = 0, pozitie1 = 0, pozitie2 = 0;

            for (int i = 0; i < array.length; i++) {
                if (array[i] % 2 == 0) {
                    S += i;
                    C++;

                    if (C == 1) {
                        numar1 = array[i];
                        pozitie1 = i;
                    } else if (C == 2) {
                        numar2 = array[i];
                        pozitie2 = i;
                    }
                }

                if (C == 2) {
                    pair++;
                    if (pair == 1) {
                        s1 = S;
                    } else {
                        s2 = S - s1;
                        gui.appendText(getName() + " → Numere pare: " + numar1 + " (poziția " + pozitie1 +
                                ") și " + numar2 + " (poziția " + pozitie2 +
                                ") - Suma pozițiilor = " + (s1 + s2) + "\n");
                        pair = 0;
                        s1 = 0;
                    }
                    S = 0;
                    C = 0;
                    numar1 = 0;
                    numar2 = 0;
                    pozitie1 = 0;
                    pozitie2 = 0;
                }

                try {
                    Thread.sleep(rand.nextInt(50));
                } catch (InterruptedException e) {}
            }

            if (C == 1) {
                gui.appendText(getName() + " → Număr par fără pereche: " + numar1 + " (poziția " + pozitie1 + ")\n");
            }

            gui.appendText("Th1 a terminat sarcinile principale\n");
            th1Done = true;

            while (!th2Done || !th3Done || !th4Done) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {}
            }

            while (!th1CanPrint) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {}
            }

            Main.afisare(Thread.currentThread().getName() + ": " + PRENUME_STUDENT);

            th3CanPrint = true;
        }
    }

    static class ThreadSumaPareSfarsit extends Thread {
        public void run() {
            gui.appendText("Th2 - Suma pozițiilor numerelor pare două câte două de la sfârșit\n");

            int S = 0, C = 0, s1 = 0, s2 = 0, pair = 0;
            int numar1 = 0, numar2 = 0, pozitie1 = 0, pozitie2 = 0;

            for (int i = array.length - 1; i >= 0; i--) {
                if (array[i] % 2 == 0) {
                    S += i;
                    C++;

                    if (C == 1) {
                        numar1 = array[i];
                        pozitie1 = i;
                    } else if (C == 2) {
                        numar2 = array[i];
                        pozitie2 = i;
                    }
                }

                if (C == 2) {
                    pair++;
                    if (pair == 1) {
                        s1 = S;
                    } else {
                        s2 = S - s1;
                        gui.appendText(getName() + " → Numere pare: " + numar1 + " (poziția " + pozitie1 +
                                ") și " + numar2 + " (poziția " + pozitie2 +
                                ") - Suma pozițiilor = " + (s1 + s2) + "\n");
                        pair = 0;
                        s1 = 0;
                    }
                    S = 0;
                    C = 0;
                    numar1 = 0;
                    numar2 = 0;
                    pozitie1 = 0;
                    pozitie2 = 0;
                }

                Thread.yield();

                try {
                    Thread.sleep(rand.nextInt(30));
                } catch (InterruptedException e) {}
            }

            if (C == 1) {
                gui.appendText(getName() + " → Număr par fără pereche: " + numar1 + " (poziția " + pozitie1 + ")\n");
            }

            gui.appendText("Th2 a terminat sarcinile principale\n");
            th2Done = true;

            while (!th1Done || !th3Done || !th4Done) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {}
            }

            th2CanPrint = true;
            Main.afisare(getName() + ": " + NUME_STUDENT);

            th4CanPrint = true;
        }
    }

    static class ThreadParcurgereCrescator extends Thread {
        public void run() {
            setPriority(Thread.MIN_PRIORITY + 1);

            gui.appendText("Th3 - Parcurgere [120, 690] de la început\n");

            int count = 0;

            for (int i = 0; i < array.length; i++) {
                if (array[i] >= 120 && array[i] <= 690) {
                    count++;
                    gui.appendText("Th3 - Valoare " + count + ": array[" + i + "] = " + array[i] + "\n");
                }

                try {
                    Thread.sleep(rand.nextInt(30));
                } catch (InterruptedException e) {}
            }

            gui.appendText("Th3 a terminat sarcinile principale\n");
            th3Done = true;

            while (!th1Done || !th2Done || !th4Done) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {}
            }

            while (!th3CanPrint) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {}
            }

            Main.afisare(getName() + ": " + DISCIPLINA);
        }
    }

    static class ThreadParcurgereDescrescator extends Thread {
        public void run() {
            Thread.currentThread().getName();

            gui.appendText("Th4 - Parcurgere [1000, 1567] de la sfârșit\n");

            int count = 0;

            for (int i = array.length - 1; i >= 0; i--) {
                if (array[i] >= 1000 && array[i] <= 1567) {
                    count++;
                    gui.appendText("Th4 - Valoare " + count + ": array[" + i + "] = " + array[i] + "\n");
                }

                try {
                    Thread.sleep(rand.nextInt(30));
                } catch (InterruptedException e) {}
            }

            gui.appendText("Th4 a terminat sarcinile principale\n");
            th4Done = true;

            while (!th1Done || !th2Done || !th3Done) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {}
            }

            while (!th4CanPrint) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {}
            }

            Main.afisare(getName() + ": " + GRUPA);

            th1CanPrint = true;
        }
    }
}