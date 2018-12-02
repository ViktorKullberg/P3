package se.mau.ah0987.redditp3;


/**
 * Utility
 * Converts the reddit post date to an epoch time (long)
 */
public class DateConverter {
    public static long convertDate(String dateEpoch){
        String test = dateEpoch.substring(0, dateEpoch.length()-2);
        Double doubleObject = new Double(test);
        double number = doubleObject.doubleValue();
        long finalDate = (long)(number*1000000000);
        return finalDate;
    }
}
