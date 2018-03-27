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
	
	public boolean signin(String user, String password) {
		try {
			Connection connection = getConnection();
			PreparedStatement sql = connection.prepareStatement("select * from cnexp.users where user = ?");
			sql.setString(1, user);
			ResultSet result = sql.executeQuery();
			while(result.next()) {
				String passwordhashb64 = result.getString(4);
//				System.out.println("passwordhashb64:"+passwordhashb64);
				String salt = result.getString(5);
//				System.out.println("salt:"+salt);
				MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
				byte[] passwordhashinput = messageDigest.digest(new StringBuilder(password).append(salt).toString().getBytes());
				String passwordhashb64input = Base64.getEncoder().encodeToString(passwordhashinput);
//				System.out.println("passwordhashb64input:"+passwordhashb64input);
				if (passwordhashb64.equals(passwordhashb64input)) {
					return true;
				} else {
					continue;
				}
			}
			return false;
		} catch (SQLException | NoSuchAlgorithmException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean signup(String user, String name, String password) {
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
			SecureRandom secureRandom = new SecureRandom();
			String salt = Long.toHexString(secureRandom.nextLong());
//			System.out.println("salt:"+salt);
			byte[] passwordhash = messageDigest.digest(new StringBuilder(password).append(salt).toString().getBytes());
			String passwordhashb64 = Base64.getEncoder().encodeToString(passwordhash);
//			System.out.println("passwordhashb64:"+passwordhashb64);
			Connection connection = getConnection();
			PreparedStatement sql = connection.prepareStatement("insert into cnexp.users (user,name,passwdhashb64,salt) values(?,?,?,?)");
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
