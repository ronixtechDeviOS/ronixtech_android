package com.ronixtech.ronixhome;

/*
	Auto Raspberry Pi Finder Android Java Class
	Nizar Mahmoud https://nizarmah.me/

	Use freely, just would appreciate
	keeping this comment on top to
	show me off to other people
*/

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class PiFinder {
    private static final String TAG = PiFinder.class.getSimpleName();

    String[] ipAddress;
    String ipString = "";

    public void findMePi() {
        WifiManager manager = (WifiManager) MyApp.getInstance().getApplicationContext().getSystemService(MyApp.getInstance().WIFI_SERVICE);
        WifiInfo info = manager.getConnectionInfo();

        long infoIp = info.getIpAddress();

        String address = (infoIp & 0xFF) +
                "." + ((infoIp >> 8) & 0xFF) +
                "." + ((infoIp >> 16) & 0xFF) +
                "." + ((infoIp >> 24) & 0xFF);

        String[] ip = address.split("\\.");
        ipAddress = new String[]{ ip[0], ip[1], ip[2] };

        for (int i = 0; i < ipAddress.length; i++)
            ipString = ipString + ipAddress[i] + ".";

        class Counter {
            int i = 0, j = 0;
            public Counter(int m) {
                if (m % 2 > 0) {
                    this.i = (int) Math.floor((double) m / 2) + 1;
                    this.j = (int) Math.floor((double) m / 2);
                } else {
                    this.i = (int) Math.floor((double) m / 2) + 1;
                    this.j = (int) Math.floor((double) m / 2);
                }
            }
            public void resize(int i) {
                this.i = i;
            }
        }

        class ARP implements Runnable {
            public void run() {

                try {
                    String[] command = { "sh", "-c", "arp -vn | grep -i ' 5e:cf:7f'" };
                    Process arpa = Runtime.getRuntime().exec(command);

                    arpa.waitFor();

                    Log.i("PiFinder", "Collected ARP Table");

                    // Read out available Pi addresses
                    InputStream inStream = arpa.getInputStream();
                    // Needed to observe errors, thus this is here
                    // InputStream errorStream = ping.getErrorStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inStream));
                    String line = "";
                    StringBuilder response = new StringBuilder();

                    ArrayList<ArrayList<String>> pis = new ArrayList<ArrayList<String>>();

                    while ((line = bufferedReader.readLine()) != null) {
                        response.append(line);

                        String[] tempLine = line.split("\\s+");

                        ArrayList<String> pi = new ArrayList<String>();
                        pi.add(tempLine[1].substring(1, tempLine[1].length() - 1));
                        pi.add(tempLine[3]);

                        Utils.log(TAG, line, true);

                        pis.add(pi);
                    }
                    if (pis.size() == 0) {
                        Log.i("PiFinder", "No Pis found");
                        // Use Callback Here ( Negative )
                    } else {
                        // Use Callback Here ( Positive )
                    }
                } catch (IOException e){
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        final Thread arp = new Thread(new ARP());

        final ArrayList<String> pingables = new ArrayList<String>();

        final Counter pingableCounter = new Counter(0);

        class Pingable implements Runnable {
            int i = 0, tempI = 0;

            Pingable(int m) {
                if (m == pingables.size()) {
                    this.i = m - 1;
                    pingableCounter.resize(m - 1);
                } else this.i = m;
            }

            public void run() {
                try {
                    if (pingables.size() == 0)
                        arp.start();
                    else {
                        String[] command = { "sh", "-c", "ping -c 1 " + pingables.get(this.i) };
                        Process ping = Runtime.getRuntime().exec(command);

                        this.tempI = this.i;

                        this.i -= 1;
                        if (this.i > -1) new Thread(new Pingable(this.i)).start();

                        Log.i("PiFinder", "Pinged host on " + pingables.get(this.tempI));

                        ping.waitFor();

                        if ((this.tempI % 2) == 0)
                            pingableCounter.i--;
                        else pingableCounter.j--;
                        if ((pingableCounter.i + pingableCounter.j) == 0)
                            arp.start();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        final Counter pingerCounter;

        final Thread pinger;
        pingerCounter = new Counter(254);
        class Pinger implements Runnable {
            int y = 0, tempY = 0;

            Pinger (int y) {
                this.y = y;
            }

            public void run() {
                StringBuffer output = new StringBuffer();
                Process ping;
                try {
                    String[] command = { "sh", "-c", "((ping -c 1 " + ipString + this.y + ") > /dev/null) && (echo 'up') || (echo 'down')" };
                    ping = Runtime.getRuntime().exec(command);

                    this.tempY = this.y;

                    this.y -= 1;
                    if (this.y > 0) new Thread(new Pinger(this.y)).start();

                    ping.waitFor();
                    InputStream inStream = ping.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inStream));
                    String line = "";
                    StringBuilder response = new StringBuilder();

                    while ((line = bufferedReader.readLine()) != null) {
                        response.append(line);

                        if (line.toString().equals("up")) {
                            Log.i("PiFinder", "Available host on " + ipString + this.tempY);
                            pingables.add(ipString + this.tempY);
                        }
                    }

                    if ((this.tempY % 2) == 1)
                        pingerCounter.i--;
                    else pingerCounter.j--;
                    if ((pingerCounter.i + pingerCounter.j) == 0)
                        new Thread(new Pingable(pingables.size())).start();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        pinger = new Thread(new Pinger(255));
        pinger.start();
    }
}