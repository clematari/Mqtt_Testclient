package mqtt;


import java.io.IOException;
import phc.jgateway.com2phc;



public class Paddingtest {
	
	
	public static int padbuffer(byte[] buf_src,byte[] buf_dst, int size) throws IOException{
	       
    	int oldpos;
    	int newsize = size;
    	
	  for (int i=1; i<size-1; i++) // check only between start and stop byte
	    {
	        if (buf_src[i] == 0x7D || buf_src[i] == 0xC0 || buf_src[i] == 0xC1)
	        {
	            newsize++;
	        }
	    }
	  if (size == newsize) // no padding necessary?
	    {
	        return newsize; // just leave it
	    }
	    
	    // second pass: go backwards and do the padding.
	    //  this way no extra work buffer is needed

	    buf_dst[0] = buf_src[0]; // copy first byte, should be 0xC0
	    buf_dst[newsize-1] = buf_src[size-1]; // copy last byte, should be 0xC1
	    oldpos = size - 2; // start in front of that last byte
	    for (int i=newsize-2; i>1; i--)
	    {
	        if (buf_src[oldpos] == 0x7D || buf_src[oldpos] == 0xC0 || buf_src[oldpos] == 0xC1)
	        {
	            buf_dst[i] = (byte) (buf_src[oldpos] ^ (byte) 0x20);
	            i--;
	            buf_dst[i] = 0x7D;
	        }
	        else
	        {
	            buf_dst[i] = buf_src[oldpos];
	        }
	        oldpos--;
	    }
	    // now, oldpos should be 1

	    size = newsize;

	    return newsize; // OK
}
	
	
	public static void main(String[] args) throws IOException {
		byte[] src = {(byte) 0xC0, (byte) 0xFE, -32 , (byte) 0x06, (byte) 0x00, (byte) 0x41, (byte) 0x7D, (byte) 0x01, (byte) 0x63, (byte) 0xF2, (byte) 0xC1};
		byte[] dest = new byte [20];
		int sizeold;
		int sizenew;
		
		sizeold = 11;
		sizenew= sizeold;
		
		

		com2phc.PrintInOut (src);
	    System.out.println ("oldsize:"+sizeold);
	    sizenew = padbuffer(src,dest,sizeold);
		com2phc.PrintInOut (dest);
		System.out.println ("newsize:"+sizenew);
		
		dest[19] = -32 ^ (byte) 0x20;
		System.out.println ("newsize:"+sizenew);
		
		
		
		
		
				
	
}

}
