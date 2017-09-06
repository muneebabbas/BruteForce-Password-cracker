// Class stores the information related to a particular job
import java.security.MessageDigest;
import java.util.*;
import java.net.*;

public class Job{
	public char[] rangeStart;
	public char[] rangeEnd;
	public String completed;
	public char[] arr;
	public String hash;
	public boolean running;
	public boolean found;
	public int speed;
	public String ip;
	public int port;

	public Job(){
		running = false;
		found = false;
		speed = 0;
	}
	public Job(String start, String end, String hashPassword){
		rangeStart = start.toCharArray();
		rangeEnd = end.toCharArray();
		completed = start;
		running = false;
		found = false;
		arr = new char[]{'a', 'b', 'c', 'd', 'e', 'f','g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'
								, 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
								, '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
		hash = hashPassword;
		speed = 0;

	}

	public static String calcHash(String str){

		try{
		MessageDigest md = MessageDigest.getInstance("MD5");
		
		md.update(str.getBytes("UTF-8"));
		byte byteData[] = md.digest();
	// Convert the byte buffer to hex
		StringBuffer hexString = new StringBuffer();
		
		for (int i = 0; i < byteData.length; i++){
			String hex=Integer.toHexString(0xff & byteData[i]);
			if (hex.length() == 1) hexString.append('0');
			hexString.append(hex);
		}

		return hexString.toString();
		} catch (java.security.NoSuchAlgorithmException e){}
		catch (java.io.UnsupportedEncodingException e){}
		return null;
	}	
}
