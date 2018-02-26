package org.asdnh.attendancescanner;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.realm.RealmObject;

/**
 * Created by Jared on 2/26/2018.
 */

public class Student extends RealmObject{

    public String date;
    public String name;
    public String timeOut;
    public String destination;
    public String timeIn;

    public Student() {

        date = "????";

    }

}
