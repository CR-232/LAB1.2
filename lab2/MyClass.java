public class MyClass {
    static class MyThread extends Thread {
        public MyThread(ThreadGroup group, String name, int priority) {
            super(group, name);
            this.setPriority(priority);
        }
        public void run() {

            System.out.println(
                    "Thread: " + getName() +
                            " | Group: " + getThreadGroup().getName() +
                            " | Priority: " + getPriority());
        }
    }

    public static void main(String[] args) {

        ThreadGroup mainGroup = new ThreadGroup("Main");

        MyThread Th1 = new MyThread(mainGroup, "Th1", 8);
        MyThread Th2 = new MyThread(mainGroup, "Th2", 3);

        Th1.start();
        Th2.start();


        ThreadGroup gn = new ThreadGroup(mainGroup, "GN");

        MyThread Th_a = new MyThread(gn, "ThA", 3);
        Th_a.start();


        ThreadGroup gh = new ThreadGroup(gn, "GH");

        MyThread Tha = new MyThread(gh, "Tha", 4);
        MyThread Thb = new MyThread(gh, "Thb", 3);
        MyThread Thc = new MyThread(gh, "Thc", 6);
        MyThread Thd = new MyThread(gh, "Thd", 3);

        Tha.start();
        Thb.start();
        Thc.start();
        Thd.start();

        ThreadGroup gm = new ThreadGroup(mainGroup, "GM");

        MyThread Th1_gm = new MyThread(gm, "Th1", 2);
        MyThread Th2_gm = new MyThread(gm, "Th2", 3);
        MyThread Th3_gm = new MyThread(gm, "Th3", 3);

        Th1_gm.start();
        Th2_gm.start();
        Th3_gm.start();

        System.out.println("\n Structura ThreadGroup");
    }
}
