package com.ronixtech.ronixhome;

public class Constants {
    public static final String PACKAGE_NAME = "com.ronixtech.ronixhome";

    public static final String MQTT_USERNAME = "qumrwmme";
    public static final String MQTT_PASSWORD = "oJHjXS7Xi0F9";
    public static final int MQTT_PORT = 11853;

    //endpoint URLs
    public static final String DEVICE_URL = "http://192.168.4.1";
    public static final String SEND_SSID_PASSWORD_URL = "/ronix/wifi/connect";
    public static final String GET_CHIP_ID_URL = "/ronix/getchipid";
    public static final String GET_DEVICE_TYPE_URL = "/ronix/gettypeid";
    public static final String CONTROL_DEVICE_URL = "/ronix/control/command";
    public static final String GET_DEVICE_STATUS = "/ronix/status";
    public static final String GET_SSID_URL = "http://ronixtech.com/ronix_services/task/srv.php";
    public static final String MQTT_URL = "tcp://m13.cloudmqtt.com";
    public static final String MQTT_TOPIC = "ronix/network";

    public static final String LOGIN_URL = "";
    public static final String REGISTER_URL = "";


    //json parameters
    public static final String PARAMETER_ERROR = "error";
    public static final String PARAMETER_ID = "id";
    public static final String PARAMETER_NAME = "name";
    public static final String PARAMETER_FIRST_NAME = "first_name";
    public static final String PARAMETER_LAST_NAME = "last_name";
    public static final String PARAMETER_EMAIL = "email";
    public static final String PARAMETER_FACEBOOK_ACCESS_TOKEN = "access_token";
    public static final String PARAMETER_SSID = "essid";
    public static final String PARAMETER_PASSWORD = "passwd";
    public static final String PARAMETER_DEVICE_TYPE_ID = "unit_type_id";
    public static final String PARAMETER_DEVICE_CHIP_ID = "unit_chip_id_txt";
    public static final String PARAMETER_COMMAND_ZERO = "command_0";
    public static final String PARAMETER_COMMAND_ONE = "command_1";

    public static final String PARAMETER_FIRST_LINE_DIMMING_CONTROL_STATE = "B";
    public static final String PARAMETER_SECOND_LINE_DIMMING_CONTROL_STATE = "C";
    public static final String PARAMETER_THIRD_LINE_DIMMING_CONTROL_STATE = "D";

    public static final String PARAMETER_FIRST_LINE_DIMMING_CONTROL_VALUE = "k";
    public static final String PARAMETER_SECOND_LINE_DIMMING_CONTROL_VALUE = "l";
    public static final String PARAMETER_THIRD_LINE_DIMMING_CONTROL_VALUE = "m";

    public static final String DB_NAME = "main-db";

    //device type constants
    public static final String DEVICE_NAME_IDENTIFIER = "RONIXTECHUNIT_";
    public static final String DEVICE_DEFAULT_PASSWORD = "ronixtechunit";

    //adding floor/room fragment constants
    public static final int SOURCE_NEW_DEVICE = 0;
    public static final int SOURCE_HOME_FRAGMENT = 1;
    public static final int SOURCE_NAV_DRAWER = 2;

    //types constants
    public static final int TYPE_PLACE = 0;
    public static final int TYPE_FLOOR = 1;
    public static final int TYPE_ROOM = 2;
    public static final int TYPE_LINE = 3;

    //notification channel constant, only for api26+
    public static final String CHANNEL_ID = "4";

    public static final int UPDATING_DEVICES_NOTIFICATION = 44;

}