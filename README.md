Documentation for Typing Game Server and Client
Overview:
The Typing Game is a two-player multiplayer typing game consisting of a Server and a Client. The game is designed to test players' typing speed and accuracy, calculating metrics like Words Per Minute (WPM) and typing accuracy. The game follows a client-server architecture where the server handles game logic, player communication, and result processing, while the client provides the user interface and interaction.
System Architecture:
Server:
The server manages the game logic, keeps track of player statistics, and determines the winner based on typing speed and accuracy.
It runs on a dedicated machine and listens for incoming connections from clients (players).
Once two players are connected, the server sends the test text to both clients and waits for the players to type and submit their results.
The server calculates performance metrics (WPM, accuracy), determines the winner, and displays the leaderboard.
Client:
The client is a graphical user interface (GUI) that allows players to interact with the game.
It connects to the server, receives the test text, and sends the player's typed text back to the server.
The client includes a countdown timer and displays the server's results once the game is over.
The client prevents submission once the game is finished and displays the final result.
Game Flow:
Server Initialization:
The server listens for incoming player connections on a specified port.
When two players connect, it sends the test text to both players and starts the game.
Client Interaction:
The client connects to the server and displays the test text to the player.
The countdown timer starts at 60 seconds.
The player types the test text and submits their typed text before the timer runs out.
The client automatically submits the text when the timer reaches zero.
Results Calculation:
The server calculates the performance metrics (WPM, accuracy) for both players.
The server compares the players' results and determines the winner.
The leaderboard is updated with the players' scores.
Results Display:
The server sends the results to both clients, displaying each player’s statistics and the overall winner.
Server: TypingGameServer
Responsibilities:
Accept connections from two players.
Send test text to players.
Receive typed text from both players.
Calculate Words Per Minute (WPM) and accuracy based on the typed text.
Determine the winner by comparing the composite scores (WPM + accuracy).
Update the leaderboard with players' scores.
Send the results to both players.
Key Classes and Methods:
TypingGameServer:
main(): Starts the server, listens for incoming player connections, and starts a new game handler when two players are connected.
GameHandler:
run(): Manages the game logic for a pair of players. Sends the test text to both players, calculates the WPM and accuracy, determines the winner, and sends the results to both players.
Player:
A class that represents a player, storing their name, typed text, WPM, accuracy, time taken, and composite score. Implements Comparable<Player> to allow sorting by score for the leaderboard.
Helper Methods:
calculateWPM(): Calculates Words Per Minute based on the player's typed text and time taken.
calculateAccuracy(): Calculates typing accuracy by comparing the player's typed text with the test text.
generateResultsMessage(): Generates a formatted message showing the results for both players and the winner.
Client: TypingGameClient
Responsibilities:
Connect to the server.
Display the test text and countdown timer to the player.
Allow the player to type and submit their text.
Receive game results from the server and display them.
Update the GUI with real-time game state changes.
Key Classes and Methods:
TypingGameClient:
main(): Initializes the client and starts the connection to the server.
startConnection(): Connects to the server, initializes input and output streams, and starts a new thread (ServerListener) to listen for incoming messages from the server.
sendTypedText(): Sends the player's typed text to the server. If the game is over, it prevents further submissions.
appendToServerMessages(): Updates the GUI with messages from the server.
switchToResultView(): Switches the GUI to display the "Game Over" screen after submitting the typed text.
startCountdownTimer(): Starts the countdown timer, updating the label every second. When the timer reaches zero, the game automatically submits the typed text.
ServerListener:
A background thread that listens for messages from the server, such as the test text or game results.
Appends the received messages to the serverMessageArea in the GUI.
Communication Protocol:
Server to Client:
The server sends the test text to both clients when the game starts.
The server also sends the results to both clients once the game ends (including WPM, accuracy, and winner).
Client to Server:
The client sends the typed text to the server after the player finishes typing or when the countdown timer expires.
GUI Layout (Client Side):
Top Section:
serverMessageArea: Displays messages from the server (e.g., the test text, game results).
Center Section:
userInputArea: A multi-line text area where the player types the test text.
Bottom Section:
timerLabel: Displays the remaining time in the game.
submitButton: A button that allows the player to submit their typed text.
Timer Functionality (Client Side):
A countdown timer starts at 60 seconds when the client connects to the server.
Every second, the timer is updated on the GUI.
Once the timer reaches zero, the client automatically submits the typed text.
Player Scoring System:
Words Per Minute (WPM):
WPM is calculated as:
WPM=Number of Words Typed×60Time Taken in Seconds
WPM= Time Taken in Seconds Number of Words Typed×60
​Accuracy:
Accuracy is calculated using the Levenshtein distance algorithm to compare the player's typed text to the original text.
The accuracy is represented as a percentage:
Accuracy=(Length of Text−Levenshtein DistanceLength of Text)×100Accuracy=(Length of TextLength of Text−Levenshtein Distance )×100
Composite Score:
The composite score is a weighted sum of WPM and accuracy:
Composite Score=(WPM×0.5)+(Accuracy×0.5)
Composite Score=(WPM×0.5)+(Accuracy×0.5)
The player with the highest composite score wins.
Example Communication Flow:
Server sends test text:
Server → "Please type the following text: Artificial Intelligence (AI)..."
Client sends typed text:
Client → "Artificial Intelligence (AI) is a branch of computer science..."
Server processes results:
Server calculates WPM and accuracy and determines the winner.
Server sends results:
Server → "Game Over! Player 1: WPM = 60, Accuracy = 95%. Player 2: WPM = 55, 
Accuracy = 98%. Player 1 is the winner!"
Client displays results:
Client → "Game Over! Player 1: WPM = 60, Accuracy = 95%. Player 2: WPM = 55, 
Accuracy = 98%. Player 1 is the winner!"
Error Handling:
Server-Side:
If there’s an error accepting connections or handling player data, the server logs the error and continues to wait for new connections.
Client-Side:
If the client fails to connect to the server, a dialog box informs the user about the connection issue.
If the client receives an unexpected message from the server, it shows an error message.
Future Improvements:
Multiple Players: Extend the server to support more than two players in a game.
Enhanced Scoring: Introduce additional scoring criteria (e.g., time bonuses).
Player Profiles: Implement player authentication and profile management, with scores stored on a server.
Advanced Text Generation: Add dynamic text generation or text difficulty levels to vary the typing challenge.
