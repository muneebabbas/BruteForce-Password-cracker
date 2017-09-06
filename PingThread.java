// This thread is used to ping the workers and get the progress of work
// If a worker doesn't respond within time, the remaining part of his job is handed to another worker
import java.net.*;
import java.util.*;
import java.io.*;
import java.util.concurrent.CopyOnWriteArrayList;


class PingThread implements Runnable{
	public CopyOnWriteArrayList<WorkerClass> workers;	
	public CopyOnWriteArrayList<Dropped> dropped;
	public PingThread(CopyOnWriteArrayList<WorkerClass> worker, CopyOnWriteArrayList<Dropped> x){
		workers = worker;
		dropped = x;
	}

	public void run(){

		try{
		final String magicNumber = "15540";
		byte[] sendData = new byte[1024];
		String message;
		String receive;
		DatagramPacket sendPacket;
		DatagramPacket receivePacket;
		byte[] receiveData = new byte[1024];
		DatagramSocket listenSocket;
		listenSocket = new DatagramSocket();
		listenSocket.setSoTimeout(1000);
		
		while (true)
		{
			Iterator<WorkerClass> it = workers.iterator();
			WorkerClass worker;
			while(it.hasNext())
			{
				worker = it.next();
				message = magicNumber + '/' + "PING" + '/';
				sendData = message.getBytes();
				// System.out.println(worker.ip);
				// System.out.println(worker.port);
				InetAddress ipWorker = InetAddress.getByName(worker.ip);
				sendPacket = new DatagramPacket(sendData, sendData.length, ipWorker, worker.port);
				listenSocket.send(sendPacket);
				receivePacket = new DatagramPacket(receiveData, receiveData.length);
				try{
				listenSocket.receive(receivePacket);
				} catch (SocketTimeoutException e)
				{
					if (worker.done == false)
					{
						// Dropped newDropped = new Dropped(worker.rangeCompleted, worker.rangeEnd, worker.hash);
						System.out.println("************ Worker Dropped ****************");
						StringTokenizer token = new StringTokenizer(worker.message, "/");
						String command = token.nextToken();
						message = token.nextToken();
						String rangeStart = token.nextToken();
						String rangeEnd = token.nextToken();
						String hash = token.nextToken();
						System.out.println(rangeStart + " : " + rangeEnd + " " + hash);
						Dropped newDropped = new Dropped(rangeStart, rangeEnd, hash);
						dropped.add(newDropped);						
						if (command.equals("2"))
						{
							rangeStart = token.nextToken();
							rangeEnd = token.nextToken();
							hash = token.nextToken();
							System.out.println(rangeStart + " : " + rangeEnd + " " + hash);						
							newDropped = new Dropped(rangeStart, rangeEnd, hash);
							dropped.add(newDropped);
						}
						System.out.println("************ Worker Dropped ****************");
						dropped.add(newDropped);
					}
					
					workers.remove(worker);
					continue;

				}	

				receive = new String(receivePacket.getData());
				StringTokenizer st = new StringTokenizer(receive, "/");
				message = st.nextToken();

				if (message.equals("NO_JOB"))
				{
					// System.out.println("NO_JOB");
					worker.done = true;
					worker.working = false;
					continue;
				}				
				else
				{
					// one job in progress and queue is empty
					if (message.equals("1"))
					{
						System.out.println("One job in progress: job queue empty");
						worker.working = false;
					}
					// one job in progress and one job queued
					else if (message.equals("2"))
					{
						System.out.println("One job in progress: one job queued");
					}
					int speed = Integer.parseInt(st.nextToken());
					if (speed == 0)
						worker.speed = worker.speed + 100000;
					else
						worker.speed = speed;

					worker.message = new String(receivePacket.getData());

				}
			}
			Thread.sleep(1000);
		}
	
	} catch (Exception e) {e.printStackTrace(); return;}

	}

}










