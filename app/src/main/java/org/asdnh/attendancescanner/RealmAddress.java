package org.asdnh.attendancescanner;

/**
 * Created by Jared on 2/26/2018.
 * Uses shared preferences to set realm data appropriately
 */

public class RealmAddress {

    private static  String INSTANCE_ADDRESS;
    private static String AUTH_URL;
    private static String REALM_BASE_URL;


    public static String getInstanceAddress() {
        return INSTANCE_ADDRESS;
    }


    public static void setInstanceAddress(String instanceAddress) {

        INSTANCE_ADDRESS = instanceAddress;
        setAuthUrl();
        setRealmBaseUrl();
    }


    public static String getAuthUrl() {
        return AUTH_URL;
    }


    public static void setAuthUrl() {
        AUTH_URL = "https://" + INSTANCE_ADDRESS + "/auth";
    }


    public static String getRealmBaseUrl() {
        return REALM_BASE_URL;
    }


    public static void setRealmBaseUrl() {
        REALM_BASE_URL = "realms://" + INSTANCE_ADDRESS;
    }

}
