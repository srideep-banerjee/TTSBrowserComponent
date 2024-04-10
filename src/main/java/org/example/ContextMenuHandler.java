package org.example;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.callback.CefContextMenuParams;
import org.cef.callback.CefMenuModel;
import org.cef.handler.CefContextMenuHandlerAdapter;

import java.awt.*;
import java.net.URI;

public class ContextMenuHandler extends CefContextMenuHandlerAdapter {
    private final int RELOAD = 3;
    String url;
    private CefBrowser devTools;
    private CefBrowser browser;

    public ContextMenuHandler(CefBrowser browser, String url) {
        this.devTools = browser.getDevTools();
        this.browser = browser;
        this.url = url;
    }

    @Override
    public void onBeforeContextMenu(
            CefBrowser browser, CefFrame frame, CefContextMenuParams params, CefMenuModel model
    ) {
        // Clear the default context menu
        model.clear();

        // Add a custom menu item
        model.addItem(RELOAD, "Reload");
        model.setEnabled(RELOAD, true);
    }

    @Override
    public boolean onContextMenuCommand(CefBrowser browser, CefFrame frame,
                                        CefContextMenuParams params, int commandId, int eventFlags
    ) {
        // Register a listener for custom menu item clicks
        switch (commandId) {
            case RELOAD -> {
                browser.reload();
                return true;
            }
        }
        return false;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
