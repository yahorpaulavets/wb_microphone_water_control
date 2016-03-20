package leikyahiro.com;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadFactory;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {
       listenForFile();

//        while(true) {
//            System.out.print("SENDING..");
//            sendBroadcast("hello: true");
//            Thread.sleep(1000);
//        }

    }

    private static void listenForFile() throws IOException {
        int filesize=6022386; // filesize temporary hardcoded

        long start = System.currentTimeMillis();
        int bytesRead;
        int current = 0;

        // create socket
        ServerSocket servsock = new ServerSocket(8888);
        while (true) {
            System.out.println("Waiting...");

            Socket sock = servsock.accept();
            System.out.println("Accepted connection : " + sock);

            // receive file
            byte [] mybytearray  = new byte [filesize];
            InputStream is = sock.getInputStream();
            FileOutputStream fos = new FileOutputStream("C:\\1.wav"); // destination path and name of file
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            bytesRead = is.read(mybytearray,0,mybytearray.length);
            current = bytesRead;

            // thanks to A. Cádiz for the bug fix
            do {
                bytesRead =
                        is.read(mybytearray, current, (mybytearray.length-current));
                if(bytesRead >= 0) current += bytesRead;
            } while(bytesRead > -1);

            bos.write(mybytearray, 0 , current);
            bos.flush();
            long end = System.currentTimeMillis();
            System.out.println(end-start);
            bos.close();

            sendBroadcast("ml: true");

            sock.close();
        }
    }

    public static void sendBroadcast(String messageStr) {

        try {
            //Open a random port to send the package
            DatagramSocket socket = new DatagramSocket();
            socket.setBroadcast(true);
            byte[] sendData = messageStr.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, getBroadcastAddress(), 8888);
            socket.send(sendPacket);
            System.out.println("Broadcast packet sent to: " + getBroadcastAddress().getHostAddress());
        } catch (IOException e) {
            System.out.print("IOException: " + e.getMessage());
        }
    }

    private static InetAddress getBroadcastAddress() throws IOException {
        Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
        while (en.hasMoreElements()) {
            NetworkInterface ni = en.nextElement();
            System.out.println(" Display Name = " + ni.getDisplayName());

            List<InterfaceAddress> list = ni.getInterfaceAddresses();
            if(ni.isLoopback()) { continue; }
            for (InterfaceAddress ia : list) {
                System.out.println(" Broadcast = " + ia.getBroadcast());

                if (ia.getBroadcast() != null) {
                    return ia.getBroadcast();
                }
            }
        }

        return null;
    }

}
