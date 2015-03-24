package com.vaadingame;

import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.ui.VerticalLayout;

public class PlayersView extends VerticalLayout implements View{
    final Navigator navigator;

    public PlayersView(final Navigator navigator) {
        setSizeFull();
        this.navigator = navigator;

    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent viewChangeEvent) {
        Page.getCurrent().setTitle("Battleship - lista graczy");
        //jeśli niezalogowany to przekierowanie do logowania
        if(getSession().getAttribute("login")==null) navigator.navigateTo("login");
    }
}
