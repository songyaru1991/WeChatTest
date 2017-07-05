import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.log4j.Logger;

public class WeChat{
	private static Logger logger=Logger.getLogger(WeChat.class);	
	public static void main(String[] args){
		logger.info("WeChat Group Chat Started.");
		Date date =new Date();
		SimpleDateFormat format=new SimpleDateFormat("yyyy/mm/dd HH:mm:ss");
		logger.info("Current Time: "+format.format(date));
		//new Thread(new InitialAccessTokenThread()).start();
		new Thread(new WeChatThread()).start();
	}
}
