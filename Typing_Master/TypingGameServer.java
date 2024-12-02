import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.PriorityBlockingQueue;

public class TypingGameServer {
    private static final int PORT = 12345;
    private static final String TEST_TEXT ="Artificial Intelligence (AI) is a branch of computer science that enables\n machines to perform tasks that typically require human intelligence. It\n uses algorithms to analyze data, learn patterns, and make decisions\n or predictions. AI powers technologies like voice assistants,\n self-driving cars, and recommendation systems. It has revolutionized industries\n";
    ///new RandomSentenceGenerator().getGeneratedSentences()
    private static final PriorityBlockingQueue<Player> leaderboard = new PriorityBlockingQueue<>();

    public static void main(String[] args) {
        new TypingGameServer();
        System.out.println("Server is running...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                System.out.println("Waiting for two players to connect...");
                Socket player1Socket = serverSocket.accept();
                System.out.println("Player 1 connected.");
                Socket player2Socket = serverSocket.accept();
                System.out.println("Player 2 connected.");

                new Thread(new GameHandler(player1Socket, player2Socket)).start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    static class GameHandler implements Runnable {
        private final Socket player1;
        private final Socket player2;

        public GameHandler(Socket player1, Socket player2) {
            this.player1 = player1;
            this.player2 = player2;
        }

        @Override
        public void run() {
            try (
                    BufferedReader input1 = new BufferedReader(new InputStreamReader(player1.getInputStream()));
                    PrintWriter output1 = new PrintWriter(player1.getOutputStream(), true);
                    BufferedReader input2 = new BufferedReader(new InputStreamReader(player2.getInputStream()));
                    PrintWriter output2 = new PrintWriter(player2.getOutputStream(), true)
            ) {
                // Send the test text to both players
                System.out.println("Sending test text to players...");
                long player1StartTime = System.currentTimeMillis();
                output1.println(TEST_TEXT);

                long player2StartTime = System.currentTimeMillis();
                output2.println(TEST_TEXT);

                // Receive typed text from players and record end times
                String player1Text = input1.readLine();
                long player1EndTime = System.currentTimeMillis();

                String player2Text = input2.readLine();
                long player2EndTime = System.currentTimeMillis();

                // Calculate individual times
                long player1TimeTaken = (player1EndTime - player1StartTime) / 1000;
                long player2TimeTaken = (player2EndTime - player2StartTime) / 1000;

                // Process results
                Player player1 = new Player("Player 1", player1Text, calculateWPM(player1Text, player1TimeTaken), calculateAccuracy(player1Text, TEST_TEXT), player1TimeTaken);
                Player player2 = new Player("Player 2", player2Text, calculateWPM(player2Text, player2TimeTaken), calculateAccuracy(player2Text, TEST_TEXT), player2TimeTaken);

                // Determine winner using composite score
                Player winner = determineWinner(player1, player2);

                // Add to leaderboard
                leaderboard.offer(player1);
                leaderboard.offer(player2);

                // Send results to both players
                output1.println(generateResultsMessage(player1, player2, winner));
                output2.println(generateResultsMessage(player2, player1, winner));

                // Display leaderboard
                System.out.println("Leaderboard:");
                leaderboard.forEach(p -> System.out.println(p.name + ": WPM = " + p.wpm + ", Accuracy = " + p.accuracy + "%, Score = " + p.compositeScore));

            } catch (IOException e) {
                System.err.println("Game error: " + e.getMessage());
            } finally {
                try {
                    player1.close();
                    player2.close();
                } catch (IOException e) {
                    System.err.println("Error closing sockets: " + e.getMessage());
                }
            }
        }

        private Player determineWinner(Player player1, Player player2) {
            return player1.compositeScore >= player2.compositeScore ? player1 : player2;
        }

        private int calculateWPM(String typedText, long totalTimeSeconds) {
            int wordCount = typedText.trim().isEmpty() ? 0 : typedText.split("\\s+").length;
            return totalTimeSeconds == 0 ? 0 : (int) ((wordCount * 60) / totalTimeSeconds);
        }

        private int calculateAccuracy(String typedText, String originalText) {
            return calculateLevenshteinAccuracy(typedText, originalText);
        }

        private int calculateLevenshteinAccuracy(String s1, String s2) {
            int[][] dp = new int[s1.length() + 1][s2.length() + 1];
            for (int i = 0; i <= s1.length(); i++) {
                for (int j = 0; j <= s2.length(); j++) {
                    if (i == 0) {
                        dp[i][j] = j;
                    } else if (j == 0) {
                        dp[i][j] = i;
                    } else {
                        dp[i][j] = Math.min(dp[i - 1][j - 1] + (s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1),
                                Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1));
                    }
                }
            }
            int maxLen = Math.max(s1.length(), s2.length());
            int distance = dp[s1.length()][s2.length()];
            return maxLen == 0 ? 100 : ((maxLen - distance) * 100) / maxLen;
        }

        private String generateResultsMessage(Player player, Player opponent, Player winner) {
            StringBuilder message = new StringBuilder();
            message.append("Game Over!\n");

            // Display the current player's stats (time, WPM, accuracy, composite score)
            message.append(player.name).append("'s Stats:\n");
            message.append("Your Time Taken: ").append(player.timeTaken).append(" seconds\n");
            message.append("Your WPM: ").append(player.wpm).append("\n");
            message.append("Your Accuracy: ").append(player.accuracy).append("%\n");
            message.append("Your Composite Score: ").append(player.compositeScore).append("\n");

            // Display the opponent's stats
            message.append(opponent.name).append("'s Stats:\n");
            message.append(opponent.name).append("'s Time Taken: ").append(opponent.timeTaken).append(" seconds\n");
            message.append(opponent.name).append("'s WPM: ").append(opponent.wpm).append("\n");
            message.append(opponent.name).append("'s Accuracy: ").append(opponent.accuracy).append("%\n");
            message.append(opponent.name).append("'s Composite Score: ").append(opponent.compositeScore).append("\n");

            // Display the result (winner/loser)
            message.append("Result: ");
            if (player.equals(winner)) {
                message.append("You are the Winner!");
            } else if (opponent.equals(winner)) {
                message.append("You are the Loser!");
            } else {
                message.append("It's a Tie!");
            }

            return message.toString();
        }


    }

    static class Player implements Comparable<Player> {
        String name;
        String typedText;
        int wpm;
        int accuracy;
        long timeTaken;
        double compositeScore;

        public Player(String name, String typedText, int wpm, int accuracy, long timeTaken) {
            this.name = name;
            this.typedText = typedText;
            this.wpm = wpm;
            this.accuracy = accuracy;
            this.timeTaken = timeTaken;
            this.compositeScore = calculateCompositeScore();
        }

        private double calculateCompositeScore() {
            final double WPM_WEIGHT = 0.5;
            final double ACCURACY_WEIGHT = 0.5;
            return (this.wpm * WPM_WEIGHT) + (this.accuracy * ACCURACY_WEIGHT);
        }

        @Override
        public int compareTo(Player other) {
            return Double.compare(other.compositeScore, this.compositeScore);
        }
    }
}