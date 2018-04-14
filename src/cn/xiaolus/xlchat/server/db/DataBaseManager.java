package cn.xiaolus.xlchat.server.db;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;

/**
 * 功能：
 * 数据库管理器，继承自DataBaseConnector
 * 
 * @author 小路
 *
 */
public class DataBaseManager extends DataBaseConnector {
	
	/**
	 * 构造函数
	 * @param driver 数据库驱动名称
	 * @param host 数据库主机名
	 * @param user 数据库用户名
	 * @param password 数据库密码
	 */
	public DataBaseManager(String driver, String host, String user, char[] password) {
		super(driver, host, user, password);
	}
	
	/**
	 * 初始化数据库用户表
	 * @return 初始化是否成功
	 */
	public boolean initDatabase() {
		try {
			Connection connection = getConnection();
			PreparedStatement sql;
//			创建表的SQL语句
			sql = connection.prepareStatement("CREATE OR REPLACE TABLE `xlchat`.`users` (`id` int(9) NOT NULL AUTO_INCREMENT,`user` varchar(120) NOT NULL,`name` varchar(120) NOT NULL,`passwdhashb64` varchar(120) NOT NULL,`salt` varchar(120) NOT NULL,PRIMARY KEY (`id`, `user`));");
			return sql.execute();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		
	}
	
	/**
	 * 登录方法
	 * @param user 用户名
	 * @param password 密码
	 * @return 是否登陆成功
	 */
	public boolean signin(String user, String password) {
		try {
//			获得数据库连接
			Connection connection = getConnection();
//			预构造SQL语句
			PreparedStatement sql = connection.prepareStatement("select * from xlchat.users where user = ?");
//			设置SQL参数
			sql.setString(1, user);
//			提交查询
			ResultSet result = sql.executeQuery();
//			遍历结果集合对象
			while(result.next()) {
//				从数据库获得密码HASH值的base64编码值
				String passwordhashb64 = result.getString(4);
//				从数据库获得盐值
				String salt = result.getString(5);
//				进行HASH操作，从用户输入的口令得到HASH值的base64编码值
				MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
				byte[] passwordhashinput = messageDigest.digest(new StringBuilder(password).append(salt).toString().getBytes());
//				用户输入的密码的HASH值的base64编码值
				String passwordhashb64input = Base64.getEncoder().encodeToString(passwordhashinput);
//				如果二者相符合，则登录成功
				if (passwordhashb64.equals(passwordhashb64input)) {
					return true;
				} else {
					continue;
				}
			}
			return false;
		} catch (NoSuchAlgorithmException | SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	/**
	 * 注册聊天用户的方法
	 * @param user 用户名
	 * @param name 用户昵称
	 * @param password 密码
	 * @return 注册是否成功
	 */
	public boolean signup(String user, String name, String password) {
		try {
//			进行HASH操作，处理用户密码
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
//			创建安全随机数生成器对象
			SecureRandom secureRandom = new SecureRandom();
//			获得盐值，盐值是一个十六进制字符串
			String salt = Long.toHexString(secureRandom.nextLong());
//			用户密码和盐值拼接，再进行HASH操作
			byte[] passwordhash = messageDigest.digest(new StringBuilder(password).append(salt).toString().getBytes());
//			得到HASH值的base64编码值
			String passwordhashb64 = Base64.getEncoder().encodeToString(passwordhash);
//			获得数据库连接
			Connection connection = getConnection();
//			预构造SQL语句
			PreparedStatement sql = connection.prepareStatement("insert into xlchat.users (user,name,passwdhashb64,salt) values(?,?,?,?)");
//			设置SQL参数
			sql.setString(1, user);
			sql.setString(2, name);
			sql.setString(3, passwordhashb64);
			sql.setString(4, salt);
//			提交更改
			sql.executeUpdate();
			return true;
		} catch (NoSuchAlgorithmException | SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
}
