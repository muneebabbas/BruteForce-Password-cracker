public class Test{
	public static void main(String args[])  { 

		TestClass myClass = new TestClass(5);		
		increment(myClass.x);
		System.out.println(myClass.x);
	}

	public static void increment(int lass)
	{
		lass = lass + 1;
		System.out.println("From function -->" + lass);
	}
}