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

    // Flag-uri pentru a ști când thread-urile au terminat
    private static boolean th1Done = false;
    private static boolean th2Done = false;
    private static boolean th3Done = false;
    private static boolean th4Done = false;

    // Flag-uri pentru a controla ordinea afișării
    private static boolean th2CanPrint = false;
    private static boolean th4CanPrint = false;
    private static boolean th1CanPrint = false;
    private static boolean th3CanPrint = false;

    // Firele de execuție - păstrăm denumirile scurte pentru thread-uri
    static Thread Th1 = new ThreadSumaPareInceput();
    static Thread Th2 = new ThreadSumaPareSfarsit();
    static Thread Th3 = new ThreadParcurgereCrescator();
    static Thread Th4 = new ThreadParcurgereDescrescator();

    public static void main(String[] args) {
        gui = new Lab3GUI("Laborator PCD - Varianta 2");
        gui.appendText("=== LABORATOR PCD - VARIANTA 2 ===\n\n");

        // Generare array cu valori între 120-1567
        array = new int[100];
        gui.appendText("Array generat (valori 120-1567):\n");

        for (int i = 0; i < array.length; i++) {
            array[i] = rand.nextInt(1448) + 120;
            gui.appendText(array[i] + " ");
            if ((i + 1) % 10 == 0) gui.appendText("\n");
        }
        gui.appendText("\n=== PORNIRE FIRE DE EXECUȚIE ===\n");

        // Setăm numele thread-urilor - metoda setName()
        Th1.setName("Th1");
        Th2.setName("Th2");
        Th3.setName("Th3");
        Th4.setName("Th4");

        // Pornim thread-urile - metoda start()
        Th1.start();
        Th2.start();
        Th3.start();
        Th4.start();
    }

    // Afișează text caracter cu caracter cu pauză de 100ms
    public static void afisare(String text) {
        try {
            for (char c : text.toCharArray()) {
                Thread.sleep(100); // metoda sleep()
                gui.appendText(String.valueOf(c));
            }
            gui.appendText("\n");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // ==================== THREAD 1 ====================
    static class ThreadSumaPareInceput extends Thread {
        public void run() {
            gui.appendText("Th1 - Suma pozițiilor numerelor pare două câte două de la început\n");

            int S = 0, C = 0, s1 = 0, s2 = 0, pair = 0;
            int numar1 = 0, numar2 = 0, pozitie1 = 0, pozitie2 = 0;

            // Parcurge array-ul de la început
            for (int i = 0; i < array.length; i++) {
                if (array[i] % 2 == 0) { // element par
                    S += i; // adaugă poziția
                    C++;

                    // Salvează numărul par și poziția sa
                    if (C == 1) {
                        numar1 = array[i];
                        pozitie1 = i;
                    } else if (C == 2) {
                        numar2 = array[i];
                        pozitie2 = i;
                    }
                }

                // La fiecare 2 elemente pare
                if (C == 2) {
                    pair++;
                    if (pair == 1) {
                        s1 = S;
                    } else {
                        s2 = S - s1;
                        // Afișează numerele pare și suma lor
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
                    Thread.sleep(rand.nextInt(50)); // metoda sleep() - pauză random
                } catch (InterruptedException e) {}
            }

            // Dacă a rămas un număr par fără pereche
            if (C == 1) {
                gui.appendText(getName() + " → Număr par fără pereche: " + numar1 + " (poziția " + pozitie1 + ")\n");
            }

            gui.appendText("Th1 a terminat sarcinile principale\n");
            th1Done = true; // Marchează că a terminat

            // Așteaptă ca toate thread-urile să termine
            while (!th2Done || !th3Done || !th4Done) {
                try {
                    Thread.sleep(50); // metoda sleep()
                } catch (InterruptedException e) {}
            }

            // Așteaptă rândul să afișeze (după Th4)
            while (!th1CanPrint) {
                try {
                    Thread.sleep(50); // metoda sleep()
                } catch (InterruptedException e) {}
            }

            // Afișează prenumele - metoda currentThread()
            Main.afisare(Thread.currentThread().getName() + ": " + PRENUME_STUDENT);

            // Dă permisiune lui Th3
            th3CanPrint = true;
        }
    }

    // ==================== THREAD 2 ====================
    static class ThreadSumaPareSfarsit extends Thread {
        public void run() {
            gui.appendText("Th2 - Suma pozițiilor numerelor pare două câte două de la sfârșit\n");

            int S = 0, C = 0, s1 = 0, s2 = 0, pair = 0;
            int numar1 = 0, numar2 = 0, pozitie1 = 0, pozitie2 = 0;

            // Parcurge array-ul de la sfârșit
            for (int i = array.length - 1; i >= 0; i--) {
                if (array[i] % 2 == 0) { // element par
                    S += i; // adaugă poziția
                    C++;

                    // Salvează numărul par și poziția sa
                    if (C == 1) {
                        numar1 = array[i];
                        pozitie1 = i;
                    } else if (C == 2) {
                        numar2 = array[i];
                        pozitie2 = i;
                    }
                }

                // La fiecare 2 elemente pare
                if (C == 2) {
                    pair++;
                    if (pair == 1) {
                        s1 = S;
                    } else {
                        s2 = S - s1;
                        // Afișează numerele pare și suma lor
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

                Thread.yield(); // metoda yield() - cedează procesorul

                try {
                    Thread.sleep(rand.nextInt(30)); // metoda sleep() - pauză random
                } catch (InterruptedException e) {}
            }

            // Dacă a rămas un număr par fără pereche
            if (C == 1) {
                gui.appendText(getName() + " → Număr par fără pereche: " + numar1 + " (poziția " + pozitie1 + ")\n");
            }

            gui.appendText("Th2 a terminat sarcinile principale\n");
            th2Done = true; // Marchează că a terminat

            // Așteaptă ca toate thread-urile să termine
            while (!th1Done || !th3Done || !th4Done) {
                try {
                    Thread.sleep(50); // metoda sleep()
                } catch (InterruptedException e) {}
            }

            // Th2 afișează PRIMUL
            th2CanPrint = true;
            Main.afisare(getName() + ": " + NUME_STUDENT); // metoda getName()

            // Dă permisiune lui Th4
            th4CanPrint = true;
        }
    }

    // ==================== THREAD 3 ====================
    static class ThreadParcurgereCrescator extends Thread {
        public void run() {
            setPriority(Thread.MIN_PRIORITY + 1); // metoda setPriority()

            gui.appendText("Th3 - Parcurgere [120, 690] de la început\n");

            int count = 0;

            // Parcurge array-ul de la început
            for (int i = 0; i < array.length; i++) {
                // Dacă valoarea e în interval [120, 690]
                if (array[i] >= 120 && array[i] <= 690) {
                    count++;
                    gui.appendText("Th3 - Valoare " + count + ": array[" + i + "] = " + array[i] + "\n");
                }

                try {
                    Thread.sleep(rand.nextInt(30)); // metoda sleep() - pauză random
                } catch (InterruptedException e) {}
            }

            gui.appendText("Th3 a terminat sarcinile principale\n");
            th3Done = true; // Marchează că a terminat

            // Așteaptă ca toate thread-urile să termine
            while (!th1Done || !th2Done || !th4Done) {
                try {
                    Thread.sleep(50); // metoda sleep()
                } catch (InterruptedException e) {}
            }

            // Așteaptă rândul să afișeze (după Th1)
            while (!th3CanPrint) {
                try {
                    Thread.sleep(50); // metoda sleep()
                } catch (InterruptedException e) {}
            }

            Main.afisare(getName() + ": " + DISCIPLINA); // metoda getName()
        }
    }

    // ==================== THREAD 4 ====================
    static class ThreadParcurgereDescrescator extends Thread {
        public void run() {
            Thread.currentThread().getName(); // metoda currentThread() și getName()

            gui.appendText("Th4 - Parcurgere [1000, 1567] de la sfârșit\n");

            int count = 0;

            // Parcurge array-ul de la sfârșit
            for (int i = array.length - 1; i >= 0; i--) {
                // Dacă valoarea e în interval [1000, 1567]
                if (array[i] >= 1000 && array[i] <= 1567) {
                    count++;
                    gui.appendText("Th4 - Valoare " + count + ": array[" + i + "] = " + array[i] + "\n");
                }

                try {
                    Thread.sleep(rand.nextInt(30)); // metoda sleep() - pauză random
                } catch (InterruptedException e) {}
            }

            gui.appendText("Th4 a terminat sarcinile principale\n");
            th4Done = true; // Marchează că a terminat

            // Așteaptă ca toate thread-urile să termine
            while (!th1Done || !th2Done || !th3Done) {
                try {
                    Thread.sleep(50); // metoda sleep()
                } catch (InterruptedException e) {}
            }

            // Așteaptă rândul să afișeze (după Th2)
            while (!th4CanPrint) {
                try {
                    Thread.sleep(50); // metoda sleep()
                } catch (InterruptedException e) {}
            }

            Main.afisare(getName() + ": " + GRUPA); // metoda getName()

            // Dă permisiune lui Th1
            th1CanPrint = true;
        }
    }
}