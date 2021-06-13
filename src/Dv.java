import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.Vector;
import java.util.concurrent.TimeoutException;

public class Dv
{
    static Map<String, Map<String, Integer>> startRouter(Map<String, Integer> voisin, DatagramSocket socket,
            Map<String, String> IpRouter, boolean isHost) throws IOException, ClassNotFoundException, InterruptedException
    {
    	Thread.sleep(2500);
    	
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
        int host = isHost ? 1 : 0;
        while (tableReceived < voisin.size() - host)//à ajuster pour l'host
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
    
    static Map<String, Map<String, Integer>> runAlgo(Map<String, Integer> voisins, String name, boolean isHost1, 
    		boolean isHost2, Map <String, String> IpRouter, DatagramSocket socket) throws IOException, InterruptedException, ClassNotFoundException
    {
    	Thread.sleep(2500);
    	
    	Map<String, Integer> Dx = new HashMap<String, Integer>();
    	Map<String, Map<String, Integer>> DV = new HashMap<String, Map<String, Integer>>();
    	ArrayList<String> N = new ArrayList<String>(IpRouter.values());
    	N.add("0");
    	N.add("1");

    	for(String y : N)
    	{
        	Map<String, Integer> tempMap = new HashMap<String, Integer>();
            if (voisins.get(y) != null)
            {
            	if(y.equals("0") || y.equals("1"))
            	{
            		Dx.put(y, 0);
            	}
            	else
            	{
                	tempMap.put(y, voisins.get(y));
                    DV.put(y, tempMap);
                    Dx.put(y, voisins.get(y));
            	}
            }
            else
            {
                Dx.put(y, 1000);
            }
    	}
    	
    	for(String w : DV.keySet())
    	{
    		for(String y : N)
    		{
    			if(!y.equals(w) && !y.equals(name))
    			{
    				DV.get(w).put(y, 1000);
    			}
    		}
    		
    	}
    	
    	SendDxToVoisins(Dx, voisins, IpRouter, socket);
    	
    	boolean timeout = false;
		socket.setSoTimeout(5000);

    	while(!timeout)
    	{
    		try
    		{
                byte[] tableb = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(tableb, tableb.length);
                
                socket.receive(receivePacket);
                
                ByteArrayInputStream in = new ByteArrayInputStream(receivePacket.getData());
                InetAddress senderip = receivePacket.getAddress();
                String senderName = IpRouter.get(senderip.toString().substring(1));
                
                
                ObjectInputStream is = new ObjectInputStream(in);
                Map<String, Integer> DvReceived = (Map<String, Integer>) is.readObject();
                boolean dxHasChanged = false;
                                
                //Update DV
                DV.put(senderName, DvReceived);
                
                for(String y : N)
                {
                	if(!y.equals(senderName))
                	{
                    	if(DV.get(senderName).get(y) + Dx.get(senderName) < Dx.get(y))
                    	{
                    		Dx.put(y,DV.get(senderName).get(y) + Dx.get(senderName));
                    		dxHasChanged = true;
                    	}
                	}
                }
                
                if(dxHasChanged)
                {
                	SendDxToVoisins(Dx, voisins, IpRouter, socket);
                }
    		}
    		catch(SocketTimeoutException e)
    		{
    			System.out.println("Exception received: " + e);
    			timeout = true;
    		}
    	}
    	
    	System.out.println("Routeur stable");
    	DV.forEach((key, value) -> System.out.println(key + ":" + value));
    	return DV;
    	
    }
    
    static void SendDxToVoisins(Map<String, Integer> Dx, Map<String, Integer> voisins, 
    		Map <String, String> IpRouter, DatagramSocket socket) throws IOException, InterruptedException
    {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(outputStream);
        oos.writeObject(Dx);
        byte[] data = outputStream.toByteArray();
        for (String routerVoisin : voisins.keySet())
        {
        	if(!routerVoisin.equals("0") && !routerVoisin.equals("1"))
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
                System.out.println("Packet sent to voisins " + routerVoisin);
        	}
        }

    }  
    
    static void ReceiveMsg(DatagramSocket socket, String name,Map<String, Map<String, Integer>> DV, 
    		boolean isHost2, InetAddress IPReceiver, Map <String, String> IpRouter) throws IOException
    {
    	socket.setSoTimeout(0);
        byte[] pck = new byte[1024];
        DatagramPacket packet = new DatagramPacket(pck, pck.length);
        socket.receive(packet);
        System.out.println("Message reçus par le routeur " + name);
        pck = packet.getData();
        char dest = (char) pck[0];
        System.out.println("Destination " + dest);
        SendMsg(socket, packet, String.valueOf(dest), DV, name, isHost2, IPReceiver, IpRouter);

    }
    
    static void SendMsg(DatagramSocket socket, DatagramPacket packet, String dest, Map<String, Map<String, Integer>> DV, 
    		String name, boolean isHost2, InetAddress IPReceiver, Map <String, String> IpRouter) throws IOException
    {
        InetAddress destIp = null;        

    	if(isHost2)
    	{
    		destIp = IPReceiver;
    	}
    	else
    	{
        	int bestCostToSend = 1000;
        	String nextToSend = "";
        	for(String voisin : DV.keySet())
        	{
        		if(DV.get(voisin).get(dest) < bestCostToSend)
        		{
        			nextToSend = voisin;
        			bestCostToSend = DV.get(voisin).get(dest);
        		}
        	}
        	
            for (Entry<String, String> entry: IpRouter.entrySet())
            {
                if(Objects.equals(nextToSend, entry.getValue()))
                {   
                    destIp = InetAddress.getByName(entry.getKey());   
                }
            }    
    	}
        
        packet.setAddress(destIp);
        socket.send(packet);
 
    }
}
