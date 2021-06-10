import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Vector;

import javax.sound.midi.Receiver;

public class router
{
    static Map <String, String> IpRouter = new HashMap<String, String>();
    
    // InetAddress ipAddress;
    Map<String, Map<String, Integer>> mapVoisins;

    Map<String, Integer> D;
    int port = 50500;

    int[][] table;

    static String getName(InetAddress ip)
    {
        String name;
        switch (ip.toString())
        {
            case "172.0.0.2":
                name = "A";
                break;
            case "172.0.0.3":
                name = "B";
                break;
            case "172.0.0.4":
                name = "C";
                break;
            case "172.0.0.5":
                name = "D";
                break;
            case "172.0.0.6":
                name = "E";
                break;
            case "172.0.0.7":
                name = "F";
                break;
            default:
                name = "";
        }
        return name;
    }

    static Map<String, Map<String, Integer>> startRouterLs(InetAddress[] ipAddresses, Map<String, Integer> voisin, DatagramSocket socket)
            throws IOException, ClassNotFoundException
    {
        Map<String, Map<String, Integer>> mapVoisins = new HashMap<String, Map<String, Integer>>();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(outputStream);
        oos.writeObject(voisin);
        byte[] data = outputStream.toByteArray();
        for (InetAddress ip : ipAddresses)
        {
            DatagramPacket dp = new DatagramPacket(data, data.length, ip, 50500);
            socket.send(dp);
        }
        int tableReceived = 0;
        /*
         * ByteArrayOutputStream receiveOutputStream = new
         * ByteArrayOutputStream(); ObjectOutputStream roos = new
         * ObjectOutputStream(receiveOutputStream);
         */
        while (tableReceived < ipAddresses.length)
        {
            byte[] tableb = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(tableb, tableb.length);
            socket.receive(receivePacket);
            ByteArrayInputStream in = new ByteArrayInputStream(receivePacket.getData());
            InetAddress senderip = receivePacket.getAddress();
            String senderName = getName(senderip);
            ObjectInputStream is = new ObjectInputStream(in);
            Map<String, Integer> v = (Map<String, Integer>) is.readObject();
            mapVoisins.put(senderName, v);
        }
        return mapVoisins;
    }

    static Map<String, String> Ls(Map<String, Integer> voisins, InetAddress[] reseau, String name, Map<String, Map<String, Integer>> mapVoisins)
    {
        Map<String, Integer> D = new HashMap<String, Integer>();
        Map<String, String> p = new HashMap<String, String>();

        Vector<String> N = new Vector<String>();
        N.add(name);
        for (InetAddress router : reseau)
        {
            String routerName = getName(router);
            if (voisins.get(routerName) != null)
            {
                D.put(routerName, voisins.get(routerName));
            }
            else
            {
                D.put(routerName, 1000);
            }
        }
        while (N.size() == reseau.length)
        {
            int min = 1000;
            String minNode = "";
            for (String router : D.keySet())
            {
                int val = D.get(router);
                if (val < min)
                {
                    min = val;
                    minNode = router;
                }
            }
            N.add(minNode);
            Map<String, Integer> v = mapVoisins.get(minNode);
            for (String n : v.keySet())
            {
                D.put(n, Math.min(D.get(n), D.get(minNode) + v.get(n)));
                //p.put(minNode, n);
                p.put(n, minNode);

            }
            // update D(v) pour tout voisin de minNode(D(v) = min (D(v),
            // D(routeur) + c(w, min))
        }
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
    public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException
    {
        Map<String, Integer> voisinRouter = new HashMap<String, Integer>();
        String name = args[1];
        setInitialTables(name, voisinRouter);
        
        DatagramSocket socket = new DatagramSocket(50500);
        switch (args[0])
        {
            case "LS":
                InetAddress[] reseau = getReseau();
                Map<String, Map<String, Integer>> mapvoisins =  startRouterLs(reseau, voisinRouter, socket);
                Map<String, String> p = Ls(voisinRouter, reseau, name, mapvoisins);
                receiveMsg(socket, name, p);
                socket.close();
                break;
            case "DV":
                break;

        }
        // DatagramSocket socket = new DatagramSocket();
        
    }
    public static InetAddress[] getReseau() throws UnknownHostException
    {
        InetAddress[] ip = {InetAddress.getByName("172.0.0.2"),
                InetAddress.getByName("172.0.0.3"),
                InetAddress.getByName("172.0.0.4"), 
                InetAddress.getByName("172.0.0.5"),
                InetAddress.getByName("172.0.0.6"),
                InetAddress.getByName("172.0.0.7")};
        return ip;
    }
    public static void setInitialTables(String name, Map<String, Integer> voisinRouter)
    {
        IpRouter.put("172.0.0.2", "A");
        IpRouter.put("172.0.0.3", "B");
        IpRouter.put("172.0.0.4", "C");
        IpRouter.put("172.0.0.5", "D");
        IpRouter.put("172.0.0.6", "E");
        IpRouter.put("172.0.0.7", "F");
        switch (name)
        {
            case "A":
                voisinRouter.put("B", 5);
               // voisinRouter.put("C", 1000);
                voisinRouter.put("D", 45);
               // voisinRouter.put("E", 1000);
                //voisinRouter.put("F", 1000);
                voisinRouter.put("0", 0);
                break;
            case "B":
                voisinRouter.put("A", 5);
                voisinRouter.put("C", 70);
               // voisinRouter.put("D", 1000);
                voisinRouter.put("E", 3);
               // voisinRouter.put("F", 1000);
                break;
            case "C":
               // voisinRouter.put("A", 1000);
                voisinRouter.put("B", 70);
                voisinRouter.put("D", 50);
                //voisinRouter.put("E", 1000);
                voisinRouter.put("F", 78);
                break;
            case "D":
                voisinRouter.put("A", 45);
               // voisinRouter.put("B", 1000);
                voisinRouter.put("C", 50);
                voisinRouter.put("E", 8);
               // voisinRouter.put("F", 1000);
                break;
            case "E":
             //   voisinRouter.put("A", 1000);
                voisinRouter.put("B", 3);
              //  voisinRouter.put("C", 1000);
                voisinRouter.put("D", 8);
                voisinRouter.put("F", 7);
                break;
            case "F":
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
