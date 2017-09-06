import java.util.*;
import java.net.*;
import java.io.*;
// Java Worker <ip_server> <port_server>
// Note: messages are delimitted by '/'
public class Worker{
	public static void main (String args[]){
		
		// First thing: connect to server
		try{
		final String magicNumber = "15540";
		String id;
		DatagramSocket listenSocket = new DatagramSocket();	
		System.out.println("Port: " + listenSocket.getLocalPort());
		int portServer = Integer.parseInt(args[1]);
		InetAddress ipServer = InetAddress.getByName(args[0]);
		String message = "REQUEST_TO_JOIN/";
		byte[] sendData = new byte[1024];
		sendData = message.getBytes();
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ipServer, portServer);
		listenSocket.send(sendPacket);
		System.out.println("REQUEST_TO_JOIN");
		listenSocket.setSoTimeout(5000);
		ArrayList<Job> jobQueue = new ArrayList<Job>();

		//Now waiting for server to acknowledge the Request

		byte[] receiveData = new byte[1024];
 		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
 		StringTokenizer st = null;
 		while (true){
 			try {
	 		
	 		listenSocket.receive(receivePacket);
	 		System.out.println("Check1");
	 		String fromServer = new String(receivePacket.getData());
	 		if (receivePacket.getAddress().toString().substring(1).equals(args[0]) && receivePacket.getPort() == portServer) // Checking if packet is received from server
	 		{
	 			st = new StringTokenizer(fromServer, "/");
	 			message = st.nextToken();
	 			id = st.nextToken();
	 	 		System.out.println(id);	
	 			break;
	 		}	
			}catch (SocketTimeoutException e) 
			{
	 			System.out.println("Connection to Server: Timed out");
	 			return;
			}
			 catch (Exception e) {e.printStackTrace();}
			}
	 		
	 		
	 	System.out.println("Connection to Server Established");
		listenSocket.setSoTimeout(100);
		boolean initFlag = true; // true if a job is currently 

		Job currentJob = new Job();
		// waiting for commands from the server
		while (true){

			try {
			listenSocket.receive(receivePacket);
			String fromServer = new String(receivePacket.getData());
			st = new StringTokenizer(fromServer, "/");
			message = st.nextToken();

			if (!message.equals(magicNumber))
				continue;
			message = st.nextToken();

			}catch (SocketTimeoutException e) {message = "";}

		
			// if not magic number, continue to next iteration


			if (!jobQueue.isEmpty() && !currentJob.running)
			{
				// System.out.println("Job Thread starting");
				currentJob = jobQueue.remove(0);
				System.out.println(currentJob.rangeEnd);
				Thread thread = new Thread(new WorkerThread(currentJob, ipServer, portServer, id));
				thread.start();
			}

//=======================================================================================================================================================================
// New job being assigned. Send JOB_ACK only if the current job is complete
//=======================================================================================================================================================================
			if (message.equals("JOB"))
			{
				if (!jobQueue.isEmpty())
					continue;
				String rangeStart = st.nextToken();
				String rangeEnd = st.nextToken();
				String hash = st.nextToken();
				System.out.println("=============== New Job Received ===============");
				System.out.println(rangeStart);
				System.out.println(rangeEnd);
				System.out.println("=============== New Job Received ===============");				
				Job newJob = new Job(rangeStart, rangeEnd, hash);
				jobQueue.add(newJob);
				message = "ACK_JOB/" + rangeStart + '/' + rangeEnd + '/' + hash + '/';
				sendData = new byte[1024];
				sendData = message.getBytes();
				InetAddress ip = InetAddress.getByName(receivePacket.getAddress().toString().substring(1));
				int port = receivePacket.getPort();
				sendPacket = new DatagramPacket(sendData, sendData.length, ip, port);
				listenSocket.send(sendPacket);
			}
			
//=====================================================================================================================================
// Do I make the server send additional details such as rangeStart, rangeEnd and hash while sending the cancel command?
// Currently, working under the assumption that only CANCEL_JOB is being sent
//=====================================================================================================================================
			else if (message.equals("CANCEL_JOB"))
			{	
				String hash = st.nextToken();
				if (currentJob.hash.equals(hash) && currentJob.running){
					currentJob.running = false;
				if (!jobQueue.isEmpty())
				{
					Job queued = jobQueue.get(0);
					if (queued.hash.equals(hash))
						jobQueue.remove(0);
				}
				
				}
			}
//=====================================================================================================================================
// Send the Server the status of the current job
//=====================================================================================================================================

			else if (message.equals("PING"))
			{

				System.out.println("Ping Received");
				if (!currentJob.running)
				{
					message = "NO_JOB/";
				}
				InetAddress ip = InetAddress.getByName(receivePacket.getAddress().toString().substring(1));
				int port = receivePacket.getPort();

				if (currentJob.running){
					String speed = Integer.toString(currentJob.speed);
					String rangeEnd = new String(currentJob.rangeEnd);
					String completed = new String(currentJob.completed);
					String command = "1/";
					message = Integer.toString(currentJob.speed) + '/'  + currentJob.completed + '/' + rangeEnd + '/' + currentJob.hash + '/';
					if (!jobQueue.isEmpty()){
						Job queued = jobQueue.get(0);
						command = "2/";
						completed = new String(queued.rangeStart);
						rangeEnd = new String(queued.rangeEnd);
						message = message + completed + '/' + rangeEnd + '/' + queued.hash + '/';
					}

					message = command + message;
				}
				System.out.println(message);
				sendData = new byte[1024];
				sendData = message.getBytes();
				sendPacket = new DatagramPacket(sendData, sendData.length, ip, port);
				listenSocket.send(sendPacket);
			}
//=====================================================================================================================================
// When timeout occurs code gets directly to this point
//=====================================================================================================================================
		}

	} catch (Exception e) {e.printStackTrace();}
}

}









