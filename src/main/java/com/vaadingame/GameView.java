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
    private Player enemy; //przeciwnik

    private VerticalLayout listLayout;
    private Panel panelPlayers;
    private Table tablePlayers;
    private Window waitingWindow;

    private VerticalLayout gameLayout;
    private Table tableMyBoard; //plansza gracza
    private Table tableEnemyBoard; //plansza przeciwnika
    private Label lblTurn; //czyja kolej
    private Label lblMyName;
    private Label lblEnemyName;
    private boolean myTurn; //czy teraz mój ruch

    public GameView(final Navigator navigator) {
        setSizeFull();
        this.navigator = navigator;
        setMargin(true);

        //region widok listy graczy
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
        //na początku ustawiam widok na listę graczy
        setContent("list");
        //endregion

        //region widok gry
        gameLayout = new VerticalLayout();
        gameLayout.setSizeFull();
        gameLayout.setHeightUndefined();

        //region plansza gracza
        tableMyBoard = new Table();
        //dodaję kolumny
        for(int i=1; i<=10; i++) {
            tableMyBoard.addContainerProperty(i + "", String.class, null);
            tableMyBoard.setColumnWidth(i + "", 40);
        }
        //dodaje wiersze
        for(int i=0; i<10; i++) {
            tableMyBoard.addItem(new Object[]{" ", " ", " ", " ", " ", " ", " ", " ", " ", " "}, i + 1);
        }
        //ukrywam nagłówki
        tableMyBoard.setColumnHeaderMode(Table.ColumnHeaderMode.HIDDEN);
        //ustawiam wysokość tabeli
        tableMyBoard.setPageLength(10);
        //kolorowanie danego pola w zależności od jego wartości
        tableMyBoard.setCellStyleGenerator(new Table.CellStyleGenerator() {
            @Override
            public String getStyle(Table table, Object itemId, Object propertyId) {
                if (propertyId != null) {
                    if (table.getItem(itemId).getItemProperty(propertyId).getValue().toString().equals(" "))
                        return "myboard-water";
                    else if (table.getItem(itemId).getItemProperty(propertyId).getValue().toString().equals("  "))
                        return "myboard-ship";
                    else if (table.getItem(itemId).getItemProperty(propertyId).getValue().toString().equals("   "))
                        return "myboard-mishit";
                    else if (table.getItem(itemId).getItemProperty(propertyId).getValue().toString().equals("    "))
                        return "myboard-hit";
                    else return null;
                }
                return null;
            }
        });
        //endregion

        //region plansza przeciwnika
        tableEnemyBoard = new Table();
        //dodaję kolumny
        for(int i=1; i<=10; i++) {
            tableEnemyBoard.addContainerProperty(i + "", String.class, null);
            tableEnemyBoard.setColumnWidth(i + "", 40);
        }
        //dodaje wiersze
        for(int i=0; i<10; i++) {
            tableEnemyBoard.addItem(new Object[]{" ", " ", " ", " ", " ", " ", " ", " ", " ", " "}, i + 1);
        }
        //ukrywam nagłówki
        tableEnemyBoard.setColumnHeaderMode(Table.ColumnHeaderMode.HIDDEN);
        //ustawiam wysokość tabeli
        tableEnemyBoard.setPageLength(10);
        //kolorowanie danego pola w zależności od jego wartości
        tableEnemyBoard.setCellStyleGenerator(new Table.CellStyleGenerator() {
            @Override
            public String getStyle(Table table, Object itemId, Object propertyId) {
                if (propertyId != null) {
                    if (table.getItem(itemId).getItemProperty(propertyId).getValue().toString().equals(" "))
                        return "enemyboard-unknown";
                    else if (table.getItem(itemId).getItemProperty(propertyId).getValue().toString().equals("  "))
                        return "enemyboard-mishit";
                    else if (table.getItem(itemId).getItemProperty(propertyId).getValue().toString().equals("   "))
                        return "enemyboard-hit";
                    else return null;
                }
                return null;
            }
        });
        //endregion

        //region rozmieszczenie komponentów
        HorizontalLayout horizontalLayoutBoards = new HorizontalLayout();
        HorizontalLayout horizontalLayoutNames = new HorizontalLayout();

        lblTurn = new Label("Ruch przeciwnika");
        lblMyName = new Label("Twój nick");
        lblEnemyName = new Label("Nick przeciwnika");

        lblTurn.setWidthUndefined();
        lblMyName.setWidthUndefined();
        lblEnemyName.setWidthUndefined();

        gameLayout.addComponent(lblTurn);
        gameLayout.setComponentAlignment(lblTurn, Alignment.MIDDLE_CENTER);

        gameLayout.addComponent(horizontalLayoutNames);
        horizontalLayoutNames.addComponents(lblMyName, lblEnemyName);
        horizontalLayoutNames.setSizeFull();
        horizontalLayoutNames.setComponentAlignment(lblMyName, Alignment.MIDDLE_CENTER);
        horizontalLayoutNames.setComponentAlignment(lblEnemyName, Alignment.MIDDLE_CENTER);

        gameLayout.addComponent(horizontalLayoutBoards);
        horizontalLayoutBoards.addComponent(tableMyBoard);
        horizontalLayoutBoards.addComponent(tableEnemyBoard);
        horizontalLayoutBoards.setSizeFull();
        horizontalLayoutBoards.setComponentAlignment(tableMyBoard, Alignment.MIDDLE_CENTER);
        horizontalLayoutBoards.setComponentAlignment(tableEnemyBoard, Alignment.MIDDLE_CENTER);
        //endregion
        //endregion
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

    //rozpoczęcie gry
    private void startGame() {
        //TODO startGame
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
                    enemy = player;
                    enemy.accepted(GameView.this);
                    window.close();
                    setContent("game");
                    startGame();
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

    // zaproszenie zaakceptowane
    @Override
    public void accepted(Player player) {
        getUI().getSession().lock();
        try {
            waitingWindow.close();
            enemy = player;
            setContent("game");
            startGame();
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

}
