package tp1;

import java.net.*;

public class UDP 
{

	public static void main(String[] args) throws Exception 
	{
		
		if(args.length > 0)
		{
			InetAddress IPAddress = InetAddress.getByName("localhost");
			byte[] sendData = new byte[1024];
			byte[] receiveData = new byte[1024];

			switch(args[0])
			{
				case "R":
					DatagramSocket serverSocket = new DatagramSocket(49153);
					DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
					System.out.println("Waiting to receive...");
					serverSocket.receive(receivePacket);
					System.out.println(new String(receivePacket.getData()));
					serverSocket.close();
					break;
				case "S":
					DatagramSocket clientSocket = new DatagramSocket();
					String message = "Hello world!";
					sendData = message.getBytes();
					DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 49153);
					clientSocket.send(sendPacket);
					System.out.println("Packet sent");
					clientSocket.close();
					break;
			}
		}

	}

}
