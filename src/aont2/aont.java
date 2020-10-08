//Fragment input data to different sizes, encrypt in parallel and then AONT in parallel
package aont2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class aont {

	public static int getRandomInt(int min, int max){

	    int x = (int)(Math.random()*((max-min)+1))+min;
	    return x;

	}
	
	public List<byte[]> splitArray(byte[] items) {
		  List<byte[]> result = new ArrayList<byte[]>();
		  if (items ==null || items.length == 0) {
		      return result;
		  }

		  int from = 0;
		  int to = 0;
		  int slicedItems = 0;
		  int maxSubArraySize = (items.length)/2;
		  while (slicedItems < items.length){
			  int size = getRandomInt(1,maxSubArraySize);
			  to = from + size;
			  byte[] slice = Arrays.copyOfRange(items, from, to);
			  result.add(slice);
			  slicedItems += slice.length;
			  from = to;
			  if (items.length -slicedItems <= maxSubArraySize)
		    	  maxSubArraySize = items.length - slicedItems ;
		  }
		  return result;
	}
	
	public static void main(String[] args) {
		aont aont = new aont();
		String originalString = "Hello";
		final String secretKey = "ssshhhhhhhhhhh!!!!";
		byte[] inputArray = originalString.getBytes();
		
		//Fragmentation of inputArray into fragments of different sizes
		List<byte[]> fragment = aont.splitArray(inputArray);
		for(int i = 0; i < fragment.size(); i++) {
			System.out.println(Arrays.toString(fragment.get(i)));
		}
		
		//Encrypt each fragment in parallel
		String [] encryptedArray = new String [fragment.size()];
		IntStream.range(0, fragment.size()).parallel().forEach(i->{
			encryptedArray[i]=aes.encrypt(Arrays.toString(fragment.get(i)), secretKey);
		});

		System.out.println("encryptedArray:");
		System.out.println(Arrays.toString(encryptedArray));	
		
		//Apply Bastion AONT to each encrypted fragment in parallel
		List<byte[]> encryptedListBytes = new ArrayList<byte[]>();
		for (int i=0; i<encryptedArray.length; i++) {
			encryptedListBytes.add(encryptedArray[i].getBytes());
		}
		
		System.out.println("encryptedListBytes:");
		for(int i = 0; i < encryptedListBytes.size(); i++) {
			System.out.println(Arrays.toString(encryptedListBytes.get(i)));
		}
		
		
		int t[] = new int[fragment.size()];
		IntStream.range(0, t.length).parallel().forEach(i->{
			t[i]=0;
		});
		
		for (int i=0; i<t.length; i++) {
			for (int j=0; j<encryptedListBytes.get(i).length; j++) {
				t[i]=t[i]^encryptedListBytes.get(i)[j];
			}
		}

		
		IntStream.range(0, t.length).parallel().forEach(i->{
			IntStream.range(0, encryptedListBytes.get(i).length ).parallel().forEach(j->{
				encryptedListBytes.get(i)[j] = (byte) (t[i]^encryptedListBytes.get(i)[j]);
			});
		});
		
		System.out.println("encryptedListBytes after AONT:");
		for(int i = 0; i < encryptedListBytes.size(); i++) {
			System.out.println(Arrays.toString(encryptedListBytes.get(i)));
		}
		
		
		
	}
}
