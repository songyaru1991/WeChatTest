package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;

import org.apache.log4j.Logger;

import Utils.DatabaseUtility;

public class WeChatUserInfoDAO {
	private static Logger logger=Logger.getLogger(WeChatUserInfoDAO.class);
	
	public void UpdateUserWeChatStatus(String WeChatUserID,int statusCode)throws Exception{
		//WEIXIN_STATUS 1:已關注 2:已凍結 4:未關注
		String sSQL="UPDATE WECHAT_USER_INFOS SET WEIXIN_STATUS=? WHERE WECHAT_USER_ID=?";
		PreparedStatement pstmt=null;
		Connection Conn=null;
		int effectRows=-1;
		
		try{
			DatabaseUtility dbUtility=new DatabaseUtility("AlarmOracleDB");
			Conn=dbUtility.makeConnection();
			pstmt=Conn.prepareStatement(sSQL);
			pstmt.setInt(1, statusCode);
			pstmt.setString(2, WeChatUserID);
			effectRows=pstmt.executeUpdate();
			if(effectRows>=1)
				logger.info("Updating wechat user's status is success.");
		}
		catch(Exception ex){
			logger.error("Updating wechat user's status is failed",ex);
		}
		finally{
			if(!Conn.isClosed())
				Conn.close();
		}
	}

}
