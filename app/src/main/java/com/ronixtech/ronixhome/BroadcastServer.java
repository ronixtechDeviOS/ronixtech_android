package com.ronixtech.ronixhome;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.StrictMode;

import com.ronixtech.ronixhome.activities.MainActivity;
import com.ronixtech.ronixhome.entities.Device;
import com.ronixtech.ronixhome.entities.Line;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Calendar;
import java.util.List;

public class BroadcastServer {
    private static final String TAG = BroadcastServer.class.getSimpleName();

    private static String IP_ADDRESS = "0.0.0.0";
    private static int PORT = 8080;

    private static boolean serverRunning = false;

    private static void initServer(){
        new Thread() {
            @Override
            public void run() {
                Utils.log(TAG, "Runnable: Hello, world!", true);
                try {
                    //Keep a socket open to listen to all the UDP trafic that is destined for this port
                    DatagramSocket socket = new DatagramSocket(PORT, InetAddress.getByName(IP_ADDRESS));
                    socket.setBroadcast(true);

                    while (serverRunning) {
                        Utils.log(TAG, "Ready to receive broadcast packets!", true);

                        //Receive a packet
                        byte[] recvBuf = new byte[15000];
                        DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
                        socket.receive(packet);

                        //Packet received
                        Utils.log(TAG, "Packet received from: " + packet.getAddress().getHostAddress(), true);
                        String data = new String(packet.getData()).trim();
                        Utils.log(TAG, "Packet received; data: " + data, true);

                        try{
                            String chipID = "";
                            if(data.contains("U_W_UID")){
                                JSONObject jsonObject = new JSONObject(data);
                                JSONObject unitStatus = jsonObject.getJSONObject("UNIT_STATUS");
                                if(unitStatus != null && unitStatus.has("U_W_STT")){
                                    JSONObject wifiStatus = unitStatus.getJSONObject("U_W_STT");
                                    if(wifiStatus != null && wifiStatus.has("U_W_UID")){
                                        chipID = wifiStatus.getString("U_W_UID");
                                    }
                                }
                            }
                            MySettings.setGetStatusState(true);
                            Device device = MySettings.getDeviceByChipID2(chipID);
                            if(device != null){
                                if(data!= null && data.length() >= 1 && data.contains("UNIT_STATUS")){
                                    JSONObject jsonObject = new JSONObject(data);
                                    if(jsonObject.has("UNIT_STATUS")){
                                        //parse received unit status and update relevant device, which has the received chip_id
                                        JSONObject unitStatus = jsonObject.getJSONObject("UNIT_STATUS");

                                        if(unitStatus != null && unitStatus.has("U_W_STT")){
                                            JSONObject wifiStatus = unitStatus.getJSONObject("U_W_STT");
                                            if(wifiStatus != null) {
                                                if(wifiStatus.has("U_W_UID")) {
                                                    //String chipID = wifiStatus.getString("U_W_UID");
                                                }else{
                                                    device.setFirmwareUpdateAvailable(true);
                                                }
                                                /*if(wifiStatus.has("R_M_ALV")){
                                                    String R_M_ALV_string = wifiStatus.getString("R_M_ALV");
                                                    int R_M_ALV = Integer.parseInt(R_M_ALV_string);
                                                    if(R_M_ALV == 1){
                                                        try {
                                                            JSONObject jsonObject1 = new JSONObject();
                                                            jsonObject1.put(Constants.PARAMETER_ACCESS_TOKEN, device.getAccessToken());
                                                            jsonObject1.put("R_M_ALV", "0");
                                                            MqttMessage mqttMessage1 = new MqttMessage();
                                                            mqttMessage1.setPayload(jsonObject1.toString().getBytes());
                                                            Utils.log(TAG, "MQTT Publish topic: " + String.format(Constants.MQTT_TOPIC_CONTROL, device.getChipID()), true);
                                                            Utils.log(TAG, "MQTT Publish data: " + mqttMessage1, true);
                                                            mqttAndroidClient.publish(String.format(Constants.MQTT_TOPIC_CONTROL, device.getChipID()), mqttMessage1);
                                                            device.setDeviceMQTTReachable(true);
                                                        }catch (JSONException e){
                                                            Utils.log(TAG, "Exception: " + e.getMessage(), true);
                                                        }catch (MqttException e){
                                                            Utils.log(TAG, "Exception: " + e.getMessage(), true);
                                                        }
                                                    }
                                                }*/
                                                if(wifiStatus.has("U_W_FWV")) {
                                                    String currentFirmwareVersion = wifiStatus.getString("U_W_FWV");
                                                    if (currentFirmwareVersion != null && currentFirmwareVersion.length() >= 1){
                                                        device.setFirmwareVersion(currentFirmwareVersion);
                                                        if(MySettings.getDeviceLatestWiFiFirmwareVersion(device.getDeviceTypeID()).length() >= 1) {
                                                            int currentVersion = Integer.valueOf(currentFirmwareVersion);
                                                            int onlineVersion = Integer.valueOf(MySettings.getDeviceLatestWiFiFirmwareVersion(device.getDeviceTypeID()));
                                                            if (onlineVersion != currentVersion) {
                                                                device.setFirmwareUpdateAvailable(true);
                                                            }else{
                                                                device.setFirmwareUpdateAvailable(false);
                                                            }
                                                        }
                                                    }else{
                                                        device.setFirmwareUpdateAvailable(true);
                                                    }
                                                }else{
                                                    device.setFirmwareUpdateAvailable(true);
                                                }
                                            }
                                        }else{
                                            device.setFirmwareUpdateAvailable(true);
                                        }

                                        if(device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines ||
                                                device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_1line_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_2lines_old || device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_old ||
                                                device.getDeviceTypeID() == Device.DEVICE_TYPE_wifi_3lines_workaround){
                                            if(unitStatus != null && unitStatus.has("U_H_STT")){
                                                JSONObject hardwareStatus = unitStatus.getJSONObject("U_H_STT");

                                                if(hardwareStatus.has("U_H_FWV")) {
                                                    String currentHWFirmwareVersion = hardwareStatus.getString("U_H_FWV");
                                                    if (currentHWFirmwareVersion != null && currentHWFirmwareVersion.length() >= 1){
                                                        device.setHwFirmwareVersion(currentHWFirmwareVersion);
                                                        if(MySettings.getDeviceLatestHWFirmwareVersion(device.getDeviceTypeID()).length() >= 1) {
                                                            int currentHWVersion = Integer.valueOf(currentHWFirmwareVersion);
                                                            int onlineHWVersion = Integer.valueOf(MySettings.getDeviceLatestHWFirmwareVersion(device.getDeviceTypeID()));
                                                            if (onlineHWVersion != currentHWVersion) {
                                                                device.setHwFirmwareUpdateAvailable(true);
                                                            }else{
                                                                device.setHwFirmwareUpdateAvailable(false);
                                                            }
                                                        }
                                                    }else{
                                                        device.setHwFirmwareUpdateAvailable(true);
                                                    }
                                                }else{
                                                    device.setHwFirmwareUpdateAvailable(true);
                                                }


                                                String line0PowerStateString, line1PowerStateString, line2PowerStateString;
                                                int line0PowerState = 0, line1PowerState = 0, line2PowerState = 0;
                                                if(hardwareStatus.has("L_0_STT")){
                                                    line0PowerStateString = hardwareStatus.getString("L_0_STT");
                                                    line0PowerState = Integer.valueOf(line0PowerStateString);
                                                }
                                                if(hardwareStatus.has("L_1_STT")){
                                                    line1PowerStateString = hardwareStatus.getString("L_1_STT");
                                                    line1PowerState = Integer.valueOf(line1PowerStateString);
                                                }
                                                if(hardwareStatus.has("L_2_STT")){
                                                    line2PowerStateString = hardwareStatus.getString("L_2_STT");
                                                    line2PowerState = Integer.valueOf(line2PowerStateString);
                                                }

                                                String line0DimmingValueString, line1DimmingValueString, line2DimmingValueString;
                                                int line0DimmingValue = 0, line1DimmingValue = 0, line2DimmingValue = 0;
                                                if(hardwareStatus.has("L_0_DIM")){
                                                    line0DimmingValueString = hardwareStatus.getString("L_0_DIM");
                                                    if(line0DimmingValueString.equals(":")){
                                                        line0DimmingValue = 10;
                                                    }else{
                                                        line0DimmingValue = Integer.valueOf(line0DimmingValueString);
                                                    }
                                                }
                                                if(hardwareStatus.has("L_1_DIM")){
                                                    line1DimmingValueString = hardwareStatus.getString("L_1_DIM");
                                                    if(line1DimmingValueString.equals(":")){
                                                        line1DimmingValue = 10;
                                                    }else{
                                                        line1DimmingValue = Integer.valueOf(line1DimmingValueString);
                                                    }
                                                }
                                                if(hardwareStatus.has("L_2_DIM")){
                                                    line2DimmingValueString = hardwareStatus.getString("L_2_DIM");
                                                    if(line2DimmingValueString.equals(":")){
                                                        line2DimmingValue = 10;
                                                    }else{
                                                        line2DimmingValue = Integer.valueOf(line2DimmingValueString);
                                                    }
                                                }

                                                String line0DimmingStateString, line1DimmingStateString, line2DimmingStateString;
                                                int line0DimmingState = 0, line1DimmingState = 0, line2DimmingState = 0;
                                                if(hardwareStatus.has("L_0_D_S")){
                                                    line0DimmingStateString = hardwareStatus.getString("L_0_D_S");
                                                    line0DimmingState = Integer.valueOf(line0DimmingStateString);
                                                }
                                                if(hardwareStatus.has("L_1_D_S")){
                                                    line1DimmingStateString = hardwareStatus.getString("L_1_D_S");
                                                    line1DimmingState = Integer.valueOf(line1DimmingStateString);
                                                }
                                                if(hardwareStatus.has("L_2_D_S")){
                                                    line2DimmingStateString = hardwareStatus.getString("L_2_D_S");
                                                    line2DimmingState = Integer.valueOf(line2DimmingStateString);
                                                }


                                                List<Line> lines = device.getLines();
                                                for (Line line:lines) {
                                                    if(line.getPosition() == 0){
                                                        line.setPowerState(line0PowerState);
                                                        line.setDimmingState(line0DimmingState);
                                                        line.setDimmingVvalue(line0DimmingValue);
                                                    }else if(line.getPosition() == 1){
                                                        line.setPowerState(line1PowerState);
                                                        line.setDimmingState(line1DimmingState);
                                                        line.setDimmingVvalue(line1DimmingValue);
                                                    }else if(line.getPosition() == 2){
                                                        line.setPowerState(line2PowerState);
                                                        line.setDimmingState(line2DimmingState);
                                                        line.setDimmingVvalue(line2DimmingValue);
                                                    }
                                                }

                                                String temperatureString, beepString, hwLockString;
                                                int temperatureValue;
                                                boolean beep, hwLock;
                                                if(hardwareStatus.has("U_H_TMP")){
                                                    temperatureString = hardwareStatus.getString("U_H_TMP");
                                                    temperatureValue = Integer.parseInt(temperatureString);
                                                    device.setTemperature(temperatureValue);
                                                }
                                                if(hardwareStatus.has("U_BEEP_")){
                                                    beepString = hardwareStatus.getString("U_BEEP_");
                                                    beep = Boolean.parseBoolean(beepString);
                                                    device.setBeep(beep);
                                                }
                                                if(hardwareStatus.has("U_H_LCK")){
                                                    hwLockString = hardwareStatus.getString("U_H_LCK");
                                                    hwLock = Boolean.parseBoolean(hwLockString);
                                                    device.setHwLock(hwLock);
                                                }


                                                device.setLastSeenTimestamp(Calendar.getInstance().getTimeInMillis());
                                            }else{
                                                device.setFirmwareUpdateAvailable(true);
                                            }
                                        }else if(device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_1lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_2lines || device.getDeviceTypeID() == Device.DEVICE_TYPE_PLUG_3lines){
                                            if(unitStatus != null && unitStatus.has("U_H_STT")){
                                                JSONObject hardwareStatus = unitStatus.getJSONObject("U_H_STT");

                                                if(hardwareStatus.has("U_H_FWV")) {
                                                    String currentHWFirmwareVersion = hardwareStatus.getString("U_H_FWV");
                                                    if (currentHWFirmwareVersion != null && currentHWFirmwareVersion.length() >= 1){
                                                        device.setHwFirmwareVersion(currentHWFirmwareVersion);
                                                        if(MySettings.getDeviceLatestHWFirmwareVersion(device.getDeviceTypeID()).length() >= 1) {
                                                            int currentHWVersion = Integer.valueOf(currentHWFirmwareVersion);
                                                            int onlineHWVersion = Integer.valueOf(MySettings.getDeviceLatestHWFirmwareVersion(device.getDeviceTypeID()));
                                                            if (onlineHWVersion != currentHWVersion) {
                                                                device.setHwFirmwareUpdateAvailable(true);
                                                            }else{
                                                                device.setHwFirmwareUpdateAvailable(false);
                                                            }
                                                        }
                                                    }else{
                                                        device.setHwFirmwareUpdateAvailable(true);
                                                    }
                                                }else{
                                                    device.setHwFirmwareUpdateAvailable(true);
                                                }


                                                String line0PowerStateString, line1PowerStateString, line2PowerStateString;
                                                int line0PowerState = 0, line1PowerState = 0, line2PowerState = 0;
                                                if(hardwareStatus.has("L_0_STT")){
                                                    line0PowerStateString = hardwareStatus.getString("L_0_STT");
                                                    line0PowerState = Integer.valueOf(line0PowerStateString);
                                                }
                                                if(hardwareStatus.has("L_1_STT")){
                                                    line1PowerStateString = hardwareStatus.getString("L_1_STT");
                                                    line1PowerState = Integer.valueOf(line1PowerStateString);
                                                }
                                                if(hardwareStatus.has("L_2_STT")){
                                                    line2PowerStateString = hardwareStatus.getString("L_2_STT");
                                                    line2PowerState = Integer.valueOf(line2PowerStateString);
                                                }

                                                List<Line> lines = device.getLines();
                                                for (Line line:lines) {
                                                    if(line.getPosition() == 0){
                                                        line.setPowerState(line0PowerState);
                                                    }else if(line.getPosition() == 1){
                                                        line.setPowerState(line1PowerState);
                                                    }else if(line.getPosition() == 2){
                                                        line.setPowerState(line2PowerState);
                                                    }
                                                }

                                                String temperatureString, beepString, hwLockString;
                                                int temperatureValue;
                                                boolean beep, hwLock;
                                                temperatureString = hardwareStatus.getString("U_H_TMP");
                                                beepString = hardwareStatus.getString("U_BEEP_");
                                                hwLockString = hardwareStatus.getString("U_H_LCK");

                                                temperatureValue = Integer.parseInt(temperatureString);
                                                beep = Boolean.parseBoolean(beepString);
                                                hwLock = Boolean.parseBoolean(hwLockString);

                                                device.setTemperature(temperatureValue);
                                                device.setBeep(beep);
                                                device.setHwLock(hwLock);

                                                device.setLastSeenTimestamp(Calendar.getInstance().getTimeInMillis());
                                            }else {
                                                device.setFirmwareUpdateAvailable(true);
                                            }
                                        }
                                    }
                                }else{
                                    device.setFirmwareUpdateAvailable(true);
                                }
                                MySettings.addDevice(device);
                                DevicesInMemory.updateDevice(device);
                                Device localDevice = DevicesInMemory.getLocalDevice(device);
                                if(localDevice != null){
                                    DevicesInMemory.updateLocalDevice(localDevice);
                                }
                                MainActivity.getInstance().refreshDevicesListFromMemory();
                            }
                            MySettings.setGetStatusState(false);
                        }catch (JSONException e){
                            Utils.log(TAG, "Exception: " + e.getMessage(), true);
                        }


                        /*// Send the packet data back to the UI thread
                        Intent localIntent = new Intent(Constants.BROADCAST_ACTION)
                                    // Puts the data into the Intent
                                    .putExtra(Constants.EXTENDED_DATA_STATUS, data);
                        // Broadcasts the Intent to receivers in this app.
                        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);*/
                    }
                } catch (IOException e) {
                    Utils.log(TAG, "Exception: " + e.getMessage(), true);
                }
            }
        }.start();
    }

    public static void startServer(){
        if(!serverRunning){
            serverRunning = true;
            initServer();
        }
    }

    public static void stopServer(){
        serverRunning = false;
    }

    public static void sendMessage(String messageStr){
        // Hack Prevent crash (sending should be done using an async task)
        StrictMode.ThreadPolicy policy = new   StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            //Open a random port to send the package
            DatagramSocket socket = new DatagramSocket();
            socket.setBroadcast(true);
            byte[] sendData = messageStr.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, getBroadcastAddress(), PORT);
            socket.send(sendPacket);
            Utils.log(TAG, "Broadcast packet sent to: " + getBroadcastAddress().getHostAddress(), true);
        } catch (IOException e) {
            Utils.log(TAG, "Exception: " + e.getMessage(), true);
        }
    }

    public static InetAddress getBroadcastAddress() throws IOException {
        WifiManager wifi = (WifiManager) MyApp.getInstance().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();
        // handle null somehow

        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        return InetAddress.getByAddress(quads);
    }
}
