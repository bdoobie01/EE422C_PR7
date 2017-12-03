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
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.*;
import java.net.Socket;

public class ChatClient extends Application {

    /*GUI Elements go here*/
    private BorderPane borderPane;
    private VBox center;

    private TextArea incoming;
    private TextField outgoing;

    private Button button;

    private BufferedReader reader;
    private PrintWriter writer;

    public static ByteArrayOutputStream byteArrayOutputStream;

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
            primaryStage.setTitle("Chat Client");

            center = new VBox();
            center.setPadding(new Insets(10));
            center.setSpacing(5);

            incoming = new TextArea();
            outgoing = new TextField();

            button = new Button("Send");
            button.setOnAction(actionEvent -> {
                try {
                    writer.println(outgoing.getText());
                    writer.flush();
                    outgoing.setText("");
                    outgoing.requestFocus();
                } catch(Exception e) {
                    e.printStackTrace();
                }
            });

            center.getChildren().addAll(incoming, outgoing, button);

            borderPane.setCenter(center);

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
        @SuppressWarnings("resource")
        Socket socket = new Socket("127.0.0.1", 4242);

        InputStreamReader streamReader = new InputStreamReader(socket.getInputStream());
        reader = new BufferedReader(streamReader);

        writer = new PrintWriter(socket.getOutputStream());

        System.out.println("Client: networking established");

        Thread readerThread = new Thread(new IncomingReader());
        readerThread.start();
    }

    private class IncomingReader implements Runnable {
        /**
         * Incoming lines are printed to UI while they exist
         */
        @Override
        public void run() {
            String message;
            try {
                while ((message = reader.readLine()) != null) {
                    incoming.appendText(message + "\n");

                }
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }
}
