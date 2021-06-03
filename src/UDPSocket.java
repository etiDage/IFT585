import java.io.File;
import java.net.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class UDPSocket
{

    public static void main(String[] args) throws Exception
    {
    	byte[] ipAddr = new byte[4];
    	ipAddr[0] = (byte)192;
    	ipAddr[1] = (byte)168;
    	ipAddr[2] = (byte)1;
    	ipAddr[3] = (byte)114;
        InetAddress IPAddress = InetAddress.getByName(args[2]);
        int port = Integer.parseInt(args[3]);
        int ackPort = Integer.parseInt(args[4]);
        int packetSize = 1024;
        Path pathToFile = Paths.get(args[1]);
        System.out.println(pathToFile.toAbsolutePath());
        
        switch (args[0])
        {
            case "R":
                File result = new File(args[1]);
                Receiver receiver = new Receiver(port, IPAddress, packetSize, ackPort);
                receiver.ReceiveFile(result);
                break;
            case "T":
                File input = new File(args[1]);

                Transmitter transmitter = new Transmitter(packetSize, IPAddress, ackPort, input);
                transmitter.transmitFile(port);
                break;
        }
    }

}
