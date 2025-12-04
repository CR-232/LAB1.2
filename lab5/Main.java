import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

//CLASA DE BAZA - Main
public class Main {
    public static void main(String[] args) {
        ProducerConsumerGUI gui = new ProducerConsumerGUI();

        int D = 5;
        int Y = 4;
        int Z = 2;

        Depozit depozit = new Depozit(D, Y, Z, gui);

        ExecutorService executor = Executors.newFixedThreadPool(7); // 3 producers + 4 consumers

        Producer p1 = new Producer(1, depozit, gui);
        Producer p2 = new Producer(2, depozit, gui);
        Producer p3 = new Producer(3, depozit, gui);

        Consumer c1 = new Consumer(1, depozit, gui);
        Consumer c2 = new Consumer(2, depozit, gui);
        Consumer c3 = new Consumer(3, depozit, gui);
        Consumer c4 = new Consumer(4, depozit, gui);


        executor.submit(p1);
        executor.submit(p2);
        executor.submit(p3);

        executor.submit(c1);
        executor.submit(c2);
        executor.submit(c3);
        executor.submit(c4);


        executor.shutdown();

        try {

            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}

//CLASA PRODUCER - Vadim
class Producer implements Runnable {
    private int id;
    private Depozit depozit;
    private ProducerConsumerGUI gui;
    private Random random = new Random();

    public Producer(int id, Depozit depozit, ProducerConsumerGUI gui) {
        this.id = id;
        this.depozit = depozit;
        this.gui = gui;
    }

    public void pune(int nr1, int nr2) {
        depozit.pune(nr1, nr2);
        gui.log("Producător " + id + " a pus produsele: " + nr1 + " și " + nr2);
    }

    public void run() {
        while (true) {
            if (depozit.productieCompleta()) {
                gui.log("Producător " + id + " s-a oprit - productie completa");
                return;
            }

            int nr1 = genereazaNumarImpar();
            int nr2 = genereazaNumarImpar();

            gui.log("Producător " + id + " a generat: " + nr1 + " și " + nr2);

            pune(nr1, nr2);

            try {
                Thread.sleep(random.nextInt(300) + 100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private int genereazaNumarImpar() {
        int nr = random.nextInt(50) + 1;
        if (nr % 2 == 0) {
            nr++;
        }
        return nr;
    }
}

//CLASA CONSUMER - Maxim
class Consumer implements Runnable {
    private int id;
    private Depozit depozit;
    private ProducerConsumerGUI gui;
    private Random random = new Random();
    private int consumate = 0;

    public Consumer(int id, Depozit depozit, ProducerConsumerGUI gui) {
        this.id = id;
        this.depozit = depozit;
        this.gui = gui;
    }

    public int ia() {
        int nr = depozit.ia();

        if (nr == -1) {
            gui.log("Consumator " + id + " s-a oprit - nu mai sunt produse. A consumat: " + consumate + "/" + depozit.Z);
            return -1;
        }

        consumate++;
        gui.log("Consumator " + id + " a consumat: " + nr + " (total consumate: " + consumate + "/" + depozit.Z + ")");
        return nr;
    }

    public void run() {
        while (consumate < depozit.Z) {
            int nr = ia();

            if (nr == -1) {
                return;
            }

            try {
                Thread.sleep(random.nextInt(200) + 100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }

        gui.log("Consumator " + id + " a terminat! A consumat total: " + consumate + " produse");
    }
}

//CLASA DEPOZIT - Clasa comuna
class Depozit {
    private int[] buffer;
    int D;
    int Z;
    private ProducerConsumerGUI gui;

    private int pozitieScrie = 0;
    private int pozitieCiteste = 0;
    private int cantitate = 0;

    private int totalProduse = 0;
    private int totalConsumate = 0;
    private int totalNecesar;

    public Depozit(int D, int Y, int Z, ProducerConsumerGUI gui) {
        this.D = D;
        this.Z = Z;
        this.buffer = new int[D];
        this.totalNecesar = Y * Z;
        this.gui = gui;

        gui.log("=== CONFIGURARE ===");
        gui.log("Depozit: " + D);
        gui.log("Consumatori: " + Y);
        gui.log("Produse/consumator: " + Z);
        gui.log("Total necesar: " + totalNecesar);
        gui.log("===================");
    }

    public synchronized boolean productieCompleta() {
        return totalProduse >= totalNecesar;
    }

    public synchronized void pune(int nr1, int nr2) {

        while (cantitate >= D - 1) {
            try {
                wait();
            } catch (InterruptedException e) {}
        }

        if (totalProduse >= totalNecesar) {
            notifyAll();
            return;
        }


        buffer[pozitieScrie] = nr1;
        pozitieScrie = (pozitieScrie + 1) % D;
        cantitate++;
        totalProduse++;


        buffer[pozitieScrie] = nr2;
        pozitieScrie = (pozitieScrie + 1) % D;
        cantitate++;
        totalProduse++;

        notifyAll();
    }

    public synchronized int ia() {
        while (cantitate == 0) {
            if (totalProduse >= totalNecesar && totalConsumate >= totalNecesar) {
                return -1;
            }

            try {
                wait();
            } catch (InterruptedException e) {}
        }

        int nr = buffer[pozitieCiteste];
        pozitieCiteste = (pozitieCiteste + 1) % D;
        cantitate--;
        totalConsumate++;

        notifyAll();
        return nr;
    }
}
