package com.company;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Formatter;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*  There are two ways to create threads:
    1)Extending the Thread class
    2)Implementing the Runnable Interface
    I'm using second choice to extend JFrame class (as Java doesn't support multiple inheritance)
*/
public final class TicTacToeClient extends JFrame implements Runnable {
    private final JTextField idField;
    private final JTextArea displayArea;
    private final Square[][] board;
    private Square currentSquare;
    private Scanner input;
    private Formatter output;
    private final String ticTacToeHost;
    private boolean myTurn;
    private String mySymbol;

    public TicTacToeClient(String host) {
        // setting name of server
        ticTacToeHost = host;
        // setting up JTextArea
        displayArea = new JTextArea(4, 30);
        displayArea.setEditable(false);
        add(new JScrollPane(displayArea), BorderLayout.SOUTH);

        // setting up panel for squares in board
        JPanel boardPanel = new JPanel();
        boardPanel.setLayout(new GridLayout(3, 3, 0, 0));
        board = new Square[3][3];

        for (int row = 0; row < board.length; row++) {
            for (int column = 0; column < board[row].length; column++) {
                // create square
                board[row][column] = new Square(" ", row * 3 + column);
                boardPanel.add(board[row][column]);
            }
        }

        // setting up textfield
        idField = new JTextField();
        idField.setEditable(false);
        add(idField, BorderLayout.NORTH);

        // setting up panel to contain boardPanel
        JPanel panel = new JPanel();
        panel.add(boardPanel, BorderLayout.CENTER);
        add(panel, BorderLayout.CENTER);

        setSize(300, 225);
        setVisible(true);

        startClient();
    }

    // private inner class for the squares on the board
    private class Square extends JPanel {

        private String symbol;
        private final int location;

        public Square(String symbol, int location) {
            this.symbol = symbol;
            this.location = location;

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    setCurrentSquare(Square.this);

                    sendClickedSquare(getSquareLocation());
                }
            });
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(30, 30);
        }

        @Override
        public Dimension getMinimumSize() {
            return getPreferredSize();
        }

        public void setSymbol(String newSymbol) {
            symbol = newSymbol;
            repaint();
        }

        public int getSquareLocation() {
            return location;
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawRect(0, 0, 29, 29);
            g.drawString(symbol, 11, 20);
        }
    }

    public void setCurrentSquare(Square square) {
        currentSquare = square;
    }

    public void sendClickedSquare(int location) {
        if (myTurn) {
            output.format("%d\n", location);
            output.flush();
            myTurn = false;
        }
    }

    // starting the client thread
    public void startClient() {
        // connecting to server and get streams
        try {
            // making connection to the server
            Socket connection = new Socket(InetAddress.getByName(ticTacToeHost), 55555);

            // getting streams for input and output
            input = new Scanner(connection.getInputStream());
            output = new Formatter(connection.getOutputStream());
        } catch (IOException ioException) {
            System.out.println(ioException.toString());
        }

        // creating and starting worker thread for this client
        ExecutorService worker = Executors.newFixedThreadPool(1);
        worker.execute(this);
    }

    // run() method controls client's sent and received info
    @Override
    public void run() {
        mySymbol = input.nextLine();

        SwingUtilities.invokeLater(() -> {
            idField.setText("You are player \"" + mySymbol + "\"");
        });

        myTurn = (mySymbol.equals("X"));

        // receiving messages sent to client and outputting them
        while (true) {
            if (input.hasNextLine()) {
                process(input.nextLine());
            }
        }
    }

    // processing messages sent to the client
    private void process(String message) {
        switch (message) {
            case "Valid move.":
                display("Valid move, please wait.\n");
                setSymbol(currentSquare, mySymbol);
                break;
            case "Invalid move, try again":
                display(message + "\n");
                myTurn = true;
                break;
            case "Opponent moved":
                int location = input.nextInt();
                input.nextLine();
                int row = location / 3;
                int column = location % 3;
                setSymbol(board[row][column],
                        (mySymbol.equals("X") ? "O" : "X"));
                display("Opponent moved. Your turn.\n");
                myTurn = true;
                break;
            default:
                display(message + "\n");
                break;
        }
    }

    // manipulating displayArea in event-dispatch thread
    private void display(String message) {
        SwingUtilities.invokeLater(() -> {
            displayArea.append(message);
        });
    }

    // utility method to set mark on board in event-dispatch thread
    private void setSymbol(Square square, String symbol) {
        SwingUtilities.invokeLater(() -> {
            square.setSymbol(symbol);
        });
    }
}