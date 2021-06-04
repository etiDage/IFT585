import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class router
{
    DatagramSocket socket;
    InetAddress ipAddress;
    int port;
    int[][] table;
    
    router(InetAddress ipAddress, int port) throws SocketException
    {
        this.port = port;
        this.ipAddress = ipAddress;
        socket = new DatagramSocket(port, ipAddress);
    }
    
    void startRouter()
    {
        
    }
}
