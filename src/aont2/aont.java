//Fragment input data to constant / different sizes, encrypt in parallel and then AONT in parallel
package aont2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import javax.crypto.SecretKey;

import aont2.CryptoUtils;
import aont2.aes_gcm;

import java.io.File;
import java.nio.file.Files;


public class aont {
	
	public static int getRandomInt(int min, int max){

	    int x = (int)(Math.random()*((max-min)+1))+min;
	    return x;

	}
	
	// split array into fragments of different sizes
	public List<byte[]> splitArray(byte[] items) {
		  List<byte[]> result = new ArrayList<byte[]>();
		  if (items ==null || items.length == 0) {
		      return result;
		  }

		  int from = 0;
		  int to = 0;
		  int slicedItems = 0;
		  int maxSubArraySize = (items.length)/2;
		  //int maxSubArraySize = 20;
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
	
	// split array into fragments of constant size
	public List<byte[]> splitArrayConstant(byte[] array, int numOfChunks) {
		
		List<byte[]> result = new ArrayList<byte[]>();
		int chunkSize = (int)Math.ceil((double)array.length/numOfChunks);
	    for(int i = 0; i < numOfChunks; ++i) {
	    	int start = i * chunkSize;
	    	int length = Math.min(array.length - start, chunkSize);
	    	byte[] temp = new byte[length];
	    	System.arraycopy(array, start, temp, 0, length);
	    	result.add(temp);
	    }
	    return result;
	}
		

	
	public static void main(String[] args) throws Exception {
		long startTime = System.nanoTime();
		aont aont = new aont();
		String filePath = "/home/yeeman/Documents/2000MB.txt";
		File file = new File(filePath);
		byte[] inputArray = Files.readAllBytes(file.toPath());
		
		// encrypt and decrypt need the same key.
        // get AES 256 bits (32 bytes) key
        SecretKey secretKey = CryptoUtils.getAESKey(256);

        // encrypt and decrypt need the same IV.
        // AES-GCM needs IV 96-bit (12 bytes)
        byte[] iv = CryptoUtils.getRandomNonce(12);
        
        //INTSTREAM METHOD
			
		//Fragmentation of inputArray into 4 fragments of constant size
		List<byte[]> fragment = aont.splitArrayConstant(inputArray,4);
		
		//Encrypt each fragment in parallel
		List<byte[]> encryptedListBytes = new ArrayList<byte[]>();
		
		IntStream.range(0, fragment.size()).parallel().forEach(i->{
			byte encrypt[] = new byte[fragment.get(i).length];
			try {
				encrypt = aes_gcm.encryptWithPrefixIV(fragment.get(i), secretKey, iv);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			encryptedListBytes.add(encrypt);
		}); 
				
		//Apply AONT
		int t[] = {0,0,0,0};
		IntStream.range(0, t.length).parallel().forEach(i->{
			for (int j=0; j<encryptedListBytes.get(i).length; j++) {
				t[i]=t[i]^encryptedListBytes.get(i)[j];
			}
		});
			
		IntStream.range(0, t.length).parallel().forEach(i->{
			for (int j=0; j<encryptedListBytes.get(i).length; j++) {
				encryptedListBytes.get(i)[j] = (byte) (t[i]^encryptedListBytes.get(i)[j]);
			}
		}); 
        

		System.out.println("Length of encryptedListBytes: "+encryptedListBytes.size());
		
		long endTime = System.nanoTime(); 
		System.out.println("Took "+(endTime - startTime) + " ns"); 	
		
		
		
		
	}
}
