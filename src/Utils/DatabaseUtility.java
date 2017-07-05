package Utils;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


public class DatabaseUtility {
	private String server;
	private String serverName;
	private String port;
	private String userName;
	private String password;
	private String SID;
	
	public DatabaseUtility(String server) throws Exception{
		this.server=server;
		this.getDatabaseConfig();
	}
	
	public Connection makeConnection()throws Exception{
		Connection conn;
		DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
		/*Oracle DB*/
		String URL="jdbc:oracle:thin:@//"+serverName+":"+port+"/"+SID+"";
		//String URL="jdbc:oracle:thin:@//10.8.91.142:1521/AlarmTestDB";
		conn= DriverManager.getConnection(URL, userName, password);
		return conn;
	};
	
	private void getDatabaseConfig()throws Exception{
		//Windows:c:/FoxLinkNotification/Database_Config/dbConfig.json
		//Mac: /Users/sfc/Desktop/dbConfig.json
		JSONParser parseDBconfig=new JSONParser();
		JSONObject getDBconfig=(JSONObject) parseDBconfig.parse(new FileReader("/Users/sfc/Desktop/dbConfig.json"));
		/*JSONObject getDBconfigDetail=new JSONObject((JSONObject)getDBconfig.get(server));*/
		 JSONObject getDBconfigDetail = (JSONObject) getDBconfig.get(server);
		if(getDBconfigDetail!=null){
			this.serverName=(String) getDBconfigDetail.get("serverName");
			this.port=(String)getDBconfigDetail.get("port");
			this.userName=(String)getDBconfigDetail.get("user");
			this.password=(String)getDBconfigDetail.get("password");
			this.SID=(String)getDBconfigDetail.get("SID");
		}
		
	}
}
