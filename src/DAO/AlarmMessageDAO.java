package DAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import Model.AlarmMessage;
import Utils.DatabaseUtility;

public class AlarmMessageDAO {
	private static Logger logger=Logger.getLogger(AlarmMessageDAO.class);
	@SuppressWarnings("null")
	public List<AlarmMessage> getAlarmMessages()throws Exception{
		ArrayList<AlarmMessage> alarmMessages=new ArrayList<AlarmMessage>();
		String sSQL="select * "
				+ " from alarm_message "
				+ " where message_status=0"
				+ " and MESSAGE_SEND_TYPE in ('43','44')"
				+ " order by CREATE_TIME asc";
		Statement stmt=null;
		Connection Conn=null;
		ResultSet rs=null;
		try{
			DatabaseUtility dbUtility=new DatabaseUtility("AlarmOracleDB");
			Conn=dbUtility.makeConnection();
			stmt=Conn.createStatement();
			rs=stmt.executeQuery(sSQL);
			while(rs.next()){
				AlarmMessage messages=new AlarmMessage();
				messages.setMessageID(rs.getLong(1));
				messages.setSystemName(rs.getString(2));
				if(!rs.getString(3).isEmpty())
					messages.setAppName(rs.getString(3));
				messages.setFactoryCode(rs.getString(4));
				if(rs.getString(5)!=null)
					messages.setMessageTitle(rs.getString(5).trim());
				else
					messages.setMessageTitle("No Value");
				
				if(rs.getString(6)!=null){
					if(rs.getString(6).trim().getBytes().length>130&&rs.getInt(7)!=2)
						messages.setMessageContent(rs.getString(6).trim());
					else
						messages.setMessageContent(rs.getString(6));
				}
				else
					messages.setMessageContent("No Value");
				
				

				messages.setMessageSendType(rs.getInt(7));
				messages.setReceiverPriority(rs.getInt(8));
				messages.setMessageStatus(rs.getInt(9));
				messages.setMessageSender(rs.getString(10));
				messages.setSendByDepartment(rs.getInt(11));
				messages.setDrivedGroupID(rs.getString(13));
				messages.setAppendParamter(rs.getString(14));
				messages.setWeChatAppID(rs.getInt(15));
				messages.setSingleMsg(rs.getInt(17));
				alarmMessages.add(messages);
			}
			
			rs.close();
			stmt.close();
			
		}
		catch(Exception ex){
			logger.error("Get Alarm Message is failed, due to: ",ex);
		}
		finally{
			if(!Conn.isClosed())
				Conn.close();
		}
		return alarmMessages;
	}
	
	public boolean updateMessageStatus(AlarmMessage message)throws Exception{
		boolean isUpdated=false;
		Connection Conn=null;
		PreparedStatement pstmt=null;
		int effectRows=-1;
		String sSQL="update alarm_message set message_status=1 where event_id=?";
		try{
			DatabaseUtility dbUtility=new DatabaseUtility("AlarmOracleDB");
			Conn=dbUtility.makeConnection();
			pstmt=Conn.prepareStatement(sSQL);
			pstmt.setLong(1, message.getMessageID());
			synchronized(pstmt){
				effectRows=pstmt.executeUpdate();
			}
			if(effectRows==1)
				isUpdated=true;
			pstmt.close();
			Conn.close();
		}
		catch(Exception ex){
			logger.error("Updating message status is failed, due to : ",ex);
		}
		return isUpdated;
	}

}
