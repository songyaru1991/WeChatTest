package Model;

public class AccessTokenBean {
	private String accessToken;
	private int expiresIn;
	
	public int getExpiresIn(){
		return this.expiresIn;
	}
	
	public void setExpiresIn(int expiresIn){
		this.expiresIn=expiresIn;
	}
	
	public String getAccessToken(){
		return this.accessToken;
	}
	
	public void setAccessToken(String accessToken){
		this.accessToken=accessToken;
	}
}
