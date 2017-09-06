// This thread is used to divide the job into pieces and hand them over to the workers
import java.net.*;
import java.util.*;
import java.io.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerThread implements Runnable{
	CopyOnWriteArrayList<WorkerClass> workers;
	Hashtable<String, JobClass> jobs;
	CopyOnWriteArrayList<Dropped> dropped;
	ArrayList<JobClass> completed;
	Hashtable<String, ArrayList<String>> inProgress;

	public ServerThread(CopyOnWriteArrayList<WorkerClass> worker, Hashtable<String, JobClass> job, CopyOnWriteArrayList<Dropped> x, ArrayList<JobClass> y, Hashtable<String, ArrayList<String>> inP){
		workers = worker;
		jobs = job;
		dropped = x;
		completed = y;
		inProgress = inP;

	}

	public void run()
	{
		try{

		final String magicNumber = "15540";
		DatagramSocket listenSocket = new DatagramSocket();
		listenSocket.setSoTimeout(1000);
		JobClass currentJob = new JobClass();
		WorkerClass currentWorker;
		listenSocket = new DatagramSocket();
		while (true)
		{
			if (workers.isEmpty())
				continue;
			// System.out.println("OuterLoop");
			int jobSize;
			if (!jobs.isEmpty() || !dropped.isEmpty()){
			// Trying to find the first job in the Vector that is not running
			// if job is not found the code doesn't enter the while loop

				Enumeration<JobClass> iter = jobs.elements();
				boolean found = false;
				while(iter.hasMoreElements())
				{
					currentJob = iter.nextElement();
					if (!currentJob.rangeEnd.toString().equals("99999")){
						System.out.println("currentJob: " + currentJob.hash);
						currentJob.running = true;		
						found = true;
						break;
					}
				}
			

				// currentJob contains the job that should be worked on.

				while (!currentJob.rangeEnd.equals("99999") || !dropped.isEmpty())
				{

					// System.out.println("InnerLoop");
					if(!dropped.isEmpty())	
					{
						Dropped drop = dropped.get(0);
						Iterator<WorkerClass> it = workers.iterator();
						while(it.hasNext()){
							currentWorker = it.next();
							if (!currentWorker.working){
									currentWorker.hash = drop.hash;
									String rangeStart = drop.rangeStart;
									String rangeEnd = drop.rangeEnd;
									currentWorker.rangeStart = rangeStart;
									currentWorker.rangeEnd = rangeEnd;
									currentWorker.working = true;
									currentWorker.done = false;
									String message = magicNumber + '/' + "JOB/" + rangeStart + '/' + rangeEnd + '/' + drop.hash + '/' ;
									byte[] sendData = new byte[1024];
									sendData = message.getBytes();
									InetAddress ipWorker = InetAddress.getByName(currentWorker.ip);
									DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ipWorker, currentWorker.port);
									listenSocket.send(sendPacket);
									// Wait for ACK_JOB from the Worker
									byte[] receiveData = new byte[1024];
									DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
									try{
									listenSocket.receive(receivePacket);
									} catch (SocketTimeoutException e)
									{
										continue;
									}
									String fromWorker = new String (receivePacket.getData());
									StringTokenizer st = new StringTokenizer(fromWorker, "/");
									message = st.nextToken();
									if (message.equals("ACK_JOB"))
										{
											dropped.remove(0);
											System.out.println("=============================================");
											System.out.println("Job Handed out");
											System.out.println(rangeStart);
											System.out.println(rangeEnd);
											System.out.println("=============================================");
											// Add the job inProgress HashTable
											String value = rangeStart +  ':' + rangeEnd;
											String hash = currentJob.hash;
											ArrayList<String> rangeList = new ArrayList<String>();
											if (inProgress.containsKey(hash)){
												rangeList = inProgress.get(hash);
												rangeList.add(value);
												inProgress.put(hash, rangeList);
											}
											else{
												rangeList.add(value);
												inProgress.put(hash, rangeList);
											}																					
										}

								}
						}
					}
					

					else if(found)
					{

					if (currentJob.completed)
					{
						System.out.println("Job removed from Vector");
						Thread.sleep(500);
						// jobs.removeElement(currentJob);
						break;
					}

					Iterator<WorkerClass> it = workers.iterator();
					while (it.hasNext()){
						currentWorker = it.next();
						if (!currentWorker.working){
							jobSize = currentWorker.speed*10;
							
							if (!currentJob.workers.contains(currentWorker)){
								currentJob.workers.add(currentWorker);
							}
							
							currentWorker.hash = currentJob.hash;
							String rangeStart = currentJob.rangeEnd;
							String rangeEnd = getRange(currentJob.rangeEnd.toCharArray(), jobSize);
							currentWorker.rangeStart = rangeStart;
							currentWorker.rangeEnd = rangeEnd;
							currentWorker.working = true;
							currentWorker.done = false;
							String message = magicNumber + '/' + "JOB/" + rangeStart + '/' + rangeEnd + '/' + currentJob.hash + '/' ;
							byte[] sendData = new byte[1024];
							sendData = message.getBytes();
							InetAddress ipWorker = InetAddress.getByName(currentWorker.ip);
							DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ipWorker, currentWorker.port);
							listenSocket.send(sendPacket);

							// Wait for ACK_JOB from the Worker
							byte[] receiveData = new byte[1024];
							DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
							try{
							listenSocket.receive(receivePacket);
							} catch (SocketTimeoutException e)
							{
								continue;
							}
							String fromWorker = new String(receivePacket.getData());
							StringTokenizer st = new StringTokenizer(fromWorker, "/");
							message = st.nextToken();

							if (message.equals("ACK_JOB"))
							{
								currentJob.rangeEnd = rangeEnd;
								System.out.println("=============================================");
								System.out.println("Job Handed out");
								System.out.println("Speed: " + currentWorker.speed);
								System.out.println(rangeStart);
								System.out.println(rangeEnd);
								System.out.println("=============================================");

								// Add the job inProgress HashTable
								String value = rangeStart +  ':' + rangeEnd;
								String hash = currentJob.hash;
								ArrayList<String> rangeList = new ArrayList<String>();
								if (inProgress.containsKey(hash)){
									rangeList = inProgress.get(hash);
									rangeList.add(value);
									inProgress.put(hash, rangeList);
								}
								else{
									rangeList.add(value);
									inProgress.put(hash, rangeList);
								}


								// Check if all of the job has been assigned and add it to the completed ArrayList of jobs
								if (rangeEnd.equals("99999")){
									completed.add(currentJob);
									currentJob.completed = true;								
								}

							}
						}
						Thread.sleep(50);
					}
				}
			}

		}	
	}
} catch (Exception e) {e.printStackTrace();}

}

// Given a start range, this function calculates the ending range containing num permutations
// returns the 99999 if overflows
	public static String getRange(char[] startRange, int num)
	{
		num = num - 1;
		String rangeEnd = "99999";
		char[] arr = new char[]{'a', 'b', 'c', 'd', 'e', 'f','g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'
								, 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
								, '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};		
								
		int a, b, c, d, e;
		a = b = c = d = e = 0;

		a = num/14776336;		
		num = num - a*14776336;
		a += indexOf(arr, startRange[0]);

		b = num/238328;
		num = num - b*238328;
		b += indexOf(arr, startRange[1]);		

		c = num/3844;
		num = num - c*3844;
		c += indexOf(arr, startRange[2]);

		d = num/62;
		num = num - d*62;
		d += indexOf(arr, startRange[3]);

		e = num;
		num = num - e;
		e += indexOf(arr, startRange[4]);

		if (e > 61){
			d += e/62;
			e = e % 62;
		}
		if (d > 61){
			c += d/62;
			d = d % 62;
		}
		if (c > 61){
			b += c/62;
			c = c % 62;
		}
		if (b > 61){
			a += b/62;
			b = b % 62;
		}

		if (a > 61)
			return rangeEnd;				

		rangeEnd = "" + arr[a] + arr[b] + arr[c] + arr[d] + arr[e];
		return rangeEnd;
		
	}


	public static int indexOf(char[] arr, char a)
	{
		int len = arr.length;
		for (int i = 0; i < len; ++i)
		{
			if (arr[i] == a)
				return i;
		}
		return -1;
	}	






		

}