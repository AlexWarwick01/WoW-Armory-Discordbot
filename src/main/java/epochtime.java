import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class epochtime {
    //Epoch time is needed to effectively render the character portrait and any other images
    public static int getEpochTime(){
        Date today = Calendar.getInstance().getTime();
        // Constructs a SimpleDateFormat using the given pattern
        SimpleDateFormat crunchifyFormat = new SimpleDateFormat("MMM dd yyyy HH:mm:ss.SSS zzz");
        String currentTime = crunchifyFormat.format(today);

        try {
            Date date = crunchifyFormat.parse(currentTime);

            // getTime() returns the number of milliseconds since January 1, 1970, 00:00:00 GMT represented by this Date object.
            long epochTime = date.getTime();

            System.out.println("Current Time in Epoch: " + epochTime);
            return (int) epochTime;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return 0;
    }

}
