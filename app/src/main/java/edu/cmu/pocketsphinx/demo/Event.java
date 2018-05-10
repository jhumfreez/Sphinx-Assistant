//Event.java
//stores event information to be added to calendar
package edu.cmu.pocketsphinx.demo;


import android.icu.util.Calendar;
import android.os.Build;
import android.support.annotation.RequiresApi;

public class Event {
    //fields
   private String title;
   private Calendar start;
   private Calendar end;

    //constructor
    public Event(String title, Calendar s, Calendar e){
        this.title = title;
        start = s;
        end = e;
    }

    //returns title of event
    public String getTitle(){
        return title;
    }

    //returns year of event
    @RequiresApi(api = Build.VERSION_CODES.N)
    public int getYear(){
    return start.get(Calendar.YEAR);
    }

    //returns day of event
    @RequiresApi(api = Build.VERSION_CODES.N)
    public int getDay(){
        return start.get(Calendar.DAY_OF_MONTH);
    }

    //returns month of event
    @RequiresApi(api = Build.VERSION_CODES.N)
    public int getMonth(){
        return start.get(Calendar.MONTH);
    }

    //returns beginning time of event
    @RequiresApi(api = Build.VERSION_CODES.N)
    public long getBegin() {
        return start.getTimeInMillis();
    }

    //returns end time of event
    @RequiresApi(api = Build.VERSION_CODES.N)
    public long getEnd() {
        return end.getTimeInMillis();
    }
}
