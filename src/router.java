import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Vector;

public class router
{
    static Map <String, String> IpRouter = new HashMap<String, String>();
    
    // InetAddress ipAddress;
    Map<String, Map<String, Integer>> mapVoisins;

    Map<String, Integer> D;
    int port = 50500;

    int[][] table;

    static void receiveMsg(DatagramSocket socket, String name,Map<String, String> p, 
    		boolean isHost2, InetAddress IPReceiver, Map<String, Integer> voisinRouter) throws IOException
    {
        
        byte[] pck = new byte[1024];
        DatagramPacket packet = new DatagramPacket(pck, pck.length);
        socket.receive(packet);
        System.out.println("Message reçus par le routeur " + name);
        pck = packet.getData();
        char dest = (char) pck[0];
        System.out.println("Destination " + dest);
        sendMsg(socket, packet, String.valueOf(dest), p, name, isHost2, IPReceiver, voisinRouter);
        
    }
    
    static void sendMsg(DatagramSocket socket, DatagramPacket packet, String dest, Map<String, String> p, 
    		String name, boolean isHost2, InetAddress IPReceiver, Map<String, Integer> voisinRouter) throws IOException
    { 
        InetAddress destIp = null;        

    	if(isHost2)
    	{
    		destIp = IPReceiver;
    	}
    	else 
    	{

            String prochain = "";
            while(!voisinRouter.containsKey(dest))
            {
                dest = p.get(dest);
                prochain = dest;
            }
            System.out.println("Prochain: " + prochain);
            for (Entry<String, String> entry: IpRouter.entrySet())
            {
                if(Objects.equals(prochain, entry.getValue()))
                {   
                    destIp = InetAddress.getByName(entry.getKey());   
                }
            }    		
    	}
        
        packet.setAddress(destIp);
        socket.send(packet);
        
    }
    
    public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException, InterruptedException
    {
    	
        SetRouterTable();
        
        String currentAddress = InetAddress.getLocalHost().getHostAddress();
        String name = IpRouter.get(currentAddress);
        
    	System.out.println(InetAddress.getLocalHost().getHostAddress());
        System.out.println(name);
        
        Map<String, Integer> voisinRouter = new HashMap<String, Integer>();
        setInitialTables(name, voisinRouter);
        
        String host1 = args[1];
        String host2 = args[2];
        
        boolean isHost1 = host1.equals(name);
        boolean isHost2 = host2.equals(name);
        
        InetAddress IPReceiver = InetAddress.getByName(args[3]);
        
        DatagramSocket socket = new DatagramSocket(50500);
        Map<String, Map<String, Integer>> mapvoisins;
        
        switch (args[0])
        {
            case "LS":
               //Map<String, Map<String, Integer>> mapvoisins =  startRouterLs(voisinRouter, socket);
                mapvoisins =  Ls.startRouter(voisinRouter, socket, IpRouter);

       //         Map<String, String> p = Ls(voisinRouter, name, mapvoisins, isHost1, isHost2);
                Map<String, String> p = Ls.runAlgo(voisinRouter, name, mapvoisins, isHost1, isHost2, IpRouter);

                receiveMsg(socket, name, p, isHost2, IPReceiver, voisinRouter);
                socket.close();
                break;
            case "DV":
                System.out.println("In DV switch case.");
                //mapvoisins = Dv.startRouter(voisinRouter, socket, IpRouter, (isHost1 || isHost2));
                
                Map<String, Map<String, Integer>> DV = Dv.runAlgo(voisinRouter, name, isHost1, isHost2, IpRouter, socket);
                
                Dv.ReceiveMsg(socket, name, DV, isHost2, IPReceiver, IpRouter);
                
                socket.close();

                break;

        }
        // DatagramSocket socket = new DatagramSocket();
        
    }
    
    public static InetAddress[] getReseau() throws UnknownHostException
    {
        InetAddress[] ip = {InetAddress.getByName("172.17.0.2"),
                InetAddress.getByName("172.17.0.3"),
                InetAddress.getByName("172.17.0.4"), 
                InetAddress.getByName("172.17.0.5"),
                InetAddress.getByName("172.17.0.6"),
                InetAddress.getByName("172.17.0.7")};
        return ip;
    }
    
    public static void SetRouterTable()
    {
        IpRouter.put("172.17.0.2", "A");
        IpRouter.put("172.17.0.3", "B");
        IpRouter.put("172.17.0.4", "C");
        IpRouter.put("172.17.0.5", "D");
        IpRouter.put("172.17.0.6", "E");
        IpRouter.put("172.17.0.7", "F");
    }
    
    public static void setInitialTables(String name, Map<String, Integer> voisinRouter)
    {
        switch (name)
        {
            case "A":
            	IpRouter.remove("172.17.0.2");
                voisinRouter.put("B", 5);
               // voisinRouter.put("C", 1000);
                voisinRouter.put("D", 45);
               // voisinRouter.put("E", 1000);
                //voisinRouter.put("F", 1000);
                voisinRouter.put("0", 0);
                break;
            case "B":
            	IpRouter.remove("172.17.0.3");
                voisinRouter.put("A", 5);
                voisinRouter.put("C", 70);
               // voisinRouter.put("D", 1000);
                voisinRouter.put("E", 3);
               // voisinRouter.put("F", 1000);
                break;
            case "C":
            	IpRouter.remove("172.17.0.4");

               // voisinRouter.put("A", 1000);
                voisinRouter.put("B", 70);
                voisinRouter.put("D", 50);
                //voisinRouter.put("E", 1000);
                voisinRouter.put("F", 78);
                break;
            case "D":
            	IpRouter.remove("172.17.0.5");

                voisinRouter.put("A", 45);
               // voisinRouter.put("B", 1000);
                voisinRouter.put("C", 50);
                voisinRouter.put("E", 8);
               // voisinRouter.put("F", 1000);
                break;
            case "E":
            	IpRouter.remove("172.17.0.6");

             //   voisinRouter.put("A", 1000);
                voisinRouter.put("B", 3);
              //  voisinRouter.put("C", 1000);
                voisinRouter.put("D", 8);
                voisinRouter.put("F", 7);
                break;
            case "F":
            	IpRouter.remove("172.17.0.7");

              //  voisinRouter.put("A", 1000);
              //  voisinRouter.put("B", 1000);
                voisinRouter.put("C", 78);
            //    voisinRouter.put("D", 1000);
                voisinRouter.put("E", 7);
                voisinRouter.put("1", 0);
                break;

            default:
                break;
        }
    }
}
