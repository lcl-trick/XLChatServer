package cn.xiaolus.xlchat.server.ui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
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

import org.json.JSONObject;

import cn.xiaolus.xlchat.server.db.DataBaseManager;
import cn.xiaolus.xlchat.server.util.JSONInputStream;
import cn.xiaolus.xlchat.server.util.JSONOutputStream;
import cn.xiaolus.xlchat.server.util.XCChatMessage;
import cn.xiaolus.xlchat.server.util.XCMessage;
import cn.xiaolus.xlchat.server.util.XCSigninMessage;
import cn.xiaolus.xlchat.server.util.XCSignoutMessage;
import cn.xiaolus.xlchat.server.util.XCSignupMessage;
import cn.xiaolus.xlchat.server.util.XCStateMessage;
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
		tableOnlineUsers.setBorder(new TitledBorder(null, "\u5728\u7EBF\u7528\u6237", TitledBorder.LEADING, TitledBorder.TOP, null, null));
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
		private JSONInputStream jis;
		private JSONOutputStream jos;
		
		public UserHandler(Socket socket) {
			currentUserSocket = socket;
			try {
				jis = new JSONInputStream(currentUserSocket.getInputStream());
				jos = new JSONOutputStream(currentUserSocket.getOutputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public void run() {
			try {
				while(true) {
					JSONObject receive = jis.readJSONObject();
					XCMessage msg = null;
					if ((msg = XCMessage.fromJSONObject(receive, XCChatMessage.class) )!=null) {
						processChatMessage((XCChatMessage)msg);
					} else if ((msg = XCMessage.fromJSONObject(receive, XCSigninMessage.class) )!=null) {
						processSigninMessage((XCSigninMessage)msg);
					} else if ((msg = XCMessage.fromJSONObject(receive, XCSignoutMessage.class) )!=null) {
						processSignoutMessage((XCSignoutMessage)msg);
					} else if ((msg = XCMessage.fromJSONObject(receive, XCSignupMessage.class) )!=null) {
						processSignupMessage((XCSignupMessage)msg);
					} else {
						System.out.println("接收到无法解析的JSON对象："+receive.toString());
					}
				}
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
		
		private void processChatMessage(XCChatMessage msg) {
			String srcUser = msg.getSrcUser();
			String dstUser = msg.getDstUser();
			String msgContent = msg.getMsgContent();
			if (dstUser.equals("")) {
				System.out.println("转发 "+srcUser+" 发送的公聊消息："+msgContent);
				transferMsgToOtherUsers(msg);
			} else {
				System.out.println("转发 "+srcUser+" 发给 "+dstUser+" 的私聊消息："+msgContent);
				JSONOutputStream jos = userManager.getUserJSONOutputStream(dstUser);
				synchronized (jos) {
					try {
						JSONObject send = new JSONObject(msg);
						jos.writeJSONObject(send);
						jos.flush();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
//		private void processSigninMessage(XCSigninMessage msg) {
//			int flag = 0;
//			String srcUser = msg.getSrcUser();
//			String password = msg.getPassword();
//			DataBaseManager dbManager = new DataBaseManager("com.mysql.jdbc.Driver",
//					"jdbc:mysql://db.cstacauc.cn?useSSL=true",
//					"user", "password".toCharArray());
//			XCStateMessage message = new XCStateMessage();
//			message.setSrcUser("");
//			message.setDstUser(srcUser);
//			try {
//				dbManager.connect();
//				if (dbManager.signin(srcUser, password)) {
//					message.setStatus(0);
//					message.setError("");
//					XCUserStateMessage onlineMessage = new XCUserStateMessage();
//					onlineMessage.setSrcUser(srcUser);
//					onlineMessage.setUserOnline(true);
//					transferMsgToOtherUsers(onlineMessage);
//					System.out.println(srcUser+" 已登录");
//					userManager.addUser(srcUser, currentUserSocket, jis, jos);
//					flag = 1;
//				} else {
//					message.setStatus(-1);
//					message.setError("用户名或密码错误");
//				}
//			} catch (ClassNotFoundException | SQLException e) {
//				message.setStatus(-1);
//				message.setError(e.getLocalizedMessage());
//				e.printStackTrace();
//			}
//			JSONObject send = new JSONObject(message);
//			synchronized (jos) {
//				try {
//					jos.writeJSONObject(send);
//					jos.flush();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
//			if (flag == 1) {
//				XCUserStateMessage onlineListMessage = new XCUserStateMessage();
//				onlineListMessage.setDstUser(srcUser);
//				onlineListMessage.setUserOnline(true);
//				sendOnlineUserList(onlineListMessage);
//			}
//		}
		
		private void processSigninMessage(XCSigninMessage msg) {
			int flag = 0;
			String srcUser = msg.getSrcUser();
			String password = msg.getPassword();
			XCStateMessage message = new XCStateMessage();
			message.setSrcUser("");
			message.setDstUser(srcUser);
					message.setStatus(0);
					message.setError("");
					XCUserStateMessage onlineMessage = new XCUserStateMessage();
					onlineMessage.setSrcUser(srcUser);
					onlineMessage.setUserOnline(true);
					transferMsgToOtherUsers(onlineMessage);
					System.out.println(srcUser+" 已登录");
					userManager.addUser(srcUser, currentUserSocket, jis, jos);
					flag = 1;
			JSONObject send = new JSONObject(message);
			synchronized (jos) {
				try {
					jos.writeJSONObject(send);
					jos.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (flag == 1) {
				XCUserStateMessage onlineListMessage = new XCUserStateMessage();
				onlineListMessage.setDstUser(srcUser);
				onlineListMessage.setUserOnline(true);
				sendOnlineUserList(onlineListMessage);
			}
		}
		
		private void processSignoutMessage(XCSignoutMessage msg) {
			String srcUser = msg.getSrcUser();
			XCStateMessage message = new XCStateMessage();
			message.setSrcUser("");
			message.setDstUser(srcUser);
			if (userManager.isUserOnline(srcUser)) {
				message.setStatus(0);
				message.setError("");
				XCUserStateMessage offlineMessage = new XCUserStateMessage();
				offlineMessage.setSrcUser(srcUser);
				offlineMessage.setUserOnline(false);
				transferMsgToOtherUsers(offlineMessage);
				System.out.println(srcUser+" 已注销");
				userManager.removeUser(srcUser);
			} else {
				message.setStatus(-1);
				message.setError("登录状态异常：未登录用户不可以注销");
			}
			JSONObject send = new JSONObject(message);
			synchronized (jos) {
				try {
					jos.writeJSONObject(send);
					jos.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		private void processSignupMessage(XCSignupMessage msg) {
			String srcUser = msg.getSrcUser();
			String name = msg.getName();
			String password = msg.getPassword();
			XCStateMessage message = new XCStateMessage();
			message.setSrcUser("");
			message.setDstUser(srcUser);
			DataBaseManager dbManager = new DataBaseManager("com.mysql.jdbc.Driver",
					"jdbc:mysql://db.cstacauc.cn?useSSL=true",
					"user", "password".toCharArray());
			try {
				dbManager.connect();
				if (dbManager.signup(srcUser, name, password)) {
					message.setStatus(0);
					message.setError("");
				} else {
					message.setStatus(-1);
					message.setError("无法注册");
				}
			} catch (ClassNotFoundException | SQLException e) {
				message.setStatus(-1);
				message.setError("无法注册");
				e.printStackTrace();
			}
			
			JSONObject send = new JSONObject(message);
			synchronized (jos) {
				try {
					jos.writeJSONObject(send);
					jos.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		private void sendOnlineUserList(XCUserStateMessage onlineListMessage) {
			String[] users = userManager.getAllOnlineUsers();
			for (String user : users) {
				onlineListMessage.setSrcUser(user);
				JSONObject send = new JSONObject(onlineListMessage);
				synchronized (jos) {
					try {
						jos.writeJSONObject(send);
						jos.flush();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		private void transferMsgToOtherUsers(XCMessage msg) {
			String [] users = userManager.getAllOnlineUsers();
			for (String user : users) {
				if (!msg.getSrcUser().equals(user)) {
					msg.setDstUser("");
					JSONOutputStream jos = userManager.getUserJSONOutputStream(user);
					synchronized (jos) {
						JSONObject send = new JSONObject(msg);
						try {
							jos.writeJSONObject(send);
							jos.flush();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				} else {
					System.out.println("不发送给自己，skipped");
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
	 * 返回一个用户的JSON输出流
	 * @param username 用户名
	 * @return JSON输出流对象
	 */
	public JSONOutputStream getUserJSONOutputStream(String username) {
		if (isUserOnline(username)) {
			return onlineUsers.get(username).getJos();
		} else {
			return null;
		}
	}
	
	/**
	 * 返回一个用户的对象输入流
	 * @param username 用户名
	 * @return 对象输入流对象
	 */
	public JSONInputStream getUserJSONInputStream(String username) {
		if (isUserOnline(username)) {
			return onlineUsers.get(username).getJis();
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
	public boolean addUser(String username, Socket socket, JSONInputStream jis,JSONOutputStream jos) {
		if (username == null || socket == null) {
			return false;
		}
		onlineUsers.put(username, new User(socket,jis,jos));
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
		return onlineUsers.keySet().toArray(new String[0]);
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
	private JSONInputStream jis;
	private JSONOutputStream jos;
	private final Date logonTime;
	
	public User(Socket socket) {
		this.socket = socket;
		try {
			jos = new JSONOutputStream(socket.getOutputStream());
			jis = new JSONInputStream(socket.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		logonTime = new Date();
	}
	
	public User(Socket socket,JSONInputStream jis,JSONOutputStream jos) {
		this.socket = socket;
		this.jis = jis;
		this.jos = jos;
		logonTime = new Date();
	}

	public User(Socket socket, JSONInputStream jis,JSONOutputStream jos, Date logonTime) {
		this.socket = socket;
		this.jis = jis;
		this.jos = jos;
		this.logonTime = logonTime;
	}

	public Socket getSocket() {
		return socket;
	}

	public JSONInputStream getJis() {
		return jis;
	}

	public JSONOutputStream getJos() {
		return jos;
	}

	public Date getLogonTime() {
		return logonTime;
	}
	
}
