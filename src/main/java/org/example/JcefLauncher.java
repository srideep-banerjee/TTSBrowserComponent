package org.example;

import me.friwi.jcefmaven.CefAppBuilder;
import me.friwi.jcefmaven.CefInitializationException;
import me.friwi.jcefmaven.UnsupportedPlatformException;
import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.CefSettings;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.browser.CefMessageRouter;
import org.cef.handler.CefDisplayHandlerAdapter;
import org.cef.handler.CefLifeSpanHandlerAdapter;
import org.cef.handler.CefLoadHandlerAdapter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

public class JcefLauncher {
    String url;
    CefApp app;
    public static volatile boolean exit = false;

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
                if (!url.startsWith("devtools://")) {
                    contextMenuHandler.setUrl(url);
                }
            }

            @Override
            public boolean onConsoleMessage(CefBrowser browser, CefSettings.LogSeverity level, String message, String source, int line){
                return true;
            }
        });

        client.addLoadHandler(new CefLoadHandlerAdapter() {
            @Override
            public void onLoadEnd(CefBrowser browser, CefFrame frame, int httpStatusCode) {
                super.onLoadEnd(browser, frame, httpStatusCode);
                browser.executeJavaScript(
                        "var customEvent = new Event('windowclose')",
                        frame.getURL(),
                        0
                );
            }
        });
        client.addLifeSpanHandler(new CefLifeSpanHandlerAdapter() {@Override
            public boolean doClose(CefBrowser browser) {
                System.out.println("Do close called");
                exit = true;
                return false;
            }
        });

        // Create a JFrame to host the browser
        JFrame frame = new JFrame("Time Table Scheduler");
        frame.setSize(1260, 700);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.getContentPane().add(browser.getUIComponent(), BorderLayout.CENTER);
        frame.setVisible(true);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                browser.executeJavaScript(
                        "document.dispatchEvent(customEvent);",
                        browser.getFocusedFrame().getURL(),
                        0
                );
            }
        });
        CefMessageRouter.CefMessageRouterConfig s = new CefMessageRouter.CefMessageRouterConfig();
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        while(!JcefLauncher.exit);
        app.dispose();
        System.out.println("System exiting");
        System.exit(0);
    }
}
