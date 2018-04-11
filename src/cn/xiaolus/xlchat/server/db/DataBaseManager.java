package cn.xiaolus.xlchat.server.db;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;

public class DataBaseManager extends DataBaseConnector {
	
	public DataBaseManager(String driver, String host, String user, char[] password) {
		super(driver, host, user, password);
	}
	
	/**
	 * 初始化数据库表
	 * @return 初始化是否成功
	 */
	public boolean initDatabase() {
		try {
			Connection connection = getConnection();
			PreparedStatement sql;
			sql = connection.prepareStatement("CREATE OR REPLACE TABLE `xlchat`.`users` (`id` int(9) NOT NULL AUTO_INCREMENT,`user` varchar(120) NOT NULL,`name` varchar(120) NOT NULL,`passwdhashb64` varchar(120) NOT NULL,`salt` varchar(120) NOT NULL,PRIMARY KEY (`id`, `user`));");
			return sql.execute();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		
	}
	
	/**
	 * 登录聊天器的方法
	 * @param user 用户名
	 * @param password 密码
	 * @return 是否登陆成功
	 */
	public boolean signin(String user, String password) {
		try {
			Connection connection = getConnection();
			PreparedStatement sql = connection.prepareStatement("select * from xlchat.users where user = ?");
			sql.setString(1, user);
			ResultSet result = sql.executeQuery();
			while(result.next()) {
				String passwordhashb64 = result.getString(4);
				String salt = result.getString(5);
				MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
				byte[] passwordhashinput = messageDigest.digest(new StringBuilder(password).append(salt).toString().getBytes());
				String passwordhashb64input = Base64.getEncoder().encodeToString(passwordhashinput);
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
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
			SecureRandom secureRandom = new SecureRandom();
			String salt = Long.toHexString(secureRandom.nextLong());
			byte[] passwordhash = messageDigest.digest(new StringBuilder(password).append(salt).toString().getBytes());
			String passwordhashb64 = Base64.getEncoder().encodeToString(passwordhash);
			Connection connection = getConnection();
			PreparedStatement sql = connection.prepareStatement("insert into xlchat.users (user,name,passwdhashb64,salt) values(?,?,?,?)");
			sql.setString(1, user);
			sql.setString(2, name);
			sql.setString(3, passwordhashb64);
			sql.setString(4, salt);
			sql.executeUpdate();
			return true;
		} catch (NoSuchAlgorithmException | SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
}
