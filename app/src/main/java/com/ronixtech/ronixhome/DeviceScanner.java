package com.ronixtech.ronixhome;

import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.ronixtech.ronixhome.activities.MainActivity;
import com.ronixtech.ronixhome.entities.Device;
import com.ronixtech.ronixhome.entities.Line;
import com.ronixtech.ronixhome.fragments.DashboardDevicesFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import androidx.work.Worker;

public class DeviceScanner extends Worker{
    private static final String TAG = DeviceScanner.class.getSimpleName();

    @Override
    public Worker.Result doWork(){
        Log.d(TAG, "doWork for DeviceScanner");
        // Do the work here--in this case, compress the stored images.
        // In this example no parameters are passed; the task is
        // assumed to be "compress the whole library."
        //myCompress();

        try {
            if(MySettings.getAllDevices() != null && MySettings.getAllDevices().size() >= 1){
                boolean allDevicesReachable = true;
                for (Device dev : MySettings.getAllDevices()) {
                    if(dev.getIpAddress() != null && dev.getIpAddress().length() >= 1) {
                        getDeviceInfo(dev);
                    }else{
                        MySettings.scanNetwork();
                        allDevicesReachable = false;
                    }
                }
                if(allDevicesReachable){
                    Utils.hideUpdatingNotification();
                }
            }
        } catch (Exception e) {
        }

        // Indicate success or failure with your return value:
        return Result.SUCCESS;

        // (Returning RETRY tells WorkManager to try this task again
        // later; FAILURE says not to try again.)
    }

    private void getDeviceInfoRetrofit(Device device){
        /*Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(device.getIpAddress())
                .addConverterFactory(GetDeviceStatus.class)
                // add other factories here, if needed.
                .build();*/
    }


    private void getDeviceInfo(Device device){
        //volley request to device to get its status
        String url = "http://" + device.getIpAddress() + Constants.GET_DEVICE_STATUS;

        Log.d(TAG,  "getDeviceStatus URL: " + url);
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "getDeviceStatus response: " + response);

                device.setErrorCount(0);

                DataParser dataParser = new DataParser(device, response);
                dataParser.execute();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Volley Error: " + error.getMessage());
                MySettings.updateDeviceErrorCount(device, device.getErrorCount() + 1);
                if(device.getErrorCount() >= Device.MAX_CONSECUTIVE_ERROR_COUNT) {
                    MySettings.updateDeviceIP(device, "");
                    MySettings.updateDeviceErrorCount(device, 0);
                    MySettings.scanNetwork();
                }
            }
        });
        request.setShouldCache(false);
        request.setRetryPolicy(new DefaultRetryPolicy(200, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        HttpConnectorDeviceStatus.getInstance(MainActivity.getInstance()).addToRequestQueue(request);
    }

    public class DataParser extends AsyncTask<Void, Void, Void> {
        private final String TAG = DashboardDevicesFragment.DataParser.class.getSimpleName();

        Device device;
        JSONObject jsonObject;

        public DataParser(Device device, String data) {
            try{
                jsonObject = new JSONObject(data);
                this.device = device;
            }catch (Exception e){
                Log.d(TAG, "Json exception " + e.getMessage());
            }
        }

        @Override
        protected void onPreExecute(){

        }

        @Override
        protected void onProgressUpdate(Void... params){

        }

        @Override
        protected void onPostExecute(Void params) {

        }

        @Override
        protected Void doInBackground(Void... params) {
            if(jsonObject != null){
                try {
                    JSONObject unitStatus = jsonObject.getJSONObject("UNIT_STATUS");

                    JSONObject wifiStatus = unitStatus.getJSONObject("U_W_STT");
                    String chipID = wifiStatus.getString("U_W_UID");
                    if(device.getChipID().length() >= 1) {
                        if (!device.getChipID().toLowerCase().equals(chipID.toLowerCase())) {
                            MySettings.updateDeviceIP(device, "");
                            MySettings.updateDeviceErrorCount(device, 0);
                            MySettings.scanNetwork();
                            return null;
                        }
                    }

                    JSONObject hardwareStatus = unitStatus.getJSONObject("U_H_STT");
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


                    /*String deviceStatus = jsonObject.getString(Constants.PARAMETER_DEVICE_STATUS_KEY);
                    String[] status = deviceStatus.split("%");
                    String line1PowerStateString = status[0];
                    String line2PowerStateString = status[1];
                    String line3PowerStateString = status[2];
                    String line1DimmingValueString = status[3];
                    String line2DimmingValueString = status[4];
                    String line3DimmingValueString = status[5];
                    String line1DimmingStateString = status[11];
                    String line2DimmingStateString = status[12];
                    String line3DimmingStateString = status[13];

                    int line1PowerState = Integer.valueOf(line1PowerStateString.substring(line1PowerStateString.indexOf("=")+1));
                    int line2PowerState = Integer.valueOf(line2PowerStateString.substring(line2PowerStateString.indexOf("=")+1));
                    int line3PowerState = Integer.valueOf(line3PowerStateString.substring(line3PowerStateString.indexOf("=")+1));


                    int line1DimmingValue, line2DimmingValue, line3DimmingValue;
                    if(!line1DimmingValueString.substring(line1DimmingValueString.indexOf("=")+1).equals(":")){
                        line1DimmingValue = Integer.valueOf(line1DimmingValueString.substring(line1DimmingValueString.indexOf("=")+1));
                    }else{
                        line1DimmingValue = 10;
                    }

                    if(!line2DimmingValueString.substring(line2DimmingValueString.indexOf("=")+1).equals(":")){
                        line2DimmingValue = Integer.valueOf(line2DimmingValueString.substring(line2DimmingValueString.indexOf("=")+1));
                    }else{
                        line2DimmingValue = 10;
                    }

                    if(!line3DimmingValueString.substring(line3DimmingValueString.indexOf("=")+1).equals(":")){
                        line3DimmingValue = Integer.valueOf(line3DimmingValueString.substring(line3DimmingValueString.indexOf("=")+1));
                    }else{
                        line3DimmingValue = 10;
                    }


                    int line1DimmingState = Integer.valueOf(line1DimmingStateString.substring(line1DimmingStateString.indexOf("=")+1));
                    int line2DimmingState = Integer.valueOf(line2DimmingStateString.substring(line2DimmingStateString.indexOf("=")+1));
                    int line3DimmingState = Integer.valueOf(line3DimmingStateString.substring(line3DimmingStateString.indexOf("=")+1));*/

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
                    device.setLines(lines);
                    MySettings.addDevice(device);
                    if(MainActivity.getInstance() != null){
                        MainActivity.getInstance().updateDevicesList();
                    }
                }catch (JSONException e){
                    Log.d(TAG, "Json exception: " + e.getMessage());
                }
            }
            return null;
        }
    }
}
