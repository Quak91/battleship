package com.vaadingame;

public interface Player {
    String getName();
    void receiveInvitation(Player player);
    void declined();
    void accepted(Player player);

    // komunikacja w trakcie gry
    void getShot(int x, int y); // przeciwnik do mnie strzela
    void hit(int x, int y);     // trafiłem
    void mishit(int x, int y);  // nie trafiłem
}
