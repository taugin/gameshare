package com.chukong.sdkdemo.dns;

public class DnsServer {
    private static boolean isShutDown = false;
    public static void main(String[] args) {
        UDPSocketMonitor monitor = new UDPSocketMonitor("127.0.0.1", 53);
        monitor.start();
        
        while (!isShutDown) {
            try {
                Thread.sleep(10000000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
