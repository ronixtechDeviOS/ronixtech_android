package com.ronixtech.ronixhome;

public class Constants {
    public static final String PACKAGE_NAME = "com.ronixtech.ronixhome";

    //endpoint URLs
    public static final String DEVICE_URL = "http://192.168.4.1";
    public static final String DEVICE_STATUS_CONTROL_URL = "/ronix/json/post";

    public static final String SEND_SSID_PASSWORD_URL = "/ronix/wifi/connect";
    public static final String GET_CHIP_ID_URL = "/ronix/getchipid";
    public static final String GET_DEVICE_TYPE_URL = "/ronix/gettypeid";
    public static final String CONTROL_DEVICE_URL = "/ronix/control/command";
    public static final String CONTROL_SOUND_DEVICE_CHANGE_MODE_URL = "/change_mode";
    public static final String GET_DEVICE_STATUS = "/ronix/status";
    public static final String DEVICE_REBOOT_URL = "/ronix/reboot";
    public static final String DEVICE_FACTORY_RESET = "/ronix/factory_reset";

    public static final String DEVICE_RESET_PAIRINGS_URL = "/ronix/reset_pairings";
    public static final String DEVICE_ADD_PAIRINGS_URL = "/ronix/pair_controller";

    public static final String DEVICE_ADD_LINE_PAIRINGS_URL = "/ronix/pair_device";

    public static final String GET_SSID_URL = "http://ronixtech.com/ronix_services/task/srv.php";
    public static final String MQTT_URL = "tcp://207.191.231.32"; //tcp://m13.cloudmqtt.com"
    public static final int MQTT_PORT = 1993; //11853
    public static final String MQTT_USERNAME = "ronixtech"; //"qumrwmme"
    public static final String MQTT_PASSWORD = "12345678?"; //"oJHjXS7Xi0F9"
    public static final String MQTT_TOPIC_STATUS = "ronix/status/%s"; //%s is the chip_id
    public static final String MQTT_TOPIC_CONTROL = "ronix/control/%s"; //%s is the chip_id

    public static final String DEVICE_FIRMWARE_URL = "http://ronixtech.com/ronix_services/wifi_updates/%s/%s/%s";
    //http://ronixtech.com/ronix_services/wifi_updates/%FIRMWARE_VERSION%/%DEVICE_TYPE%/%user1/2.bin%

    public static final String DEVICE_FIRMWARE_URL_1 = "http://ronixtech.com/ronix_services/wifi_updates/user1.bin";
    public static final String DEVICE_FIRMWARE_URL_2 = "http://ronixtech.com/ronix_services/wifi_updates/user2.bin";

    public static final String DEVICE_PIR_FIRMWARE_URL_1 = "http://ronixtech.com/ronix_services/wifi_updates/101406/PIR_WI_01/user1.bin";
    public static final String DEVICE_PIR_FIRMWARE_URL_2 = "http://ronixtech.com/ronix_services/wifi_updates/101406/PIR_WI_01/user2.bin";

    public static final String DEVICE_LATEST_FIRMWARE_VERSIONS_URL = "http://ronixtech.com/ronix_services/wifi_updates/switches_controller/latest_version.php";

    public static final int SERVER_TIMEOUT = 5000;
    public static final int SERVER_NUMBER_OF_RETRIES = 2;

    public static final String DEVICE_DEFAULT_WIFI_FIRMWARE_VERSION = "0";
    public static final String DEVICE_DEFAULT_HW_FIRMWARE_VERSION = "0";

    public static final String DEVICE_GET_FIRMWARE_FILE_NAME_URL = "/flash/next";
    public static final String DEVICE_UPLOAD_FIRMWARE_URL = "/flash/upload";
    public static final String DEVICE_FIRMWARE_REBOOT_URL = "/flash/reboot";

    public static final String DEVICE_HARDWARE_SYNC_URL = "/pgm/sync";
    public static final String DEVICE_HARDWARE_UPLOAD_FIRMWARE_URL = "/pgm/upload";

    public static final String DEVICE_SOUND_SYSTEM_SHUTDOWN_URL = "/shutdown";

    public static final String LOGIN_URL = "";
    public static final String REGISTER_URL = "";

    public static final String DEVICE_FIRMWARE_FILE_NAME_1 = "user1.bin";
    public static final String DEVICE_FIRMWARE_FILE_NAME_2 = "user2.bin";
    public static final String DEVICE_HW_FIRMWARE_FILE_NAME = "hw.hex";
    public static final String DEVICE_HW_FIRMWARE_ONLINE_FILE_NAME = "hw.bin";

    //post parameters
    public static final String PARAMETER_ERROR = "error";
    public static final String PARAMETER_ID = "id";
    public static final String PARAMETER_NAME = "name";
    public static final String PARAMETER_FIRST_NAME = "first_name";
    public static final String PARAMETER_LAST_NAME = "last_name";
    public static final String PARAMETER_EMAIL = "email";
    public static final String PARAMETER_FACEBOOK_ACCESS_TOKEN = "access_token";
    public static final String PARAMETER_SSID = "U_W_SSD";
    public static final String PARAMETER_PASSWORD = "U_W_PWD";
    public static final String PARAMETER_SSID_GET_METHOD = "essid";
    public static final String PARAMETER_PASSWORD_GET_METHOD = "passwd";
    public static final String PARAMETER_DEVICE_TYPE_ID = "unit_type_id";
    public static final String PARAMETER_DEVICE_CHIP_ID = "unit_chip_id_txt";
    public static final String PARAMETER_COMMAND_ZERO = "command_0";
    public static final String PARAMETER_COMMAND_ONE = "command_1";
    public static final String PARAMETER_ACCESS_TOKEN = "R_TOKEN";
    public static final String PARAMETER_SOUND_CONTROLLER_MODE = "mode";
    public static final String PARAMETER_SOUND_CONTROLLER_SHUTDOWN_MODE = "option";
    public static final String PARAMETER_SOUND_CONTROLLER_OPTION_SHUTDOWN = "shutdown";
    public static final String PARAMETER_SOUND_CONTROLLER_OPTION_REBOOT = "reboot";
    public static final String PARAMETER_DEVICE_FIRMWARE_URL = "R_FWURL";

    public static final String PARAMETER_FIRST_LINE_DIMMING_CONTROL_STATE = "B";
    public static final String PARAMETER_SECOND_LINE_DIMMING_CONTROL_STATE = "C";
    public static final String PARAMETER_THIRD_LINE_DIMMING_CONTROL_STATE = "D";

    public static final String PARAMETER_FIRST_LINE_DIMMING_CONTROL_VALUE = "k";
    public static final String PARAMETER_SECOND_LINE_DIMMING_CONTROL_VALUE = "l";
    public static final String PARAMETER_THIRD_LINE_DIMMING_CONTROL_VALUE = "m";

    public static final String PARAMETER_SOUND_CONTROLLER_MODE_LINE_IN = "line1";
    public static final String PARAMETER_SOUND_CONTROLLER_MODE_LINE_IN_2 = "line2";
    public static final String PARAMETER_SOUND_CONTROLLER_MODE_UPNP = "stream";
    public static final String PARAMETER_SOUND_CONTROLLER_MODE_USB = "usb";

    public static final String DB_NAME = "main-db";

    //device type constants
    public static final String DEVICE_NAME_IDENTIFIER = "RONIXTECHUNIT_";
    public static final String DEVICE_DEFAULT_PASSWORD = "ronixtechunit";
    public static final String DEVICE_DEFAULT_ACCESS_TOKEN = "ronix_token";

    //adding floor/room fragment constants
    public static final int SOURCE_NEW_DEVICE = 0;
    public static final int SOURCE_HOME_FRAGMENT = 1;
    public static final int SOURCE_NAV_DRAWER = 2;
    public static final int SOURCE_NEW_PLACE = 3;

    //removing network constant
    public static final int REMOVE_NETWORK_FROM_DB_NO = 0;
    public static final int REMOVE_NETWORK_FROM_DB_YES = 1;

    //types constants
    public static final int TYPE_PLACE = 0;
    public static final int TYPE_FLOOR = 1;
    public static final int TYPE_ROOM = 2;
    public static final int TYPE_LINE = 3;

    //action constants
    public static final int ACTION_YES = 0;
    public static final int ACTION_NO = 1;
    public static final int ACTION_CANCEL = 2;

    //notification channel constant, only for api26+
    public static final String CHANNEL_ID = "4";

    public static final int UPDATING_DEVICES_NOTIFICATION = 44;

    public static final int DELAY_TIME_MS = 250;

}