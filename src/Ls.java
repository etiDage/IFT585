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
        System.out.println("Start router LS");
        
        Map<String, Map<String, Integer>> mapVoisins = new HashMap<String, Map<String, Integer>>();
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(outputStream);
        oos.writeObject(voisin);
        byte[] data = outputStream.toByteArray();
        
        for (String ipString : IpRouter.keySet())
        {
            InetAddress ip = InetAddress.getByName(ipString);
            System.out.println(ipString + " " + IpRouter.get(ipString));
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
        Map<String, Integer> D = new HashMap<String, Integer>();
        Map<String, String> p = new HashMap<String, String>();

        System.out.println("Starting LS");
        
        Vector<String> N = new Vector<String>();
        N.add(name);
        for (String routerName : IpRouter.values())
        {
            if (voisins.get(routerName) != null)
            {
                D.put(routerName, voisins.get(routerName));
            }
            else
            {
                D.put(routerName, 1000);
            }
        }
        System.out.println("Size de N:" + N.size());
        while (N.size() < IpRouter.size() + 1)
        {
            int min = 1000;
            String minNode = "";
            for (String router : D.keySet())
            {
                if(!N.contains(router))
                {
                    int val = D.get(router);
                    if (val < min)
                    {
                        min = val;
                        minNode = router;
                    }                   
                }
            }
            N.add(minNode);
            if(minNode.equals("0") || minNode.equals("1"))
                continue;
            Map<String, Integer> v = mapVoisins.get(minNode);
            System.out.println("printing v : ");
//            v.forEach((key, value) -> System.out.println(key + ":" + value));

            for (String n : v.keySet())
            {
                if(!n.equals(name))
                {
                    System.out.println("N: " + n);
                    System.out.println("minNode: " + minNode);
                    
                    if(n.equals("0") || n.equals("1"))
                    {
                        System.out.println("D: " + D.get(n));
                        System.out.println("V: " + v.get(n));
                        D.put(n, D.get(minNode));                       
                    }
                    else
                    {
                        D.put(n, Math.min(D.get(n), D.get(minNode) + v.get(n)));                        
                    }
                    //p.put(minNode, n);
                    p.put(n, minNode);                  
                }

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
        D.forEach((key, value) -> System.out.println(key + ":" + value));
        return p;

    }
}
