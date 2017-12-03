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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

public class ChatClient extends Application {

    /*GUI Elements go here*/
    private BorderPane borderPane;
    private VBox center;

    private TextArea incoming;
    private TextField outgoing;

    private BufferedReader reader;
    private PrintWriter writer;

    public static ByteArrayOutputStream byteArrayOutputStream;

    @Override
    public void init() throws Exception {}

    @Override
    public void start(Stage primaryStage) throws Exception {

        //@TODO CREATE UI ELEMENTS
        /*Initialize UI here*/
        try {
            primaryStage.initStyle(StageStyle.TRANSPARENT);
            primaryStage.initModality(Modality.NONE);
            primaryStage.setTitle("Chat Client");

            borderPane = new BorderPane();


            incoming = new TextArea();
            incoming.
            outgoing = new TextField();

            borderPane.setCenter();


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

    class SendButtonListener implements ActionListener {
        /**
         * When e is performed, outgoing text is printed to the writer and cleared
         * @param e
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            writer.println(outgoing.getText());
            writer.flush();
            outgoing.setText("");
            outgoing.requestFocus();
        }
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
                    incoming.append(message + "\n");
                }
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }
}
