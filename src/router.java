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

    static Map<String, Map<String, Integer>> startRouterLs(Map<String, Integer> voisin, DatagramSocket socket)
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

    static Map<String, String> Ls(Map<String, Integer> voisins, String name, Map<String, Map<String, Integer>> mapVoisins, boolean isHost1, boolean isHost2)
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
    static void receiveMsg(DatagramSocket socket, String name,Map<String, String> p ) throws IOException
    {
        
        byte[] pck = new byte[1024];
        DatagramPacket packet = new DatagramPacket(pck, pck.length);
        socket.receive(packet);
        System.out.println("Message reçus par le routeur " + name);
        pck = packet.getData();
        char dest = (char) pck[0];
        sendMsg(socket, packet, String.valueOf(dest), p, name);
        
    }
    static void sendMsg(DatagramSocket socket, DatagramPacket packet, String dest, Map<String, String> p, String name) throws IOException
    {
        String prochain = "";
        while(dest != name)
        {
            prochain = dest;
            dest = p.get(dest);
        }
        InetAddress destIp = null;        
        for (Entry<String, String> entry: IpRouter.entrySet())
        {
            if(Objects.equals(prochain, entry.getValue()))
            {   
                destIp = InetAddress.getByName(entry.getKey());   
            }
        }

        packet.setAddress(destIp);
        socket.send(packet);
        
    }
    public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException, InterruptedException
    {
    	System.out.println(InetAddress.getLocalHost().getHostAddress());
        Map<String, Integer> voisinRouter = new HashMap<String, Integer>();
        SetRouterTable();
        String currentAddress = InetAddress.getLocalHost().getHostAddress();
        String name = IpRouter.get(currentAddress);
        System.out.println(name);
        setInitialTables(name, voisinRouter);
        String host1 = args[1];
        String host2 = args[2];
        boolean isHost1 = host1.equals(name);
        boolean isHost2 = host2.equals(name);
        
        DatagramSocket socket = new DatagramSocket(50500);
        Map<String, Map<String, Integer>> mapvoisins;
        switch (args[0])
        {
            case "LS":
            	System.out.println("In LS switch case.");
               //Map<String, Map<String, Integer>> mapvoisins =  startRouterLs(voisinRouter, socket);
                mapvoisins =  Ls.startRouter(voisinRouter, socket, IpRouter);

       //         Map<String, String> p = Ls(voisinRouter, name, mapvoisins, isHost1, isHost2);
                Map<String, String> p = Ls.runAlgo(voisinRouter, name, mapvoisins, isHost1, isHost2, IpRouter);

                receiveMsg(socket, name, p);
                socket.close();
                break;
            case "DV":
                System.out.println("In LS switch case.");
                mapvoisins = Dv.startRouter(voisinRouter, socket, IpRouter);
                
                Dv.runAlgo(voisinRouter, name, mapvoisins, isHost1, isHost2, IpRouter);
                
                

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
