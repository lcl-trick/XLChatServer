package cn.xiaolus.xlchat.server.ui;

import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;
import java.awt.event.ActionEvent;

import cn.xiaolus.xlchat.server.db.DataBaseManager;

/**
 * 功能：
 * 注册界面类，继承自JFrame类
 * 
 * @author 小路
 *
 */
public class XLChatSignup extends JFrame {
	
	private static final long serialVersionUID = 5047756297176762089L;
//	配置文件
	private Properties properties = new Properties();
//	主页面Panel，界面元素
	private JPanel contentPane;
//	用户名输入框，界面元素
	private JTextField textFieldUser;
//	密码和密码确认输入框，界面元素
	private JPasswordField passwordField;
	private JPasswordField passwordFieldConfirm;
//	用户昵称输入框，界面元素
	private JTextField textFieldName;
//	注册按钮
	private JButton btnSignUp;
//	返回按钮
	private JButton btnBack;

	/**
	 * 构造方法，创建窗体
	 */
	public XLChatSignup() {
		setResizable(false);
		setTitle("注册");
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		btnSignUp = new JButton("注册");
		btnSignUp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				signup();
			}
		});
		btnSignUp.setBounds(108, 223, 117, 29);
		contentPane.add(btnSignUp);
		
		textFieldUser = new JTextField();
		textFieldUser.setBounds(83, 36, 299, 26);
		contentPane.add(textFieldUser);
		textFieldUser.setColumns(10);
		
		passwordField = new JPasswordField();
		passwordField.setBounds(83, 128, 299, 26);
		contentPane.add(passwordField);
		
		passwordFieldConfirm = new JPasswordField();
		passwordFieldConfirm.setBounds(83, 176, 299, 26);
		contentPane.add(passwordFieldConfirm);
		
		textFieldName = new JTextField();
		textFieldName.setBounds(83, 81, 299, 26);
		contentPane.add(textFieldName);
		textFieldName.setColumns(10);
		
		JLabel label = new JLabel("帐号");
		label.setBounds(38, 41, 33, 16);
		contentPane.add(label);
		
		JLabel label_1 = new JLabel("姓名");
		label_1.setBounds(38, 86, 33, 16);
		contentPane.add(label_1);
		
		JLabel label_2 = new JLabel("密码");
		label_2.setBounds(38, 133, 33, 16);
		contentPane.add(label_2);
		
		JLabel label_3 = new JLabel("确认");
		label_3.setBounds(38, 181, 52, 16);
		contentPane.add(label_3);
		
		btnBack = new JButton("返回");
		btnBack.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
//				返回时关闭当前窗口
				dispose();
			}
		});
		btnBack.setBounds(237, 223, 117, 29);
		contentPane.add(btnBack);
		loadProperties();
	}
	
	/**
	 * 加载配置文件
	 */
	private void loadProperties() {
		try {
//			获得资源的输入流
			InputStream inputStream = getClass().getResourceAsStream("/config/config.properties");
//			加载配置文件
			properties.load(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 注册方法
	 */
	public void signup() {
//		获得用户名和用户名称，去除结尾的空格
		String user = textFieldUser.getText().trim();
		String name = textFieldName.getText().trim();
//		获得用户密码和确认的密码
		String password = new String(passwordField.getPassword());
		String passwordConfirm = new String(passwordFieldConfirm.getPassword());
		if (user.length() > 0 && name.length() > 0 && password.length() > 0 && passwordConfirm.length() > 0) {
//			如果注册信息全部填写
			if (password.equals(passwordConfirm)) {
//				如果两次密码输入一致
//				创建数据库管理器
				DataBaseManager dbManager = new DataBaseManager(properties.getProperty("DatabaseDriver"),
						properties.getProperty("DatabaseHost"),
						properties.getProperty("DatabaseUsername"),
						properties.getProperty("DatabasePassword").toCharArray());
				try {
//					连接到数据库
					dbManager.connect();
//					执行数据库注册操作，并获得结果
					if (dbManager.signup(user, name, password)) {
//						数据库回应称注册成功
						JOptionPane.showMessageDialog(this, "注册成功");
						dispose();
					} else {
						JOptionPane.showMessageDialog(this, "数据库异常，注册失败");
						dispose();
					}
				} catch (ClassNotFoundException | SQLException e) {
//					因为服务器异常导致注册失败
					JOptionPane.showMessageDialog(this, "服务器异常，无法注册");
					dispose();
					e.printStackTrace();
				}
			} else {
				JOptionPane.showMessageDialog(this, "两次密码输入不一致");
			}
		} else {
			JOptionPane.showMessageDialog(this, "请完善注册信息");
		}
	}
}
