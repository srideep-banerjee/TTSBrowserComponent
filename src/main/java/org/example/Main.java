package org.example;

public class Main {
    public static void main(String[] args) {
        if(args == null || args.length==0) {
            System.err.println("No initial link provided, exiting");
            System.exit(1);
        }
        JcefLauncher jcefLauncher = new JcefLauncher(args[0]);
        jcefLauncher.launch();
    }
}