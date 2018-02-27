package org.asdnh.attendancescanner;

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

    }

}
