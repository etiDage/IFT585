import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;

public class Transmitter
{
    byte[] sendData = new byte[1024];
    DatagramSocket clientSocket;
    DatagramSocket ackSocket;
    int packetSize;
    InetAddress IPAddress;
    int errorPacket = 0;
    int window = 6;
    File file;
    byte[] fileContent;
    
    Transmitter(int packetSize, InetAddress IPAddress, int ackPort, File file) throws Exception
    {
        clientSocket = new DatagramSocket(); 
        ackSocket = new DatagramSocket(ackPort);

        this.packetSize = packetSize;
        this.IPAddress = IPAddress;
        this.file = file;
        this.fileContent = Files.readAllBytes(file.toPath());
    }
    
    void transmitFile(int port) throws IOException, InterruptedException
    {    	
        int packetNb = (int) Math.ceil((double) file.length()/(packetSize - 4));
        if(packetNb < 1)
            packetNb = 1;
        int currentWindow = 0;
        sendNumberOfPacket(packetNb, port);
        System.out.println("Numbre of packet to send: " + packetNb);
        int lastReadAck = -1;
        int i = 0;
        
        while(lastReadAck != packetNb - 1)
        {
        	if(currentWindow >= window || i >= packetNb)
        	{
        		lastReadAck = readAck();
        		if(lastReadAck == -1)
        		{
        			i= errorPacket;
        			currentWindow = 0;
        		}
        		else
        		{
            		currentWindow--;
        		}
        	}
        	if(i < packetNb)
        	{
            	sendPacket(i, port);
                currentWindow++;
        	}
        	i++;
        }
        clientSocket.close();
    }
    
    void sendPacket(int i, int port) throws IOException
    {
    	byte[] numPacket = intToBytes(i);
        int start = i*(packetSize - 4);
        int end = start + (packetSize - 4);
        if(end > fileContent.length)
        {
        	end = fileContent.length;
        }
        System.out.println("Start: " + start + " End: " + end);
        byte[] packet = Arrays.copyOfRange(fileContent, start, end);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(numPacket);
        outputStream.write(packet);

        DatagramPacket sendPacket = new DatagramPacket(outputStream.toByteArray(), packet.length + 4, IPAddress, port);
        clientSocket.send(sendPacket);
        System.out.println("Sending packet "+ i);
    }
    
    void sendNumberOfPacket(int nbPacket, int port) throws IOException
    {
    	int receivedNb = -1;
    	boolean timeout = false;
        ackSocket.setSoTimeout(500);
    	while(receivedNb != nbPacket)
    	{
        	byte[] numberOfPacket = intToBytes(nbPacket);
            DatagramPacket sendPacket = new DatagramPacket(numberOfPacket, numberOfPacket.length, IPAddress, port);
            clientSocket.send(sendPacket);
            DatagramPacket receivePacket = new DatagramPacket(new byte[numberOfPacket.length], numberOfPacket.length);
            try {
            	ackSocket.receive(receivePacket);
            	receivedNb = getAckNumberFromData(receivePacket.getData());
            	System.out.println("Received number: " + receivedNb);
            }catch(SocketTimeoutException e)  {
            	
            }
    	}
    }
    
    int readAck() throws IOException
    {
    	ackSocket.setSoTimeout(500);
    	int lastAck = -1;
        try {
        	DatagramPacket ackPacket = new DatagramPacket(new byte[4], 4);
        	ackSocket.receive(ackPacket);   	
        	int numAck = getAckNumberFromData(ackPacket.getData());
        	
        	if(numAck == lastAck)
        	{
        		errorPacket = numAck + 1;
        		return -1;
        	}
        	lastAck = numAck;
        	System.out.println("Ack" + numAck + " received." );
    	}catch(SocketTimeoutException e){
    		return -1;
    	}
    	return lastAck;
	}
    
    private byte[] intToBytes(int i)
    {
    	ByteBuffer bb = ByteBuffer.allocate(4);
    	bb.putInt(i);
    	return bb.array();
    }
    
    private int getAckNumberFromData(byte[] data)
    {
    	byte[] intBytes = new byte[4];
    	intBytes[0] = data[0];
    	intBytes[1] = data[1];
    	intBytes[2] = data[2];
    	intBytes[3] = data[3];
    	ByteBuffer byteBuffer = ByteBuffer.wrap(intBytes);
    	return byteBuffer.getInt();
    }

   
}
