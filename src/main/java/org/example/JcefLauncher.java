package org.example;

import me.friwi.jcefmaven.CefAppBuilder;
import me.friwi.jcefmaven.CefInitializationException;
import me.friwi.jcefmaven.UnsupportedPlatformException;
import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.CefSettings;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.handler.CefDisplayHandler;
import org.cef.handler.CefDisplayHandlerAdapter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

public class JcefLauncher {
    String url;
    Timer timer;
    CefApp app;

    public JcefLauncher(String url) {
        this.url = url;
    }

    public void launch() {
        System.out.println("Launch called");
        //Create a new CefAppBuilder instance
        CefAppBuilder builder = new CefAppBuilder();

        //Configure the builder instance
        builder.getCefSettings().windowless_rendering_enabled = false;

        //Build a CefApp instance using the configuration above
        try {
            app = builder.build();
        } catch (IOException | InterruptedException | CefInitializationException | UnsupportedPlatformException e) {
            System.out.println(e);
            System.exit(1);
        }

        // Create a CefClient instance
        CefClient client = app.createClient();

        // Create a CefBrowser instance
        CefBrowser browser = client.createBrowser(this.url, false, false);

        final ContextMenuHandler contextMenuHandler = new ContextMenuHandler(browser, this.url);
        client.addContextMenuHandler(contextMenuHandler);

        client.addDisplayHandler(new CefDisplayHandlerAdapter() {
            @Override
            public void onAddressChange(CefBrowser browser, CefFrame frame, String url) {
                contextMenuHandler.setUrl(url);
            }

            @Override
            public boolean onConsoleMessage(CefBrowser browser, CefSettings.LogSeverity level, String message, String source, int line){
                return true;
            }
        });

        // Create a JFrame to host the browser
        JFrame frame = new JFrame("Time Table Scheduler");
        frame.setSize(1260, 700);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.getContentPane().add(browser.getUIComponent(), BorderLayout.CENTER);
//        frame.addComponentListener(new ComponentAdapter() {
//
//            @Override
//            public void componentResized(ComponentEvent e) {
//                int w = frame.getWidth();
//                int h = frame.getHeight();
//                if (w < 1260 && h < 700) {
//                    if (timer == null) {
//                        timer = new Timer(100, (ActionListener) -> {
//                            timer.stop();
//                            timer = null;
//                            frame.setSize(new Dimension(1260, 700));
//                            frame.repaint();
//                            frame.revalidate();
//                        });
//                        timer.start();
//                    } else {
//                        timer.restart();
//                    }
//                }
//            }
//        });
        frame.setVisible(true);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                app.dispose();
                System.out.println("System exiting");
                System.exit(0);
            }
        });
        while (true) {
            try {
                Thread.sleep(30000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (!frame.isVisible()){
                app.dispose();
                System.out.println("System exiting");
                System.exit(0);
            }
        }
    }
}
