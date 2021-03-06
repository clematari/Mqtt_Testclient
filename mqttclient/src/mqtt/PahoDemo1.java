package mqtt;
//import phc.jgateway.*; 
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.nio.charset.StandardCharsets;
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
	final static int dim_setze_wert		= 22;
	
	final static int amd0 = 	(byte)0x40;
	final static int dim0 = 	(byte)0xA0;
	final static int emd0=		(byte)0x00;
	
	final static String Mqtt_adr = "tcp://10.0.0.143:1883";
	final static String Mqtt_user = "openhabian";
	final static String Mqtt_pwd = "3313";
	
	byte[]	testb = new byte[32];
	int sizeold = 11;
	int sizenew = 11;
	
	MqttClient client;
	

public PahoDemo1() {
}

public static void main(String[] args) {
		new PahoDemo1().doDemo();
}

public void doDemo() {
	String topic = "phc/";

	
	
	try {
        client = new MqttClient(Mqtt_adr,topic);
	    MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setUserName(Mqtt_user);
        connOpts.setPassword(Mqtt_pwd.toCharArray());
        connOpts.setCleanSession(true);
        client.connect(connOpts);
        System.out.println (MqttClient.generateClientId());
        client.setCallback(this);
        client.subscribe(topic + "#");
        MqttMessage message = new MqttMessage();
        message.setPayload("jPHCready".getBytes());
        System.out.println ("before client2");
        

        
    } catch (MqttException e) {
        e.printStackTrace();
    }
}

public void sendStatusAnswer(String topic2, byte[] outb) {

	MqttClient client2;



	try {
	    client2 = new MqttClient(Mqtt_adr,topic2);
	    MqttConnectOptions connOpts2 = new MqttConnectOptions();
        connOpts2.setUserName(Mqtt_user);
        connOpts2.setPassword(Mqtt_pwd.toCharArray());
        connOpts2.setCleanSession(true);
        client2.connect(connOpts2);
	    
	    MqttMessage message2 = new MqttMessage();
	    message2.setPayload(outb);
	    client2.publish(topic2, message2);
	    client2.disconnect();
	  
    
		} catch (MqttException e) {
			e.printStackTrace();
		}
}

public static boolean checkBit(byte x, int k) {
    return (x & 1 << k) != 0;
}

public void connectionLost(Throwable cause) {
    // TODO Auto-generated method stub
	System.out.println ("Connection lost");
}

public static String byteToInt(byte b) {
    int i = b & 0xFF;
    return Integer.toString(i);
}

public void messageArrived(String topic, MqttMessage message)
        throws Exception {
	String outmessage = message.toString();
	System.out.println("arrived:" + topic + " payload:" + outmessage);
	
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
    byte Value	= 0;
    byte Cmdlen	= 0x01;
    
    byte[] PHCStatusByte = new byte[1];
    String  topic_answer="";
    byte PHCByte = 0;
    String PHCStatusStr="";
    
   /* System.out.println(topicString[1]); */
   
	switch (topicString[1]) {

		case "amd":{
			
				amdNr = Integer.parseInt(topicString[2]);
				Chan = Integer.parseInt(topicString[3]);
				
				amdNr = amd0 + amdNr;
				
								
				if (outmessage.equals("0")) action = ausschalten;
				if (outmessage.equals("1")) action = einschalten;
				if (outmessage.equals("2")) action = umschalten;
				if (outmessage.equals("3")) action = info;
				
				
				
				
				testb = com2phc.WriteAMDChannel(amdNr , action, Chan, (byte)0x00, Cmdlen);
				
				com2phc.PrintInOut (testb); 
				 
				PHCStatusByte[0]=com2phc.getPHCStatusByte(testb);
				PHCByte = PHCStatusByte[0];
				PHCStatusStr = byteToInt(PHCByte);

				topic_answer= "phcstatus/amdstatus/"+(amdNr-amd0);
				sendStatusAnswer(topic_answer, PHCStatusStr.getBytes());
				
				topic_answer= "phcstatus/amd/"+(amdNr-amd0)+"/"+Chan;
				
							
				if (checkBit(PHCByte,Chan))
					{
						sendStatusAnswer(topic_answer, "ON".getBytes());}
				else
					{ 
						sendStatusAnswer(topic_answer, "OFF".getBytes());}
				
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
			
			 testb = com2phc.WriteAMDChannel(amdNr , action, Chan, (byte)0x00, Cmdlen);
			
			 com2phc.PrintInOut (testb); 
			 
				PHCStatusByte[0]=com2phc.getPHCStatusByte(testb);
				PHCByte = PHCStatusByte[0];
				PHCStatusStr = byteToInt(PHCByte);

				topic_answer= "phcstatus/dimstatus/"+(amdNr-dim0);
				sendStatusAnswer(topic_answer, PHCStatusStr.getBytes());
				
				topic_answer= "phcstatus/dim/"+(amdNr-dim0)+"/"+Chan;
				
		
				if (checkBit(PHCByte,Chan))
					{
						sendStatusAnswer(topic_answer, "ON".getBytes());}
				else
					{ 
						sendStatusAnswer(topic_answer, "OFF".getBytes());}
				
					}

				
				
		break;
		
		// Dimmen mit Prozentangabe phc/dim/0/0/ 100(0%) bis 200(100%)
		case "dimproz":{
			
			amdNr = Integer.parseInt(topicString[2]);
			Chan = Integer.parseInt(topicString[3]);
			
			amdNr = amdNr + dim0; 
			
			Value = (byte) (Double.parseDouble(outmessage) * 2.55);
			Cmdlen = (byte)0x03;
			action = dim_setze_wert;
			
			testb = com2phc.WriteAMDChannel(amdNr , action, Chan, Value, Cmdlen);
			
			 com2phc.PrintInOut (testb); 
			 
				PHCStatusByte[0]=com2phc.getPHCStatusByte(testb);
				PHCByte = PHCStatusByte[0];
				PHCStatusStr = byteToInt(PHCByte);

				topic_answer= "phcstatus/dimstatus/"+(amdNr-dim0);
				sendStatusAnswer(topic_answer, PHCStatusStr.getBytes());
				
				topic_answer= "phcstatus/dim/"+(amdNr-dim0)+"/"+Chan;
				
		
				if (checkBit(PHCByte,Chan))
					{
						sendStatusAnswer(topic_answer, "ON".getBytes());}
				else
					{ 
						sendStatusAnswer(topic_answer, "OFF".getBytes());}
				
					}

				
				
		break;
		
		case "emd":{
			
			amdNr = Integer.parseInt(topicString[2]);
			Chan = Integer.parseInt(topicString[3]);
			
			amdNr = amdNr + emd0; 
			
			action = dim_info;
			
			 testb = com2phc.WriteAMDChannel(amdNr , action, Chan, (byte)0x00, Cmdlen);
			
			 com2phc.PrintInOut (testb);

				
				}
		break;
		
		
	}
			
		
}


    
 
public void deliveryComplete(IMqttDeliveryToken token) {
    // TODO Auto-generated method stub

} 

}