package com.vaadingame;

import com.vaadin.data.Item;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

import java.util.LinkedList;

public class PlayersView extends VerticalLayout implements View, PlayersBroadcaster.BroadcastListener{
    final Navigator navigator;
    private String name;
    private Table table;

    public PlayersView(final Navigator navigator) {
        setSizeFull();
        this.navigator = navigator;
        table = new Table("Lista graczy");
        table.addContainerProperty("Lp", Integer.class, null);
        table.addContainerProperty("Nazwa gracza", String.class, null);
        table.addContainerProperty("Zaproś do gry", Button.class, null);
        addComponent(table);
        addComponent(new Button("Wyloguj", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                getSession().setAttribute("login", null);
                PlayersBroadcaster.unregister(PlayersView.this);
                navigator.navigateTo("");
            }
        }));
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent viewChangeEvent) {
        //jeśli niezalogowany to przekierowanie do logowania
        if(getSession().getAttribute("login")==null) navigator.navigateTo("login");
        else {
            Page.getCurrent().setTitle("Battleship - lista graczy");
            this.name = getSession().getAttribute("login").toString();
            PlayersBroadcaster.register(this);
        }
    }

    @Override
    public void receiveList(LinkedList<PlayersBroadcaster.BroadcastListener> list) {
        getUI().getSession().lock();
        try {
            table.removeAllItems();
            int i = 1;
            for (PlayersBroadcaster.BroadcastListener listener : list) {
                //TODO nie dodawać siebie do tabeli (gracz nie może grać sam ze sobą)
                Object newItemId = table.addItem();
                Item row = table.getItem(newItemId);
                row.getItemProperty("Lp").setValue(i);
                row.getItemProperty("Nazwa gracza").setValue(listener.getName());
                row.getItemProperty("Zaproś do gry").setValue(new Button("Zaproś do gry"));
                i++;
            }
            table.setPageLength(table.size());
        } finally {
            getUI().getSession().unlock();
        }
    }

    @Override
    public String getName() {
        return name;
    }
}
