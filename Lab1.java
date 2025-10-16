import java.io.*;

public class Lab1 {
    public static void main(String[] args) throws InterruptedException {
        int[] tab = new int[100];
        for (int i = 0; i < 100; i++) {
            tab[i] = (int)(Math.random() * 100) + 1;
            System.out.print(tab[i] + " ");
            if(i == 50){System.out.println();}
        }
        System.out.println();


        ThreadCalc thread1 = new ThreadCalc(0, 49, tab);
        ThreadCalc thread2 = new ThreadCalc(50, 99, tab);

        ThreadCalcule thread3 = new ThreadCalcule(0, 49, tab);
        ThreadCalcule thread4 = new ThreadCalcule(50, 99, tab);

        thread1.start();
        thread2.start();

        thread3.start();
        thread4.start();

        thread1.join();
        thread2.join();
        thread3.join();
        thread4.join();

        String message = "Laborator efectuat de elevii din grupa CR_232: Cruc Maxim si Cotoman Vadim";
        for (char c : message.toCharArray()) {
            System.out.print(c);
            Thread.sleep(100);
        }
        System.out.println();
    }
}

class ThreadCalc extends Thread {
    int from;
    int to;
    int[] tab;

    public ThreadCalc(int from, int to, int[] tab) {
        this.from = from;
        this.to = to;
        this.tab = tab;
    }

    @Override
    public void run() {
        int S1 = 0;
        int S2 = 0;
        int k = 0;

        for (int i = to; i >= from; i--) {
            if (tab[i] % 2 == 0) {
                if (k == 0) {
                    S1 = tab[i];
                } else {
                    S2 = tab[i];
                }
                k++;

                if (k == 2) {
                    int S = S1 + S2;
                    System.out.println(getName() + " -> (descrescător) Suma a două valori pare: " + S
                            + " (valori: " + S1 + ", " + S2 + ")");
                    S1 = 0;
                    S2 = 0;
                    k = 0;
                }
            }
        }

        if (k == 1) {
            System.out.println(getName() + " -> (descrescător) Valoare pară rămasă singură (fără pereche): " + S1);
        }
    }
}


class ThreadCalcule extends Thread {
    int from;
    int to;
    int[] tab;

    public ThreadCalcule(int from, int to, int[] tab) {
        this.from = from;
        this.to = to;
        this.tab = tab;
    }

    @Override
    public void run() {
        int S1 = 0;
        int S2 = 0;
        int k = 0;

        for (int i = from; i <= to; i++) {
            if (tab[i] % 2 == 0) {
                if (k == 0) {
                    S1 = tab[i];
                } else {
                    S2 = tab[i];
                }
                k++;

                if (k == 2) {
                    int S = S1 + S2;

                    System.out.println(getName() + " -> (crescător) Suma a două valori pare: " + S
                            + " (valori: " + S1 + ", " + S2 + ")");
                    S1 = 0;
                    S2 = 0;
                    k = 0;
                }
            }
        }

        if (k == 1) {
            System.out.println(getName() + " -> (crescător) Valoare pară rămasă singură (fără pereche): " + S1);
        }
    }
}