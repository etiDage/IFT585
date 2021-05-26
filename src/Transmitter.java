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
    byte[] sendData = new byte[1024];
    DatagramSocket clientSocket;
    DatagramSocket ackSocket;
    int packetSize;
    InetAddress IPAddress;
    Transmitter(int packetSize, InetAddress IPAddress, int ackPort) throws SocketException
    {
        clientSocket = new DatagramSocket(); 
        ackSocket = new DatagramSocket(ackPort);

        this.packetSize = packetSize;
        this.IPAddress = IPAddress;
    }
    
    void transmitFile(int port, File file) throws IOException, InterruptedException
    {
        byte[] fileContent = Files.readAllBytes(file.toPath());
        int packetNb = (int) (file.length()/packetSize);
        if(packetNb < 1)
            packetNb = 1;
        System.out.println(packetNb);
        for(int i = 0; i < packetNb; i++)
        {
            int start = i*packetSize;
            int end = start + packetSize;
            byte[] packet = Arrays.copyOfRange(fileContent, start, end);
            DatagramPacket sendPacket = new DatagramPacket(packet, i, packet.length, IPAddress, port);
            clientSocket.send(sendPacket);
            System.out.println("Sending packet "+ i);
            Thread.sleep(0,100);         
        }
        clientSocket.close();
    }
}
