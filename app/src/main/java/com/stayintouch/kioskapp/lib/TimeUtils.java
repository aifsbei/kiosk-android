package com.stayintouch.kioskapp.lib;

import java.util.Locale;

public class TimeUtils {

    public static String formatMillis(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        seconds %= 60;
        minutes %= 60;

        if (hours > 0) {
            return String.format(Locale.FRANCE, "%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format(Locale.FRANCE, "%02d:%02d", minutes, seconds);
        }
    }

    public static int[] getHoursAndMinutesFromMillis(long millis) {
        int totalMinutes = (int) (millis / (1000 * 60));
        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;
        return new int[]{hours, minutes};
    }
}