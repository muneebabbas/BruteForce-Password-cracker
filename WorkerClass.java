// This class stores the string and ip of the workers
import java.io.Serializable;
public class WorkerClass implements Serializable{
	private static final long serialVersionUID = -403250971215465050L;
	public String id;
	public String ip;
	public int port;
	public boolean working;
	public boolean done;
	public String rangeStart;
	public String rangeEnd;
	public String rangeCompleted;
	public String hash;
	public int speed;
	public String message;

	public WorkerClass(String ips, int portNum){
		ip = ips;
		port = portNum;
		working = false;
		speed = 500000;
		done = true;
	}
}