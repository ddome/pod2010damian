package ar.edu.itba.pod.legajo47189.tools;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class Helper {
    
    public static String RandomIdGenerator()
    {
        long  milis = Calendar.getInstance().getTimeInMillis();
        Random generator2 = new Random(milis);
        return generator2.nextInt() + "_" + milis;
    }
    
    public static List<Integer> generateSequence(Integer number, Integer max)
    {
        List<Integer> sequence = new ArrayList<Integer>();
        long  milis = Calendar.getInstance().getTimeInMillis();
        Random generator = new Random(milis);
        
        if (number <= 0 || max <= 0 || number > max)
        {
            return sequence;
        }
        
        int random;
        System.out.println(max);
        number = max > number ? max : number;
        for (int i=0; i<number; i++)
        {
            do
            {
                random = generator.nextInt(max);
            } while (sequence.contains(random));
            sequence.add(random);
        }
        
        return sequence;
    }
    
    public static long GetNow()
    {
        return Calendar.getInstance().getTimeInMillis();
    }
}
