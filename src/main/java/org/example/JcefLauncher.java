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
import org.cef.callback.CefBeforeDownloadCallback;
import org.cef.callback.CefDownloadItem;
import org.cef.callback.CefDownloadItemCallback;
import org.cef.handler.*;
import org.cef.network.CefRequest;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

public class JcefLauncher {
    String url;
    CefApp app;
    String token;
    public static volatile boolean exit = false;

    public JcefLauncher(String url, String token) {
        this.url = url;
        this.token = token;
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

        client.addDownloadHandler(new CefDownloadHandlerAdapter() {
            @Override
            public void onBeforeDownload(CefBrowser browser, CefDownloadItem downloadItem, String suggestedName, CefBeforeDownloadCallback callback) {
                callback.Continue(null, true);
            }
        });

        client.addLoadHandler(new CefLoadHandlerAdapter() {

            @Override
            public void onLoadStart(CefBrowser browser, CefFrame frame, CefRequest.TransitionType transitionType) {
                super.onLoadStart(browser, frame, transitionType);
                if (transitionType == CefRequest.TransitionType.TT_RELOAD) {
                    browser.executeJavaScript(
                            "window.apiToken = \""+token+"\"",
                            frame.getURL(),
                            0
                    );
                }
            }

            @Override
            public void onLoadEnd(CefBrowser browser, CefFrame frame, int httpStatusCode) {
                super.onLoadEnd(browser, frame, httpStatusCode);
                StringBuilder loadJs = new StringBuilder()
                        .append("var closeEvent = new Event('windowclose');")
                        .append("var downloadEvent = new Event('downloadprogress');")
                        .append("window.apiToken = '").append(token).append("'");
                browser.executeJavaScript(
                        loadJs.toString(),
                        frame.getURL(),
                        0
                );
            }

            @Override
            public void onLoadError(CefBrowser browser, CefFrame frame, CefLoadHandler.ErrorCode errorCode, String errorText, String failedUrl) {
                super.onLoadError(browser, frame, errorCode, errorText, failedUrl);
                browser.executeJavaScript(
                        "window.apiToken = \""+token+"\"",
                        frame.getURL(),
                        0
                );
            }
        });
        client.addLifeSpanHandler(new CefLifeSpanHandlerAdapter() {@Override
            public boolean doClose(CefBrowser browser) {
                System.out.println("Do close called");
                exit = true;
                return true;
            }
        });

        // Create a JFrame to host the browser
        JFrame frame = new JFrame("Time Table Scheduler");
        try {
            frame.setIconImage(ImageIO.read(new File("web\\timetableicon.png")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        frame.setSize(1260, 700);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.getContentPane().add(browser.getUIComponent(), BorderLayout.CENTER);
        frame.setVisible(true);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                browser.executeJavaScript(
                        "window.dispatchEvent(closeEvent);",
                        browser.getFocusedFrame().getURL(),
                        0
                );
            }
        });
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        while(!JcefLauncher.exit);
        app.dispose();
        frame.dispose();
        System.out.println("System exiting");
        System.exit(0);
    }
}
