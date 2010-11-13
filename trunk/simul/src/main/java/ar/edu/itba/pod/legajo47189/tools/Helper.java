package ar.edu.itba.pod.legajo47189.tools;

import java.util.Calendar;
import java.util.Random;

public class Helper {
    
    public static String RandomIdGenerator()
    {
        long  milis = Calendar.getInstance().getTimeInMillis();
        Random generator2 = new Random(milis);
        return generator2.nextInt() + "_" + milis;
    }
    
    public static long GetNow()
    {
        return Calendar.getInstance().getTimeInMillis();
    }
    
    
}
