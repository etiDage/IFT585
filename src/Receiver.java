import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.file.Files;

public class Receiver
{
    byte[] receiveData = new byte[1024];
    DatagramSocket serverSocket;
    int packetSize;
    Receiver(int port, int packetSize) throws SocketException
    {
        serverSocket = new DatagramSocket(port);
        this.packetSize = packetSize;
    }
    
    void ReceiveFile(File result) throws IOException
    {
        do 
        {
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);
            byte[] data = receivePacket.getData();
            System.out.println(data);
            Files.write(result.toPath(), data);
        }
        while(receiveData.length >= packetSize);
        serverSocket.close();
    }
}
