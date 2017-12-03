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
import javafx.event.EventHandler;
import javafx.geometry.Insets;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChatClient extends Application {

    /*GUI Elements go here*/
    private BorderPane borderPane;
    private VBox leftTray;
    private HBox topTray;
    private HBox bottomTray;

    private TextArea incoming;
    private TextField outgoing;

    private Button newChat;
    private Button sendButton;


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

            leftTray = new VBox();
            leftTray.setPadding(new Insets(10));
            leftTray.setSpacing(5);

            bottomTray = new HBox();
            bottomTray.setPadding(new Insets(10));
            bottomTray.setSpacing(5);

            topTray = new HBox();
            topTray.setPadding(new Insets(10));
            topTray.setSpacing(5);

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

            sendButton = new Button("Send");
            sendButton.setOnAction(actionEvent -> {
                try {
                    sendMessage();
                } catch(Exception e) {
                    e.printStackTrace();
                }
            });

            leftTray.getChildren().addAll(newChat);
            bottomTray.getChildren().addAll(outgoing, sendButton);

            borderPane.setTop(topTray);
            borderPane.setBottom(bottomTray);
            borderPane.setLeft(leftTray);
            borderPane.setCenter(incoming);


            Scene scene = new Scene(borderPane, 500,500);
            primaryStage.setScene(scene);
            primaryStage.sizeToScene();
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }

        /*set up networking here*/
        setUpNetworking();
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
}
