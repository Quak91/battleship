package com.vaadingame;

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlayersBroadcaster {
    static ExecutorService executorService = Executors.newSingleThreadExecutor();
    private static LinkedList<BroadcastListener> listeners = new LinkedList<BroadcastListener>();

    public interface BroadcastListener {
        void receiveList(LinkedList<BroadcastListener> list);
        String getName();
    }

    public static synchronized void register(BroadcastListener listener) {
        if(!listeners.contains(listener))
            listeners.add(listener);
        broadcastList();
    }

    public static synchronized void unregister(BroadcastListener listener) {
        if(listeners.contains(listener))
            listeners.remove(listener);
        broadcastList();
    }

    public static synchronized void broadcastList() {
        for (final BroadcastListener listener: listeners) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    listener.receiveList(listeners);
                }
            });
        }
    }
}
