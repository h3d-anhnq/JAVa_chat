package server;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.awt.Color;

public class Server {

	private int port;
	private List<User> clients;
	private ServerSocket server;

	public static void main(String[] args) throws IOException {
		new Server(12341).run();
	}

	public Server(int port) {
		this.port = port;
		this.clients = new ArrayList<User>();
	}

	public void run() throws IOException {
		server = new ServerSocket(port);
		System.out.println("Port 12341 is now open.");

		while (true) {
			// accepts a new client
			Socket client = server.accept();

			// get nickname of newUser
			String nickname = (new Scanner(client.getInputStream())).nextLine();
			nickname = nickname.replace(",", ""); // ',' use for serialisation
			nickname = nickname.replace(" ", "_");
			System.out.println(
					"New Client: \"" + nickname + "\"\n\t     Host:" + client.getInetAddress().getHostAddress());

			// create new User
			User newUser = new User(client, nickname);

			// add newUser message to list
			this.clients.add(newUser);

			// Welcome msg
			for (int i = 0; i < clients.size(); i++) {
				clients.get(i).getOutStream().println("<b>Welcome</b> " + newUser.toString());
			}
			
//			newUser.getOutStream().println("<b>Welcome</b> " + newUser.toString());

			// create a new thread for newUser incoming messages handling
			new Thread(new UserHandler(this, newUser)).start();
		}
	}

	// delete a user from the list
	public void removeUser(User user) {
		this.clients.remove(user);
	}
	
	// send incoming msg to all Users
	public void broadcastMessages(String msg, User userSender) {
		for (User client : this.clients) {
			client.getOutStream().println(userSender.toString() + "<span>: " + msg + "</span>");
		}
	}
	
	// send incoming file to all Users 
	public void broadcastFile(String file_name,byte[] byte_arr, User userSender) throws IOException {
		for (User client : this.clients) {
			if (client.getUserID() != userSender.getUserID()) {
				client.getOutStream().println("-+File+-");
				client.getOutStream().println(byte_arr.length);
				client.getOutStream().println(userSender.getNickname());
				client.getOutStream().println(file_name);
				DataOutputStream dos = new DataOutputStream(client.getOutputStream());
				dos.write(byte_arr, 0, byte_arr.length);
				dos.close();
			}
		}
	}

	// send list of clients to all Users
	public void broadcastAllUsers() {
		for (User client : this.clients) {
			client.getOutStream().println(this.clients);
		}
	}

	// send message to a User (String)
	public void sendMessageToUser(String msg, User userSender, String user) {
		boolean find = false;
		for (User client : this.clients) {
			if (client.getNickname().equals(user) && client != userSender) {
				find = true;
				userSender.getOutStream().println(userSender.toString() + " -> " + client.toString() + ": " + msg);
				client.getOutStream().println("(<b>Private</b>)" + userSender.toString() + "<span>: " + msg + "</span>");
			}
		}
		if (!find) {
			userSender.getOutStream().println(userSender.toString() + " -> (<b>no one!</b>): " + msg);
		}
	}
}
