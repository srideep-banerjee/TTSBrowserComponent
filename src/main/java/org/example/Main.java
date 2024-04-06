package org.example;

public class Main {
    public static void main(String[] args) {
        if(args == null || args.length < 1) {
            System.err.println("No initial link provided, exiting");
            System.exit(1);
        }
        if(args == null || args.length < 2) {
            System.err.println("No token provided, exiting");
        }
        JcefLauncher jcefLauncher = new JcefLauncher(args[0], args[1]);
        jcefLauncher.launch();
    }
}