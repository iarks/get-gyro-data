package iarks.org.bitbucket.getgyrodata;

import android.app.Activity;
import android.util.Log;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by Arkadeep on 31-Jul-17.
 */

class UDPClient implements Runnable
{
    private DatagramSocket clientSocket;
    private InetAddress IPAddress;
    private int port;
    private byte[] data;

    UDPClient(int portNumber, String ip)
    {
        try
        {
            clientSocket = new DatagramSocket();
            IPAddress = InetAddress.getByName(ip);
            port = portNumber;
        } catch (Exception e)
        {
        }

    }

    void setData(String message)
    {
        data = message.getBytes();
    }

    @Override
    public void run()
    {
        DatagramPacket sendPacket = new DatagramPacket(data, data.length, IPAddress, 49443);
        try
        {
            clientSocket.send(sendPacket);
            Log.d("data = ", new String(data));
        }
        catch (Exception e)
        {
//            Log.d("run exception", "HERE");
//            Log.d("e.message = ", e.getMessage());
        }
    }
}
