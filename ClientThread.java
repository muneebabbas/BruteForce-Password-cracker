// This thread is used to divide the job into pieces and hand them over to the workers
import java.net.*;
import java.util.*;
import java.io.*;

public class ClientThread implements Runnable{
	DatagramSocket listenSocket;
	InetAddress ipServer;
	int portServer;
	String hash;
	public ClientThread(DatagramSocket listenSockets, InetAddress x, int y, String z){
		listenSocket = listenSockets;
		ipServer = x;
		portServer = y;
		hash = z;
	}

	public void run()
	{
		try{
		System.out.println("Enter anything to cancel the job");
		Scanner userInput = new Scanner(System.in);
		String response = userInput.next();
		String message = "CANCEL_JOB/" + hash + '/';
		byte[] sendData = new byte[1024];
		sendData = message.getBytes();
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ipServer, portServer);
		listenSocket.send(sendPacket);
	} catch (Exception e) {e.printStackTrace();}


	}
}