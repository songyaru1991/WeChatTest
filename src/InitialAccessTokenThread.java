import org.apache.log4j.Logger;

import Model.AccessTokenBean;
import Utils.InitialAccessTokenUtil;


public class InitialAccessTokenThread implements Runnable {
	private static Logger logger=Logger.getLogger(InitialAccessTokenThread.class);
	public static String app_id="wx91d365d1fb695837";
	public static String app_secret="Vcrj6mdd4FvdzUc53uNjncSzpCn0utSc7xjd_Ta7ix-08J37POrmhMwwM38P-EA9";
	public static AccessTokenBean accessToken=null;
	
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		Thread WeChatThread=null;
		while(true){
			try{
				accessToken=InitialAccessTokenUtil.getAccessToken(app_id, app_secret);
				if(null!=accessToken){
					logger.info(String.format("獲取Access Token中，有效時間為%d token:%s", accessToken.getExpiresIn(), accessToken.getAccessToken()));
                    String result = String.format("獲取Access Token中，有效時間為%d token:%s", accessToken.getExpiresIn(), accessToken.getAccessToken()); 
                    System.out.println(result);
                   
                    if(WeChatThread==null){
                    	WeChatThread=new Thread(new WeChatThread());
                    	WeChatThread.start();
                    }
                    else{
                    	if(!WeChatThread.isAlive()){
                    		WeChatThread=null;
                    		WeChatThread=new Thread(new WeChatThread());
                        	WeChatThread.start();
                    	}
                    }
                    System.out.println("Get AccessToken Thread is sleep 720000 seconds. ");
                    Thread.currentThread().sleep((accessToken.getExpiresIn()+6000)*1000);
                    //For Test
                    //Thread.currentThread().sleep(120000);
                    WeChatThread.sleep(60*100);
				}
				else{
					//Sleep 60 seconds
					Thread.sleep(60*1000);
					if(WeChatThread!=null)
						WeChatThread.sleep(60*100);
				}
			}
			catch(InterruptedException ex){
				try{
					Thread.sleep(60*1000);
					if(WeChatThread!=null)
						WeChatThread.sleep(60*100);
				}
				catch(InterruptedException e1){
					System.out.println(String.format("%s", e1));
				}
				System.out.println(String.format("%s", ex));
			}
			
		}
		
	}

}
