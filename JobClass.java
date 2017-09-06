// This class stores information about a specific job along with details of the Client who requested the job
import java.util.*;
import java.io.Serializable;
public class JobClass implements Serializable {
	private static final long serialVersionUID = -40337217931273938L;	
	public String clientIp;
	public int clientPort;
	public String hash;
	public Vector<WorkerClass> workers; // contains the workers currently working on this job
	public String rangeEnd;
	public boolean running;
	public boolean completed;
	public boolean found;
	public String password;
	public ArrayList<String> others;
	public long time;

	public JobClass()
	{
		running = false;
	}

	public JobClass(String clientIpAdd, int clientPortNo, String hashPass){
		clientIp = clientIpAdd;
		clientPort = clientPortNo;
		hash = hashPass;
		workers = new Vector<WorkerClass>(10);
		running = false;
		completed = false;
		found = false;
		rangeEnd = "aaaaa";
		others = new ArrayList<String>(10);
		time = System.nanoTime();
	}
}