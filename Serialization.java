import java.util.*;
import java.net.*;
import java.io.*;
import java.util.concurrent.CopyOnWriteArrayList;

// Java Server <listenPort>
public class Serialization{
	public static void main(String args[]){
		// JobClass job = new JobClass("127.0.0.1", 8888, "Bx907dXnxjbdja9UDASxnXnjd8ud");
		JobClass job = new JobClass();


		try{
			serialize(job, "serialization.txt");

			JobClass newJob = (JobClass) deserialize("serialization.txt");
			System.out.println(newJob.clientIp + newJob.clientPort + newJob.hash);


		} catch (Exception e)
		{
			e.printStackTrace();
		}

	}

public static Object deserialize(String fileName)
	{
		try{
        FileInputStream fis = new FileInputStream(fileName);
        BufferedInputStream bis = new BufferedInputStream(fis);
        ObjectInputStream ois = new ObjectInputStream(bis);
        Object obj = ois.readObject();
        ois.close();
        return obj;
    	} catch(Exception e)
    	{
    		System.out.println("ExceEption in method deserialize");
    		e.printStackTrace();
    		return null;
    	}
    }

        public static void serialize(Object obj, String fileName){
        	try{
        FileOutputStream fos = new FileOutputStream(fileName);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(obj);
        oos.close();
    	} catch (Exception e)
    	{
    		System.out.println("ExceEption in method deserialize");
    		e.printStackTrace();
    	}
    }
}




