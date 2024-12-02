import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class TypingGameClient {
    private static final String SERVER_ADDRESS = "192.168.180.143"; // Replace with your server IP
    private static final int SERVER_PORT = 12345;

    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;

    private JFrame frame;
    private JTextArea serverMessageArea;
    private JTextArea userInputArea; // Multi-line input area
    private JButton submitButton;
    private JLabel timerLabel; // Label to display the countdown timer

    private boolean isListening;
    private boolean gameOver = false; // To track if the game is over
    private int timeRemaining = 60; // Countdown time in seconds
    private Timer countdownTimer;

    public TypingGameClient() {
        // Set up the GUI
        frame = new JFrame("Typing Game Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setSize(600, 600); // Increased frame size for better visibility

        // Server message display with larger area and no scrolling
        serverMessageArea = new JTextArea(15, 40); // Increased height to 15 lines
        serverMessageArea.setFont(new Font("Arial", Font.PLAIN, 16)); // Larger font size
        serverMessageArea.setEditable(false);
        serverMessageArea.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1)); // Optional border

        // User input area
        userInputArea = new JTextArea(5, 40); // Multi-line input area
        userInputArea.setFont(new Font("Arial", Font.PLAIN, 16)); // Increased font size for input
        userInputArea.setLineWrap(true); // Wrap text within the area
        userInputArea.setWrapStyleWord(true); // Wrap at word boundaries

        // Submit button
        submitButton = new JButton("Submit");
        submitButton.setPreferredSize(new Dimension(100, 30)); // Moderate size
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendTypedText();
            }
        });

        // Timer label
        timerLabel = new JLabel("Time Remaining: 60 seconds");
        timerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        timerLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        // Layout for input area
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(userInputArea, BorderLayout.CENTER);

        // Timer and Submit button layout
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        bottomPanel.add(timerLabel, BorderLayout.CENTER);
        bottomPanel.add(submitButton, BorderLayout.EAST); // Submit button beside the timer

        frame.add(serverMessageArea, BorderLayout.NORTH);
        frame.add(inputPanel, BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH); // Timer and button at the bottom

        frame.setLocationRelativeTo(null); // Center the frame
        frame.setVisible(true);

        // Start the countdown timer
        startCountdownTimer();
    }

    private void startConnection() {
        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);

            // Start a thread to listen for server messages
            isListening = true;
            new Thread(new ServerListener()).start();

            // Read the initial message from the server
            String serverMessage = input.readLine();
            appendToServerMessages( serverMessage);

        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Error connecting to the server: " + e.getMessage(),
                    "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void sendTypedText() {
        if (gameOver) return; // Prevent further submissions after game over

        String typedText = userInputArea.getText().trim();

        if (typedText.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please type something before submitting.",
                    "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        output.println(typedText); // Send the typed text to the server
        userInputArea.setText(""); // Clear the input area

        // Switch to result view after submission
        switchToResultView();
    }

    private void appendToServerMessages(String message) {
        SwingUtilities.invokeLater(() -> {
            if (!gameOver) { // Only append messages if the game is not over
                serverMessageArea.append(message + "\n");
            }
        });
    }

    private void switchToResultView() {
        SwingUtilities.invokeLater(() -> {
            frame.getContentPane().removeAll(); // Remove all components from the frame

            // Display the "Game Over" message
            serverMessageArea.setText("Game Over!\n\n");

            // Add the message area to the frame for results
            frame.add(serverMessageArea, BorderLayout.CENTER);
            frame.revalidate(); // Refresh the frame to apply changes
            frame.repaint();
        });
    }

    private void startCountdownTimer() {
        countdownTimer = new Timer(1000, e -> {
            timeRemaining--;
            timerLabel.setText("Time Remaining: " + timeRemaining + " seconds");

            if (timeRemaining == 0) {
                submitButton.doClick();
                countdownTimer.stop();
                return;
                // gameOver = true;
                // Mark the game as over

                // Automatically trigger the submit action when time is up
                //  sendTypedText();  // Call the sendTypedText method directly
            }
        });
        countdownTimer.start();
    }

    private class ServerListener implements Runnable {
        @Override
        public void run() {
            try {
                while (isListening) {
                    String serverMessage = input.readLine();
                    if (serverMessage != null && !gameOver) {
                        appendToServerMessages(  serverMessage);
                    }
                }
            } catch (IOException e) {
                if (isListening) {
                    JOptionPane.showMessageDialog(frame, "Error receiving data from server: " + e.getMessage(),
                            "Communication Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TypingGameClient clientGUI = new TypingGameClient();
            clientGUI.startConnection();
        });
    }
}