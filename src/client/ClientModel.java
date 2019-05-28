package client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import javax.swing.JTextPane;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

public class ClientModel {
	
	public void writeToFile(int a, Socket server, File chosen_file) {
		try {
			byte b[] = new byte[a];
			InputStream is;
			is = server.getInputStream();
			is.read(b, 0, b.length);
			FileOutputStream fos;
			fos = new FileOutputStream(chosen_file);
			fos.write(b, 0, b.length);
			fos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
