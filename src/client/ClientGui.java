package client;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.net.*;
import java.io.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.html.*;

import java.util.ArrayList;
import java.util.Arrays;

public class ClientGui extends Thread {

	final JTextPane chat_area = new JTextPane();
	final JTextPane listUsers = new JTextPane();
	final JTextField typeChat = new JTextField();
	private String oldMsg = "";
	private Thread read;
	private String serverName;
	private int PORT;
	private String name;
	final JFrame frame = new JFrame("Chat");
	BufferedReader input;
	PrintWriter output;
	Socket server;

	public ClientGui() {
		this.serverName = "localhost";
		this.PORT = 12341;
		this.name = "nickname";

		String fontfamily = "Arial, sans-serif";
		Font font = new Font(fontfamily, Font.PLAIN, 15);

		frame.getContentPane().setLayout(null);
		frame.setSize(700, 500);
		frame.setResizable(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		chat_area.setBounds(25, 25, 490, 320);
		chat_area.setFont(font);
		chat_area.setMargin(new Insets(6, 6, 6, 6));
		chat_area.setEditable(false);
		JScrollPane jtextFilDiscuSP = new JScrollPane(chat_area);
		jtextFilDiscuSP.setBounds(25, 25, 490, 320);

		chat_area.setContentType("text/html");
		chat_area.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);

		listUsers.setBounds(520, 25, 156, 320);
		listUsers.setEditable(true);
		listUsers.setFont(font);
		listUsers.setMargin(new Insets(6, 6, 6, 6));
		listUsers.setEditable(false);
		JScrollPane jsplistuser = new JScrollPane(listUsers);
		jsplistuser.setBounds(520, 25, 156, 320);

		listUsers.setContentType("text/html");
		listUsers.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);

		// Field message user input
		typeChat.setBounds(0, 350, 400, 50);
		typeChat.setFont(font);
		typeChat.setMargin(new Insets(6, 6, 6, 6));
		final JScrollPane jtextInputChatSP = new JScrollPane(typeChat);
		jtextInputChatSP.setBounds(25, 350, 650, 50);

		// button send
		final JButton jsbtn = new JButton("Send message");
		jsbtn.setFont(font);
		jsbtn.setBounds(575, 410, 100, 35);

		// button send file
		final JButton filebtn = new JButton("Send file");
		filebtn.setFont(font);
		filebtn.setBounds(455, 410, 100, 35);

		// button send file
		final JButton imagebtn = new JButton("Send image");
		imagebtn.setFont(font);
		imagebtn.setBounds(335, 410, 100, 35);

		// button Disconnect
		final JButton btn_disconnect = new JButton("Disconnect");
		btn_disconnect.setFont(font);
		btn_disconnect.setBounds(25, 410, 130, 35);

		// Add event for the send button
		typeChat.addKeyListener(new KeyAdapter() {
			// send message on Enter
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					sendMessage();
				}

				// Get last message typed
				if (e.getKeyCode() == KeyEvent.VK_UP) {
					String currentMessage = typeChat.getText().trim();
					typeChat.setText(oldMsg);
					oldMsg = currentMessage;
				}

				if (e.getKeyCode() == KeyEvent.VK_DOWN) {
					String currentMessage = typeChat.getText().trim();
					typeChat.setText(oldMsg);
					oldMsg = currentMessage;
				}
			}
		});

		// Click on send button
		jsbtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				sendMessage();
			}
		});

		// Click on send file button
		filebtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				sendFile();
			}
		});

		// Click on send image button
		imagebtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				sendImage();
			}
		});

		// Connection view
		final JTextField jtfName = new JTextField(this.name);
		final JTextField jtfport = new JTextField(Integer.toString(this.PORT));
		final JTextField jtfAddr = new JTextField(this.serverName);
		final JButton btn_connect = new JButton("Connect");

		// check if those field are not empty
		jtfName.getDocument().addDocumentListener(new TextListener(jtfName, jtfport, jtfAddr, btn_connect));
		jtfport.getDocument().addDocumentListener(new TextListener(jtfName, jtfport, jtfAddr, btn_connect));
		jtfAddr.getDocument().addDocumentListener(new TextListener(jtfName, jtfport, jtfAddr, btn_connect));

		btn_connect.setFont(font);
		jtfAddr.setBounds(25, 380, 135, 40);
		jtfName.setBounds(375, 380, 135, 40);
		jtfport.setBounds(200, 380, 135, 40);
		btn_connect.setBounds(575, 380, 100, 40);

		chat_area.setBackground(Color.LIGHT_GRAY);
		listUsers.setBackground(Color.LIGHT_GRAY);

		frame.add(btn_connect);
		frame.add(jtextFilDiscuSP);
		frame.add(jsplistuser);
		frame.add(jtfName);
		frame.add(jtfport);
		frame.add(jtfAddr);
		frame.setVisible(true);

		appendToPane(chat_area, "<h2>Please enter server IP, server port, your nickname</h2>" + "<br/>");

		// On connect
		btn_connect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				try {
					name = jtfName.getText();
					String port = jtfport.getText();
					serverName = jtfAddr.getText();
					PORT = Integer.parseInt(port);

					appendToPane(chat_area, "<span>Connecting to " + serverName + " on port " + PORT + "...</span>");
					server = new Socket(serverName, PORT);

					appendToPane(chat_area, "<span>Connected to " + server.getRemoteSocketAddress() + "</span>");

					input = new BufferedReader(new InputStreamReader(server.getInputStream()));
					output = new PrintWriter(server.getOutputStream(), true);

					// send nickname to server
					output.println(name);

					// create new Read Thread
					read = new Read();
					read.start();
					frame.remove(jtfName);
					frame.remove(jtfport);
					frame.remove(jtfAddr);
					frame.remove(btn_connect);
					frame.add(jsbtn);
					frame.add(jtextInputChatSP);
					frame.add(filebtn);
					frame.add(imagebtn);
					frame.add(btn_disconnect);
					frame.revalidate();
					frame.repaint();
					chat_area.setBackground(Color.WHITE);
					listUsers.setBackground(Color.WHITE);
				} catch (Exception ex) {
					appendToPane(chat_area, "<span>Could not connect to Server</span>");
					JOptionPane.showMessageDialog(frame, ex.getMessage());
				}
			}
		});

		// On disconnect
		btn_disconnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				frame.add(jtfName);
				frame.add(jtfport);
				frame.add(jtfAddr);
				frame.add(btn_connect);
				frame.remove(jsbtn);
				frame.remove(jtextInputChatSP);
				frame.remove(btn_disconnect);
				frame.remove(filebtn);
				frame.remove(imagebtn);
				frame.revalidate();
				frame.repaint();
				read.interrupt();
				listUsers.setText(null);
				chat_area.setBackground(Color.LIGHT_GRAY);
				listUsers.setBackground(Color.LIGHT_GRAY);
				appendToPane(chat_area, "<span>Connection closed.</span>");
				output.close();
			}
		});

	}

	// check if if all field are not empty
	public class TextListener implements DocumentListener {
		JTextField jtf1;
		JTextField jtf2;
		JTextField jtf3;
		JButton jcbtn;

		public TextListener(JTextField jtf1, JTextField jtf2, JTextField jtf3, JButton jcbtn) {
			this.jtf1 = jtf1;
			this.jtf2 = jtf2;
			this.jtf3 = jtf3;
			this.jcbtn = jcbtn;
		}

		public void changedUpdate(DocumentEvent e) {
		}

		public void removeUpdate(DocumentEvent e) {
			if (jtf1.getText().trim().equals("") || jtf2.getText().trim().equals("")
					|| jtf3.getText().trim().equals("")) {
				jcbtn.setEnabled(false);
			} else {
				jcbtn.setEnabled(true);
			}
		}

		public void insertUpdate(DocumentEvent e) {
			if (jtf1.getText().trim().equals("") || jtf2.getText().trim().equals("")
					|| jtf3.getText().trim().equals("")) {
				jcbtn.setEnabled(false);
			} else {
				jcbtn.setEnabled(true);
			}
		}

	}

	public void sendMessage() {
		try {
			String message = typeChat.getText().trim();
			if (message.equals("")) {
				return;
			}
			this.oldMsg = message;
			output.println(message);
			typeChat.requestFocus();
			typeChat.setText(null);
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(null, ex.getMessage());
			System.exit(0);
		}
	}

	public void sendFile() {
		try {
			String userDirLocation = System.getProperty("user.dir");
			File userDir = new File(userDirLocation);
			JFileChooser fc = new JFileChooser(userDir);
			fc.showOpenDialog(frame);

			File file = fc.getSelectedFile();
			FileInputStream fis = new FileInputStream(file);
			byte byte_arr[] = new byte[9999];
			fis.read(byte_arr, 0, byte_arr.length);

			output.println("-+file+-");
			output.println(byte_arr.length);
			output.println(file.getName());

			DataOutputStream dos = new DataOutputStream(server.getOutputStream());
			dos.write(byte_arr, 0, byte_arr.length);
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(null, ex.getMessage());
			System.exit(0);
		}
	}

	// Scale picture that too large
	public static BufferedImage scale(BufferedImage src, int w, int h) {
		BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		int x, y;
		int ww = src.getWidth();
		int hh = src.getHeight();
		int[] ys = new int[h];
		for (y = 0; y < h; y++) {
			ys[y] = y * hh / h;
		}
		for (x = 0; x < w; x++) {
			int newX = x * ww / w;
			for (y = 0; y < h; y++) {
				int col = src.getRGB(newX, ys[y]);
				img.setRGB(x, y, col);
			}
		}
		return img;
	}

	public void sendImage() {
		try {
			String userDirLocation = System.getProperty("user.dir");
			File userDir = new File(userDirLocation);
			JFileChooser fc = new JFileChooser(userDir);
			fc.showOpenDialog(frame);
			File file = fc.getSelectedFile();

			BufferedImage bImage = ImageIO.read(file);
			if (bImage.getWidth() > 1000) {
				bImage = scale(bImage, bImage.getWidth() / 10, bImage.getHeight() / 10);
			}

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ImageIO.write(bImage, "jpg", bos);
			byte[] byte_arr = bos.toByteArray();

//			ByteArrayInputStream bis = new ByteArrayInputStream(byte_arr);
//			BufferedImage bImage2 = ImageIO.read(bis);
//			ImageIO.write(bImage2, "jpg", new File("E:\\ServerOOP\\Chat\\src\\server\\slack_server.jpg"));
//			System.out.println("image created");

			output.println("-+image+-");
			output.println(byte_arr.length);
			DataOutputStream dos = new DataOutputStream(server.getOutputStream());
			dos.write(byte_arr, 0, byte_arr.length);
			System.out.println("Done");
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(null, ex.getMessage());
			System.exit(0);
		}
	}

	public static void main(String[] args) throws Exception {
		ClientGui client = new ClientGui();
	}

	// read new incoming messages
	class Read extends Thread {
		public void run() {
			String message;
			while (!Thread.currentThread().isInterrupted()) {
				try {
					message = input.readLine();

					if (message != null) {
						if (message.equals("-+File+-")) {
							int a = Integer.parseInt(input.readLine());
							String from = input.readLine();
							String file_name = input.readLine();
							int dialogResult = JOptionPane.showConfirmDialog(null,
									"Accept " + file_name + " file from " + from + "?", "Accpet File",
									JOptionPane.YES_NO_OPTION);
							if (dialogResult == JOptionPane.YES_OPTION) {
								byte b[] = new byte[a];
								String userDirLocation = System.getProperty("user.dir");
								File userDir = new File(userDirLocation);
								JFileChooser fc = new JFileChooser(userDir);
								fc.showSaveDialog(frame);
								File chosen_file = fc.getSelectedFile();

								InputStream is = server.getInputStream();
								is.read(b, 0, b.length);

								FileOutputStream fos;
								fos = new FileOutputStream(chosen_file);
								fos.write(b, 0, b.length);
								fos.close();
								String messs = "<i><b>File save to " + chosen_file.getName() + "!</b></i>";
								appendToPane(chat_area, messs);
							}
						} else if (message.charAt(0) == '[') {
							message = message.substring(1, message.length() - 1);
							ArrayList<String> ListUser = new ArrayList<String>(Arrays.asList(message.split(", ")));
							listUsers.setText(null);
							for (String user : ListUser) {
								appendToPane(listUsers, "@" + user);
							}
						} else {
							appendToPane(chat_area, message);
						}
					}
				} catch (IOException ex) {
					System.err.println("Failed to parse incoming message");
				}
			}
		}
	}

	// send html to pane
	private void appendToPane(JTextPane tp, String msg) {
		HTMLDocument doc = (HTMLDocument) tp.getDocument();
		HTMLEditorKit editorKit = (HTMLEditorKit) tp.getEditorKit();
		try {
			editorKit.insertHTML(doc, doc.getLength(), msg, 0, 0, null);
			tp.setCaretPosition(doc.getLength());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}