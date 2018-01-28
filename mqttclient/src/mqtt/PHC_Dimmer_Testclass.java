package mqtt;
import phc.jgateway.com2phc;

public class PHC_Dimmer_Testclass {
	

		final static int info		 = 	1;
		final static int einschalten = 	2;
		final static int ausschalten = 	3;
		final static int umschalten = 	6;
		
		final static int dim_einschalten 	= 12;
		final static int dim_ausschalten 	= 4;
		final static int dim_umschalten 	= 11;
		final static int dim_info 			= 01;
		
		final static int amd0 = 	(byte)0x40;
		final static int dim0 = 	(byte)0xA0;

		byte[]	testb = new byte[20];
		
		
	public PHC_Dimmer_Testclass() {
	}

	public static void main(String[] args) {
			new PHC_Dimmer_Testclass().doDemo();
	}

	public void doDemo() {
		String topic        = "phc/dim/2/1";
		String outmessage ="1";
		
		String[] topicString = topic.split("/");
		
	System.out.println ("demo Starts here");
	System.out.println (topicString[1]);
	System.out.println (topicString[2]);
	System.out.println (topicString[3]);

	

			
	    int action 	= 0;
	    int amdNr 	= 0;
	    int Chan 	= 0;
	    
	    amdNr = Integer.parseInt(topicString[2]);
		Chan = Integer.parseInt(topicString[3]);
	   
		switch (topicString[1]) {

			case "amd":
				if (topicString.length == 4) {
														
					if (outmessage.equals("0")) action = ausschalten;
					if (outmessage.equals("1")) action = einschalten;
					if (outmessage.equals("2")) action = umschalten;
					if (outmessage.equals("3")) action = info;
									
					amdNr = amd0 + amdNr;
					
					System.out.println (action);
					
					testb = com2phc.WriteAMDChannel(amdNr , action, Chan);
					com2phc.PrintInOut (testb);
					if (outmessage.equals("3")) {
						System.out.println (String.format("%02X",testb[testb.length-12])); 
					}
				}
			
			case "dim":
				
				amdNr = dim0 + amdNr; 
				
				if (outmessage.equals("0")) action = dim_ausschalten;
				if (outmessage.equals("1")) action = dim_einschalten;
				if (outmessage.equals("2")) action = dim_umschalten;
				if (outmessage.equals("3")) action = dim_info;
				
				testb = com2phc.WriteAMDChannel(amdNr , action, Chan);
				com2phc.PrintInOut (testb);
				if (outmessage.equals("3")) {
					System.out.println (String.format("%02X",testb[testb.length-12])); 
				}
				
				
			break;
		}
			
	}
}
