package com.vaadingame;

import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.*;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.UI;

@Push
@PreserveOnRefresh
@Theme("mytheme")
@Widgetset("com.vaadingame.MyAppWidgetset")
public class MyUI extends UI {
    private Navigator navigator;

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        navigator = new Navigator(this, this);
        navigator.addView("", new RegistrationView(navigator));
        navigator.addView("login", new LoginView(navigator));
        navigator.addView("players", new PlayersView(navigator));
        navigator.addView("game", new GameView(navigator));
    }

    @WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = MyUI.class, productionMode = false)
    public static class MyUIServlet extends VaadinServlet {
    }
}
