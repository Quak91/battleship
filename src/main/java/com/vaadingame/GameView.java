package com.vaadingame;

import com.vaadin.data.Item;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import java.util.LinkedList;

public class GameView extends VerticalLayout implements View, Broadcaster.BroadcastListener, Player{
    final Navigator navigator;
    private String name; //nazwa gracza
    private Player opponent; //przeciwnik

    private VerticalLayout listLayout;
    private Panel panelPlayers;
    private Table tablePlayers;
    private Window waitingWindow;

    private VerticalLayout gameLayout;

    public GameView(final Navigator navigator) {
        setSizeFull();
        this.navigator = navigator;
        setMargin(true);

        // widok listy graczy
        panelPlayers = new Panel("Lista graczy");
        panelPlayers.setSizeUndefined();
        listLayout = new VerticalLayout();
        listLayout.setSizeUndefined();
        listLayout.setMargin(true);
        tablePlayers = new Table();
        tablePlayers.addContainerProperty("Lp", Integer.class, null);
        tablePlayers.addContainerProperty("Nazwa gracza", String.class, null);
        tablePlayers.addContainerProperty("Zaproś do gry", Button.class, null);
        listLayout.addComponent(tablePlayers);
        panelPlayers.setContent(listLayout);
        Button btnLogout = new Button("Wyloguj", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                getSession().setAttribute("login", null);
                Broadcaster.unregister(GameView.this);
                navigator.navigateTo("");
            }
        });
        listLayout.addComponent(new Label("&nbsp;", ContentMode.HTML));
        listLayout.addComponent(btnLogout);
        listLayout.setComponentAlignment(btnLogout, Alignment.BOTTOM_RIGHT);

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
            this.addComponent(panelPlayers);
            setComponentAlignment(panelPlayers, Alignment.TOP_CENTER);
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
    public void receiveList(LinkedList<Broadcaster.BroadcastListener> listeners) {
        getUI().getSession().lock();
        try {
            tablePlayers.removeAllItems();
            int i = 1;
            for (final Broadcaster.BroadcastListener listener : listeners) {
                if (!getName().equals(((Player) listener).getName())) {
                    Object newItemId = tablePlayers.addItem();
                    Item row = tablePlayers.getItem(newItemId);
                    row.getItemProperty("Lp").setValue(i);
                    row.getItemProperty("Nazwa gracza").setValue(((Player) listener).getName());
                    row.getItemProperty("Zaproś do gry").setValue(new Button("Zaproś do gry", new Button.ClickListener() {
                        //wysyłanie zaproszenia
                        @Override
                        public void buttonClick(Button.ClickEvent clickEvent) {
                            invite((Player)listener);
                        }
                    }));
                    i++;
                }
            }
            tablePlayers.setPageLength(tablePlayers.size());
        } finally {
            getUI().getSession().unlock();
        }
    }

    @Override
    public String getName() {
        return name;
    }

    // zaproszenie do gry
    private void invite(Player player) {
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

    // zaproszenie zaakceptowane
    @Override
    public void accepted(Player player) {
        getUI().getSession().lock();
        try {
            waitingWindow.close();
            opponent = player;
            setContent("game");
            //TODO startGame
        } finally {
            getUI().getSession().unlock();
        }
    }

    // zaproszenie odrzucone
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

    // otrzymanie zaproszenia
    @Override
    public void receiveInvitation(final Player player) {
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
                    // akceptuje zaproszenie
                    opponent = player;
                    opponent.accepted(GameView.this);
                    window.close();
                    setContent("game");
                    //TODO startGame
                }
            });
            Button btnNo = new Button("Nie", new Button.ClickListener() {
                @Override
                public void buttonClick(Button.ClickEvent clickEvent) {
                    // odrzuca zaproszenie
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
