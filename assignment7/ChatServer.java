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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class ChatServer {
	private ArrayList<PrintWriter> clientOutpoutStreams;
	private HashMap<String, String> loginData = new HashMap<String, String>();

	public ChatServer() {
		loginData.put("brian", "doobie");
	}

	/**
	 * Creates new ChatServer w/ call to setUpNetworking()
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			new ChatServer().setUpNetworking();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * creates list of clientOutputStreams (printWriters), sets up socket on
	 * port 4242
	 * 
	 * @throws IOException
	 */
	private void setUpNetworking() throws IOException {

		clientOutpoutStreams = new ArrayList<PrintWriter>();
		@SuppressWarnings("resource")

		ServerSocket serverSocket = new ServerSocket(4242);

		while (true) {
			Socket clientSocket = serverSocket.accept();
			PrintWriter writer = new PrintWriter(clientSocket.getOutputStream());
			clientOutpoutStreams.add(writer);

			Thread t = new Thread(new ClientHandler(clientSocket, writer));
			t.start();
			System.out.println("Server: networking established");
		}
	}

	/**
	 * writes message to all PrintWriters in list
	 * 
	 * @param message
	 */
	private void notifyClients(String message) {
		for (PrintWriter writer : clientOutpoutStreams) {
			writer.println(message);
			writer.flush();
		}
	}

	/*
	
	 */
	class ClientHandler implements Runnable {
		private BufferedReader reader;
		private PrintWriter cwriter;

		/**
		 * New ClientHandler for given socket Initializes reader to
		 * socket.getInputStream()
		 * 
		 * @param clientSocket
		 * @throws IOException
		 */
		public ClientHandler(Socket clientSocket, PrintWriter writer) throws IOException {
			Socket socket = clientSocket;
			cwriter = writer;
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		}

		/**
		 * Reads lines from the BufferedReader while available
		 */
		public void run() {
			
			//Authentication
			Boolean authenticated = false;
			while (!authenticated) {
				try {
					String authMess = reader.readLine();
					if (authMess.toCharArray()[0] != '1') {
						continue;
					} else {
						authMess = authMess.replaceFirst("1", "");
						String[] spt = authMess.split("\\^");
						String username = spt[0];
						String pwd = spt[1];

						if (loginData.containsKey(username)) {
							if (loginData.get(username).equals(pwd)) {
								authenticated = true;
							}
						}
						if (!authenticated) {
							cwriter.println("1Invalid username or password");
						} else {
							cwriter.println("1Authenticated");
						}
						cwriter.flush();

					}
				} catch (Exception e) {
				}
			}
			
			
			String message;
			try {
				while ((message = reader.readLine()) != null) {
					System.out.println("read " + message);
					notifyClients(message);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
