
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ClientDns
{
    static int port = 53;
    public static void main(String args[]) throws IOException
    {
    	byte[] ipAddr = new byte[] {8, 8, 8, 8};
        InetAddress DnsServerIPAddress = InetAddress.getByAddress(ipAddr);
    
        String SearchedDomain = args[0];
        String htmlFilePath = "";
        if(args.length > 1)
        {
            htmlFilePath = args[1];
        }
        
        InetAddress domainAddress = DNSRequest(SearchedDomain, DnsServerIPAddress);
        System.out.println("domain Address: " + domainAddress.toString());
        
        ArrayList<String> response = HttpGetRequest(domainAddress, SearchedDomain, htmlFilePath);
        
        System.out.println("Status of request:\n" + response.get(0));
                
        String htmlContent = "";
        
        int htmlStartIndex = 0;
        
        for(String line : response)
        {
        	if(line.contains("<html>") || line.contains("<HTML>"))
        	{
        		htmlStartIndex = response.indexOf(line);
        		break;
        	}
        }
        
        for(int i = htmlStartIndex; i < response.size(); i++)
        {
        	htmlContent += response.get(i) + "\n";
        }
        
        System.out.println("HTML CONTENT:\n\n" + htmlContent);
        
        Document htmlDoc = Jsoup.parse(htmlContent);
        
        Elements imgTags = htmlDoc.getElementsByTag("img");
        
        
        System.out.println("Requesting images:\n\n");
        
        for(Element img : imgTags)
        {
        	String imgPath = "/" + img.attr("src");
        	HttpGetImageRequest(domainAddress, SearchedDomain, imgPath);
        }
                
    }
    
    public static InetAddress DNSRequest(String SearchedDomain,InetAddress DnsServeripAddress ) throws IOException
    {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(byteArrayOutputStream);
        
        //Write id 1337 
        dos.writeShort(0x0837);
        
        //WriteFlag
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

        
        for(String labels : part)
        {
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
        
        byte[] DNSRequest = byteArrayOutputStream.toByteArray();
        
        //Send request to DNS server
        System.out.println("Sending Request for " + SearchedDomain);
        DatagramSocket socket = new DatagramSocket();
        DatagramPacket DNSRequestPacket = new DatagramPacket(DNSRequest, DNSRequest.length, DnsServeripAddress, port);
        socket.send(DNSRequestPacket);
        
        System.out.println("Receiving answer from Dns Server");

        byte[] receivePckData = new byte[1024];
        DatagramPacket packet = new DatagramPacket(receivePckData, receivePckData.length);
        socket.receive(packet);
        socket.close();
        
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

        ByteArrayOutputStream domainAddressOutput = new ByteArrayOutputStream();

        for (int i = 0; i < addrLen; i++ ) {
            domainAddressOutput.write(inputStream.readByte()& 0xFF);
        }
        byte[] byteAdr = domainAddressOutput.toByteArray();

        InetAddress address = InetAddress.getByAddress(byteAdr);
        return address;    
    }
    
    public static ArrayList<String> HttpGetRequest(InetAddress address, String domainName, String filePath) throws IOException
    {
    	Socket socket = new Socket(address, 80);
    	PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
    	printWriter.println("GET " + filePath + " HTTP/1.1");
    	printWriter.println("Host: " + domainName);
    	printWriter.println("Connection: close");
    	printWriter.println("User-agent: Mozilla/5.0");
    	printWriter.println("Accept: text/html, image/gif, image/jpeg, image/tiff");
    	printWriter.println("Accept-language:fr");
    	printWriter.println("");
    	printWriter.flush();
    	BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    	ArrayList<String> response = new ArrayList<String>();
    	String line;
    	
    	while((line = br.readLine()) != null) 
    	{
    		response.add(line);
    	}
    	
    	br.close();
    	socket.close();
    	
    	return response;
    }
    
    public static void HttpGetImageRequest(InetAddress address, String domainName, String filePath) throws IOException
    {
    	Socket socket = new Socket(address, 80);
    	PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
    	printWriter.println("GET " + filePath + " HTTP/1.1");
    	printWriter.println("Host: " + domainName);
    	printWriter.println("Connection: close");
    	printWriter.println("User-agent: Mozilla/5.0");
    	printWriter.println("Accept: text/html, image/gif, image/jpeg, image/tiff");
    	printWriter.println("Accept-language:fr");
    	printWriter.println("");
    	printWriter.flush();
    	
    	String[] tempArray = filePath.split("/");
    	String imgName = tempArray[tempArray.length - 1];
    	
		OutputStream fileos = new FileOutputStream(imgName);
		ByteArrayOutputStream os = new ByteArrayOutputStream();		
    	InputStream is = socket.getInputStream();
    	    	    
    	byte[] b = new byte[2048];
    	int length;
    	
    	while((length = is.read(b)) != -1)
    	{
    		os.write(b, 0, length);
    	}
    	
    	byte[] fileContent = os.toByteArray();
    	
    	String[] ctnArray = new String(fileContent).split("\n");
    	
    	int contentLength = 0;
    	
    	for(String line : ctnArray)
    	{
    		if(line.contains("Content-Length:"))
    		{
    			Pattern ptrn = Pattern.compile("[0-9]+");
    			
    			Matcher m = ptrn.matcher(line);
    			
    			if(m.find())
    			{
    				contentLength = Integer.parseInt(m.group());
    			}
    		}
    	}
    	
    	int sizeToChop = fileContent.length - contentLength;
    	
    	System.out.println("Saving Image: " + imgName);
    	fileos.write(Arrays.copyOfRange(fileContent, sizeToChop, fileContent.length));
    	
    	is.close();
    	os.close();
    	fileos.close();
    	socket.close();
    	
    }
}
