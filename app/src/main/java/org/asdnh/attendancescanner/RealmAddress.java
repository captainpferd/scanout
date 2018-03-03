package org.asdnh.attendancescanner;

/**
 * Created by Jared on 2/26/2018.
 * Uses shared preferences to set realm data appropriately
 */

public class RealmAddress {
    private static  String INSTANCE_ADDRESS = "attendance-scanner.us1a.cloud.realm.io";
    static String AUTH_URL = "https://" + INSTANCE_ADDRESS + "/auth";
    static String REALM_BASE_URL = "realms://" + INSTANCE_ADDRESS;

    public static String getInstanceAddress() {
        return INSTANCE_ADDRESS;
    }

    public static void setInstanceAddress(String instanceAddress) {
        INSTANCE_ADDRESS = instanceAddress;
    }

    public static String getAuthUrl() {
        return AUTH_URL;
    }

    public static void setAuthUrl(String authUrl) {
        AUTH_URL = authUrl;
    }

    public static String getRealmBaseUrl() {
        return REALM_BASE_URL;
    }

    public static void setRealmBaseUrl(String realmBaseUrl) {
        REALM_BASE_URL = realmBaseUrl;
    }
}
