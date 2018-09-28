package phc.jgateway;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class com2phc {
	
	private static final int AMD_RETURN_POSITION = 12;

	public static byte[] SendtoPHCMaster(byte[] data) throws IOException{
	       
    	byte[] sb;
    	byte[] sbin = new byte[20];
     	
    	
    	sb = Convert2PHC(data);
    	/* PrintInOut (sb); */
    	
    	Socket socket = null;
        PrintWriter out = null;
        BufferedReader in = null;
 
        try {
            socket = new Socket("10.0.0.101", 4000);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host");
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection");
            System.exit(1);
        }
 
        BufferedReader read = new BufferedReader(new InputStreamReader(System.in));
          
        socket.getOutputStream().write(sb);
        socket.getOutputStream().flush();
        try {
        
        socket.setSoTimeout(500);
        socket.getInputStream().read(sbin);
        } catch (IOException e) {
            System.err.println("No valid response ");
            /*System.exit(1);*/
            out.close();
            in.close();
            read.close();
            socket.close();
       
    		return sbin;
            
            
        }

        out.close();
        in.close();
        read.close();
        socket.close();
        return sbin;
    }
	
	static public byte[] Convert2PHC(byte[] data){
        
	    byte[] PHCBuf = data;
	    byte[] CRCBuf = new byte[7];
	    byte[] OutBuf = new byte[20];
	    int Checksumme;
	    int LSB;
	    int MSB;
	    
		OutBuf[0] = (byte) 0xc0; // Frame Start	
		OutBuf[1] = (byte) 0xfe;  // from RS232
		OutBuf[2] = (byte) 0x00;  // to STM0  		
		OutBuf[3] = (byte) 0x06;  // opcode: PHC packet
		OutBuf[4] = (byte) 0x00;  // sequence #
		
		OutBuf[5] = PHCBuf[0]; //
		OutBuf[6] = PHCBuf[1];
		OutBuf[7] = PHCBuf[2];
		
		for (int i = 0; i <7; i++) {
			CRCBuf[i] = OutBuf[i+1];
		};
		
		Checksumme = CRC16(CRCBuf);
		
		LSB = (Checksumme & 0xFF);
		MSB = ((Checksumme & 0xFF00) >>> 8);
		
		OutBuf[8] = (byte) LSB;         // Least significant "byte"
		OutBuf[9] = (byte) MSB;  		// Most significant "byte"
		OutBuf[10] = (byte) 0xc1; 		// Frame Start	
	
		byte[] OutBuf_new = OutBuf;
		int sizenew_local = padbuffer(OutBuf,OutBuf_new,11);
	
	    return OutBuf_new;
	    }

	   static public int CRC16(byte[] data){
	           
	   
	   int crc = 0xffff;		//Startwert		   
	   int polynomial = 0x8408; //Polynom
	   
	   byte[] bytes = data;

	   for (byte b : bytes) {
	       crc ^= 0x00FF & b;
	       
	       for (int i = 0; i < 8; i++) {
	           if ((crc & 0x0001) != 0) {
	               crc = (crc >>> 1) ^ polynomial;
	           } else {
	               crc = (crc >>> 1);
	           }
	       }
	   }

	  
	   return crc ^= 0xFFFF ;
	   }
	   
	   public static byte[] WriteAMDChannel (int AMD, int phcCmd, int Channel){
	   
	   	
		int combibyte1 = (byte) (phcCmd); // lower 4 bits sind commando
		int combibyte2 = (Channel << 5); // channel sind binär codiert in bit 2-4, daher xxxx 111x  0,2,4,6,a,c,e

		
	   	byte combibyte = (byte) (combibyte1 + combibyte2);
	   	Channel = unsignedToBytes(combibyte);
		   
		byte[] Code1 = {(byte)AMD, (byte)0x01, (byte)Channel};
		byte[] ReturnCode = new byte[20];
		try {
			ReturnCode = SendtoPHCMaster(Code1);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			// this is a change
			e1.printStackTrace();
		}
		return ReturnCode;
	   }
		
	   public static boolean isBitSet(int a, int n){
		    return (a & (1 << n)) != 0;
		}
	
	
	
	public static  void  PrintInOut(byte[] in) {
  	  
    	//System.out.println("INP<:");
    	for (int i = 0; i <in.length; i++)
    	  { System.out.print(String.format("%02X", in[i]));
    	  	System.out.print(" ");
    	  }
    	 System.out.println("");
    } 
	
	public static byte getPHCStatusByte(byte[] in) {
	byte outbyte = 0;
	
	outbyte = in[in.length - AMD_RETURN_POSITION];
	if ((outbyte == (byte) 0x7D) || (outbyte == (byte) 0xC0) || (outbyte == (byte) 0xC1))  {
		outbyte = (byte) (in[in.length - AMD_RETURN_POSITION -1] ^ (byte) 0x20) ;
	}
	
	return outbyte;
	}
	
	public static int unsignedToBytes(byte b) {
		    return b & 0xFF;
		  }
	
	public static int padbuffer(byte[] buf_src,byte[] buf_dst, int size) {
	       
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
	
	
	
}
