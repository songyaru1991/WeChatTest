import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
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
import Model.AlarmMessage;


public class WeChatThread implements Runnable {
	private static String weChatAccessToken=null;
	private static Logger logger=Logger.getLogger(WeChatThread.class);
	public WeChatThread(String accessToken){
		weChatAccessToken=accessToken;
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
		boolean sendSuccess=false;
		HttpURLConnection Conn=null;
		try{
			URL url=new URL("https://qyapi.weixin.qq.com/cgi-bin/chat/send?access_token="+weChatAccessToken+"");
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
			receiver.put("type", "group");
			receiver.put("id", message.getDrivedGroupID());
			JSONObject text=new JSONObject();
			text.put("content", message.getMessageContent());
			inputValue.put("receiver", receiver);
			inputValue.put("text", text);
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
			while ((output = br.readLine()) != null) {
				System.out.println(output);
				JSONParser parser=new JSONParser();
				JSONObject sendResults=(JSONObject)parser.parse(output);
				if(sendResults.get("errcode").toString().equals("0")){
					AlarmMessageDAO manipulateMessage=new AlarmMessageDAO();
					if(manipulateMessage.updateMessageStatus(message))
						sendSuccess=true;
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
		boolean sendSuccess=false;
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
				receiver.put("type", "group");
				receiver.put("id", message.getDrivedGroupID());
				JSONObject image=new JSONObject();
				image.put("media_id", mediaID);
				inputValue.put("image",image);
				inputValue.put("receiver", receiver);
				
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
				while ((output = br.readLine()) != null) {
					System.out.println(output);
					JSONParser parser=new JSONParser();
					JSONObject sendResults=(JSONObject)parser.parse(output);
					if(sendResults.get("errcode").toString().equals("0"))
						sendSuccess=true;
					else
						sendSuccess=false;
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
		while(true){
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
							else
								logger.info("Sned text message to group chat is failed.");
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
									}
								}
								else
									logger.info("Send text to group chat is failed. ");
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
			catch(Exception ex){
				ex.printStackTrace();
			}
		}
	}
}
