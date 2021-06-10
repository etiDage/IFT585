import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Host
{

    public static void main(String[] args) throws IOException
    {
        DatagramSocket socket = new DatagramSocket(50500);
        byte[] p = new byte[1024];

        switch (args[0])
        {
            case "1":
                String msg = "2Hello";
                p = msg.getBytes();
                DatagramPacket packet = new DatagramPacket(p, p.length, InetAddress.getByName("172.0.0.2"), 50500);
                socket.send(packet);
                socket.close();
                break;
            case "2":
                DatagramPacket receivePacket = new DatagramPacket(p, p.length);
                socket.receive(receivePacket);
                p = receivePacket.getData();
                String receivedMsg = p.toString().substring(1);
                System.out.println("Message Reçu: " + receivedMsg);
                socket.close();
                break;
            default:
                break;
        }

    }

}
