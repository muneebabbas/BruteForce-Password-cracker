import java.util.*;
import java.net.*;
import java.io.*;
import java.util.concurrent.CopyOnWriteArrayList;

// Java Server <listenPort>
@SuppressWarnings("unchecked")
public class Server{
	public static void main (String args[]){

		try{
		final String magicNumber = "15540";
		int listenPort = Integer.parseInt(args[0]);
		DatagramSocket listenSocket = new DatagramSocket(listenPort);
		CopyOnWriteArrayList<Dropped> dropped = new CopyOnWriteArrayList<Dropped>();
		CopyOnWriteArrayList<WorkerClass> workers = new CopyOnWriteArrayList<WorkerClass>();
		Hashtable<String, JobClass> jobs = new Hashtable<String, JobClass>(10);		
		ArrayList<JobClass> completed = new ArrayList<JobClass>(10);
//============================================================================================================
		/* Creating a HashTable with hash as the key, and Array List as the value. Check if a job has been
		completely assigned. If so, check if any job is in progress. if not, send message to client and 
		remove the job */

		Hashtable<String, ArrayList<String>> inProgress = new Hashtable<String, ArrayList<String>>(10);



//============================================================================================================

		Scanner userInput = new Scanner(System.in);
		System.out.println("Do you want to load progress from file? (y/n)");
		String response = userInput.next();

		if (response.equals("y")){
			dropped = (CopyOnWriteArrayList<Dropped>) deserialize("dropped.txt");
			workers = (CopyOnWriteArrayList<WorkerClass>) deserialize("workers.txt");
			jobs = (Hashtable<String, JobClass>) deserialize("jobs.txt");
			completed = (ArrayList<JobClass>) deserialize("completed.txt");
			inProgress = (Hashtable<String, ArrayList<String>>) deserialize("inProgress.txt");
		}

		Thread thread = new Thread(new PingThread(workers, dropped));
		thread.start();
		Thread thread1 = new Thread(new ServerThread(workers, jobs, dropped, completed, inProgress));
		thread1.start();
		// thread1.join();
		listenSocket.setSoTimeout(1000);
		StringTokenizer st = null;
		String message = "";

		while(true){

			byte[] receiveData = new byte[1024];
	 		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
	 		try{
	 		listenSocket.receive(receivePacket);
	 		String received = new String(receivePacket.getData());
	 		st = new StringTokenizer(received, "/");
	 		message = st.nextToken();
	 		} catch(SocketTimeoutException e)
	 		{
	 			message = "";
	 		}

//=============================================================================================================
// New Worker joining; Possible optimization --> Use a hashTable to store the the workers. ipAndPort as the key
//=============================================================================================================
	 		if (message.equals("REQUEST_TO_JOIN"))
	 		{
	 			System.out.println("Worker:REQUEST_TO_JOIN");
	 			String ipWorker = receivePacket.getAddress().toString().substring(1); // because ip is of the format /127.0.0.1
	 			System.out.println(ipWorker);
	 			int portWorker = receivePacket.getPort();
	 			System.out.println(portWorker);
	 			WorkerClass worker = new WorkerClass(ipWorker, portWorker);
	 			workers.add(worker);
	 			message = "ACCEPT/" + ipWorker + ':' + Integer.toString(portWorker) + '/' ;
				InetAddress ipServer = InetAddress.getByName(ipWorker);
				byte[] sendData = new byte[1024];
				sendData = message.getBytes(); 
				DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ipServer, portWorker);
				listenSocket.send(sendPacket);
	 		}
//=============================================================================================================
// New job from a client, Optimisation, save job in hashtable with hash as key
//=============================================================================================================
	 		else if (message.equals("JOB"))
	 		{
	 			String ipClient = receivePacket.getAddress().toString().substring(1); // because ip is of the format /127.0.0.1
	 			int portClient = receivePacket.getPort();
	 			String hash = st.nextToken();
	 			JobClass job = null;

	 			// if the same job has been previously assigned by same or some other client, get JobCLass object from hash
	 			if (jobs.containsKey(hash)){
	 				job = jobs.get(hash);
	 				
	 				// if requested job was already requested and completed; send the result
	 				if (job.found){
	 					InetAddress ip = InetAddress.getByName(ipClient);
	 					byte[] sendData = new byte[1024];
	 					message = "DONE_FOUND/" + job.hash + '/' + job.password + '/';
	 					sendData = message.getBytes();
	 					DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ip, portClient);
	 					listenSocket.send(sendPacket);
	 				}

	 				// if requested job already requested but not completed, add this client to ArrayList to reply later when the job is completed
	 				else{
	 					String ipAndPort = ipClient + ':' + Integer.toString(portClient);
	 					job.others.add(ipAndPort);
	 				}
	 			}
	 			else{
		 			job = new JobClass(ipClient, portClient, hash);
		 			jobs.put(hash, job);
		 			System.out.println("New job added");
	 			}
	 		}

//=============================================================================================================
// Find the job from the list and notify client. Also, send cancel Job to all the workers working on this job
//=============================================================================================================
	 		else if (message.equals("DONE_FOUND"))
	 		{
	 			System.out.println("DONE_FOUND");
	 			// Acknowledge to the Worker	
	 			String ipWorker = receivePacket.getAddress().toString().substring(1); // because ip is of the format /127.0.0.1
	 			int portWorker = receivePacket.getPort();
	 			byte[] sendData = new byte[1024];
	 			String sendMessage = "ACK_FOUND/";
	 			sendData = sendMessage.getBytes();
	 			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(ipWorker), portWorker);
	 			listenSocket.send(sendPacket);

	 			String hash = st.nextToken();
	 			String password = st.nextToken();
	 			JobClass doneJob = jobs.get(hash);
	 			doneJob.password = password;
	 			doneJob.found = true;
	 			if (doneJob == null)
	 				continue;
	 			doneJob.completed = true;
	 			Thread.sleep(500);
	 			doneFound(doneJob, listenSocket, hash, password, true);
	 			cancelJob(doneJob, listenSocket, hash);
	 		}

//=============================================================================================================
// Set the working to false
//=============================================================================================================
	 		else if (message.equals("DONE_NOT_FOUND"))
	 		{
	 			System.out.println("NotFound");
	 			String ipWorker = receivePacket.getAddress().toString().substring(1); // because ip is of the format /127.0.0.1
	 			int portWorker = receivePacket.getPort();
	 			System.out.println(ipWorker + ":" + portWorker);
	 			byte[] sendData = new byte[1024];
	 			String sendMessage = "ACK_NOT_FOUND/";
	 			sendData = sendMessage.getBytes();
	 			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(ipWorker), portWorker);
	 			listenSocket.send(sendPacket);
	 			String hash = st.nextToken();
	 			String ipAndPort = st.nextToken();
	 			String range = st.nextToken();
	 			// Remove this job from the inProgress hashtable
	 			ArrayList<String> progress = inProgress.get(hash);
	 			Iterator<String> it = progress.iterator();
	 			while(it.hasNext()){
	 				String rangeEnd = it.next();
	 				if (rangeEnd.equals(range))
	 					it.remove();
	 			} 
	 			StringTokenizer pt = new StringTokenizer(ipAndPort, ":");
	 			ipWorker = pt.nextToken();
	 			portWorker = Integer.parseInt(pt.nextToken());
	 			System.out.println(ipWorker + ":" + portWorker);

	 		}

//=============================================================================================================
// Ping received from Client
//=============================================================================================================

	 		else if (message.equals("PING"))
	 		{
	 			String hash = st.nextToken();
	 			JobClass myJob = jobs.get(hash);
	 			if (myJob == null)
	 				continue;
	 			myJob.time = System.nanoTime();
	 		}

//=============================================================================================================
// Cancel Job sent from Client
//=============================================================================================================

	 		else if (message.equals("CANCEL_JOB"))
	 		{
	 			System.out.println("Job cancelled");
	 			String hash = st.nextToken();
	 			JobClass job = jobs.get(hash);
	 			if (job == null)
	 				continue;
	 			if (job.others.isEmpty()){
	 				job.completed = true;
	 				cancelJob(job, listenSocket, job.hash);
	 				jobs.remove(job.hash);
	 			}
	 		}

	 		// Code reaches this point every time a timeout occurs or a packet is received
	 		// Serialize objects and store them in files

	 		serialize(dropped, "dropped.txt");
	 		serialize(workers, "workers.txt");
	 		serialize(jobs, "jobs.txt");
	 		serialize(completed, "completed.txt");
	 		serialize(inProgress, "inProgress.txt");

	 		// Check if the completed ArrayList is not empty 

	 		if (!completed.isEmpty())
	 		{
	 			JobClass completedJob = completed.get(0);
	 			String hash = completedJob.hash;
	 			ArrayList<String> inProgList = inProgress.get(hash);
	 			if (inProgList.isEmpty())
	 			{
	 				doneFound(completedJob, listenSocket, hash, "", false);
	 			}
	 		}

	 		Enumeration<JobClass> iter = jobs.elements();
	 		JobClass myJob;
	 		while(iter.hasMoreElements())
	 		{
	 			myJob = iter.nextElement();
	 			if (!myJob.completed)
	 			{
	 				long elapsedTime = System.nanoTime() - myJob.time;
	 				if (elapsedTime > 25000000000L)
	 				{
	 					myJob.completed = true;
	 					cancelJob(myJob, listenSocket, myJob.hash);
	 					jobs.remove(myJob.hash);
	 				}
	 			}
	 		}



		} // while
	} catch (Exception e) {e.printStackTrace();}	
} //main


//=============================================================================================================
// Send doneFound to the primary client along with any other clients who requested the same job;
//=============================================================================================================
public static void doneFound(JobClass doneJob, DatagramSocket listenSocket, String hash, String password, boolean done)
{
	try{
	InetAddress ip = InetAddress.getByName(doneJob.clientIp);
	byte[] sendData = new byte[1024];
	String message;
	if (done)
		message = "DONE_FOUND/" + hash + '/' + password + '/';
	else
		message = "DONE_NOT_FOUND/" + hash + '/' + password + '/';

	sendData = message.getBytes();
	DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ip, doneJob.clientPort);
	listenSocket.send(sendPacket);
	if (!doneJob.others.isEmpty())
	{
		Iterator<String> it = doneJob.others.iterator();
		while(it.hasNext()){
			String ipAndPort = it.next();
			StringTokenizer st = new StringTokenizer(ipAndPort, ":");
			ip = InetAddress.getByName(st.nextToken());
			int port = Integer.parseInt(st.nextToken());
			sendPacket = new DatagramPacket(sendData, sendData.length, ip, port);
			listenSocket.send(sendPacket);
		}
	} 

	}catch (Exception e)
	{e.printStackTrace();}
}


//=============================================================================================================
// Function to send CancelJob messages to all the workers working on a particular job
//=============================================================================================================
public static void cancelJob(JobClass doneJob, DatagramSocket listenSocket, String hash)
{
	try
	{
	String magicNumber = "15540";
	Iterator<WorkerClass> iter = doneJob.workers.iterator();
	WorkerClass cancelWorker;
	String message = magicNumber + '/' + "CANCEL_JOB/" + hash + '/';
	byte[] sendData = new byte[1024];
	sendData = message.getBytes();
	while(iter.hasNext())
	{
		cancelWorker = iter.next();
		InetAddress ip = InetAddress.getByName(cancelWorker.ip);
		System.out.println("IP_Cancel: " + cancelWorker.ip + ":" + cancelWorker.port);
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ip, cancelWorker.port);
		listenSocket.send(sendPacket);
		cancelWorker.working = false;
	}	

	} catch (Exception e)
	{
		e.printStackTrace();
	} 
}

//=============================================================================================================
// Functions to serialize an object
//=============================================================================================================
public static void serialize(Object object, String fileName)
 {
 	try{
 	FileOutputStream ifstream = new FileOutputStream(fileName);
 	BufferedOutputStream buffered = new BufferedOutputStream(ifstream);
 	ObjectOutputStream ostream = new ObjectOutputStream(buffered);
 	ostream.writeObject(object);
 	ostream.close();
 	} catch (Exception e)
 	{
 		e.printStackTrace();
 	}
 }
//=============================================================================================================
// deserialize an object
//=============================================================================================================
public static Object deserialize(String fileName)
{
	try{
	FileInputStream fis = new FileInputStream(fileName);
	BufferedInputStream is = new BufferedInputStream(fis);
	ObjectInputStream ostream = new ObjectInputStream(is);
	Object object = ostream.readObject();
	ostream.close();
	return object;
	} catch (Exception e) {e.printStackTrace(); return null;}

}






















}