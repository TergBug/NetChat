package net;

import java.net.*;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.BorderFactory;

import java.awt.Color;
import java.awt.Container;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

@SuppressWarnings("serial")
public class client extends JFrame{
	private static JTextField tf = new JTextField();
	private static JTextArea ta = new JTextArea();
	private static JButton b1 = new JButton();
	
	private static volatile boolean stopT1 = false;
	private static Thread T1;
	private static volatile boolean pauseT1 = false;
	private static Socket socket = null;
	private static ServerSocket serversocket = null;
	private static DataOutputStream out;
	private static DataInputStream in;
	private static String readmes = "";
	
	private static String username;
	//private static String password;
	private static String convuser;
	private static volatile String input = "";
	public client() {
		this.setTitle("Chater");
		this.setSize(500, 300);
		this.setLocation(100, 100);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.set();
		Container c = this.getContentPane();
		c.setLayout(new GridBagLayout());
		GridBagConstraints bgc = new GridBagConstraints();
		bgc.gridx = 0;
		bgc.gridy = 0;
		bgc.gridwidth = 2;
		bgc.gridheight = 1;
		bgc.weightx = 0.0;
		bgc.weighty = 1.0;
		bgc.insets = new Insets(5, 5, 5, 5);
		bgc.fill = GridBagConstraints.BOTH;
		c.add(ta, bgc);
		bgc.gridx = 0;
		bgc.gridy = 1;
		bgc.gridwidth = 1;
		bgc.gridheight = 1;
		bgc.weightx = 1.0;
		bgc.weighty = 0.0;
		bgc.fill = GridBagConstraints.HORIZONTAL;
		c.add(tf, bgc);
		bgc.gridx = 1;
		bgc.gridy = 1;
		bgc.weightx = 0.0;
		bgc.weighty = 0.0;
		c.add(b1, bgc);
		
		b1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					action();
				}catch(IOException ioe) {
					set();
				}
			}
		});
	}
	public void set() {
		tf.setText("");
		tf.setVisible(true);
		tf.setBorder(BorderFactory.createLineBorder(Color.black, 1));
		ta.setText("");
		ta.setVisible(true);
		ta.setEditable(false);
		ta.setBorder(BorderFactory.createLineBorder(Color.black, 1));
		b1.setText("Send");
		b1.setVisible(true);
		b1.setBackground(Color.green);
	}
	public void action() throws IOException{
		input = tf.getText();
		tf.setText("");
	}
	public static void main(String[] args) throws IOException, InterruptedException{
		client app = new client();
		app.setVisible(true);
		boolean doing = true;
		try {
			while(doing) {
				ta.setText("Enter your username...");
				while(input.equals("")) {}
				username = input;
				input = "";
				ta.setText("Your username is "+username);
				String header = ta.getText();
				ta.setText(ta.getText()+"\nWaiting for server...");
				do{
					try{
						Thread.sleep(300);
						socket = new Socket("localhost", 3346);
					} catch(IOException e) {
						serversocket = new ServerSocket(3346);
						socket = serversocket.accept();
					}
				}while(socket==null);
				out = new DataOutputStream(socket.getOutputStream());
				in = new DataInputStream(socket.getInputStream());
				T1 = new Thread(new Runnable() {
					public void run(){
						stopT1 = false;
						pauseT1 = false;
						while(!stopT1) {
							if(!pauseT1) {
								try {
									out.writeUTF("ok");
									out.flush();
									Thread.sleep(300);
								}catch(IOException e) {}
								catch(InterruptedException e) {}
							}
							else {
								while(true) {
									try {
										if(!pauseT1) {break;}
										Thread.sleep(300);
									}catch(InterruptedException e) {}
								}
							}
						}
					}
				});
				T1.start();
				pauseT1 = true;
				out.writeUTF(username);
				out.flush();
				convuser = in.readUTF();
				pauseT1 = false;
				ta.setText(header+"\nConnection accepted to "+convuser);
				header = ta.getText();
				while(!socket.isClosed() && !stopT1) {
					readmes = in.readUTF();
					if(readmes.equals("ok") && !input.equals("")) {
						pauseT1 = true;
						ta.setText(ta.getText()+"\n:> "+input);
						//header = ta.getText();
						out.writeUTF(input);
						out.flush();
						pauseT1 = false;
						if(input.equals("bye")) {
							pauseT1 = false;
							stopT1 = true;
							in.close();
							out.close();
							socket.close();
							if(serversocket!=null) serversocket.close();
						}
						input="";
					}
					else if(!readmes.equals("ok")) {
						ta.setText(ta.getText()+"\n"+convuser+":> "+readmes);
						//header = ta.getText();
						if(readmes.equals("bye")) {
							pauseT1 = false;
							stopT1 = true;
							in.close();
							out.close();
							socket.close();
							if(serversocket!=null) serversocket.close();
						}
					}
				}
				ta.setText(ta.getText()+"\nClosing connections");
				while(true) {
					Thread.sleep(300);
					if(!app.isVisible()) {
						doing = false;
						break;
					}
					else if(input.equalsIgnoreCase("new")) {
						doing = true;
						input = "";
						break;
					}
				}
			}
		}catch(IOException e) {
		}
	}
}