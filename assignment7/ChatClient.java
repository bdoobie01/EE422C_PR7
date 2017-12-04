/* Chat Program <ChatClient>
 * EE422C Project 7 submission by
 * Replace <...> with your actual data.
 * Turan Vural
 * tzv57
 * <Student1 5-digit Unique No.>
 * <Student2 Name>
 * <Student2 EID>
 * <Student2 5-digit Unique No.>
 * Group on Canvas:
 * Slip days used: <0>
 * Fall 2017
 * GitHub URL: https://github.com/bdoobie01/EE422C_PR7.git
 */

package assignment7;

import com.sun.org.apache.xpath.internal.operations.Bool;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import sun.management.snmp.util.SnmpTableCache;
import sun.rmi.runtime.Log;

import java.io.*;
import java.net.Socket;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ChatClient extends Application {

    /*
    0: Chat message "0<chatCode><message>"
    1: Login request "1<username>^<password>"
    2: ChatGroup request "2<username1>^<username2>^<username3>...."
    3: Create username request "3<username>^<password>"
    4: Client closing "4"

    0: Chat message "0<chatCode><message>"
    1: Authentication "1Invalid username or password" or "1Authenticated"
    2: ChatGroup reply "2New Chatgroup created" or "2Unable to create new Chatgroup"
    3: Create username reply "3Username created" or "3Username is unavailable"
    4: 4<code>" when a different client in that group chat disconnects
    */

    private static String user = "<>";

    private static BufferedReader reader;
    private static PrintWriter writer;

    private static Map<Thread, IncomingReader> map = new HashMap<Thread, IncomingReader>();
    private static Socket socket;

    private static LoginClient loginClient;

    public static void main(String[] args) {
        try {
            launch(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void start(Stage primaryStage) throws Exception {
        try {

            setUpNetworking();

            loginClient = new LoginClient();
            loginClient.start(new Stage());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void setUpNetworking() throws Exception {
        //@SuppressWarnings("resource")
        if (socket == null) {
            socket = new Socket("127.0.0.1", 4242);

            InputStreamReader streamReader = new InputStreamReader(socket.getInputStream());
            reader = new BufferedReader(streamReader);

            writer = new PrintWriter(socket.getOutputStream());

            System.out.println("Client: networking established");

            IncomingReader incomingReader = new IncomingReader();
            Thread readerThread = new Thread(incomingReader);
            map.put(readerThread, incomingReader);
            readerThread.start();
        }
    }

    private static void closeNetworking() throws IOException {
        sendMessage("4");
        writer.close();
        socket.close();
        reader.close();
    }

    /*private void sendMessage() {
        writer.println("0" + user + outgoing.getText());
        writer.flush();
        outgoing.setText("");
        outgoing.requestFocus();
    }*/

    private static void sendMessage(String s) {
        writer.println(s);
        writer.flush();
    }

    private static class IncomingReader implements Runnable {
        private volatile boolean running = true;

        /**
         * Incoming lines are printed to UI while they exist
         */
        @Override
        public void run() {
            while (running) {
                String message;
                try {
                    while ((message = reader.readLine()) != null) {
                        try {
                            char c = message.charAt(0);

                            switch (c) {
                                case '0': // DM
                                    handleMessage(message);
                                    break;

                                case '1': // Login
                                    handleLogin(message);
                                    break;

                                case '2': // New Chat
                                    handleNewChatGroup(message);
                                    break;

                                case '3': // New Login
                                    handleNewUsernameRequest(message);
                                    break;

                                case '4': // Client Close
                                    handleClientClose(message);
                                    break;

                                default:
                                    break;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void handleMessage(String message) {

        }

        private void handleLogin(String message) {
            Platform.runLater(() -> {
                boolean authenticated = Boolean.parseBoolean(message.substring(1));
                if (authenticated) {
                    try {
                        user = loginClient.username.getText();
                        loginClient.stage.close();
                        ContactClient contactClient = new ContactClient();
                        contactClient.start(new Stage());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid username or password.");
                    alert.showAndWait();
                }

            });
        }

        private void handleNewChatGroup(String message) {
            Platform.runLater(() -> {

            });
        }

        private void handleNewUsernameRequest(String message) {
            Platform.runLater(() -> {
                boolean authenticated = Boolean.parseBoolean(message.substring(1));
                if (authenticated) {
                    try {
                        user = loginClient.username.getText();
                        loginClient.stage.close();
                        ContactClient contactClient = new ContactClient();
                        contactClient.start(new Stage());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid username.");
                    alert.showAndWait();
                }
            });
        }

        private void handleClientClose(String message) {

        }

        public void terminate() {
            running = false;
        }
    }

    private static class LoginClient extends Application {

        private Stage stage;

        private VBox loginBox;

        private TextField username;
        private TextField password;

        private Button login;
        private Button register;

        @Override
        public void start(Stage primaryStage) throws Exception {
            stage = primaryStage;

            loginBox = new VBox();
            loginBox.setPadding(new Insets(10));
            loginBox.setSpacing(5);

            username = new TextField("username");
            password = new TextField("password");

            login = new Button("login");
            login.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    sendMessage("1" + username.getText() + "^" + password.getText());
                }
            });
            register = new Button("register");
            register.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    sendMessage("3" + username.getText() + "^" + password.getText());
                }
            });

            loginBox.getChildren().addAll(username, password, login, register);
            stage.setScene(new Scene(loginBox));
            stage.show();

        }
    }

    private static class ContactClient extends Application {

        Stage stage;

        BorderPane borderPane;
        HBox upperBox;
        Label userLabel;
        Button newGroup;
        Accordion accordion;

        TitledPane friendsPane;
        TitledPane onlinePane;
        VBox friendsBox;
        VBox onlineBox;


        @Override
        public void start(Stage primaryStage) throws Exception {
            stage = primaryStage;
            stage.setTitle(user);

            borderPane = new BorderPane();
            upperBox = new HBox();
            userLabel = new Label(user);
            newGroup = new Button("+");
            accordion = new Accordion();

            friendsPane = new TitledPane();
            friendsPane.setText("Friends");
            friendsPane.setContent(friendsBox = new VBox());

            onlinePane = new TitledPane();
            onlinePane.setText("Online");
            onlinePane.setContent(onlineBox = new VBox());

            upperBox.getChildren().addAll(userLabel, newGroup);
            accordion.getPanes().addAll(friendsPane,onlinePane);


            borderPane.setTop(upperBox);
            borderPane.setCenter(accordion);

            stage.setScene(new Scene(borderPane));
            stage.show();


            newGroup.setOnAction(actionEvent -> {
                try {
                    GroupChatClient groupChatClient = new GroupChatClient();
                    groupChatClient.start(new Stage());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private static class MessageClient extends Application {

        private Stage stage;
        private String title;

        SplitPane splitPane;
        StackPane topPane;
        StackPane bottomPane;

        public MessageClient(String s) {
            title = s;
        }

        @Override
        public void start(Stage primaryStage) throws Exception {
            stage = primaryStage;
            stage.setTitle(title);

            splitPane = new SplitPane();
            topPane = new StackPane();
            bottomPane = new StackPane();

            splitPane.setOrientation(Orientation.VERTICAL);
            splitPane.getItems().addAll(topPane,bottomPane);

            stage.setScene(new Scene(splitPane));
            stage.show();
        }
    }

    private static class GroupChatClient extends Application {

        private Stage stage;

        private HBox box;
        private TextField usernames;
        private Button create;

        @Override
        public void start(Stage primaryStage) throws Exception {
            stage = primaryStage;
            stage.setTitle("Make group chat");

            box = new HBox();
            usernames = new TextField("friend1 friend2");
            create = new Button("Create");

            box.getChildren().addAll(usernames, create);

            stage.setScene(new Scene(box));
            stage.show();

            create.setOnAction(actionEvent -> {
                try {
                    String [] s = usernames.getText().split("\\W+");
                    String message = "2" + s[0];

                    for(int i = 1; i < s.length; i++) {
                        message+="^"+s[i];
                    }

                    sendMessage(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
