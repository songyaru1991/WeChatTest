import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import DAO.AlarmMessageDAO;
import DAO.WeChatUserInfoDAO;
import Model.AccessTokenBean;
import Model.AlarmMessage;
import Utils.CustomException;
import Utils.InitialAccessTokenUtil;


public class WeChatThread implements Runnable {
	private static String weChatAccessToken=null;
	private static Logger logger=Logger.getLogger(WeChatThread.class);
	public final static String access_token_url = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=APPID&corpsecret=CORPSECRET";
	public static String app_id="wx91d365d1fb695837";
	public static String app_secret="Vcrj6mdd4FvdzUc53uNjncSzpCn0utSc7xjd_Ta7ix-08J37POrmhMwwM38P-EA9";
	public WeChatThread(String accessToken){
		weChatAccessToken=accessToken;
	}
	
	public WeChatThread(){
		
	}
	
	private void getAccessToken(){
		AccessTokenBean accessToken=null;
		String requestURL=access_token_url.replace("APPID", app_id).replace("CORPSECRET", app_secret);
		JSONObject jsonObject=HttpRequest(requestURL);
		
		if(null!=jsonObject){
			accessToken=new AccessTokenBean();
			accessToken.setAccessToken(jsonObject.get("accessToken").toString());
			accessToken.setExpiresIn(Integer.parseInt(jsonObject.get("expiresIn").toString()));
			System.out.println(String.format("獲取Access Token中，token:%s", accessToken.getAccessToken()));
			logger.info(String.format("獲取Access Token中，token:%s", accessToken.getAccessToken()));
			this.weChatAccessToken=jsonObject.get("accessToken").toString();
		}
	}
	
	@SuppressWarnings("unchecked")
	private  JSONObject HttpRequest(String requestURL){
		JSONObject jsonObject=null;
		try {
			URL url = new URL(requestURL);
		    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");
			conn.setConnectTimeout(15000);
			conn.setReadTimeout(15000);
			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "+ conn.getResponseCode());
			}	
			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
		    String response=br.readLine();
				if(response!=null){
					jsonObject=new JSONObject();
					JSONParser parser=new JSONParser();
					JSONObject accessTokenObj=(JSONObject)parser.parse(response);
					jsonObject.put("accessToken", accessTokenObj.get("access_token").toString());
					jsonObject.put("expiresIn", accessTokenObj.get("expires_in").toString());//expires_in
				}
				conn.disconnect();
		}
		catch(SocketTimeoutException ex){
		   System.out.println("Get Access Token is failed,due to: Connection Timed Out.");
		   logger.info("Get Access Token is failed,due to: Connection Timed Out.");
		}
		catch (ConnectException ce) {  
		   System.out.println("Get Access Token is failed,due to: Connection Timed Out.");
		   logger.info("Get Access Token is failed,due to: Connection Timed Out.");
		} 
		catch (Exception e) {  
		   String result = String.format("Https Request Error:%s", e);  
		   System.out.println(result); 
		}  
		return jsonObject;
	}
	
	private boolean CheckUserIsFollow(String userID,String accessToken){
		boolean isFollow=true;
		JSONObject userDetailInfos=null;
		HttpURLConnection Conn=null;
		String checkUserIsFollowURL="https://qyapi.weixin.qq.com/cgi-bin/user/get?access_token=ACCESS_TOKEN&userid=USERID";
		try{
			checkUserIsFollowURL=checkUserIsFollowURL.replace("ACCESS_TOKEN", accessToken).replace("USERID", userID);
			URL url=new URL(checkUserIsFollowURL);
			Conn=(HttpURLConnection)url.openConnection();
			Conn.setDoOutput(true);
			Conn.setDoOutput(true);
			Conn.setConnectTimeout(15000);
			Conn.setReadTimeout(15000);
			Conn.setRequestMethod("GET");
			
			if (Conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "+ Conn.getResponseCode());
			}	
			BufferedReader br = new BufferedReader(new InputStreamReader((Conn.getInputStream())));
		    String response=br.readLine();
				if(response!=null){
					userDetailInfos=new JSONObject();
					JSONParser parser=new JSONParser();
					userDetailInfos=(JSONObject)parser.parse(response);
					WeChatUserInfoDAO wechatUserInfo=new WeChatUserInfoDAO();
					if(Integer.valueOf(userDetailInfos.get("status").toString())==4){
						//未關注
						wechatUserInfo.UpdateUserWeChatStatus(userID,4);
						isFollow=false;
					}
					else if(Integer.valueOf(userDetailInfos.get("status").toString())==1){
						//已關注
						wechatUserInfo.UpdateUserWeChatStatus(userID,1);
						isFollow=true;
					}
					else{
						
						wechatUserInfo.UpdateUserWeChatStatus(userID,2);
						isFollow=false;
					}
				}
				Conn.disconnect();
		}
		catch(SocketTimeoutException e){
			logger.info("Request time out",e);
			Conn.disconnect();
		}
		catch(Exception ex){
			logger.error("Send Text Message Failed, due to: ",ex);
			Conn.disconnect();
		}
		finally{
			Conn.disconnect();
		}
		return isFollow;
	}
	
	private String getImageUploadPath(){
		String imageUploadPath=null;
		try{
			JSONParser parser=new JSONParser();
			JSONObject imagePathObject=(JSONObject)parser.parse(new FileReader("/Users/sfc/Desktop/dbConfig.json"));
			imageUploadPath=imagePathObject.get("imageUploadPath").toString();
		}
		catch(Exception ex){
			logger.error("Get Image Upload Path Failed, due to:",ex);
		}
		return imageUploadPath;
	}
	
	@SuppressWarnings("unchecked")
	private boolean sendWeChatTextMessage(AlarmMessage message){
		boolean sendSuccess=true;
		boolean isUserFollow=true;
		HttpURLConnection Conn=null;
		try{
			this.getAccessToken();
			String uploadURL="https://qyapi.weixin.qq.com/cgi-bin/chat/send?access_token=ACCESS_TOKEN";
			uploadURL=uploadURL.replace("ACCESS_TOKEN", weChatAccessToken);
			URL url=new URL(uploadURL);
			Conn=(HttpURLConnection)url.openConnection();
			Conn.setDoOutput(true);
			Conn.setDoOutput(true);
			Conn.setConnectTimeout(15000);
			Conn.setReadTimeout(15000);
			Conn.setRequestMethod("POST");
			Conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");
			JSONObject inputValue=new JSONObject();
			inputValue.put("sender", "FoxLink_IT");
			inputValue.put("msgtype", "text");
			JSONObject receiver=new JSONObject();
			if(message.getSingleMsg()==1){
				receiver.put("type", "single");
				isUserFollow=this.CheckUserIsFollow(message.getDrivedGroupID(),this.weChatAccessToken);
			}
			else{
				receiver.put("type", "group");
			}

			receiver.put("id", message.getDrivedGroupID().toUpperCase());
			JSONObject text=new JSONObject();
			text.put("content", message.getMessageContent());
			inputValue.put("receiver", receiver);
			inputValue.put("text", text);
			
			if(isUserFollow){
				OutputStream os = Conn.getOutputStream();
				os.write(inputValue.toString().getBytes());
				os.flush();
				os.close();
				
				if (Conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
					sendSuccess=false;
					throw new RuntimeException("Failed : HTTP error code : "
						+ Conn.getResponseCode());
				}
		 
				BufferedReader br = new BufferedReader(new InputStreamReader(
						(Conn.getInputStream())));
		 
				String output;
				System.out.println("Output from Server .... \n");
				System.out.println("WeChat ID:"+message.getDrivedGroupID().toUpperCase());
				while ((output = br.readLine()) != null) {
					System.out.println(output);
					JSONParser parser=new JSONParser();
					JSONObject sendResults=(JSONObject)parser.parse(output);
					if(sendResults.get("errcode").toString().equals("0")){
						AlarmMessageDAO manipulateMessage=new AlarmMessageDAO();
						if(manipulateMessage.updateMessageStatus(message))
							sendSuccess=true;
					}
					else{
						sendSuccess=false;
					}
				}		
			}
		}
		catch(SocketTimeoutException e){
			logger.info("Request time out",e);
			System.out.println("Request time out ");
			sendSuccess=false;
			Conn.disconnect();
		}
		catch(Exception ex){
			logger.error("Send Text Message Failed, due to: ",ex);
			Conn.disconnect();
		}
		finally{
			Conn.disconnect();
		}
		
		return sendSuccess;
	}
	
	@SuppressWarnings("unchecked")
	private boolean sendWeChatImageMessage(AlarmMessage message)throws Exception{
		boolean sendSuccess=true;
		boolean isUserFollow=true;
		this.getAccessToken();
		String uplaodURL="https://qyapi.weixin.qq.com/cgi-bin/chat/send?access_token=ACCESS_TOKEN";
		HttpURLConnection Conn=null;
		try{
			/*
			 * Step 1 : 先上傳檔案
			 * Step 2 : 取得MediaID
			 * Stpe 3 : 傳送MediaID至WeChat Server
			 * */
			uplaodURL=uplaodURL.replace("ACCESS_TOKEN", weChatAccessToken);
			String mediaID=this.uploadMeida(this.getImageUploadPath()+"/"+message.getAppendParamter(), "image");
			if(!mediaID.equals("Failed")){
				URL url=new URL(uplaodURL);
				Conn=(HttpURLConnection) url.openConnection();
				Conn.setConnectTimeout(15000);
				Conn.setReadTimeout(15000);
				Conn.setDoOutput(true);
				Conn.setDoOutput(true);
				Conn.setRequestMethod("POST");
				Conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");
				//產生JSON字串
				JSONObject inputValue=new JSONObject();
				inputValue.put("sender", "FoxLink_IT");
				inputValue.put("msgtype", "image");
				JSONObject receiver=new JSONObject();
				if(message.getSingleMsg()==1){
					receiver.put("type", "single");
					isUserFollow=this.CheckUserIsFollow(message.getDrivedGroupID(), this.weChatAccessToken);
				}
				else{
					receiver.put("type", "group");
				}
				//errcode":86219,"errmsg":"invalid chat receiver
				receiver.put("id", message.getDrivedGroupID().toUpperCase());
				JSONObject image=new JSONObject();
				image.put("media_id", mediaID);
				inputValue.put("image",image);
				inputValue.put("receiver", receiver);
				
				if(isUserFollow){
					OutputStream os = Conn.getOutputStream();
					os.write(inputValue.toString().getBytes());
					os.flush();
					os.close();
					if (Conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
						sendSuccess=false;
						throw new RuntimeException("Failed : HTTP error code : "
							+ Conn.getResponseCode());
					}
			 
					BufferedReader br = new BufferedReader(new InputStreamReader(
							(Conn.getInputStream())));
			 
					String output;
					System.out.println("Output from Server .... \n");
					System.out.println("WeChat ID:"+message.getDrivedGroupID().toUpperCase());
					while ((output = br.readLine()) != null) {
						System.out.println(output);
						JSONParser parser=new JSONParser();
						JSONObject sendResults=(JSONObject)parser.parse(output);
						if(sendResults.get("errcode").toString().equals("0")){
							sendSuccess=true;
						}
						else{
							sendSuccess=false;
						}
					}
				}
			}
				
		}
		catch(SocketTimeoutException e){
			logger.info("Request time out",e);
			System.out.println("Request time out ");
			sendSuccess=false;
			Conn.disconnect();
		}
		catch(Exception ex){
			logger.error("Send image to group chat is failed ",ex);
			ex.printStackTrace();
		}
		finally{
			Conn.disconnect();
		}
		return sendSuccess;
	}
	
	private String uploadMeida(String imagePath,String type)throws Exception{
		String url="https://qyapi.weixin.qq.com/cgi-bin/media/upload?access_token=ACCESS_TOKEN&type=TYPE";
		this.getAccessToken();
		url=url.replace("ACCESS_TOKEN", weChatAccessToken).replace("TYPE", type);
		String lineEnd = "\r\n";
        String twoHyphens = "--";
		String boundary="------------7da2e536604c8";
		String mediaID=null;
		int bytesRead,bytesAvaliable,bufferSize;
		byte[] buffer;
        int maxBufferSize = 1*1024*1024;
        HttpURLConnection Conn=null;
		try{
			URL uploadURL=new URL(url);
			Conn=(HttpURLConnection) uploadURL.openConnection();
			Conn.setDoOutput(true);
			Conn.setDoInput(true);
			Conn.setRequestMethod("POST");
			Conn.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);
			Conn.setConnectTimeout(15000);
			Conn.setReadTimeout(15000);
			DataOutputStream dataOutputStream=null;
			FileInputStream inputStream=new FileInputStream(new File(imagePath));
			dataOutputStream=new DataOutputStream(Conn.getOutputStream());
			dataOutputStream.writeBytes(twoHyphens+boundary+lineEnd);
			dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"media\"; filename=\""+imagePath+"\"\r\n");
			dataOutputStream.writeBytes(lineEnd);
			
			bytesAvaliable=inputStream.available();
			bufferSize=Math.min(bytesAvaliable, maxBufferSize);
			buffer=new byte[bufferSize];
			
			bytesRead=inputStream.read(buffer, 0, bufferSize);
			
			while(bytesRead>0){
				dataOutputStream.write(buffer,0,bufferSize);
				bytesAvaliable=inputStream.available();
				bufferSize=Math.min(bytesAvaliable, maxBufferSize);
				bytesRead=inputStream.read(buffer,0,bufferSize);
			}
			
			dataOutputStream.writeBytes(lineEnd);
			dataOutputStream.writeBytes(twoHyphens+boundary+lineEnd);
			
			BufferedReader in=new BufferedReader(new InputStreamReader(Conn.getInputStream()));
			String inputLine=null;
			
			while((inputLine=in.readLine())!=null){
				System.out.println(inputLine);
				JSONParser parser=new JSONParser();
				JSONObject sendResult=(JSONObject)parser.parse(inputLine);
				if(sendResult.get("media_id")!=null)
					mediaID=sendResult.get("media_id").toString();
				else
					mediaID="Failed";
			}
			inputStream.close();
			dataOutputStream.flush();
			dataOutputStream.close();
		}
		catch(SocketTimeoutException e){
			logger.info("Request time out",e);
			System.out.println("Request time out ");
			mediaID="Failed";
			Conn.disconnect();
		}
		catch(Exception ex){
			mediaID="Failed";
			logger.error("Upload Image Failed, due to : ",ex);
		}
		finally{
			Conn.disconnect();
		}
		return mediaID;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		boolean isContinue=true;
		while(isContinue){
			System.out.println("--- WeChat Group Chat Message Send Started ---");
			logger.info("--- WeChat Group Chat Message Send Started ---");
			try{
				//當Alarm Message List 有資料時
				AlarmMessageDAO getAlarmMessage=new AlarmMessageDAO();
				List<AlarmMessage> messages=new ArrayList<AlarmMessage>();
				messages=getAlarmMessage.getAlarmMessages();
				if(messages.size()>0){
					
					Iterator<AlarmMessage> messageIterator=messages.iterator();
					while(messageIterator.hasNext()){
						AlarmMessage message=messageIterator.next();
						if(message.getMessageSendType()==43){
							//文字群聊訊息
							if(this.sendWeChatTextMessage(message)){
								AlarmMessageDAO manipulateMessage=new AlarmMessageDAO();
								if(manipulateMessage.updateMessageStatus(message))
									logger.info("Send text message to group chat is successed.");
							}
							else{
								logger.info("Sned text message to group chat is failed.");
								//throw new CustomException("Thread Interrupted.", "Current Thread is interrupted.");
							}

						}
						else{
							 if(message.getMessageSendType()==44){
								//圖片群聊訊息
								if(this.sendWeChatTextMessage(message)){
									if(this.sendWeChatImageMessage(message)){
										AlarmMessageDAO manipulateMessage=new AlarmMessageDAO();
										if(manipulateMessage.updateMessageStatus(message))
											logger.info("Send image to group chat is successed.");
									}
									else{
										logger.info("Send image to group chat is failed.");
										//throw new CustomException("Thread Interrupted.", "Current Thread is interrupted.");
									}
								}
								else{
									logger.info("Send text to group chat is failed. ");
									//throw new CustomException("Thread Interrupted.", "Current Thread is interrupted.");
								}

							}
						}
					}
				}
				else{
					//當沒有Alarm Message，執行緒休眠60秒
					logger.info("----- No Messages          -----");
					logger.info("----- Thread Sleep 60 Sec. -----");
					System.out.println("----- No Messages          -----");
					System.out.println("----- Thread Sleep 60 Sec. -----");
					Thread.sleep(60000);
				}
			}
			catch(CustomException e){
				logger.info(e);
				try {
					Thread.sleep(60000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			catch(Exception ex){
				logger.error(ex);
				try {
					Thread.sleep(60000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
