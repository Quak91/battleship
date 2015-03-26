package com.vaadingame;

import javax.servlet.annotation.WebServlet;
import com.google.gwt.json.client.JSONException;
import com.vaadin.annotations.*;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.JavaScriptFunction;
import com.vaadin.ui.UI;
import elemental.json.JsonArray;

@Push
@PreserveOnRefresh
@Theme("mytheme")
@Widgetset("com.vaadingame.MyAppWidgetset")
public class MyUI extends UI {
    private Navigator navigator;
    private PlayersView playersView;

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        navigator = new Navigator(this, this);
        playersView = new PlayersView(navigator);
        navigator.addView("", new RegistrationView(navigator));
        navigator.addView("login", new LoginView(navigator));
        navigator.addView("players", playersView);
        navigator.addView("game", new GameView(navigator));

        // wykrywanie opuszczenia strony, źródło: https://vaadin.com/forum#!/thread/2518250
        Page.getCurrent().getJavaScript().addFunction("aboutToClose", new JavaScriptFunction() {
            @Override
            public void call(JsonArray jsonArray) throws JSONException{
                clean();
            }
        });

        Page.getCurrent().getJavaScript().execute("window.onbeforeunload = function (e) { var e = e || window.event; aboutToClose(); return; };");
    }

    //usunięcie gracza ze wszystkich broadcasterów
    private void clean() {
        PlayersBroadcaster.unregister(playersView);
    }

    @WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = MyUI.class, productionMode = false, heartbeatInterval = 1, closeIdleSessions = true)
    public static class MyUIServlet extends VaadinServlet {
    }
}
