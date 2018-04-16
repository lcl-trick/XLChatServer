package cn.xiaolus.xlchat.server.ui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.KeyStore;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.swing.BoxLayout;
import javax.swing.JTextPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JButton;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import org.json.JSONObject;

import cn.xiaolus.xlchat.server.db.DataBaseManager;
import cn.xiaolus.xlchat.util.JSONInputStream;
import cn.xiaolus.xlchat.util.JSONOutputStream;
import cn.xiaolus.xlchat.util.XCChatMessage;
import cn.xiaolus.xlchat.util.XCMessage;
import cn.xiaolus.xlchat.util.XCSigninMessage;
import cn.xiaolus.xlchat.util.XCSignoutMessage;
import cn.xiaolus.xlchat.util.XCSignupMessage;
import cn.xiaolus.xlchat.util.XCStateMessage;
import cn.xiaolus.xlchat.util.XCUserStateMessage;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * 功能：
 * 服务器界面类，继承自JFrame类
 * 
 * @author 小路
 *
 */
public class Server extends JFrame {
	
//	SSLServerSocket是用于支持安全通信的服务器Socket
	private SSLServerSocket serverSocket;
//	默认端口为9999端口
	private static final int PORT = 8888;
//	用户管理对象
	private final UserManager userManager = new UserManager();
//	表模型对象
	private final DefaultTableModel onlineUserDtm = new DefaultTableModel(new String[]{"用户名","登录时间","IP地址","端口"},0);
	private static final long serialVersionUID = 8482455133264907039L;
//	主页面Panel，界面元素
	private JPanel contentPane;
//	在线用户列表，界面元素
	private JTable tableOnlineUsers;
//	消息记录区域，界面元素
	private JTextPane textPaneMsgRecord;

	/**
	 * 启动应用程序的主方法
	 * @param args
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
	 * 构造方法，创建窗体
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
		
		textPaneMsgRecord = new JTextPane();
		textPaneMsgRecord.setEditable(false);
		textPaneMsgRecord.setBorder(new TitledBorder(null, "日志记录", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		JScrollPane scrollPane_1 = new JScrollPane(textPaneMsgRecord);
		splitPane.setLeftComponent(scrollPane_1);
		
		tableOnlineUsers = new JTable();
		tableOnlineUsers.setModel(onlineUserDtm);
		JScrollPane scrollPane_2 = new JScrollPane(tableOnlineUsers);
		splitPane.setRightComponent(scrollPane_2);
		
		JPanel southPanel = new JPanel();
		contentPane.add(southPanel, BorderLayout.SOUTH);
		southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.X_AXIS));
		
		JButton btnStart = new JButton("启动");
		btnStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
//				点击启动时，在新线程中启动服务器。
				new Thread(()->{
					startServer();
				}).start();
//				将服务器启动的按钮禁用
				btnStart.setEnabled(false);
			}
		});
		southPanel.add(btnStart);
	}
	
	/**
	 * 初始化SSL会话对象
	 * @return
	 * @throws Exception
	 */
	private static SSLContext initSSLContext() throws Exception {
//		打开密钥库文件
		FileInputStream keystorefis = new FileInputStream("XLChatKeystore.keystore");
//		密钥库的密码
		@SuppressWarnings(value = { "需从配置文件中读取！" })
		char[] password = "XiaoLuKEYSTORE0129".toCharArray();
//		创建密钥库对象并加载
		KeyStore ks = KeyStore.getInstance("PKCS12");
		ks.load(keystorefis, password);
//		获得密钥管理工厂对象
		KeyManagerFactory factory = KeyManagerFactory.getInstance("SunX509");
		factory.init(ks, password);
//		创建并用上面的密钥管理工厂对象初始化SSL会话对象
		SSLContext context = SSLContext.getInstance("TLS");
//		第一个参数表示提供的证书，因为是服务器模式所以只需要第一个参数
//		第二个参数是用于验证对方的证书，本例不需要客户端提供证书，故使用null
		context.init(factory.getKeyManagers(), null, null);
		return context;
	}
	
	/**
	 * 启动服务器线程的方法
	 */
	public void startServer() {
		try {
//			初始化SSL会话
			SSLContext context = Server.initSSLContext();
//			从SSL会话中获得支持安全通信的服务器Socket
			serverSocket = (SSLServerSocket)context.getServerSocketFactory().createServerSocket(PORT);
//			设置启用的算法套件为所有支持的算法套件
			serverSocket.setEnabledCipherSuites(serverSocket.getSupportedCipherSuites());
//			提交EDT线程输出服务器启动消息
			EventQueue.invokeLater(()->{
				String oriText = textPaneMsgRecord.getText();
				textPaneMsgRecord.setText(new StringBuilder(oriText).append('\n').append(new Date().toString()).append('\n').append("服务器启动").toString());
			});
//			持续监听连接
			while(true) {
//				接受新连接并获得Socket
				Socket socket = serverSocket.accept();
//				创建新线程为该客户端服务，同时服务器监听线程华丽转身等待下一个客户端
				new Thread(new UserHandler(socket)).start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 功能：
	 * 该类用于处理每个客户端的连接请求，继承Runnable类；
	 * 在服务器接受新的连接时被创建一个并提交到一个线程去运行
	 * 
	 * @author 小路
	 *
	 */
	class UserHandler implements Runnable {
//		当前客户端的Socket对象
		private final Socket currentUserSocket;
//		JSON输入流和输出流，非系统默认
//		是我自己封装的InputStream和OutputStream
//		可能封装的不是很标准，仅仅是在本程序能用而已
//		才疏学浅，还是不太懂封装系统类型
		private JSONInputStream jis;
		private JSONOutputStream jos;
		/**
		 * 构造函数，用于初始化JSON输入输出流
		 * @param socket 一个Socket对象，是当前用户的Socket对象
		 */
		public UserHandler(Socket socket) {
			currentUserSocket = socket;
			try {
//				用当前用户Socket中的输入输出流封装JSON输入输出流
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
//					从JSON输入流中读取JSON对象
					JSONObject receive = jis.readJSONObject();
					XCMessage msg = null;
//					判断接收到的消息，由JSON对象反序列化为消息对象
//					这里在反序列化的时候仅仅处理了消息对象，并没有考虑任何其他情况
//					所以可能不是那么太好用哈
					if ((msg = XCMessage.fromJSONObject(receive, XCChatMessage.class) )!=null) {
//						处理聊天消息
						processChatMessage((XCChatMessage)msg);
					} else if ((msg = XCMessage.fromJSONObject(receive, XCStateMessage.class) )!=null) {
//						处理状态消息
						processStateMessage((XCStateMessage)msg);
					} else if ((msg = XCMessage.fromJSONObject(receive, XCSignupMessage.class) )!=null) {
//						处理注册消息
						processSignupMessage((XCSignupMessage)msg);
					} else if ((msg = XCMessage.fromJSONObject(receive, XCSigninMessage.class) )!=null) {
//						处理登录消息
						processSigninMessage((XCSigninMessage)msg);
					} else if ((msg = XCMessage.fromJSONObject(receive, XCSignoutMessage.class) )!=null) {
//						处理注销消息，因为注销消息未作任何扩展，所以不能放在前面判断，会误认为所有消息都是注销消息
//						当然这里还有待改进
						processSignoutMessage((XCSignoutMessage)msg);
					} else {
//						如果不是任何一种消息，则提交EDT线程显示错误信息
						EventQueue.invokeLater(()->{
							String oriText = textPaneMsgRecord.getText();
							textPaneMsgRecord.setText(new StringBuilder(oriText).append('\n').append(new Date().toString()).append('\n').append("接收到无法解析的JSON对象："+receive.toString()).toString());
						});
					}
				}
//				出现异常之后需要关闭Socket
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
		
		/**
		 * 处理聊天消息
		 * @param msg 聊天消息对象
		 */
		private void processChatMessage(XCChatMessage msg) {
//			取得发送方和接收方，以及消息正文
			String srcUser = msg.getSrcUser();
			String dstUser = msg.getDstUser();
			String msgContent = msg.getMsgContent();
			if (dstUser.equals("")) {
//				如果接收方为空，则代表是公聊消息
				EventQueue.invokeLater(()->{
					String oriText = textPaneMsgRecord.getText();
					textPaneMsgRecord.setText(new StringBuilder(oriText).append('\n').append(new Date().toString()).append('\n').append("转发 "+srcUser+" 发送的公聊消息："+msgContent).toString());
				});
//				将该消息转发给所有其他在线客户端
				transferMsgToOtherUsers(msg);
			} else {
//				否则是私聊消息，仅转发给消息的接收者
				EventQueue.invokeLater(()->{
					String oriText = textPaneMsgRecord.getText();
					textPaneMsgRecord.setText(new StringBuilder(oriText).append('\n').append(new Date().toString()).append('\n').append("转发 "+srcUser+" 发给 "+dstUser+" 的私聊消息："+msgContent).toString());
				});
//				获得目的用户的JSON输出流
				JSONOutputStream jos = userManager.getUserJSONOutputStream(dstUser);
				synchronized (jos) {
					try {
						JSONObject send = new JSONObject(msg);
//						写入JSON对象到JSON输出流
						jos.writeJSONObject(send);
						jos.flush();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		/**
		 * 处理登录消息
		 * @param msg 登录消息对象
		 */
		private void processSigninMessage(XCSigninMessage msg) {
//			登录状态标志
			int flag = 0;
//			获得要登录的用户以及登录密码
			String srcUser = msg.getSrcUser();
			String password = msg.getPassword();
//			创建数据库管理器
			@SuppressWarnings(value = { "需从配置文件中读取！" })
			DataBaseManager dbManager = new DataBaseManager("com.mysql.jdbc.Driver",
					"jdbc:mysql://db.xiaolus.cn",
					"xlcuser", "xlcuser".toCharArray());
//			创建服务器登录状态回复消息
			XCStateMessage message = new XCStateMessage();
			message.setSrcUser("");
			message.setDstUser(srcUser);
			try {
//				连接到数据库
				dbManager.connect();
//				执行数据库登录操作，并获得结果
				if (dbManager.signin(srcUser, password)) {
//					数据库回应称登录成功
//					设置返回消息为成功状态
					message.setStatus(XCStateMessage.SUCCESS);
					message.setError("");
//					创建用户登录消息
					XCUserStateMessage onlineMessage = new XCUserStateMessage();
					onlineMessage.setSrcUser(srcUser);
					onlineMessage.setUserOnline(true);
//					将该用户登录的消息通知给所有其他客户端
					transferMsgToOtherUsers(onlineMessage);
//					提交EDT线程提示用户已登录
					EventQueue.invokeLater(()->{
						String oriText = textPaneMsgRecord.getText();
						textPaneMsgRecord.setText(new StringBuilder(oriText).append('\n').append(new Date().toString()).append('\n').append(srcUser+" 已登录").toString());
					});
//					将用户添加到用户管理器中
					userManager.addUser(onlineUserDtm, srcUser, currentUserSocket, jis, jos);
//					设置登录成功标志位
					flag = 1;
				} else {
//					数据库回应称登录失败
					message.setStatus(XCStateMessage.FAILED);
					message.setError("用户名或密码错误，或数据库错误");
				}
			} catch (ClassNotFoundException | SQLException e) {
//				出现异常，登录失败
				message.setStatus(XCStateMessage.FAILED);
				message.setError(e.getLocalizedMessage());
				e.printStackTrace();
			}
//			两种情况都要回复一下客户端的，创建JSON对象来发送
			JSONObject send = new JSONObject(message);
			synchronized (jos) {
				try {
//					向JSON输出流写入JSON对象
					jos.writeJSONObject(send);
					jos.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
//			要先向用户发送登录成功消息，再发送在线用户列表，不然会出问题的哦
			if (flag == 1) {
				XCUserStateMessage onlineListMessage = new XCUserStateMessage();
				onlineListMessage.setDstUser(srcUser);
				onlineListMessage.setUserOnline(true);
//				向该登录用户发送当前的所有在线用户
				sendOnlineUserList(onlineListMessage);
			}
		}
		
		/**
		 * 处理注销消息
		 * @param msg 注销消息对象
		 */
		private void processSignoutMessage(XCSignoutMessage msg) {
			String srcUser = msg.getSrcUser();
//			创建服务器注销状态回复消息
			XCStateMessage message = new XCStateMessage();
			message.setSrcUser("");
			message.setDstUser(srcUser);
//			判断用户是否在线呀，不在线怎么能注销呢？
			if (userManager.isUserOnline(srcUser)) {
//				在线的情况下就可以注销啦
				message.setStatus(XCStateMessage.SUCCESS);
				message.setError("");
//				注销状态消息
				XCUserStateMessage offlineMessage = new XCUserStateMessage();
				offlineMessage.setSrcUser(srcUser);
				offlineMessage.setUserOnline(false);
//				同样还是通知大家这个用户已经注销啦
				transferMsgToOtherUsers(offlineMessage);
//				再提交给EDT线程，在服务器这边输出一下
				EventQueue.invokeLater(()->{
					String oriText = textPaneMsgRecord.getText();
					textPaneMsgRecord.setText(new StringBuilder(oriText).append('\n').append(new Date().toString()).append('\n').append(srcUser+" 已注销").toString());
				});
//				从用户管理器中去掉这个用户
				userManager.removeUser(onlineUserDtm, srcUser);
			} else {
//				没登录当然是不可以注销哒
				message.setStatus(XCStateMessage.FAILED);
				message.setError("登录状态异常：未登录用户不可以注销");
			}
//			两种情况都要回复一下客户端的，创建JSON对象来发送
			JSONObject send = new JSONObject(message);
			synchronized (jos) {
				try {
//					向JSON输出流写入JSON对象
					jos.writeJSONObject(send);
					jos.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		/**
		 * 处理注册消息
		 * @param msg 注册消息对象
		 */
		private void processSignupMessage(XCSignupMessage msg) {
//			读取消息内容
			String srcUser = msg.getSrcUser();
			String name = msg.getName();
			String password = msg.getPassword();
//			创建服务器注册状态回复消息
			XCStateMessage message = new XCStateMessage();
			message.setSrcUser("");
			message.setDstUser(srcUser);
//			创建数据库管理器
			@SuppressWarnings(value = { "需从配置文件中读取！" })
			DataBaseManager dbManager = new DataBaseManager("com.mysql.jdbc.Driver",
					"jdbc:mysql://db.xiaolus.cn",
					"xlcuser", "xlcuser".toCharArray());
			try {
//				连接到数据库
				dbManager.connect();
//				执行数据库注册操作，并获得结果
				if (dbManager.signup(srcUser, name, password)) {
//					数据库回应称注册成功
					message.setStatus(XCStateMessage.SUCCESS);
					message.setError("");
//					提交EDT线程输出注册成功
					EventQueue.invokeLater(()->{
						String oriText = textPaneMsgRecord.getText();
						textPaneMsgRecord.setText(new StringBuilder(oriText).append('\n').append(new Date().toString()).append('\n').append(srcUser+" 已注册").toString());
					});
				} else {
//					数据库回应称注册失败
					message.setStatus(XCStateMessage.FAILED);
					message.setError("无法注册");
				}
			} catch (ClassNotFoundException | SQLException e) {
//				因为服务器异常导致注册失败
				message.setStatus(XCStateMessage.FAILED);
				message.setError("无法注册");
				e.printStackTrace();
			}
//			要回复一下客户端的，创建JSON对象来发送
			JSONObject send = new JSONObject(message);
			synchronized (jos) {
				try {
//					向JSON输出流写入JSON对象
					jos.writeJSONObject(send);
					jos.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		private void processStateMessage(XCStateMessage msg) {
			String srcUser = msg.getSrcUser();
			if(msg.getStatus()==XCStateMessage.SUCCESS || msg.getStatus()==XCStateMessage.FAILED) {
				return;
			} else if (msg.getStatus()==XCStateMessage.ACCEPT_TRA) {
				String srcHost = userManager.getUserSocket(srcUser).getInetAddress().getHostAddress();
				int port = Integer.valueOf(msg.getError());
				msg.setError(new StringBuilder(srcHost).append(":").append(port).toString());
			} else if (msg.getStatus()==XCStateMessage.REJECT_TRA || msg.getStatus()==XCStateMessage.REQ_TRA) {
				msg.setError("TransferByServer");
			}
//			两种情况都要回复一下客户端的，创建JSON对象来发送
			JSONObject send = new JSONObject(msg);
			synchronized (jos) {
				try {
//					向JSON输出流写入JSON对象
					jos.writeJSONObject(send);
					jos.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		/**
		 * 发送在线用户列表
		 * @param onlineListMessage 在线用户列表消息对象
		 */
		private void sendOnlineUserList(XCUserStateMessage onlineListMessage) {
//			从用户管理器中获得所有在线用户
			String[] users = userManager.getAllOnlineUsers();
//			遍历在线用户数组
			for (String user : users) {
				onlineListMessage.setSrcUser(user);
//				每个用户发送一次消息
				JSONObject send = new JSONObject(onlineListMessage);
				synchronized (jos) {
					try {
//						向JSON输出流写入JSON对象
						jos.writeJSONObject(send);
						jos.flush();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		/**
		 * 转发消息给其他所有在线用户
		 * @param msg 待转发的消息对象
		 */
		private void transferMsgToOtherUsers(XCMessage msg) {
//			获得所有在线用户
			String [] users = userManager.getAllOnlineUsers();
//			遍历在线用户数组
			for (String user : users) {
//				判断消息发送着，来源于自己的消息当然不用发给自己啦				
				if (!msg.getSrcUser().equals(user)) {
//					设置消息接收者
					msg.setDstUser(user);
					JSONObject send = new JSONObject(msg);
//					获得该用户的JSON输出流
					JSONOutputStream jos = userManager.getUserJSONOutputStream(user);
					synchronized (jos) {
						try {
//							向JSON输出流写入JSON对象
							jos.writeJSONObject(send);
							jos.flush();
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
 * 功能：
 * 在线用户管理器
 * 
 * @author 小路
 *
 */
class UserManager {
	
//	一个Map对象用于存储用户名和用户对象之间的映射关系
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
	public boolean addUser(DefaultTableModel dtm, String username, Socket socket, JSONInputStream jis,JSONOutputStream jos) {
		if (username == null || socket == null) {
			return false;
		}
//		将在线用户放入Map映射中
		onlineUsers.put(username, new User(socket,jis,jos));
//		提交EDT线程将在线用户显示在界面上
		EventQueue.invokeLater(()->{
			dtm.addRow(new String[]{username,new Date().toString(),socket.getInetAddress().getHostAddress(),String.valueOf(socket.getPort())});
		});
		return true;
	}
	
	/**
	 * 删除一个在线用户
	 * @param username 用户名
	 * @return 删除成功返回true，否则返回false
	 */
	public boolean removeUser(DefaultTableModel dtm, String username) {
//		判断用户在线状态，如果都不在线就别删除啦
		if (isUserOnline(username)) {
//			从Map映射中获得Set集合
			Set<String> set = onlineUsers.keySet();
			int i = 0;
//			遍历Set集合
			for (String string : set) {
				if (string.equals(username)) {
					break;
				}
				i++;
			}
//			找到该用户之后从Map映射中删除这个用户
			onlineUsers.remove(username);
//			提交EDT线程再将这个用户从界面上删除
			final int i2 = i;
			EventQueue.invokeLater(()->{
				dtm.removeRow(i2);
			});
			return true;
		}
		return false;
	}
	
	/**
	 * 获得所有在线用户的数组
	 * @return 在线用户用户名数组
	 */
	public String[] getAllOnlineUsers() {
//		转换为数组的时候接受一个数组类型参数，用于指示转换成的数组类型
		return onlineUsers.keySet().toArray(new String[0]);
	}
	
	/**
	 * 获得所有在线用户的个数
	 * @return 在线用户个数
	 */
	public int getOnlineUserCount() {
		return onlineUsers.size();
	}
}

/**
 * 功能：
 * 用户类型，一个User对象代表一个在线用户
 * 
 * @author 小路
 *
 */
class User {
//	用户的Socket对象
	private final Socket socket;
//	用户的JSON输入输出流
	private JSONInputStream jis;
	private JSONOutputStream jos;
//	用户的登录时间
	private final Date loginTime;
	
	/**
	 * 构造函数，接受一个Socket对象
	 * @param socket Socket对象，输入输出流将从Socket对象获得
	 */
	public User(Socket socket) {
		this.socket = socket;
		try {
			jos = new JSONOutputStream(socket.getOutputStream());
			jis = new JSONInputStream(socket.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
//		设置当前时间为用户登录的时间
		loginTime = new Date();
	}
	
	/**
	 * 构造函数，接受一个Socket对象以及JSON输入输出流
	 * @param socket Socket对象，将不会从Socket对象中获取输入输出流
	 * @param jis JSON输入流
	 * @param jos JSON输出流
	 */
	public User(Socket socket,JSONInputStream jis,JSONOutputStream jos) {
		this.socket = socket;
		this.jis = jis;
		this.jos = jos;
		loginTime = new Date();
	}
	
	/**
	 * 构造函数，接受一个Socket对象，JSON输入输出流以及登录时间
	 * @param socket Socket对象，将不会从Socket对象中获取输入输出流
	 * @param jis JSON输入流
	 * @param jos JSON输出流
	 * @param loginTime 登录时间
	 */
	public User(Socket socket, JSONInputStream jis,JSONOutputStream jos, Date loginTime) {
		this.socket = socket;
		this.jis = jis;
		this.jos = jos;
		this.loginTime = loginTime;
	}
	
	/**
	 * 得到Socket对象
	 * @return Socket对象
	 */
	public Socket getSocket() {
		return socket;
	}
	
	/**
	 * 得到JSON输入流
	 * @return JSON输入流
	 */
	public JSONInputStream getJis() {
		return jis;
	}
	
	/**
	 * 得到JSON输出流
	 * @return JSON输出流
	 */
	public JSONOutputStream getJos() {
		return jos;
	}
	
	/**
	 * 得到登录时间
	 * @return 登录时间
	 */
	public Date getLoginTime() {
		return loginTime;
	}
	
}
