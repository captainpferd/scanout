package org.asdnh.attendancescanner;

/**
 * Created by Jared on 2/26/2018.
 * Uses shared preferences to set realm data appropriately
 */

public class RealmAddress {

    //Store realm URL in its various forms
    private static  String INSTANCE_ADDRESS;
    private static String AUTH_URL;
    private static String REALM_BASE_URL;

    // Set the instance address to the value passed in from shared preferences and set the auth and base URL appropriately from there
    public static void setInstanceAddress(String instanceAddress) {

        INSTANCE_ADDRESS = instanceAddress;
        setAuthUrl();
        setRealmBaseUrl();
    }

    //Various setters and getters.  The setters either prefix or suffix the address depending on what is needed

    public static String getInstanceAddress() {
        return INSTANCE_ADDRESS;
    }


    public static String getAuthUrl() {
        return AUTH_URL;
    }


    public static void setAuthUrl() {
        AUTH_URL = "http://" + INSTANCE_ADDRESS + "/auth";
    }


    public static String getRealmBaseUrl() {
        return REALM_BASE_URL;
    }


    public static void setRealmBaseUrl() {
        REALM_BASE_URL = "realm://" + INSTANCE_ADDRESS;
    }

}
