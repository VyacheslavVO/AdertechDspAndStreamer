package com.vo.adertechaudioapp_v1.adertech;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class UDPSocket {
    private static final String TAG = UDPSocket.class.getSimpleName();

    interface IUDPSocketListeners {
        void onReceiveMessage(String string);
    }

    DatagramSocket socket;
    IUDPSocketListeners listeners;
    private String ipAddress;
    private int ipPort;

    public String getIpAddress() {
        return ipAddress;
    }

    public int getIpPort() {
        return ipPort;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void setIpPort(int ipPort) {
        this.ipPort = ipPort;
    }

    public UDPSocket(String ipAddress, int ipPort) {
        this.ipAddress = ipAddress;
        this.ipPort = ipPort;

        // Создаем сокет
        try {
            this.socket = new DatagramSocket(this.ipPort);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }

        receiveUDP();
    }

    public void sendUDP(final String message) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    InetAddress address = InetAddress.getByName(ipAddress);
                    byte[] buffer = message.getBytes();

                    // Создаем пакет
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, ipPort);


//                    socket.setBroadcast(true);

                    // Отправляем
                    socket.send(packet);
//                    socket.close();
                } catch (Exception e) {
                    Log.e(TAG, "Error send data: ", e);
                }
            }
        }).start();
    }

    public void receiveUDP() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {

                    byte[] buffer = new byte[1024];                                             // буфер для данных
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                    while (true) {                                                              // бесконечное получение
                        socket.receive(packet);                                                 // блокирует поток до получения данных
                        String receivedMessage = new String(buffer, 0, packet.getLength());
                        listeners.onReceiveMessage(receivedMessage);
                        // Если нужно обновить UI используйте runOnUiThread()
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error receive data: ", e);
                }
            }
        }).start();
    }
}
