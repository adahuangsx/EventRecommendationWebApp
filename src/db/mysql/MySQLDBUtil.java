package db.mysql;

public class MySQLDBUtil {
	private static final String HOSTNAME = "localhost";
	private static final String PORT_NUM = "3306"; // change it to your mysql port number
	public static final String DB_NAME = "recommendation_project";
	private static final String USERNAME = "root";
	private static final String PASSWORD = "";
	public static final String URL = "jdbc:mysql://"
			+ HOSTNAME + ":" + PORT_NUM + "/" + DB_NAME
			+ "?user=" + USERNAME + "&password=" + PASSWORD
			+ "&autoReconnect=true&serverTimezone=UTC";
	// jdbc: mysql://localhost:...
	/*
	 * static {
		...
	   }
	 * Once this class is used, this static initializer will be run.
	 * Used when the initialization is too long.
	 */
}
