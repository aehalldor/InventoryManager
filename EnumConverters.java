public class EnumConverters {
    public static enum WEEK_DAY{MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY}; 

    public static WEEK_DAY stringToWeekday(String in) {
        if(in.equals("Monday")) {
            return WEEK_DAY.MONDAY;
        }
        if(in.equals("Tuesday")) {
            return WEEK_DAY.TUESDAY;
        }
        if(in.equals("Wednesday")) {
            return WEEK_DAY.WEDNESDAY;
        }
        if(in.equals("Thursday")) {
            return WEEK_DAY.THURSDAY;
        }
        if(in.equals("Friday")) {
            return WEEK_DAY.FRIDAY;
        }
        if(in.equals("Saturday")) {
            return WEEK_DAY.SATURDAY;
        }
        if(in.equals("Sunday")) {
            return WEEK_DAY.SUNDAY;
        }
        return WEEK_DAY.MONDAY;
    }
}
