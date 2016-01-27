package com.sysdbg.caster.router;

import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.Arrays;

/**
 * Created by crady on 1/20/2016.
 */
public class BroadCastReceiver {
    private static final String TAG = "Caster.BCR";
    private static final String MULTICAST_GROUP = "225.0.0.0";
    private static final short DEFAULT_PORT = 2278;
    private static byte[] DISCOVER_KEY = null;

    static {
        try {
            DISCOVER_KEY = "caster".getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {

        }
    }

    private boolean mIsRunning;
    private short mPort;

    private MulticastSocket mServerSocket;
    private Thread mWorkingThread;

    public BroadCastReceiver() {
        this(DEFAULT_PORT);
    }

    public BroadCastReceiver(short port) {
        mPort = port;
        mIsRunning = false;
    }

    public void start() {
        if (mIsRunning)
            return;

        mIsRunning = true;
        try {
            mServerSocket = new MulticastSocket(mPort);
            mServerSocket.joinGroup(InetAddress.getByName(MULTICAST_GROUP));
            mWorkingThread = new Thread() {
                @Override
                public void run() {
                    BroadCastReceiver.this.run();
                }
            };
            mWorkingThread.start();
        } catch (IOException e) {
            Log.e(TAG, "create datagram socket fail", e);
            mIsRunning = false;
        }
    }

    public void stop() {
        if (mServerSocket != null) {
            mServerSocket.close();
            mServerSocket = null;
        }

        if (mWorkingThread != null) {
            mWorkingThread = null;
        }

        mIsRunning = false;
    }

    private void run() {
        byte[] buffer = new byte[1024];
        while(true) {
            DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
            try {
                mServerSocket.receive(receivePacket);
                byte[] receivedData = Arrays.copyOfRange(buffer, 0, receivePacket.getLength());
                if (Arrays.equals(DISCOVER_KEY, receivedData)) {
                    sendResponse(receivePacket.getAddress(), receivePacket.getPort());
                }
            } catch (IOException e) {
                break;
            }
        }
        Log.i(TAG, "worker thread exist");
    }

    private void sendResponse(InetAddress remoteAddress, int remotePort) {
        StringBuffer sb = new StringBuffer();
        sb.append(Build.BOARD);
        sb.append(" ");
        sb.append(Build.MODEL);

        Log.i(TAG, "response " + sb.toString());

        byte[] content = null;
        try {
            content = sb.toString().getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "convert response to utf-8 fail", e);
            return;
        }

        DatagramPacket pkg = new DatagramPacket(content, content.length);
        pkg.setAddress(remoteAddress);
        pkg.setPort(remotePort);
        try {
            mServerSocket.send(pkg);
        } catch (IOException e) {
            Log.e(TAG, "Send response fail.", e);
        }
    }
}
