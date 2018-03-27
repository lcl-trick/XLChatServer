package cn.xiaolus.xlchat.server.ui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.BoxLayout;
import javax.swing.JTextPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JButton;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import cn.xiaolus.xlchat.server.util.XCChatMessage;
import cn.xiaolus.xlchat.server.util.XCMessage;
import cn.xiaolus.xlchat.server.util.XCUserStateMessage;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class Server extends JFrame {

	/**
	 * 
	 */
	private ServerSocket serverSocket;
	private final int PORT = 9999;
	private final UserManager userManager = new UserManager(); 
	private final DefaultTableModel onlineUserDtm = new DefaultTableModel();
	private static final long serialVersionUID = 8482455133264907039L;
	private JPanel contentPane;
	private JTable tableOnlineUsers;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(()->{
			try {
				Server frame = new Server();
				frame.setVisible(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public Server() {
		setTitle("聊天程序服务器");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JPanel centerPanel = new JPanel();
		contentPane.add(centerPanel, BorderLayout.CENTER);
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.X_AXIS));
		
		JSplitPane splitPane = new JSplitPane();
		splitPane.setResizeWeight(0.5);
		centerPanel.add(splitPane);
		
		JTextPane textPaneMsgRecord = new JTextPane();
		textPaneMsgRecord.setBorder(new TitledBorder(null, "\u6D88\u606F\u8BB0\u5F55", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		splitPane.setLeftComponent(textPaneMsgRecord);
		
		tableOnlineUsers = new JTable();
		tableOnlineUsers.setModel(onlineUserDtm);
		splitPane.setRightComponent(tableOnlineUsers);
		
		JPanel southPanel = new JPanel();
		contentPane.add(southPanel, BorderLayout.SOUTH);
		southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.X_AXIS));
		
		JButton btnStart = new JButton("启动");
		btnStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new Thread(()->{
					startServer();
				}).start();
				btnStart.setEnabled(false);
			}
		});
		southPanel.add(btnStart);
	}
	
	public void startServer() {
		try {
			serverSocket = new ServerSocket(PORT);
			System.out.println("服务器启动");
			while(true) {
				Socket socket = serverSocket.accept();
				new Thread(new UserHandler(socket)).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	class UserHandler implements Runnable {
		private final Socket currentUserSocket;
		private ObjectInputStream ois;
		private ObjectOutputStream oos;
		
		public UserHandler(Socket socket) {
			currentUserSocket = socket;
			try {
				ois = new ObjectInputStream(currentUserSocket.getInputStream());
				oos = new ObjectOutputStream(currentUserSocket.getOutputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public void run() {
			try {
				while(true) {
					XCMessage message = (XCMessage) ois.readObject();
					if (message instanceof XCUserStateMessage) {
//						处理用户状态消息
						processUserStateMessage((XCUserStateMessage) message);
					} else if (message instanceof XCChatMessage) {
//						处理聊天消息
					} else {
						System.out.println("见鬼了");
					}
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (currentUserSocket != null) {
					try {
						currentUserSocket.close();
					} catch (IOException e2) {
						e2.printStackTrace();
					}
				}
			}
		}
		
		public void processUserStateMessage(XCUserStateMessage msg) {
			String srcUser = msg.getSrcUser();
			if(msg.isUserOnline()) {
				if (userManager.isUserOnline(srcUser)) {
					System.out.println("拒绝重复登录请求");
					return;
				} else {
					userManager.addUser(srcUser, currentUserSocket, ois, oos);
					String[] users = userManager.getAllOnlineUsers();
					for(String user : users) {
						XCUserStateMessage userStateMessage = new XCUserStateMessage(user, srcUser, true);
						synchronized (userStateMessage) {
							try {
								oos.writeObject(userStateMessage);
								oos.flush();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
					transferMsgToOtherUsers(msg);
				}
			} else if(msg.isUserOffline()) {
				if (userManager.isUserOnline(srcUser)) {
					userManager.removeUser(srcUser);
					for (int i = 0; i < onlineUserDtm.getRowCount(); i++) {
						if (onlineUserDtm.getValueAt(i, 0).equals(srcUser)) {
							onlineUserDtm.removeRow(i);
						}
					}
					transferMsgToOtherUsers(msg);
				} else {
					System.out.println("收到了幽灵👻消息");
				}
			}	
		}
		
		private void transferMsgToOtherUsers(XCMessage msg) {
			String [] users = userManager.getAllOnlineUsers();
			for (String user : users) {
				if (!msg.getSrcUser().equals(user)) {
					ObjectOutputStream oos = userManager.getUserObjectOutputStream(user);
					synchronized (oos) {
						try {
							oos.writeObject(msg);
							oos.flush();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				} else {
					continue;
				}
			}
		}
	}

}
/**
 * 管理在线用户的类
 * @author 路伟饶
 *
 */
class UserManager {
	private final Map<String, User> onlineUsers = new HashMap<String, User>();
	
	/**
	 * 判断用户是否在线
	 * @param username 用户名
	 * @return 在线返回true，否则返回false
	 */
	public boolean isUserOnline(String username) {
		return onlineUsers.containsKey(username);
	}
	/**
	 * 判断在线用户列表是否为空
	 * @return 为空返回true，否则返回false
	 */
	public boolean isEmpty() {
		return onlineUsers.isEmpty();
	}
	/**
	 * 返回一个用户的对象输出流
	 * @param username 用户名
	 * @return 对象输出流对象
	 */
	public ObjectOutputStream getUserObjectOutputStream(String username) {
		if (isUserOnline(username)) {
			return onlineUsers.get(username).getOos();
		} else {
			return null;
		}
	}
	
	/**
	 * 返回一个用户的对象输入流
	 * @param username 用户名
	 * @return 对象输入流对象
	 */
	public ObjectInputStream getUserObjectInputStream(String username) {
		if (isUserOnline(username)) {
			return onlineUsers.get(username).getOis();
		} else {
			return null;
		}
	}
	/**
	 * 返回一个用户的Socket对象
	 * @param username 用户名
	 * @return Socket对象
	 */
	public Socket getUserSocket(String username) {
		if (isUserOnline(username)) {
			return onlineUsers.get(username).getSocket();
		} else {
			return null;
		}
	}
	/**
	 * 增加一个在线用户
	 * @param username 用户名
	 * @param socket 用户的Socket对象
	 * @return 增加成功返回true，否则返回false
	 */
	public boolean addUser(String username, Socket socket, ObjectInputStream ois,ObjectOutputStream oos) {
		if (username == null || socket == null) {
			return false;
		}
		onlineUsers.put(username, new User(socket,ois,oos));
		return true;
	}
	/**
	 * 
	 * @param username
	 * @return
	 */
	public boolean removeUser(String username) {
		if (isUserOnline(username)) {
			onlineUsers.remove(username);
			return true;
		}
		return false;
	}
	/**
	 * 
	 * @return
	 */
	public String[] getAllOnlineUsers() {
		return (String[]) onlineUsers.keySet().toArray();
	}
	/**
	 * 
	 * @return
	 */
	public int getOnlineUserCount() {
		return onlineUsers.size();
	}
}

/**
 * 一个User对象代表一个在线用户
 * @author 路伟饶
 *
 */
class User {
	private final Socket socket;
	private ObjectInputStream ois;
	private ObjectOutputStream oos;
	private final Date logonTime;
	
	public User(Socket socket) {
		this.socket = socket;
		try {
			oos = new ObjectOutputStream(socket.getOutputStream());
			ois = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		logonTime = new Date();
	}
	
	public User(Socket socket,ObjectInputStream ois,ObjectOutputStream oos) {
		this.socket = socket;
		this.ois = ois;
		this.oos = oos;
		logonTime = new Date();
	}

	public User(Socket socket, ObjectInputStream ois, ObjectOutputStream oos, Date logonTime) {
		this.socket = socket;
		this.ois = ois;
		this.oos = oos;
		this.logonTime = logonTime;
	}

	public Socket getSocket() {
		return socket;
	}

	public ObjectInputStream getOis() {
		return ois;
	}

	public ObjectOutputStream getOos() {
		return oos;
	}

	public Date getLogonTime() {
		return logonTime;
	}
	
}
