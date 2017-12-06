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
import java.util.List;
import java.util.Scanner;

public class ChatServer {
	private ArrayList<PrintWriter> clientOutpoutStreams;
	private HashMap<String, String> loginData = new HashMap<String, String>();
	private HashMap<String, List<String>> codeMap = new HashMap<String, List<String>>();
	private HashMap<String, PrintWriter> liveUser = new HashMap<String, PrintWriter>();
	private HashMap<String, List<String>> openChat = new HashMap<String, List<String>>();
	private ArrayList<String> groupCodes = new ArrayList<String>();
	private String[] codestart = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f" };
	private String[] loginCombos = { "brian", "doobie", "turan", "bieberlover123" };

	public ChatServer() {
		for (int i = 0; i < loginCombos.length; i = i + 2) {
			loginData.put(loginCombos[i], loginCombos[i + 1]);
		}
		for (String s : codestart) {
			groupCodes.add(s);
		}

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
		private String userName;

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
			String message;

			try {
				while ((message = reader.readLine()) != null) {
					try {
						char[] codeVal = new char[1];
						codeVal[0] = message.charAt(0);
						message = message.replaceFirst(new String(codeVal), "");

						switch (codeVal[0]) {

							case '0': // Message
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
								handleLiveUserRequest(message);
								break;

							case '6':
								handleGroupClose(message);

							default:
								break;
						}
					} catch (Exception e) {
					}
				}
			} catch (IOException e) {
				System.out.println("Client " + userName + " has disconnected.");
			}
		}

		private void handleMessage(String message) { // 0
			char[] ccode = new char[1];
			ccode[0] = message.charAt(0);
			if (codeMap.containsKey(new String(ccode))) {
				for (String user : codeMap.get(new String(ccode))) {
					if (liveUser.containsKey(user)) {
						if (openChat.get(user).contains(ccode)) {
							toClient(liveUser.get(user), user,
									"0" + new String(ccode) + userName + ": " + message.substring(1));
						}
						else{
							if(openChat.containsKey(user)){
								openChat.get(user).add(new String(ccode));
							}
							else{
								List<String> ccode2 = new ArrayList<String>();
								ccode2.add(new String(ccode));
								openChat.put(user, ccode2);
							}
							String mes = "2" + new String(ccode) + "true";
							for(String nm : codeMap.get(new String(ccode))){
								mes += "^" + nm;
							}
							toClient(liveUser.get(user), user, mes);
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
							}
							toClient(liveUser.get(user), user,
									"0" + new String(ccode) + userName + ": " + message.substring(1));
						}
					}
				}
			}
		}

		private void handleLogin(String message) { // 1

			boolean authenticated = false;
			String[] spt = message.split("\\^");
			String username = spt[0];
			String pwd = spt[1];

			if (loginData.containsKey(username)) {
				if (loginData.get(username).equals(pwd)) {
					if (!liveUser.containsKey(username)) {
						authenticated = true;
					}
				}
			}
			if (!authenticated) {
				sendToClient("1false");
			} else {
				sendToClient("1true");
				liveUser.put(username, cwriter);
				userName = new String(username);
			}
		}

		private void handleNewChatGroup(String message) { // 2
			List<String> names = new ArrayList<String>();
			names.add(userName);
			String[] naa = message.split("\\^");
			for (String s : naa) {
				if (liveUser.containsKey(s)) {
					names.add(s);
				} else {
					sendToClient("20false");
					return;
				}
			}
			String code = groupCodes.get(0);
			groupCodes.remove(code);
			codeMap.put(code, names);
			String smessage = "2" + code + "true";
			for (String nm : names) {
				smessage += "^" + nm;
			}

			sendToClient(smessage);
			for (String nm : names) {
				if (nm != userName) {
					toClient(liveUser.get(nm), nm, smessage);
				}
			}

			for (String nm : names) {
				if (!openChat.containsKey(nm)) {
					List<String> c = new ArrayList<String>();
					c.add(code);
					openChat.put(nm, c);
				} else {
					openChat.get(nm).add(code);
				}
			}
		}

		private void handleNewUsernameRequest(String message) { // 3
			String[] spt = message.split("\\^");
			String username = spt[0];
			String pwd = spt[1];

			if (!loginData.containsKey(username)) {
				loginData.put(username, pwd);
				sendToClient("3true");
				liveUser.put(username, cwriter);
				userName = new String(username);
			} else {
				sendToClient("3false");
			}
		}

		private void handleClientClose(String message) { // 4
			if (userName != null) {

				// Remove all group chats
				List<String> badCodes = new ArrayList<String>();
				for (String cds : codeMap.keySet()) {
					for (String s : codeMap.get(cds)) {
						if (s.equals(userName)) {
							badCodes.add(cds);
						}
					}
				}

				for (String s : badCodes) {
					int num = 0;
					for (String user : codeMap.get(s)) {
						for (String key : liveUser.keySet()) {
							if (user.equals(key)) {
								num++;
							}
						}
					}
					if (num <= 1) {
						codeMap.remove(s);
						openChat.remove(s);
						groupCodes.add(s);
					}
				}

				System.out.println("Handling " + userName + " client closing");

				// Remove user from server
				liveUser.remove(userName);
				try {
					reader.close();
				} catch (IOException e) {
				}
				cwriter.close();
			}
		}


		private void handleLiveUserRequest(String msg) { // 5
			String mess = "5";
			for (String nm : liveUser.keySet()) {
				if (!nm.equals(userName)) {
					if (mess.equals("5")) {
						mess += nm;
					} else {
						mess += "^" + nm;
					}
				}

			}
			sendToClient(mess);
		}

		private void handleGroupClose(String msg) { // 6

			System.out.println("Handling " + userName + " chat window closing");

			if (openChat.containsKey(userName)) {
				openChat.remove(msg);
			}
			boolean hasVal = false;
			for (String name : openChat.keySet()) {
				for (String code : openChat.get(name)) {
					if (code.equals(msg)) {
						hasVal = true;
					}
				}
			}
			if (!hasVal) {
				if (codeMap.containsKey(msg)) {
					codeMap.remove(msg);
					groupCodes.add(msg);
				}
			}
		}

		private void sendToClient(String msg) {
			cwriter.println(msg);
			cwriter.flush();
			System.out.println(userName + ": " + msg);
		}

	}

	static private void toClient(PrintWriter w, String user, String msg) {
		w.println(msg);
		w.flush();
		System.out.println(user + ": " + msg);
	}
}