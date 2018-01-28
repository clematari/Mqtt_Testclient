package mqtt;
//import phc.jgateway.*; 
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import phc.jgateway.com2phc;

public class PahoDemo1 implements MqttCallback {

	final static int info		 = 	1;
	final static int einschalten = 	2;
	final static int ausschalten = 	3;
	final static int umschalten = 	6;
	
	final static int dim_einschalten 	= 12;
	final static int dim_ausschalten 	= 4;
	final static int dim_umschalten 	= 11;
	final static int dim_info 			= 1;
	
	final static int amd0 = 	(byte)0x40;
	final static int dim0 = 	(byte)0xA0;
	
	byte[]	testb = new byte[20];
	
	MqttClient client;

public PahoDemo1() {
}

public static void main(String[] args) {
		new PahoDemo1().doDemo();
}

public void doDemo() {
	String topic = "phc/";
	
	
	try {
        client = new MqttClient("tcp://10.0.0.103:1883",topic);
        client.connect();
        System.out.println (MqttClient.generateClientId());
        client.setCallback(this);
        client.subscribe(topic + "#");
        MqttMessage message = new MqttMessage();
        message.setPayload("jPHCready".getBytes());
        /*client.publish(topic, message); */
        System.out.println ("after publish");
        
        topic = "phc/amd/";
        
    } catch (MqttException e) {
        e.printStackTrace();
    }
}

public void connectionLost(Throwable cause) {
    // TODO Auto-generated method stub
	System.out.println ("Connection lost");
}

public void messageArrived(String topic, MqttMessage message)
        throws Exception {
	String outmessage = message.toString();
	System.out.println(topic + "-" + outmessage);
	
	String[] topicString = topic.split("/");
	
   /* int size = topicString.length;
    for (int i=0; i<size; i++)
    {
    	System.out.println (i+topicString[i]);
    }
	*/	
    int action 	= 0;
    int amdNr 	= 0;
    int Chan 	= 0;
    
    System.out.println(topicString[1]);
   
	switch (topicString[1]) {

		case "amd":{
			
				amdNr = Integer.parseInt(topicString[2]);
				Chan = Integer.parseInt(topicString[3]);
				
				amdNr = amd0 + amdNr;
				
								
				if (outmessage.equals("0")) action = ausschalten;
				if (outmessage.equals("1")) action = einschalten;
				if (outmessage.equals("2")) action = umschalten;
				if (outmessage.equals("3")) action = info;
				
				
				
				
				testb = com2phc.WriteAMDChannel(amdNr , action, Chan);
				com2phc.PrintInOut (testb);
				if (outmessage.equals("3")) {
					System.out.println (String.format("%02X",testb[testb.length-12])); 
				}
					}
		break;	
		
		case "dim":{
			
			amdNr = Integer.parseInt(topicString[2]);
			Chan = Integer.parseInt(topicString[3]);
			
			amdNr = amdNr + dim0; 
			
			if (outmessage.equals("0")) action = dim_ausschalten;
			if (outmessage.equals("1")) action = dim_einschalten;
			if (outmessage.equals("2")) action = dim_umschalten;
			if (outmessage.equals("3")) action = dim_info;
			
			 testb = com2phc.WriteAMDChannel(amdNr , action, Chan);
			
			com2phc.PrintInOut (testb);
				if (outmessage.equals("3")) {
				System.out.println (String.format("%02X",testb[testb.length-12])); 
				}	
			
			
		}
		break;
	}
			
		
	}

    
 
public void deliveryComplete(IMqttDeliveryToken token) {
    // TODO Auto-generated method stub

} 

}