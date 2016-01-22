package Utils;

public class CustomException extends Exception {
	private String ExceptionDetail="";
	
	public CustomException(String exceptionName,String exceptionDetial){
		super(exceptionName);
		this.ExceptionDetail=exceptionDetial;
	}
	
	@Override
	public String toString(){
		return this.ExceptionDetail;
	}
}
