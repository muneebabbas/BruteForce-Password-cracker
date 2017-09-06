import java.util.*;
import java.net.*;
import java.io.*;
public class Client{
	public static void main(String args[]){
		try{
		DatagramSocket listenSocket = new DatagramSocket();	
		String password = "CZZZZ";
		String hash = Job.calcHash(password);
		System.out.println(hash);
		InetAddress ipServer = InetAddress.getByName(args[0]);
		int portServer = Integer.parseInt(args[1]);	
		Thread thread = new Thread(new ClientThread(listenSocket, ipServer, portServer, hash));
		thread.start();			
		String message = "JOB/" + hash + '/';
		byte[] sendData = new byte[1024];
		sendData = message.getBytes();
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ipServer, portServer);
		listenSocket.send(sendPacket);
		byte[] receiveData = new byte[1024];
	 	DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
	 	listenSocket.setSoTimeout(5000);
	 	while(true){
	 		try{
		 	listenSocket.receive(receivePacket);	
		 	String received = new String(receivePacket.getData());
		 	System.out.println(received);
		 	break;
		 	}catch (SocketTimeoutException e)
		 	{
		 		message = "PING/" + hash + '/';
		 		sendData = new byte[1024];
		 		sendData = message.getBytes();
		 		sendPacket = new DatagramPacket(sendData, sendData.length, ipServer, portServer);
		 		listenSocket.send(sendPacket);
		 	}		 	 		
		} 
	}catch (Exception e) {e.printStackTrace();}
	}
}