package com.vaadingame;

public interface Player {
    String getName();
    void receiveInvitation(Player player);
    void declined();
    void accepted(Player player);
    void getShot();
}
