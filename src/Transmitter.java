import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.file.Files;
import java.util.Arrays;

public class Transmitter
{
    byte[] sendData = new byte[65514];
    DatagramSocket clientSocket;
    int packetSize;
    InetAddress IPAddress;
    Transmitter(int packetSize, InetAddress IPAddress) throws SocketException
    {
        clientSocket = new DatagramSocket(); 
        this.packetSize = packetSize;
        this.IPAddress = IPAddress;
    }
    
    void transmitFile(int port, File file) throws IOException
    {
        byte[] fileContent = Files.readAllBytes(file.toPath());
        System.out.println(fileContent);
        int packetNb = (int) (file.length()/65514);
        if(packetNb < 1)
            packetNb = 1;
        System.out.println(packetNb);
        for(int i = 0; i < packetNb; i++)
        {
            byte[] packet = Arrays.copyOfRange(fileContent, i*packetSize, i+1*packetSize);
            System.out.println(packet);
            DatagramPacket sendPacket = new DatagramPacket(packet, packet.length, IPAddress, port);
            clientSocket.send(sendPacket);
        }
        clientSocket.close();
    }
}
