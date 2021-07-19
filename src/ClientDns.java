
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ClientDns
{
    static String DnsServerAddress = "8.8.8.8";
    static String DomainName = "www.google.com";
    static int port = 53;
    public static void main(String args[]) throws IOException
    {
        String SearchedDomain = "www.usherbrooke.ca";
        InetAddress DnsServeripAddress = InetAddress.getByName(DnsServerAddress);
        
        String domainAddress = DNSRequest(SearchedDomain, DnsServeripAddress);
        System.out.println("domain Address: " + domainAddress);
    }
    
    public static String DNSRequest(String SearchedDomain,InetAddress DnsServeripAddress ) throws IOException
    {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(byteArrayOutputStream);
        
        //Write id 1337 
        dos.writeShort(0x0837);
        
        //WriteFlag
        //https://courses.cs.duke.edu//fall16/compsci356/DNS/DNS-primer.pdf
        //only one = RD
        dos.writeShort(0x0100);
        
        //Write Query Count
        dos.writeShort(0x0001);
        
        //Write Answer Count
        dos.writeShort(0x0000);

        //Write Authority Record Count: 
        dos.writeShort(0x0000);

        //Write Additional Record Count:
        dos.writeShort(0x0000);
        
        
        String[] part = SearchedDomain.split("\\.");
        System.out.println(SearchedDomain + " has " + part.length + " parts");

        
        for(String labels : part)
        {
            System.out.println("Writing: " + labels);

            byte[] byteLabel = labels.getBytes();
            dos.write(byteLabel.length);
            dos.write(byteLabel);
        }
        //Write end of domain name
        dos.writeByte(0x00);
        
        //Write QType 1 = host Adresses 
        dos.writeShort(0x0001);
        
        //Write QClass 1 = Internet Adresses 
        dos.writeShort(0x0001);
        
        byte[] DNSQuery = byteArrayOutputStream.toByteArray();
        
        DatagramSocket socket = new DatagramSocket();
        DatagramPacket DNSQueryPacket = new DatagramPacket(DNSQuery, DNSQuery.length, DnsServeripAddress, port);
        socket.send(DNSQueryPacket);
        
        
        byte[] receivePckData = new byte[1024];
        DatagramPacket packet = new DatagramPacket(receivePckData, receivePckData.length);
        socket.receive(packet);
        
        //print header
        DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(receivePckData));
        System.out.println("Transaction ID: 0x" + String.format("%x", inputStream.readShort()));
        System.out.println("Flags: 0x" + String.format("%x", inputStream.readShort()));
        System.out.println("Questions: 0x" + String.format("%x", inputStream.readShort()));
        System.out.println("Reponses: 0x" + String.format("%x", inputStream.readShort()));
        System.out.println("NScount: 0x" + String.format("%x", inputStream.readShort()));
        System.out.println("ARcount: 0x" + String.format("%x", inputStream.readShort()));
        
        
        int namePartLen = 0;
        String name = "";
        while ((namePartLen = inputStream.readByte()) > 0) {
            byte[] byteNamePart = new byte[namePartLen];

            for (int i = 0; i < namePartLen; i++) {
                byteNamePart[i] = inputStream.readByte();
            }
            name += new String(byteNamePart) + ".";
        }
        name = name.substring(0, name.length() - 1);
        System.out.println("Queried Name: " + name);


        System.out.println("Data Type: 0x" + String.format("%x", inputStream.readShort()));
        System.out.println("Class: 0x" + String.format("%x", inputStream.readShort()));

        System.out.println("Field: 0x" + String.format("%x", inputStream.readShort()));
        System.out.println("Type: 0x" + String.format("%x", inputStream.readShort()));
        System.out.println("Class: 0x" + String.format("%x", inputStream.readShort()));
        System.out.println("TTL: 0x" + String.format("%x", inputStream.readInt()));

        short addrLen = inputStream.readShort();
        System.out.println("Len: 0x" + String.format("%x", addrLen));

        String address = "";
        for (int i = 0; i < addrLen; i++ ) {
            address += String.format("%d", (inputStream.readByte()& 0xFF)) + ".";
        }
        address = address.substring(0, address.length() - 1);
        return address;
    }
    
}
