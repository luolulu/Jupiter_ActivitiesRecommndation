package db;

public class DBConnectionFactory {
	private static final String DEFAULT_DB = "mysql";
	public static DBConnection getConnection(String db) {
		switch(db) {
		case "mysql" : 
			//return new MySQLConnection();
			return null;
		case "mongdb" :
			//return new MongDBConnection();
			return null;
		default:
			throw new IllegalArgumentException("Invalid db: " + db);
		}
	}
	public static DBConnection getConnection() {
		return getConnection(DEFAULT_DB);
	}

}
