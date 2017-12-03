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
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ChatClient extends Application {

    /*GUI Elements go here*/
    private BorderPane borderPane;
    private VBox chatTray;
    private VBox centerTray;
    private HBox peerTray;
    private HBox outgoingTray;

    private TextArea incoming;
    private TextField outgoing;

    private Button login;
    private Button newChat;
    private Button sendButton;
    private Button newPeer;

    String user;

    private BufferedReader reader;
    private PrintWriter writer;

    private static Map<Thread, IncomingReader> map = new HashMap<Thread, IncomingReader>();
    Socket socket;

    public static void main(String [] args) {
        try {
            launch(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void init() throws Exception {}

    @Override
    public void start(Stage primaryStage) throws Exception {

        //@TODO CREATE UI ELEMENTS
        /*Initialize UI here*/
        try {

            borderPane = new BorderPane();

            primaryStage.initStyle(StageStyle.DECORATED);
//            primaryStage.initModality(Modality.NONE);
            primaryStage.setTitle("tDoobie's Super Encrypted Client");
            primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    try {
                        sendMessage("4");
                        closeNetworking();
                        for(Thread t : map.keySet()) {
                            IncomingReader incomingReader = map.get(t);
                            incomingReader.terminate();
                            t.join(10);
                        }
                        Platform.exit();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            login = new Button("Login");
            login.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    try {
                        Stage stage = new Stage();

                        LoginClient loginClient = new LoginClient();
                        loginClient.start(stage);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            chatTray = new VBox();
            chatTray.setPadding(new Insets(10));
            chatTray.setSpacing(5);

            centerTray = new VBox();
            centerTray.setPadding(new Insets(10));
            centerTray.setSpacing(5);

            peerTray = new HBox();
            peerTray.setPadding(new Insets(10));
            peerTray.setSpacing(5);

            outgoingTray = new HBox();
            outgoingTray.setPadding(new Insets(10));
            outgoingTray.setSpacing(5);

            newChat = new Button("New");

            incoming = new TextArea();
            incoming.setEditable(false);

            outgoing = new TextField();
            outgoing.setOnKeyReleased(new EventHandler<KeyEvent>() {
                @Override
                public void handle(KeyEvent event) {
                    if (event.getCode() == KeyCode.ENTER) {
                        try {
                            sendMessage();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

            sendButton = new Button(">>");
            sendButton.setOnAction(actionEvent -> {
                try {
                    sendMessage();
                } catch(Exception e) {
                    e.printStackTrace();
                }
            });

            newPeer = new Button("+");


            peerTray.getChildren().addAll(newPeer);
            peerTray.setAlignment(Pos.BASELINE_RIGHT);
            chatTray.getChildren().addAll(login, newChat);
            outgoingTray.getChildren().addAll(outgoing, sendButton);
            outgoingTray.setAlignment(Pos.BASELINE_RIGHT);
            centerTray.getChildren().addAll(peerTray, incoming, outgoingTray);

            borderPane.setLeft(chatTray);
            borderPane.setCenter(centerTray);

            Scene scene = new Scene(borderPane, 500,500);
            primaryStage.setScene(scene);
            primaryStage.sizeToScene();
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }

        /*set up networking here*/
        //setUpNetworking();
    }

    private void setUpNetworking() throws Exception {
        //@SuppressWarnings("resource")
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

    private void closeNetworking() throws IOException {
        writer.close();
        socket.close();
        reader.close();
    }

    private void sendMessage() {
        writer.println(outgoing.getText());
        writer.flush();
        outgoing.setText("");
        outgoing.requestFocus();
    }

    private void sendMessage(String s) {
        writer.println(s);
        writer.flush();
    }

    private class IncomingReader implements Runnable {
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
                        incoming.appendText(message + "\n");

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void terminate() {
            running = false;
        }
    }

    private class LoginClient extends Application {

        private VBox loginBox;

        private TextField username;
        private TextField password;

        private Button login;
        private Button register;

        @Override
        public void start(Stage primaryStage) throws Exception {
            loginBox = new VBox();
            loginBox.setPadding(new Insets(10));
            loginBox.setSpacing(5);

            username = new TextField("username");
            password = new TextField("password");

            login = new Button("login");
            login.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    try {
                        login();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            register = new Button("register");
            register.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    try {
                        register();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            loginBox.getChildren().addAll(username, password, login, register);
            primaryStage.setScene(new Scene(loginBox));
            primaryStage.show();
        }

        private void login() throws Exception {
            setUpNetworking();
            sendMessage("1"+username.getText()+"^"+password.getText());

        }

        private void register() {

        }
    }
}
