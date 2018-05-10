//Model.java
//creates, validates, and manages calendar events
package edu.cmu.pocketsphinx.demo;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.util.Calendar;
import android.icu.util.TimeZone;
import android.net.Uri;
import android.os.Build;
import android.provider.CalendarContract;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.Arrays;

public class Model {

    //fields
    //list of events
    private ArrayList<Event> events = new ArrayList<>();
    //delimiter for tokenizer
    private String delim = " ";

    //format checking
    private final String[] VALID_MONTHS = {
            "january", "february", "march", "april",
            "may", "june", "july", "august",
            "september", "october", "november", "december"
    };

    private final String[] VALID_DAYS = {
            "first", "second", "third",
            "fourth", "fifth", "sixth",
            "seventh", "eighth", "ninth",
            "tenth", "eleventh", "twelfth",
            "thirteenth", "fourteenth", "fifteenth",
            "sixteenth", "seventeenth", "eighteenth",
            "nineteenth", "twentieth", "twenty first",
            "twenty second", "twenty third", "twenty fourth",
            "twenty fifth", "twenty sixth", "twenty seventh",
            "twenty eighth", "twenty ninth", "thirtieth",
            "thirty first", "thirty second"
    };

    private final String[] VALID_MINUTES = {
            "oh clock", "oh one", "oh two", "oh three",
            "oh four", "oh five", "oh six", "oh seven",
            "oh eight", "oh nine", "ten", "eleven",
            "twelve", "thirteen", "fourteen", "fifteen",
            "sixteen", "seventeen", "eighteen", "nineteen",
            "twenty", "twenty one", "twenty two", "twenty three",
            "twenty four", "twenty five", "twenty six", "twenty seven",
            "twenty eight", "twenty nine", "thirty", "thirty one",
            "thirty two", "thirty three", "thirty four", "thirty five",
            "thirty six", "thirty seven", "thirty eight", "thirty nine",
            "forty", "forty one", "forty two", "forty three",
            "forty four", "forty five", "forty six", "forty seven",
            "forty eight", "forty nine", "fifty", "fifty one",
            "fifty two", "fifty three", "fifty four", "fifty five",
            "fifty six", "fifty seven", "fifty eight", "fifty nine"
    };


    private final String[] VALID_HOURS = {
            "one", "two", "three",
            "four", "five", "six", "seven",
            "eight", "nine", "ten", "eleven",
            "twelve", "thirteen", "fourteen", "fifteen",
            "sixteen", "seventeen", "eighteen", "nineteen",
            "twenty", "twenty one", "twenty two", "twenty three"
    };

    private final String[] SINGLE_DIGITS = Arrays.copyOfRange(VALID_HOURS, 0, 8);


    //constructor
    public Model() {

    }

    //int; IsLeapYear
    //params: int year
    //purpose: determines if given year is a leap year
    //algorithm source: https://en.wikipedia.org/wiki/Leap_year#Algorithm
    public int IsLeapYear(int year) {
        if ((year % 4 == 0) && ((year % 100 != 0) || (year % 400 == 0))) {
            return 29;
        } else {
            return 28;
        }
    }

    //boolean; ValidDate
    //params: int year, int month, int day
    //purpose: checks if given day of the month is a valid day
    @RequiresApi(api = Build.VERSION_CODES.N)
    public boolean validDate(int year, int month, int day) {

        int[] MAX_DATES = {
                31, IsLeapYear(year), 31, 30, //jan, feb, mar, apr
                31, 30, 31, 31, //may, jun, july, aug
                30, 31, 30, 31//sep, oct, nov, dec
        };

        int date_index = month;
        if ((day < MAX_DATES[date_index])) {
            return true;
        }
        return false;
    }

    //int[]; parseDate
    //params: String date
    //purpose: translates string to month-day pair array
    public int[] parseDate(String date) {

        int[] month_day = new int[2];
        String[] tokens = date.split(delim);

        //date example: january thirty first
        //january thirtieth
        //get month
        month_day[0] = Arrays.asList(VALID_MONTHS).indexOf(tokens[0]);
        //get day
        if (tokens.length == 2) {
            month_day[1] = 1 + Arrays.asList(VALID_DAYS).indexOf(tokens[1]);
        } else if (tokens.length == 3) {
            month_day[1] = 1 + Arrays.asList(VALID_DAYS).indexOf(tokens[1] + " " + tokens[2]);
        }

        return month_day;
    }

    //int;[] parseTime
    //params: String time
    //purpose: translates string to hour-minute pair array
    public int[] parseTime(String time) {

        int[] hour_minute = new int[2];
        String[] tokens = time.split(delim);
        //get hour
        int minute_index = 0;
        if (tokens.length > 2 && Arrays.asList(SINGLE_DIGITS).contains(tokens[1])) {
            //not: twelve twelve; digits
            //not: twelve <twenty one>; digits
            //not: twelve nine; length
            //not: twenty two <twenty one>; digits
            hour_minute[0] = 1 + Arrays.asList(VALID_HOURS).indexOf(tokens[0] + " " + tokens[1]);
            minute_index += 2;
        } else {
            hour_minute[0] = 1 + Arrays.asList(VALID_HOURS).indexOf(tokens[0]);
            minute_index++;
        }
        //get minute
        //cases:
        //twenty two --twenty one; mi = 2, l=4
        //twenty two --twenty; mi = 2, l=3
        //twenty --oh clock; mi = 1, l=3
        //twenty --twenty; mi = 1, l=2
        if (tokens.length > 2) {
            if (tokens.length > 3)// mi = 2, l=4
                hour_minute[1] = Arrays.asList(VALID_MINUTES).indexOf(tokens[2] + " " + tokens[3]);
            else if (minute_index == 1) //mi = 1, l=3
                hour_minute[1] = Arrays.asList(VALID_MINUTES).indexOf(tokens[1] + " " + tokens[2]);
            else //mi = 2, l=3
                hour_minute[1] = Arrays.asList(VALID_MINUTES).indexOf(tokens[2]);
        } else {//mi = 1, l=2
            hour_minute[1] = Arrays.asList(VALID_MINUTES).indexOf(tokens[1]);
        }

        return hour_minute;
    }

    //int; CreateEvent
    //params: String title, String date, String time
    //purpose: creates event objects if date given is valid
    @RequiresApi(api = Build.VERSION_CODES.N)
    public int createEvent(String title, String date, String time) {
        //make sure no string are empty
        if (date.isEmpty() || time.isEmpty()) //known bug
            return -1;

        //parse time to hour and minute as integer
        int[] month_day = parseDate(date);
        int[] hour_minute = parseTime(time);

        //to wrap around
        int year = Calendar.getInstance().get(Calendar.YEAR);
        int current_month = Calendar.getInstance().get(Calendar.MONTH);
        if (month_day[0] < current_month)
            year++;

        //validation
        if (!validDate(year, month_day[0], month_day[1]))
            return -2;

        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        //known issue or bug: year is only within year of current date
        start.set(year, month_day[0], month_day[1], hour_minute[0], hour_minute[1]);
        end.set(year, month_day[0], month_day[1], hour_minute[0] + 1, hour_minute[1]);
        events.add(new Event(title, start, end));
        return 1;
    }

    //void; pushEvent
    //params: Context con
    //purpose: sends last added event to local Android calendar
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void pushEvent(Context con) {

        long calID = 1;
        android.icu.util.TimeZone tz = TimeZone.getDefault();

        ContentResolver cr = con.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.DTSTART, events.get(events.size() - 1).getBegin());
        values.put(CalendarContract.Events.DTEND, events.get(events.size() - 1).getEnd());
        values.put(CalendarContract.Events.TITLE, events.get(events.size() - 1).getTitle());//"A Test Event from android app");
        values.put(CalendarContract.Events.DESCRIPTION, "Automated product of Sphinx-Assistant");
        values.put(CalendarContract.Events.CALENDAR_ID, calID);
        values.put(CalendarContract.Events.EVENT_TIMEZONE, tz.getID());

        if (ContextCompat.checkSelfPermission(con, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);
            assert uri != null;
            long eventID = Long.parseLong(uri.getLastPathSegment());
            Uri.Builder builder = CalendarContract.CONTENT_URI.buildUpon();
            builder.appendPath("time");
            ContentUris.appendId(builder, events.get(events.size() - 1).getBegin());
            Intent intent = new Intent(Intent.ACTION_VIEW).setData(builder.build());
            con.startActivity(intent);
        }

    }
}

