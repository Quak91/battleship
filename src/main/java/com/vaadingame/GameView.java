package com.vaadingame;

import com.vaadin.data.Item;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.ui.Button;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import java.util.LinkedList;

public class GameView extends VerticalLayout implements View, Broadcaster.BroadcastListener{
    final Navigator navigator;
    private String name; //nazwa gracza
    private String opponent; //nazwa przeciwnika

    private VerticalLayout listLayout;
    private Table table;

    private VerticalLayout gameLayout;

    public GameView(final Navigator navigator) {
        setSizeFull();
        this.navigator = navigator;

        // widok listy graczy
        listLayout = new VerticalLayout();
        listLayout.setSizeFull();
        table = new Table("Lista graczy");
        table.addContainerProperty("Lp", Integer.class, null);
        table.addContainerProperty("Nazwa gracza", String.class, null);
        table.addContainerProperty("Zaproś do gry", Button.class, null);
        listLayout.addComponent(table);
        listLayout.addComponent(new Button("Wyloguj", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                getSession().setAttribute("login", null);
                Broadcaster.unregister(GameView.this, "playerList");
                navigator.navigateTo("");
            }
        }));

        // widok gry
        gameLayout = new VerticalLayout();
        gameLayout.setSizeFull();
        this.opponent = "";
        setContent("list");
    }

    //zmiana widoku
    private void setContent(String content) {
        if (content.equals("list")) {
            this.removeAllComponents();
            this.addComponent(listLayout);
        } else
            if(content.equals("game")) {
                this.removeAllComponents();
                this.addComponent(gameLayout);
            }
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent viewChangeEvent) {
        //jeśli niezalogowany to przekierowanie do logowania
        if(getSession().getAttribute("login")==null) navigator.navigateTo("login");
        else {
            Page.getCurrent().setTitle("Battleship");
            this.name = getSession().getAttribute("login").toString();
            Broadcaster.register(this, "playerList");
        }
    }

    @Override
    public void receiveList(LinkedList<Broadcaster.BroadcastListener> list) {
        getUI().getSession().lock();
        try {
            table.removeAllItems();
            int i = 1;
            for (final Broadcaster.BroadcastListener listener : list) {
                if (!getName().equals(listener.getName())) {
                    Object newItemId = table.addItem();
                    Item row = table.getItem(newItemId);
                    row.getItemProperty("Lp").setValue(i);
                    row.getItemProperty("Nazwa gracza").setValue(listener.getName());
                    row.getItemProperty("Zaproś do gry").setValue(new Button("Zaproś do gry", new Button.ClickListener() {
                        //wysyłanie zaproszenia
                        @Override
                        public void buttonClick(Button.ClickEvent clickEvent) {
                            sendInvitation(listener.getName());
                        }
                    }));
                    i++;
                }
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

    @Override
    public String getOpponent() {
        return opponent;
    }

    // wysyłanie zaproszenia
    private void sendInvitation(String r) {
        Broadcaster.invite(getName(), r);
        opponent = r;
        Broadcaster.unregister(this, "playerList");
        Broadcaster.register(this, "game");
        //TODO wyświetlić okno w trakcie oczekiwania na decyzję przeciwnika
    }

    @Override
    public void receiveInvitation(String s) {
        //window.setmodal .setclosable
        opponent = s;
        Broadcaster.unregister(this, "playerList");
        Broadcaster.register(this, "game");
        /* TODO wyświetlić okno z pytaniem "czy akceptujesz zaproszenie od gracza s?"
            NIE -> Broadcaster.decline(opponent), opponent ="", zmiana kanału (game->playerList), window.close()
            TAK -> Broadcaster.accept(opponent), window.close(), setContent("game"), rozpoczęcie gry
         */
    }

    @Override
    public void invitationAccepted() {
        //TODO window.close(), setContent("game"), rozpoczęcie gry
    }

    @Override
    public void invitationDeclined() {
        //TODO window.close(), Notification, zmiana kanału(game->playerList)
    }
}
