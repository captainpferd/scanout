package org.asdnh.attendancescanner;

import io.realm.RealmObject;

// Class to represent a student in the sign out logs

public class Student extends RealmObject{

    String date;
    String name;
    String timeOut;
    String destination;
    String timeIn;

    public Student() {

    }

}
