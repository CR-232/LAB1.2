public class ProducerConsumer {
    public static void main(String[] args) {

        ProducerConsumerGUI gui = new ProducerConsumerGUI();

        Depozit depozit = new Depozit(5, 4, 2, gui);

        Producer p1 = new Producer(1, depozit, gui);
        Producer p2 = new Producer(2, depozit, gui);
        Producer p3 = new Producer(3, depozit, gui);

        Consumer c1 = new Consumer(1, depozit, gui);
        Consumer c2 = new Consumer(2, depozit, gui);
        Consumer c3 = new Consumer(3, depozit, gui);
        Consumer c4 = new Consumer(4, depozit, gui);

        p1.start();
        p2.start();
        p3.start();

        c1.start();
        c2.start();
        c3.start();
        c4.start();
    }
}

class Producer extends Thread {
    Depozit depozit;
    int id;
    ProducerConsumerGUI gui;

    public Producer(int id, Depozit depozit, ProducerConsumerGUI gui) {
        this.id = id;
        this.depozit = depozit;
        this.gui = gui;
    }

    public void run() {
        while (true) {
            if (!depozit.nevoieDeProducere()) {
                gui.log("Producătorul " + id + " a TERMINAT.");
                return;
            }


            int numar1 = genereazaNumarImpar();
            int numar2 = genereazaNumarImpar();

            boolean produs = depozit.putNumbers(numar1, numar2, id);
            if (!produs) {
                gui.log("Producătorul " + id + " a TERMINAT.");
                return;
            }

            try {
                sleep((int) (Math.random() * 400 + 100));
            } catch (InterruptedException ignored) {}
        }
    }

    private int genereazaNumarImpar() {
        int x = (int)(Math.random() * 100) + 1;
        return (x % 2 == 1) ? x : x + 1;
    }
}


class Consumer extends Thread {
    Depozit depozit;
    int id;
    int consumed = 0;
    ProducerConsumerGUI gui;

    public Consumer(int id, Depozit depozit, ProducerConsumerGUI gui) {
        this.id = id;
        this.depozit = depozit;
        this.gui = gui;
    }

    public void run() {
        while (consumed < depozit.Z) {
            int nr = depozit.getNumber(id);

            if (nr == -1) {
                gui.log("Consumatorul " + id + " nu mai poate consuma - DEPOZIT GOL ȘI PRODUCȚIE TERMINATĂ.");
                return;
            }

            consumed++;
            gui.log("Consumatorul " + id +
                    " a consumat: " + nr +
                    " (total consumat: " + consumed + "/" + depozit.Z +
                    ", în depozit: " + depozit.count + ")");

            try {
                sleep((int) (Math.random() * 300 + 100));
            } catch (InterruptedException ignored) {}
        }

        gui.log("Consumatorul " + id + " a TERMINAT (a consumat cele " + depozit.Z + " produse).");
        depozit.anuntaConsumatorTerminat();
    }
}


class Depozit {

    int[] buffer;
    int D;
    int Y;
    int Z;

    int write = 0;
    int read = 0;
    int count = 0;

    int produseTotal = 0;
    int consumateTotal = 0;
    int totalNecesare;

    boolean gata = false;
    ProducerConsumerGUI gui;
    private int nr;

    public Depozit(int D, int Y, int Z, ProducerConsumerGUI gui) {
        this.D = D;
        this.Y = Y;
        this.Z = Z;
        this.buffer = new int[D];
        this.totalNecesare = Y * Z;
        this.gui = gui;

        gui.log("=== CONFIGURATIE ===");
        gui.log("Capacitate depozit: " + D);
        gui.log("Număr consumatori: " + Y);
        gui.log("Produse per consumator: " + Z);
        gui.log("Total produse necesare: " + totalNecesare);
        gui.log("====================");
    }

    public synchronized boolean nevoieDeProducere() {
        return produseTotal < totalNecesare;
    }

    public synchronized boolean putNumbers(int nr1, int nr2, int pid) {
        if (produseTotal >= totalNecesare) {
            return false;
        }

        int deProdus = Math.min(2, totalNecesare - produseTotal);


        while (count > D - deProdus) {
            gui.log("Producătorul " + pid + " așteaptă - nevoie de " + deProdus + " loc(uri) (depozit: " + count + "/" + D + ")");
            try {
                wait();
            } catch (InterruptedException ignored) {}


            if (produseTotal >= totalNecesare) {
                return false;
            }
            deProdus = Math.min(2, totalNecesare - produseTotal);
        }

        if (deProdus >= 1) {
            buffer[write] = nr1;
            write = (write + 1) % D;
            count++;
            produseTotal++;
        }

        if (deProdus >= 2) {
            buffer[write] = nr2;
            write = (write + 1) % D;
            count++;
            produseTotal++;
        }

        gui.log("Producătorul " + pid + " a produs: " +
                (deProdus >= 1 ? nr1 : "") +
                (deProdus == 2 ? " și " + nr2 : "") +
                " (în depozit: " + count + "/" + D + ", total produse: " + produseTotal + "/" + totalNecesare + ")");

        notifyAll();
        return true;
    }

    public synchronized int getNumber(int cid) {

        while (count == 0) {
            if (gata) {
                return -1;
            }
            gui.log("Consumatorul " + cid + " așteaptă - depozit gol (produse totale: " + produseTotal + "/" + totalNecesare + ")");
            try {
                wait();
            } catch (InterruptedException ignored) {}
        }

        

        if (consumateTotal == totalNecesare) {
            gata = true;
            gui.log("=== TOATE PRODUSELE AU FOST CONSUMATE ===");
        }

        notifyAll();
        return nr;
    }

    public synchronized void anuntaConsumatorTerminat() {

    }
}