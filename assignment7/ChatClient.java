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

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;


import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
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

    private static String user = "";

    private static Map<String, MessageClient> openChats;

    private static BufferedReader reader;
    private static PrintWriter writer;

    private static Map<Thread, IncomingReader> map = new HashMap<Thread, IncomingReader>();
    private static Socket socket;

    private static LoginClient loginClient;
    private static ContactClient contactClient;

    private static volatile ArrayList<String> onlineUsers;
    private static volatile boolean running;
    
    private static boolean userFlag;

    public static void main(String[] args) {
        try {
            openChats = new HashMap<String, MessageClient>();
            onlineUsers = new ArrayList<String>();
            running = true;

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

    private static void sendMessage(String s) {
        writer.println(s);
        writer.flush();
    }

    private static class IncomingReader implements Runnable {

        /**
         * Incoming lines are printed to UI while they exist
         */
        @Override
        public synchronized void run() {
            while (running) {
                String message;
                try {
                    while ((message = reader.readLine()) != null) {
                        try {
                            char c = message.charAt(0);
                            message = message.substring(1);

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

                                case '5':
                                    handleOnlineList(message);

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

        private static void handleMessage(String message) {
            String chatID = "" + message.charAt(0);
            String content = message.substring(1);

            if(!openChats.keySet().contains(chatID)) {
                return;
            }

            MessageClient messageClient = openChats.get(chatID);
            messageClient.importMessage(content);

        }

        private static void handleLogin(String message) {
            Platform.runLater(() -> {
                boolean authenticated = Boolean.parseBoolean(message);
                if (authenticated) {
                    try {
                        user = loginClient.username.getText();
                        loginClient.stage.close();
                        contactClient = new ContactClient();
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

        private static void handleNewChatGroup(String message) {
            Platform.runLater(() -> {
                String chatID = "" + message.charAt(0);
                String content = message.substring(1);
                String [] parse = content.split("\\^");
                boolean auth = Boolean.parseBoolean(parse[0]);

                if(openChats.keySet().contains(chatID) || !auth) {return;}

                String chatTitle = "";
                for(int i = 1; i < parse.length; i++) {
                    chatTitle += parse[i] + " ";
                }

                MessageClient messageClient = new MessageClient(chatID,chatTitle);
                try {
					messageClient.start(new Stage());
				} catch (Exception e) {
				}
                openChats.put(chatID, messageClient);
            });
        }

        private static void handleNewUsernameRequest(String message) {
            Platform.runLater(() -> {
                boolean authenticated = Boolean.parseBoolean(message);
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

        private static void handleClientClose(String message) {

        }

        private static void handleOnlineList(String message) {
            String [] parse = message.split("\\^");
            onlineUsers.clear();

            for(int i = 0; i < parse.length; i++) {
                onlineUsers.add(parse[i]);
            }
            userFlag = true;
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

            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent t) {
                    Platform.exit();
                    sendMessage("4");
                    running = false;
                    System.exit(0);
                }
            });
        }
    }

    private static class ContactClient extends Application {

        Stage stage;

        BorderPane borderPane;
        HBox upperBox;
        Accordion accordion;

        //TitledPane friendsPane;
        TitledPane onlinePane;
        //TitledPane groupPane;
        ScrollPane scrollPane;
        VBox friendsBox;
        VBox onlineBox;
        VBox groupBox;

        Button newGroup;

        private boolean b;
        private double height;

        @Override
        public void start(Stage primaryStage) throws Exception {
            b = true;

            stage = primaryStage;
            stage.setTitle(user);
            //stage.setResizable(false);
            stage.setWidth(300);

            //stage.setHeight(400);

            borderPane = new BorderPane();
            //upperBox = new HBox();
            accordion = new Accordion();

//            friendsPane = new TitledPane();
//            friendsPane.setText("Friends");
//            friendsPane.setContent(friendsBox = new VBox());

            onlinePane = new TitledPane();
            onlinePane.setText("Online");
            onlinePane.setContent(scrollPane = new ScrollPane(onlineBox = new VBox()));
            for(String user : onlineUsers) {
                onlineBox.getChildren().addAll(new Label(user));
            }

//            groupPane = new TitledPane();
//            groupPane.setText("GroupChat/DM");
//            groupPane.setContent(newGroup = new Button("Create Group"));

            //groupBox.getChildren().addAll(newGroup = new Button("Create Group"));

            accordion.getPanes().addAll(onlinePane);

            //borderPane.setTop(upperBox);
            borderPane.setTop(accordion);
            borderPane.setBottom(newGroup = new Button("Create Chat"));
            newGroup.setPrefWidth(300);

            stage.setScene(new Scene(borderPane));
            stage.show();

            height = stage.getHeight();

            newGroup.setOnAction(actionEvent -> {
                try {
                    GroupChatClient groupChatClient = new GroupChatClient();
                    groupChatClient.start(new Stage());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent t) {
                    Platform.exit();
                    sendMessage("4");
                    running = false;
                    System.exit(0);
                }
            });

            onlinePane.expandedProperty().addListener(new ChangeListener<Boolean>() {

                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                	userFlag = false;
                    sendMessage("5");
                    if(b) {b = !b; stage.setHeight(stage.getHeight() + onlineBox.getHeight());}
                    else { b= !b; stage.setHeight(height);}

                    updateOnline();
                }
            });
            onlinePane.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
                @Override
                public void handle(ContextMenuEvent event) {


                	userFlag = false;
                    sendMessage("5");
                }
            });
        }

        private synchronized void updateOnline() {
        	int timeout = 0;
        	while(!userFlag && timeout<10000){ timeout ++;}
            onlineBox.getChildren().setAll();
            for(String s : onlineUsers) {
                onlineBox.getChildren().add(new VBox(new Label(s)));
            }
        }
    }

    private static class MessageClient extends Application {

        private Stage stage;
        private String chatID;
        private String title;

        SplitPane splitPane;
        ScrollPane topPane;
        FlowPane bottomPane;

        TextArea chatArea;
        TextField messageField;
        Button sendMessage;

        public MessageClient(String iD, String title) {
            this.chatID = iD;
            this.title = title;
        }

        @Override
        public void start(Stage primaryStage) throws Exception {
            stage = primaryStage;
            stage.setTitle(title);

            splitPane = new SplitPane();
            topPane = new ScrollPane(chatArea = new TextArea());
            bottomPane = new FlowPane();

            bottomPane.getChildren().addAll(messageField = new TextField(), sendMessage = new Button(">>"));

            splitPane.setOrientation(Orientation.VERTICAL);
            splitPane.getItems().addAll(topPane,bottomPane);

            stage.setScene(new Scene(splitPane));
            stage.show();

            sendMessage.setOnAction(actionEvent -> {
                String message = "0" +chatID+messageField.getText();
                messageField.setText("");

                sendMessage(message);
            });

            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent t) {
                    sendMessage("6"+chatID);
                    openChats.remove(chatID);
                }
            });
        }

        public void importMessage(String messageContent) {
            chatArea.appendText(messageContent + "\n");
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
            stage.setTitle("Make group chat/DM");

            box = new HBox();
            usernames = new TextField("friend1 friend2");
            create = new Button("Create");

            box.getChildren().addAll(usernames, create);

            stage.setScene(new Scene(box));
            stage.show();

            create.setOnAction(actionEvent -> {
                try {
                    String [] s = usernames.getText().split("\\W+");

                    String message;

                    if(s.length == 1) {
                        message = "2"+s[0];
                    } else {
                        message = "2" + s[0];
                        for (int i = 1; i < s.length; i++) {
                            message += "^" + s[i];
                        }
                    }

                    sendMessage(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        @Override
        public void stop() {

        }
    }
}
