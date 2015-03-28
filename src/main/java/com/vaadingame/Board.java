package com.vaadingame;

import java.util.LinkedList;
import java.util.Random;

public class Board {

    private int randomField() {
        Random random = new Random();
        return random.nextInt(10);
    }

    private int randomDirection() {
        Random random = new Random();
        return random.nextInt(2);
    }

    private void placeShip(int length, int[][] tab) {
        int x,y;
        LinkedList<Integer> directions = new LinkedList<Integer>();
        boolean done = false;
        boolean canPlaceHere;

        x=randomField();
        y=randomField();

        //losuje puste pole
        while (tab[x][y] == 1 || tab[x][y] == 2) {x= randomField(); y=randomField();}

        //losowa kolejność kierunków (poziomo lub pionowo)
        for (int i=0; i<2; i++) {
            int d = randomDirection();
            while (directions.size() < 2) {
                while (directions.contains(d)) {d = randomDirection();}
                directions.add(d);
            }
        }

        outerloop:
        for(int direction : directions) {
            canPlaceHere = true;
            if(direction == 0 && y+length-1 > 9) {
                //System.out.println("poziomo się nie zmieści");
                canPlaceHere = false; continue outerloop;
            }
            if(direction == 1 && x+length-1 > 9) {
                //System.out.println("pionowo się nie zmieści");
                canPlaceHere = false; continue outerloop;
            }

            //w tym momencie canPlaceHere=true oraz statek nie wykracza poza planszę
            if(direction == 0) {
                //sprawdzam każde pole statku oraz otoczenie tego pola
                for(int tempY = y; tempY<y+length; tempY++) {
                    if(tab[x][tempY] == 1 || tab[x][tempY] == 2) {canPlaceHere = false; break;}
                    if(x-1>-1 && tab[x-1][tempY] == 2) {canPlaceHere = false; break;}
                    if(x-1>-1 && tempY-1>-1 && tab[x-1][tempY-1] == 2) {canPlaceHere = false; break;}
                    if(x-1>-1 && tempY+1<10 && tab[x-1][tempY+1] == 2) {canPlaceHere = false; break;}
                    if(tempY-1>-1 && tab[x][tempY-1] == 2) {canPlaceHere = false; break;}
                    if(tempY+1<10 && tab[x][tempY+1] == 2) {canPlaceHere = false; break;}
                    if(x+1<10 && tempY-1>-1 && tab[x+1][tempY-1] == 2) {canPlaceHere = false; break;}
                    if(x+1<10 && tab[x+1][tempY] == 2) {canPlaceHere = false; break;}
                    if(x+1<10 && tempY+1<10 && tab[x+1][tempY+1] == 2) {canPlaceHere = false; break;}
                }
                if(canPlaceHere) {
                    //ustawiam pola otaczające statek na 1
                    for(int tempY = y; tempY<y+length; tempY++) {
                        if(x-1>-1) tab[x-1][tempY] = 1;
                        if(x-1>-1 && tempY-1>-1) tab[x-1][tempY-1] = 1;
                        if(x-1>-1 && tempY+1<10) tab[x-1][tempY+1] = 1;
                        if(tempY-1>-1) tab[x][tempY-1] = 1;
                        if(tempY+1<10) tab[x][tempY+1] = 1;
                        if(x+1<10 && tempY-1>-1) tab[x+1][tempY-1] = 1;
                        if(x+1<10) tab[x+1][tempY] = 1;
                        if(x+1<10 && tempY+1<10) tab[x+1][tempY+1] = 1;
                    }
                    //ustawiam statek
                    for(int tempY = y; tempY<y+length; tempY++) {
                        tab[x][tempY] = 2;
                    }
                    //System.out.println("umieszczam statek poziomo");
                    done = true;
                    break outerloop;
                } else {
                    //System.out.println("poziomo się nie da");
                    continue outerloop;
                }
            }
            if(direction == 1) {
                //sprawdzam każde pole statku oraz otoczenie tego pola
                for(int tempX = x; tempX<x+length; tempX++) {
                    if(tab[tempX][y] == 1 || tab[tempX][y] == 2) {canPlaceHere = false; break;}
                    if(tempX-1>-1 && tab[tempX-1][y] == 2) {canPlaceHere = false; break;}
                    if(tempX-1>-1 && y-1>-1 && tab[tempX-1][y-1] == 2) {canPlaceHere = false; break;}
                    if(tempX-1>-1 && y+1<10 && tab[tempX-1][y+1] == 2) {canPlaceHere = false; break;}
                    if(y-1>-1 && tab[tempX][y-1] == 2) {canPlaceHere = false; break;}
                    if(y+1<10 && tab[tempX][y+1] == 2) {canPlaceHere = false; break;}
                    if(tempX+1<10 && y-1>-1 && tab[tempX+1][y-1] == 2) {canPlaceHere = false; break;}
                    if(tempX+1<10 && tab[tempX+1][y] == 2) {canPlaceHere = false; break;}
                    if(tempX+1<10 && y+1<10 && tab[tempX+1][y+1] == 2) {canPlaceHere = false; break;}
                }
                if(canPlaceHere) {
                    //ustawiam pola otaczające statek na 1
                    for(int tempX = x; tempX<x+length; tempX++) {
                        if(tempX-1>-1) tab[tempX-1][y] = 1;
                        if(tempX-1>-1 && y-1>-1) tab[tempX-1][y-1] = 1;
                        if(tempX-1>-1 && y+1<10) tab[tempX-1][y+1] = 1;
                        if(y-1>-1) tab[tempX][y-1] = 1;
                        if(y+1<10) tab[tempX][y+1] = 1;
                        if(tempX+1<10 && y-1>-1) tab[tempX+1][y-1] = 1;
                        if(tempX+1<10) tab[tempX+1][y] = 1;
                        if(tempX+1<10 && y+1<10) tab[tempX+1][y+1] = 1;
                    }
                    //ustawiam statek
                    for(int tempX = x; tempX<x+length; tempX++) {
                        tab[tempX][y] = 2;
                    }
                    //System.out.println("umieszczam statek pionowo");
                    done = true;
                    break outerloop;
                } else {
                    //System.out.println("pionowo się nie da");
                    continue outerloop;
                }
            }
        }
        if(!done) {
            //System.out.println("jeszcze raz");
            placeShip(length, tab);
        }
    }

    public int[][] getRandom() {
        int[][] tab = new int[10][10];
        placeShip(5, tab);
        placeShip(4, tab);
        placeShip(3, tab);
        placeShip(3, tab);
        placeShip(2, tab);
        return tab;
    }
}
