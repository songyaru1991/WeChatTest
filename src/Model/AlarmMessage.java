package Model;

import Utils.CustomException;

public class AlarmMessage {
	private long messageID;
	private String systemName;
	private String appName;
	private String messageTitle;
	private String messageContent;
	private int messageSendType;
	private int receiverPriority;
	private int messageStatus;
	private String factoryCode;
	private String messageSender;
	private int sendByDepartment;
	private String drivedGroupID;
	private String appendParamter;
	private int WeChatAppID;
	private int isSingleMsg;
	
	public String getSystemName() {
		return systemName;
	}
	public void setSystemName(String systemName)throws CustomException {
		if(systemName.matches("^.[A-Z a-z]+$")){
			this.systemName = systemName;
		}
		else{
			throw new CustomException("系統名稱格式錯誤","系統名稱("+systemName+")格式錯誤，請確認(系統名稱只能為英文字母)");
		}
	}
	public String getAppName() {
		return appName;
	}
	public void setAppName(String appName)throws CustomException {
		if(appName.matches("^.[A-Z a-z]+$")){
			this.appName = appName;
		}
		else{
			throw new CustomException("App Name 格式錯誤","App Name("+appName+") 格式錯誤，請確認(App Name只能為英文字母)");
		}
	}
	public String getMessageTitle() {
		return messageTitle;
	}
	public void setMessageTitle(String messageTitle) {
		this.messageTitle = messageTitle;
	}
	public String getMessageContent() {
		return messageContent;
	}
	public void setMessageContent(String messageContent)throws CustomException{
		this.messageContent = messageContent;
	}
	public int getMessageSendType() {
		return messageSendType;
	}
	public void setMessageSendType(int messageSendType) throws CustomException {
		switch(messageSendType){
			case 1:
			case 2:
			case 3:
			case 41:
			case 42:
			case 43:
			case 44:
				this.messageSendType = messageSendType;
				break;
			default:
				throw new CustomException("訊息類型代碼錯誤","訊息類型代碼錯誤，請確認訊息類型代碼(1:SMS 2:Mail 3:Notification)");
		}	
//		if(messageSendType>=1 && messageSendType<=3){
//			this.messageSendType = messageSendType;
//		}
//		else{
//			throw new CustomException("訊息類型代碼錯誤","訊息類型代碼錯誤，請確認訊息類型代碼(1:SMS 2:Mail 3:Notification)");
//		}
	}
	public int getReceiverPriority() {
		return receiverPriority;
	}
	public void setReceiverPriority(int receiverPriority) throws CustomException {
		if(receiverPriority>=300 && receiverPriority<=305 || receiverPriority==0){
			this.receiverPriority = receiverPriority;
		}
		else{
			throw new CustomException("員工權限錯誤","員工權限錯誤(代碼:"+receiverPriority+")，請確認權限代碼(300: 線長 301:課長(IDL職員) 302:經理 303:協理 304:副總經理 305:總經理)");
		}
	}
	public int getMessageStatus() {
		return messageStatus;
	}
	public void setMessageStatus(int messageStatus) {
		this.messageStatus = messageStatus;
	}
	public long getMessageID() {
		return messageID;
	}
	public void setMessageID(long messageID) {
		this.messageID = messageID;
	}
	public String getFactoryCode() {
		return factoryCode;
	}
	public void setFactoryCode(String factoryCode)throws CustomException {
		if(factoryCode.matches("^.[A-Za-z]+$")){
			this.factoryCode = factoryCode;
		}
		else{
			throw new CustomException("廠區代碼錯誤","廠區代碼錯誤，代碼應為兩碼英文字母(Ex.FD:富東  KS:崑山)");
		}
	}
	public String getMessageSender() {
		return messageSender;
	}
	/*只能輸入工號*/
	public void setMessageSender(String messageSender) {
		this.messageSender = messageSender;
	}
	public int getSendByDepartment() {
		return sendByDepartment;
	}
	public void setSendByDepartment(int sendByDepartment) throws CustomException {
		if(String.valueOf(sendByDepartment).matches("\\d{4}")){
			this.sendByDepartment = sendByDepartment;
		}
		else{
			throw new CustomException("部門代碼錯誤","傳送訊息的部門代碼錯誤，請確認");
		}
	}
	public String getDrivedGroupID() {
		return drivedGroupID;
	}
	public void setDrivedGroupID(String drivedGroupID) {
		this.drivedGroupID = drivedGroupID;
	}
	public String getAppendParamter() {
		return appendParamter;
	}
	public void setAppendParamter(String appendParamter) {
		this.appendParamter = appendParamter;
	}
	public int getWeChatAppID() {
		return WeChatAppID;
	}
	public void setWeChatAppID(int weChatAppID) {
		WeChatAppID = weChatAppID;
	}
	public int getSingleMsg() {
		return isSingleMsg;
	}
	public void setSingleMsg(int isSingleMsg) {
		this.isSingleMsg = isSingleMsg;
	}
}
