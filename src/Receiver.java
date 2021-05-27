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

public class Receiver
{
    byte[] receiveData = new byte[1024];
    DatagramSocket serverSocket;
    DatagramSocket ackClientSocket = new DatagramSocket();
    int packetSize;
    int ackPort;
    InetAddress IPAddress;
    Receiver(int port, InetAddress IPAddress, int packetSize, int ackPort) throws SocketException
    {
        serverSocket = new DatagramSocket(port);
        this.packetSize = packetSize;
        this.ackPort = ackPort;
        this.IPAddress = IPAddress;
    }
    //For some reason j'ai l'impression qu'on reçoit pas le dernier packet quand on transmet des gros fichier
    //tout le reste de la transmission semble fonctionner
    void ReceiveFile(File result) throws IOException, InterruptedException
    {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        boolean timedOut = false;
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        int lastPacket = -1;
        int nbPacket = receiveNumberOfPacket();
        System.out.println("nb of packet to receive: " + nbPacket);
        while(lastPacket != nbPacket) 
        {
            byte[] data = new byte[packetSize];
            byte[] packetContent = new byte[packetSize - 4];
            timedOut = false;
            int packetNb = -1;
            serverSocket.receive(receivePacket);
            
            data = receivePacket.getData();
            packetNb = getNbPacketFromData(data);
            
            if(packetNb == lastPacket + 1)
            {
            	lastPacket = packetNb;
	            System.out.println("receiving packet "+ packetNb);
	            packetContent = getPacketContentFromData(data);
	            outputStream.write(packetContent);
            }
            SendAck(lastPacket);


        }
        byte[] filecontent = outputStream.toByteArray();
        Files.write(result.toPath(), filecontent);
        serverSocket.close();
    }
    
    int receiveNumberOfPacket() throws IOException
    {
    	DatagramPacket receivePacket = new DatagramPacket(new byte[4], 4);
    	serverSocket.receive(receivePacket);
    	int nbPacket = getNbPacketFromData(receivePacket.getData());
    	DatagramPacket sendPacket = new DatagramPacket(receivePacket.getData(), 
    			receivePacket.getLength(), IPAddress, ackPort);
    	ackClientSocket.send(sendPacket);
    	return nbPacket - 1;
    }
    
    void SendAck(int numPacket) throws IOException
    {
    	byte[] packet = intToBytes(numPacket);
    	DatagramPacket ack = new DatagramPacket(packet, packet.length, IPAddress, ackPort);
    	ackClientSocket.send(ack);
    	System.out.println("ack" + numPacket + " sent");
    }
    
    private int getNbPacketFromData(byte[] data)
    {
    	byte[] intBytes = new byte[4];
    	intBytes[0] = data[0];
    	intBytes[1] = data[1];
    	intBytes[2] = data[2];
    	intBytes[3] = data[3];
    	ByteBuffer byteBuffer = ByteBuffer.wrap(intBytes);
    	return byteBuffer.getInt();
    }
    
    private byte[] getPacketContentFromData(byte[] data)
    {
    	byte[] packetContent = new byte[data.length - 4];
    	for(int i = 0; i < data.length - 4; i++)
    	{
    		packetContent[i] = data[i + 4];
    	}
    	return packetContent;
    }
    
    private byte[] intToBytes(int i)
    {
    	ByteBuffer bb = ByteBuffer.allocate(4);
    	bb.putInt(i);
    	return bb.array();
    }

}
