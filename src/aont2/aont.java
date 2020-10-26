//Fragment input data to different sizes, encrypt in parallel and then AONT in parallel
package aont2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import javax.crypto.SecretKey;

import aont2.CryptoUtils;
import aont2.aes_gcm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

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
	
	public static void main(String[] args) throws Exception {
		long startTime = System.nanoTime();
		aont aont = new aont();
		String filePath = "/home/yeeman/Documents/100MB.txt";
		File file = new File(filePath);
		//byte[] inputArray = Files.readAllBytes(file.toPath());
		FileInputStream is = new FileInputStream(file);
		FileOutputStream os = new FileOutputStream(new File("output_aont2.txt"));
		byte[] inputArray = new byte[4096];
		int read = 0;
		while((read = is.read(inputArray)) > 0) {
			// encrypt and decrypt need the same key.
	        // get AES 256 bits (32 bytes) key
	        SecretKey secretKey = CryptoUtils.getAESKey(256);

	        // encrypt and decrypt need the same IV.
	        // AES-GCM needs IV 96-bit (12 bytes)
	        byte[] iv = CryptoUtils.getRandomNonce(12);
			
			//Fragmentation of inputArray into fragments of different sizes
			List<byte[]> fragment = aont.splitArray(inputArray);
			
			//Encrypt each fragment in parallel
			List<byte[]> encryptedListBytes = new ArrayList<byte[]>();
			
			
			for (int i=0; i<fragment.size(); i++) {
				byte encrypt[] = new byte[fragment.get(i).length];
				encrypt = aes_gcm.encryptWithPrefixIV(fragment.get(i), secretKey, iv);
				encryptedListBytes.add(encrypt);
			}
			
			
			int t[] = new int[fragment.size()];
			IntStream.range(0, t.length).parallel().forEach(i->{
				t[i]=0;
			});
			
			
			IntStream.range(0, t.length).parallel().forEach(i->{
				IntStream.range(0, encryptedListBytes.get(i).length).parallel().forEach(j->{
					t[i]=t[i]^encryptedListBytes.get(i)[j];
				});
			});
			
			IntStream.range(0, t.length).parallel().forEach(i->{
				IntStream.range(0, encryptedListBytes.get(i).length ).parallel().forEach(j->{
					encryptedListBytes.get(i)[j] = (byte) (t[i]^encryptedListBytes.get(i)[j]);
				});
			});
			for (int i=0; i<encryptedListBytes.size(); i++) {
				for (int j=0; j<encryptedListBytes.get(i).length; j++) {
					os.write(encryptedListBytes.get(i)[j]);
				}
			}
		}
		is.close();
		os.close();
		
		
		
		
		
	/*	System.out.println("encryptedListBytes after AONT:");
		for (int i=0; i<encryptedListBytes.size(); i++) {
			System.out.print("[");
			for (int j=0; j<encryptedListBytes.get(i).length; j++) {
				if (j % 100 == 0 && j > 0) {
		           System.out.println();
		        } 
				System.out.print(encryptedListBytes.get(i)[j]);
				if (j!=encryptedListBytes.get(i).length-1) {
					System.out.print(", ");
				}
				else {
					System.out.print("]");
					System.out.println();
					System.out.println();
				}	
			}	
		} */
		//System.out.println("Length of encryptedListBytes: "+encryptedListBytes.size());		
		long endTime = System.nanoTime();
		System.out.println("Took "+(endTime - startTime) + " ns"); 
		
		
		
	}
}
