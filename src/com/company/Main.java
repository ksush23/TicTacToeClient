package com.company;
import javax.swing.JFrame;

public class Main {

    public static void main(String[] args) {
        TicTacToeClient server1, server2;

        server1 = new TicTacToeClient("127.0.0.1");
        server2 = new TicTacToeClient("127.0.0.1");

        server1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        server2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}