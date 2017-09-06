import java.util.*;
public class Perm{
	public static void main (String args[])
	{
		char[] arr = new char[]{'a', 'b', 'c', 'd', 'e', 'f','g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'
								, 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
								, '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

		int max = arr.length;
		char[] rangeStart = new char[] {'9', 'I', 'z', '1', '2'};
		String end = getRange(rangeStart, 50000000);
		System.out.println(end);
		char[] rangeEnd = new char[] {'9', '0', '6', 'Z', 'v'};

		// Find the start positions of the characters in the array (RangeStart)
		int ii = indexOf(arr, rangeStart[0]);
		System.out.print(ii + " ");
		int jj = indexOf(arr, rangeStart[1]);
		System.out.print(jj + " ");

		int kk = indexOf(arr, rangeStart[2]);
		System.out.print(kk + " ");

		int ll = indexOf(arr, rangeStart[3]);
		System.out.print(ll + " ");

		int mm = indexOf(arr, rangeStart[4]);
		System.out.println(mm + " ");

		//Find the ending positions of the characters {rangeEnd}
		int counter = 0;
		int iMax = indexOf(arr, rangeEnd[0]);
		int jMax = indexOf(arr, rangeEnd[1]);
		int kMax = indexOf(arr, rangeEnd[2]);
		int lMax = indexOf(arr, rangeEnd[3]);
		int mMax = indexOf(arr, rangeEnd[4]);
		int x, y, z, a;

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
							if (i == iMax && j == jMax && k == kMax && l == lMax && m > mMax)
								break;
							System.out.println("" + arr[i] + arr[j] + arr[k] + arr[l] + arr[m]);
							counter++;						
						}
					}
				}
			}
		}

		System.out.println(counter);

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





}