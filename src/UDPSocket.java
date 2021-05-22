import java.io.File;
import java.net.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class UDPSocket
{

    public static void main(String[] args) throws Exception
    {
        InetAddress IPAddress = InetAddress.getLocalHost();
        int port = 50500;
        int packetSize = 1024;
        Path pathToFile = Paths.get(args[1]);
        System.out.println(pathToFile.toAbsolutePath());
        
        switch (args[0])
        {
            case "R":
                File result = new File(args[1]);
                Receiver receiver = new Receiver(port, packetSize);
                receiver.ReceiveFile(result);
                /*byte[] receiveData = new byte[1024];
                DatagramSocket serverSocket = new DatagramSocket(50500);
                System.out.println("ReceiveMode");
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);
                String modifiedSentence = new String(receivePacket.getData());
                System.out.println("FROM SERVER: " + modifiedSentence);
                serverSocket.close();
                break;*/
            case "T":
                File input = new File(args[1]);

                Transmitter transmitter = new Transmitter(packetSize, IPAddress);
                transmitter.transmitFile(port, input);
                /*DatagramSocket clientSocket = new DatagramSocket();
                byte[] sendData = new byte[1024];
                System.out.println("TransmitMode");
                String sentence = "Hello World";
                sendData = sentence.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 50500);
                clientSocket.send(sendPacket);
                clientSocket.close();
                break;*/
        }
    }

}
