import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
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
    //For some reason j'ai l'impression qu'on reçoit pas le dernier packet quand on transmet des gros fichier
    //tout le reste de la transmission semble fonctionner
    void ReceiveFile(File result) throws IOException
    {
        int i = 0;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        serverSocket.setSoTimeout(5000);
        boolean timedOut = false;
        while(!timedOut) 
        {
            byte[] data = new byte[1024];
            timedOut = false;
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            try {
                serverSocket.receive(receivePacket);
                data = receivePacket.getData();
                outputStream.write(data);
            }
            catch (SocketTimeoutException e) {
                timedOut = true;
            }
            if(!timedOut)
            {
                System.out.println("receiving packet "+ i);
                i++; 
            }
        }
        byte[] filecontent = outputStream.toByteArray();
        Files.write(result.toPath(), filecontent);
        serverSocket.close();
    }
}
