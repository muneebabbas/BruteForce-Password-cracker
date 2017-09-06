import java.net.*;
import java.util.*;

class WorkerThread implements Runnable{
	public Job currentJob;
	public DatagramSocket listenSocket;
	public InetAddress ipServer;
	public int portServer;
	public String id;
	public WorkerThread(Job jobs, InetAddress ip, int port, String ids){
		currentJob = jobs;
		ipServer = ip;
		portServer = port;
		id = ids;
		try{
		listenSocket = new DatagramSocket();
		listenSocket.setSoTimeout(500);
		}catch (Exception e) {e.printStackTrace();}
	}

	public void run()
	{

		String start = new String(currentJob.rangeStart);
		String end = new String(currentJob.rangeEnd);		
		String range = start + ':' + end;
		currentJob.running = true;
		long startTime = System.nanoTime();
		long estimatedTime;
		int max = currentJob.arr.length;
		// Find the start positions of the characters in the array (RangeStart)
		int ii = indexOf(currentJob.arr, currentJob.rangeStart[0]);
		int jj = indexOf(currentJob.arr, currentJob.rangeStart[1]);
		int kk = indexOf(currentJob.arr, currentJob.rangeStart[2]);
		int ll = indexOf(currentJob.arr, currentJob.rangeStart[3]);
		int mm = indexOf(currentJob.arr, currentJob.rangeStart[4]);

		//Find the ending positions of the characters {rangeEnd}
		int counter = 0;
		int iMax = indexOf(currentJob.arr, currentJob.rangeEnd[0]);
		int jMax = indexOf(currentJob.arr, currentJob.rangeEnd[1]);
		int kMax = indexOf(currentJob.arr, currentJob.rangeEnd[2]);
		int lMax = indexOf(currentJob.arr, currentJob.rangeEnd[3]);
		int mMax = indexOf(currentJob.arr, currentJob.rangeEnd[4]);
		int x, y, z, a;
		String password;
		String passHash;

		for (int i = ii; i <= iMax; i++)
		{
			x = (i == ii) ? jj : 0;
//=================================================================================================			
			for (int j = x; j < max; j++)
			{
				if (i == iMax && j > jMax)
					break;
				y = (i == ii && j == jj) ? kk : 0;
//=================================================================================================			
				for (int k = y; k < max; k++)
				{
					if (i == iMax && j == jMax && k > kMax)
						break;
					z = (i == ii && j == jj && k == kk)? ll : 0;
//=================================================================================================			
					for (int l = z; l < max; l++)
					{
						if (i == iMax && j == jMax && k == kMax && l > lMax)
							break;
					a = (i == ii && j == jj && k == kk && l == ll)? mm : 0;
//=================================================================================================			
						for (int m = a; m < max; m++)
						{
							if (i == iMax && j == jMax && k == kMax && l == lMax && m > mMax){
								break;
							}

							counter++;

							estimatedTime = System.nanoTime() - startTime;
							if (estimatedTime >= 1000000000)
							{
								currentJob.speed = counter;
								startTime = System.nanoTime();
								counter = 0;
							}

							if (!currentJob.running){
								System.out.println("Thread Stopped");
								return;
							}

							password = "" + currentJob.arr[i] + currentJob.arr[j] + currentJob.arr[k] + currentJob.arr[l] + currentJob.arr[m];
							passHash = Job.calcHash(password);
							// calculate hash of permutation and compare
							if (passHash.equals(currentJob.hash))
							{

									System.out.println("Password Found --> " + password);
									while(true){
										try{
										String message = "DONE_FOUND/" + passHash + '/' + password + '/' + id + '/' + range + '/';
										byte[] sendData = new byte[1024];
										sendData = message.getBytes();
										DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ipServer, portServer);
										listenSocket.send(sendPacket);
										byte[] receiveData = new byte[1024];
										DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
										listenSocket.receive(receivePacket);
										String fromServer = new String(receivePacket.getData());
										StringTokenizer st = new StringTokenizer(fromServer, "/");
										fromServer = st.nextToken();
										if (fromServer.equals("ACK_FOUND"))
											break;
										
									} catch(SocketTimeoutException e){continue;}
									catch (Exception e) {e.printStackTrace();}

									}

									currentJob.running = false;
									currentJob.found = true;									
									return;
							}
							else
							{
								currentJob.completed = password;
							}


						}
					}
				}
			}
		}

		System.out.println("Password not found");
		while(true)
		{
			try{
			String message = "DONE_NOT_FOUND/" + currentJob.hash + '/' + id + '/' + range + '/';
			// System.out.println("id: " + id);
			byte[] sendData = new byte[1024];
			sendData = message.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ipServer, portServer);
			listenSocket.send(sendPacket);
			byte[] receiveData = new byte[1024];
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			listenSocket.receive(receivePacket);
			System.out.println("Packet Received");
			String fromServer = new String(receivePacket.getData());
			StringTokenizer st = new StringTokenizer(fromServer, "/");
			fromServer = st.nextToken();
			System.out.println("fromServer: " + fromServer);
			if (fromServer.equals("ACK_NOT_FOUND"))
				break;
			} catch (SocketTimeoutException e) {continue;} 
			catch (Exception e) {e.printStackTrace();}


		}

		currentJob.running = false;
		currentJob.found = false;


	}


// Finding the index of a char in the array
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