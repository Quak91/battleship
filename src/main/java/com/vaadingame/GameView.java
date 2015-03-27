package com.vaadingame;

import com.vaadin.data.Item;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import java.util.LinkedList;

public class GameView extends VerticalLayout implements View, Broadcaster.BroadcastListener{
    final Navigator navigator;
    private String name; //nazwa gracza
    private Broadcaster.BroadcastListener opponent;

    private VerticalLayout listLayout;
    private Table table;
    private Window waitingWindow;

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
                Broadcaster.unregister(GameView.this);
                navigator.navigateTo("");
            }
        }));

        // widok gry
        gameLayout = new VerticalLayout();
        gameLayout.setSizeFull();
        gameLayout.addComponent(new Button("shoot", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                opponent.getShot();
            }
        }));

        setContent("list");
    }

    @Override
    public void getShot() {
        getUI().getSession().lock();
        try {
            addComponent(new Label(opponent.getName()+" strzelił do ciebie!"));
        } finally {
            getUI().getSession().unlock();
        }
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
            Broadcaster.register(this);
        }
    }

    @Override
    public void receiveList(LinkedList<Broadcaster.BroadcastListener> players) {
        getUI().getSession().lock();
        try {
            table.removeAllItems();
            int i = 1;
            for (final Broadcaster.BroadcastListener player : players) {
                if (!getName().equals(player.getName())) {
                    Object newItemId = table.addItem();
                    Item row = table.getItem(newItemId);
                    row.getItemProperty("Lp").setValue(i);
                    row.getItemProperty("Nazwa gracza").setValue(player.getName());
                    row.getItemProperty("Zaproś do gry").setValue(new Button("Zaproś do gry", new Button.ClickListener() {
                        //wysyłanie zaproszenia
                        @Override
                        public void buttonClick(Button.ClickEvent clickEvent) {
                            invite(player);
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

    // zaproszenie do gry
    private void invite(Broadcaster.BroadcastListener player) {
        System.out.println(GameView.this.getName()+" zaprasza gracza "+player.getName());
        player.receiveInvitation(this);
        Broadcaster.unregister(this);
        waitingWindow = new Window("Czekaj");
        VerticalLayout windowLayout = new VerticalLayout();
        windowLayout.setMargin(true);
        waitingWindow.setContent(windowLayout);
        windowLayout.addComponent(new Label("Proszę czekać..."));
        windowLayout.addComponent(new Label(""));
        waitingWindow.setModal(true);
        waitingWindow.setClosable(false);
        waitingWindow.center();
        getUI().addWindow(waitingWindow);
    }

    @Override
    public void accepted(Broadcaster.BroadcastListener player) {
        getUI().getSession().lock();
        try {
            waitingWindow.close();
            opponent = player;
            setContent("game");
            //TODO rozpoczęcie gry
        } finally {
            getUI().getSession().unlock();
        }
    }

    @Override
    public void declined() {
        getUI().getSession().lock();
        try {
            waitingWindow.close();
            Broadcaster.register(this);
        } finally {
            getUI().getSession().unlock();
        }
    }

    @Override
    public void receiveInvitation(final Broadcaster.BroadcastListener player) {
        getUI().getSession().lock();
        try {
            Broadcaster.unregister(this);
            final Window window = new Window("Zaproszenie");
            VerticalLayout windowLayout = new VerticalLayout();
            windowLayout.setMargin(true);
            window.setContent(windowLayout);
            windowLayout.addComponent(new Label(player.getName()+" zaprasza do gry."));
            windowLayout.addComponent(new Label("Akceptujesz zaproszenie?"));
            windowLayout.addComponent(new Label(""));
            Button btnOk = new Button("Tak", new Button.ClickListener() {
                @Override
                public void buttonClick(Button.ClickEvent clickEvent) {
                    opponent = player;
                    opponent.accepted(GameView.this);
                    window.close();
                    setContent("game");
                    //TODO rozpoczęcie gry
                }
            });
            Button btnNo = new Button("Nie", new Button.ClickListener() {
                @Override
                public void buttonClick(Button.ClickEvent clickEvent) {
                    player.declined();
                    Broadcaster.register(GameView.this);
                    window.close();
                }
            });
            HorizontalLayout buttonsLayout = new HorizontalLayout(btnOk, new Label("&nbsp;&nbsp;&nbsp;", ContentMode.HTML), btnNo);
            buttonsLayout.setSizeUndefined();
            windowLayout.addComponent(buttonsLayout);
            windowLayout.setComponentAlignment(buttonsLayout, Alignment.MIDDLE_CENTER);
            window.center();
            window.setModal(true);
            window.setClosable(false);
            getUI().addWindow(window);
        } finally {
            getUI().getSession().unlock();
        }
    }

}
