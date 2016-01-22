package Utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import Model.AccessTokenBean;

public class InitialAccessTokenUtil {
	public final static String access_token_url = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=APPID&corpsecret=CORPSECRET";
	private static Logger logger=Logger.getLogger(InitialAccessTokenUtil.class);
	
	public static AccessTokenBean getAccessToken(String appid,String appsecret){
		AccessTokenBean accessToken=null;
		String requestURL=access_token_url.replace("APPID", appid).replace("CORPSECRET", appsecret);
		JSONObject jsonObject=HttpRequest(requestURL);
		
		if(null!=jsonObject){
			accessToken=new AccessTokenBean();
			accessToken.setAccessToken(jsonObject.get("accessToken").toString());
			accessToken.setExpiresIn(Integer.parseInt(jsonObject.get("expiresIn").toString()));
		}
		return accessToken;
	}
	
	@SuppressWarnings("unchecked")
	public static JSONObject HttpRequest(String requestURL){
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

}
