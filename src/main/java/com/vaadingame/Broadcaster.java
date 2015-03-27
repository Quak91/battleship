package com.vaadingame;

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Broadcaster {
    static ExecutorService executorService = Executors.newSingleThreadExecutor();

    // lista graczy, którzy nie są w grze (przeglądają listę graczy, mogą zapraszać do gry, otrzymywać zaproszenie)
    private static LinkedList<BroadcastListener> listenersPlayerList = new LinkedList<BroadcastListener>();

    // lista graczy, którzy są w trakcie gry
    private static  LinkedList<BroadcastListener> listenersGame = new LinkedList<BroadcastListener>();

    public interface BroadcastListener {
        void receiveList(LinkedList<BroadcastListener> list);
        String getName();
        void receiveInvitation(String s);
    }

    public static synchronized void register(BroadcastListener listener, String list) {
        if(list.equals("playerList")) {
            if (!listenersPlayerList.contains(listener))
                listenersPlayerList.add(listener);
        } else
        if(list.equals("game")) {
            if(!listenersGame.contains(listener))
                listenersGame.add(listener);
        }
        broadcastList();
    }

    public static synchronized void unregister(BroadcastListener listener, String list) {
        if(list.equals("playerList")) {
            if (listenersPlayerList.contains(listener))
                listenersPlayerList.remove(listener);
        } else
        if(list.equals("game")) {
            if(listenersGame.contains(listener))
                listenersGame.remove(listener);
        }
        broadcastList();
    }

    public static synchronized void broadcastList() {
        for (final BroadcastListener listener: listenersPlayerList) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    listener.receiveList(listenersPlayerList);
                }
            });
        }
    }

    public static synchronized void invite(final String s, String r) {
        for (final BroadcastListener listener: listenersPlayerList) {
            if(listener.getName().equals(r)) {
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        listener.receiveInvitation(s);
                    }
                });
                break;
            }
        }
    }
}
