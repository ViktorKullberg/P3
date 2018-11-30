package se.mau.ah0987.redditp3;

import android.util.Log;

public class DateConverter {
    public static long convertDate(String dateEpoch){
        String test = dateEpoch.substring(0, dateEpoch.length()-2);
        Log.d("TESTDATE", String.valueOf(test));
        Double doubleObject = new Double(test);
        double number = doubleObject.doubleValue();
        long finalDate = (long)(number*1000000000);
        Log.d("DATETIME", String.valueOf(finalDate));
        return finalDate;
    }
}
