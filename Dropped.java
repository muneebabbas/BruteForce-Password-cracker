// This class contains the startRange and endRange of dropped connections
import java.io.Serializable;
public class Dropped implements Serializable{ 
	private static final long serialVersionUID = -403250947238479483L;
	public String rangeStart;
	public String rangeEnd;
	public String hash;

	public Dropped(String start, String end, String hashPass){
		rangeStart = start;
		rangeEnd = end;
		hash = hashPass;
	}
}