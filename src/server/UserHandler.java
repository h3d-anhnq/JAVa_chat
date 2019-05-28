package server;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Scanner;

import javax.imageio.ImageIO;

class UserHandler implements Runnable {

	private Server server;
	private User user;

	public UserHandler(Server server, User user) {
		this.server = server;
		this.user = user;
		this.server.broadcastAllUsers();
	}

	public void run() {
		String message;
		// when there is a new message, broadcast to all
		Scanner sc = new Scanner(this.user.getInputStream());
		while (sc.hasNextLine()) {
			message = sc.nextLine();
			if (message.equals("-+file+-")) {
				System.out.println("File");
				int a = Integer.parseInt(sc.nextLine());
				byte bype_arr[] = new byte[a];
				String name = sc.nextLine();
				FileOutputStream fos;
				try {
					fos = new FileOutputStream("E:\\ServerOOP\\Chat\\src\\server\\test_server.txt");
					InputStream is = user.getInputStream();
					is.read(bype_arr, 0, bype_arr.length);
					fos.write(bype_arr, 0, bype_arr.length);
					server.broadcastFile(name,bype_arr,user);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

			} else if (message.equals("-+image+-")) {
				System.out.println("Image");
				int a = Integer.parseInt(sc.nextLine());
				System.out.println(a);
				byte b[] = new byte[a];
				FileOutputStream fos;
				try {
					InputStream is = user.getInputStream();
					is.read(b, 0, b.length);
					ByteArrayInputStream bis = new ByteArrayInputStream(b);
					BufferedImage bImage2 = ImageIO.read(bis);
					ImageIO.write(bImage2, "jpg", new File("E:\\ServerOOP\\Chat\\src\\server\\server.jpg"));
					System.out.println("Image done");
					// Send back image to all
					String mess_back = "<img src='file:///E:/ServerOOP/Chat/src/server/server.jpg'>";
					server.broadcastMessages(mess_back, user);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
//				if (message != "") {
//					System.out.println("Normal text");
//					if (message.charAt(0) == '@') {
//						if (message.contains(" ")) {
//							System.out.println("private msg : " + message);
//							int firstSpace = message.indexOf(" ");
//							String userPrivate = message.substring(1, firstSpace);
//							server.sendMessageToUser(message.substring(firstSpace + 1, message.length()), user,
//									userPrivate);
//						} else if (message.charAt(0) == '#') {
//							user.changeColor(message);
//							// update color for all other users
//							this.server.broadcastAllUsers();
//						}
//					} else {
//				System.out.println("Broadcast");
				// broadcast the message back to all client
				server.broadcastMessages(message, user);
//					}
//				}
			}
		}
		// end of Thread
		server.removeUser(user);
		this.server.broadcastAllUsers();
		sc.close();
	}
}
