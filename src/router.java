import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Map;
import java.util.Vector;

public class router
{
    DatagramSocket socket;
    InetAddress ipAddress;
    String name;
    int port;
    Map<String, Map<String, Integer>> mapVoisins;
    Map<String, Integer> D;
    int[][] table;
    
    router(InetAddress ipAddress, int port, String name) throws SocketException
    {
        this.port = port;
        this.ipAddress = ipAddress;
        socket = new DatagramSocket(port, ipAddress);
        this.name = name;
        
    }
    
    void startRouter()
    {
        
    }
    
    void Ls(Map<String, Integer> voisins, String[]reseau)
    {
        Vector<String> N = new Vector<String>();
        N.add(name);
        for(String router : reseau)
        {
            if(voisins.get(router) != null)
            {
                D.put(router, voisins.get(router));
            }
            else
            {
                D.put(router, 1000);
            }
        }
        while(N.size() == reseau.length)
        {
            int min = 1000;
            String minNode = "";
            for(String router: D.keySet())
            {
               int val = D.get(router);
               if(val < min)
               {
                   min = val;
                   minNode = router;
               }
            }
            N.add(minNode);
               Map<String, Integer> v = mapVoisins.get(minNode);
                for(String name:v.keySet())
                {
                    D.put(name, Math.min(D.get(name), D.get(minNode) + v.get(name)));
                    
                }
            //update D(v) pour tout voisin de minNode(D(v) = min (D(v), D(routeur) + c(w, min))


        }
        
    }


    
}
