import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Dv
{
    static Map<String, Map<String, Integer>> startRouter(Map<String, Integer> voisin, DatagramSocket socket,
            Map<String, String> IpRouter) throws IOException, ClassNotFoundException, InterruptedException
    {
        Map<String, Map<String, Integer>> mapVoisins = new HashMap<String, Map<String, Integer>>();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(outputStream);
        oos.writeObject(voisin);
        byte[] data = outputStream.toByteArray();
        for (String routerVoisin : voisin.keySet())
        {
            String ipString = "";

            for (Entry<String, String> entry : IpRouter.entrySet())
            {
                if (routerVoisin.equals(entry.getValue()))
                {
                    ipString = entry.getKey();
                }
            }

            InetAddress ip = InetAddress.getByName(ipString);
            System.out.println(ipString + " " + IpRouter.get(ipString));
            Thread.sleep(250);
            DatagramPacket dp = new DatagramPacket(data, data.length, ip, 50500);
            socket.send(dp);
        }
        int tableReceived = 0;
        while (tableReceived < voisin.size())//à ajuster pour l'host
        {
            byte[] tableb = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(tableb, tableb.length);
            socket.receive(receivePacket);
            ByteArrayInputStream in = new ByteArrayInputStream(receivePacket.getData());
            InetAddress senderip = receivePacket.getAddress();
            String senderName = IpRouter.get(senderip.toString().substring(1));
            System.out.println("Sender ip: " + senderip.toString().substring(1) );
            ObjectInputStream is = new ObjectInputStream(in);
            Map<String, Integer> v = (Map<String, Integer>) is.readObject();
            mapVoisins.put(senderName, v);
            tableReceived++;
            System.out.println("Number of table received:" + tableReceived);
        }
        
        mapVoisins.forEach((key, value) -> System.out.println(key + ":" + value));

        return mapVoisins;
    }
    
    static Map<String, String> runAlgo(Map<String, Integer> voisins, String name, Map<String, Map<String, Integer>> mapVoisins, boolean isHost1, boolean isHost2, Map <String, String> IpRouter)
    {
        
        return null;
    }
}
