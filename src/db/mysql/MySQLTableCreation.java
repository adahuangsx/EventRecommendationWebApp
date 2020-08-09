package db.mysql;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class MySQLTableCreation {
	public static void main(String[] args) {
		try {
			Connection conn = null;
			try {
				
				// Step1: connect to the MySQL Database
				System.out.println("Connected to " + MySQLDBUtil.URL);
				Class.forName("com.mysql.jdbc.Driver").getConstructor().newInstance();
				/*
				 * Why Refection?
				 * Dynamically create some class in the run-time.
				 * e.g. pass the args[0] in here
				 */
				
				/*
				 * static { ... }
				 * Once this class is used, this static initializer will be run.
				 */
				conn = DriverManager.getConnection(MySQLDBUtil.URL);
			}catch(SQLException e) {
				e.printStackTrace();
			}
			if (conn == null) {
				return;
			}
			System.out.println("Connect DB successfully!");
			
			// Step2: Drop the tables in case they exist
			Statement stmt = conn.createStatement();
			String[] tableNames = new String[] {"categories", "history", "items", "users"};
			for (String tableName : tableNames) {
				String sql = "DROP TABLE IF EXISTS " + tableName;
				stmt.executeUpdate(sql); 
			}
			
			// Step 3: Create tables
			String sql = "CREATE TABLE items ("
					+ "item_id VARCHAR(255) NOT NULL,"
					+ "name VARCHAR(255),"
					+ "rating FLOAT,"
					+ "address VARCHAR(255),"
					+ "image_url VARCHAR(255),"
					+ "url VARCHAR(255),"
					+ "distance FLOAT,"
					+ "PRIMARY KEY (item_id))";
			stmt.executeUpdate(sql);
			
			sql = "CREATE TABLE categories ("
					+ "item_id VARCHAR(255) NOT NULL,"
					+ "category VARCHAR(255) NOT NULL,"
					+ "PRIMARY KEY (item_id, category),"
					+ "FOREIGN KEY (item_id) REFERENCES items(item_id))";
			stmt.executeUpdate(sql);

			sql = "CREATE TABLE users ("
					+ "user_id VARCHAR(255) NOT NULL,"
					+ "password VARCHAR(255) NOT NULL,"
					+ "first_name VARCHAR(255),"
					+ "last_name VARCHAR(255),"
					+ "PRIMARY KEY (user_id))";
			stmt.executeUpdate(sql);
			
			sql = "CREATE TABLE history ("
					+ "user_id VARCHAR(255) NOT NULL,"
					+ "item_id VARCHAR(255) NOT NULL,"
					+ "last_favor_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
					+ "PRIMARY KEY (user_id, item_id),"
					+ "FOREIGN KEY (item_id) REFERENCES items(item_id),"
					+ "FOREIGN KEY (user_id) REFERENCES users(user_id))";
			stmt.executeUpdate(sql);


			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
