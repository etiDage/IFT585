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
import java.util.Vector;

public class Ls
{
    static Map<String, Map<String, Integer>> startRouter(Map<String, Integer> voisin, DatagramSocket socket, Map <String, String> IpRouter)
            throws IOException, ClassNotFoundException, InterruptedException
    {
        Thread.sleep(2500);
        
        Map<String, Map<String, Integer>> mapVoisins = new HashMap<String, Map<String, Integer>>();
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(outputStream);
        oos.writeObject(voisin);
        byte[] data = outputStream.toByteArray();
        
        for (String ipString : IpRouter.keySet())
        {
            InetAddress ip = InetAddress.getByName(ipString);
            Thread.sleep(250);
            DatagramPacket dp = new DatagramPacket(data, data.length, ip, 50500);
            socket.send(dp);
        }
        
        int tableReceived = 0;
        /*
         * ByteArrayOutputStream receiveOutputStream = new
         * ByteArrayOutputStream(); ObjectOutputStream roos = new
         * ObjectOutputStream(receiveOutputStream);
         */
        while (tableReceived < IpRouter.size())
        {
            byte[] tableb = new byte[1024];
            
            DatagramPacket receivePacket = new DatagramPacket(tableb, tableb.length);
            socket.receive(receivePacket);

            InetAddress senderip = receivePacket.getAddress();
            String senderName = IpRouter.get(senderip.toString().substring(1));
            
            ByteArrayInputStream in = new ByteArrayInputStream(receivePacket.getData());            
            ObjectInputStream is = new ObjectInputStream(in);
            Map<String, Integer> v = (Map<String, Integer>) is.readObject();
            
            mapVoisins.put(senderName, v);
            tableReceived++;
        }
             
        return mapVoisins;
    }
    
    static Map<String, String> runAlgo(Map<String, Integer> voisins, String name, Map<String, Map<String, Integer>> mapVoisins, boolean isHost1, boolean isHost2, Map <String, String> IpRouter)
    {
        Map<String, Integer> D = new HashMap<String, Integer>();
        Map<String, String> p = new HashMap<String, String>();

        
        Vector<String> N = new Vector<String>();
        N.add(name);
        for (String v : IpRouter.values())
        {
            if (voisins.get(v) != null)
            {
                D.put(v, voisins.get(v));
            }
            else
            {
                D.put(v, 1000);
            }
        }
        while (N.size() < IpRouter.size() + 1)
        {
            int min = 1000;
            String w = "";
            for (String router : D.keySet())
            {
                if(!N.contains(router))
                {
                    int val = D.get(router);
                    if (val < min)
                    {
                        min = val;
                        w = router;
                    }                   
                }
            }
            N.add(w);
            if(w.equals("0") || w.equals("1"))
                continue;
            Map<String, Integer> voisinsW = mapVoisins.get(w);
//            System.out.println("printing v : ");
//            v.forEach((key, value) -> System.out.println(key + ":" + value));

            for (String v : voisinsW.keySet())
            {
                if(!v.equals(name))
                {
                    
                    if(v.equals("0") || v.equals("1"))
                    {
                        D.put(v, D.get(w));                       
                    }
                    else
                    {
                        D.put(v, Math.min(D.get(v), D.get(w) + voisinsW.get(v)));                        
                    }
                    //p.put(minNode, n);
                    //p.putIfAbsent(n, minNode);
                }
                p.putIfAbsent(v, w); 

            }
            // update D(v) pour tout voisin de minNode(D(v) = min (D(v),
            // D(routeur) + c(w, min))
        }
        
        if(isHost1)
        {
            p.put("0", name);
            D.put("0", 0);
        }
        if(isHost2)
        {
            p.put("1", name);
            D.put("1", 0);
        }
        System.out.println("Table de routage: ");
        D.forEach((key, value) -> System.out.println(key + ":" + value));

        return p;

    }
}
