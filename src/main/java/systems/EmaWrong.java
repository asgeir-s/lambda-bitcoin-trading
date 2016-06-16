package systems;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MAType;
import com.tictactec.ta.lib.MInteger;
import com.tictactec.ta.lib.RetCode;
import util.Database;
import util.TradersBit;
import util.model.Tick;
import util.model.TradesBitTrade;

import java.sql.Connection;
import java.util.List;

public class EmaWrong {
    // Woma
    // res: 14429, period: 1800, trades: 920
    final static String apiKey = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdHJlYW1JZCI6IjVkZmFmYTgxLWVjOWUtNGFkNi05MGVmLTA1MTgwNDdlNjEyOCIsImFwaUtleUlkIjoiM2JjZWEwOTEtYWI2NC00ZmRjLWIwYTItYjViMzZiNWU1OGUwIiwidXNlcklkIjoiYXV0aDB8NTZiYWU2OGRkYWMyYWM1YTUzODk0MzAzIiwiaWF0IjoxNDY2MTE5MDU2LCJhdWQiOiI3Vk5TMlRjMklpUUIyUHZqVUJjYjU3NDRxSDllWTdpQiIsImlzcyI6InRyYWRlcnNiaXQuY29tIiwic3ViIjoiYXV0aDB8NTZiYWU2OGRkYWMyYWM1YTUzODk0MzAzIn0.kya_AVGF-YhW5fSKxeK18VLnWvjVWPt0ABlU5pZcMMA";
    final static String streamId = "5dfafa81-ec9e-4ad6-90ef-0518047e6128";

    final static int fastEma = 30;
    final static int slowEma = 60;



    public String handler(SNSEvent event, Context context) {
        //List<Tick> ticks = Sns2Tick.sns2Ticks(event);

        Connection database = Database.getConnection();
        List<Tick> ticks = Database.getTicks(database, 1800, 61);

        TradesBitTrade status = TradersBit.getStatus(apiKey, streamId);

        TradersBit.postSignal(apiKey, streamId, compute(status.getSignal(), ticks));

        System.out.println("last tick time: " + ticks.get(ticks.size() - 1).getTickEndTime());
        return "ok";
    }


    public static int compute(int status, List<Tick> ticks) {

        //  System.out.println("number of ticks: " + ticks.size());

        double[] highPrice = new double[ticks.size()];
        double[] lowPrice = new double[ticks.size()];
        double[] closePrice = new double[ticks.size()];

        for (int i = 0; i < ticks.size(); i++) {
            highPrice[i] = ticks.get(i).getHigh();
            lowPrice[i] = ticks.get(i).getLow();
            closePrice[i] = ticks.get(i).getClose();
        }


        double[] outFastEma = new double[ticks.size()];
        double[] outSlowEma = new double[ticks.size()];


        MInteger begin = new MInteger();
        MInteger length = new MInteger();


        Core c = new Core();
        RetCode retCode = c.ema(0, ticks.size() - 1, closePrice, fastEma, begin, length, outFastEma);

        RetCode retCode2 = c.ema(0, ticks.size() - 1, closePrice, slowEma, begin, length, outSlowEma);

        //  for (int i = 0; i < length.value; i++) {
        //      System.out.println(begin.value + i + ": time: " + ticks.get(begin.value + i).getTickEndTime() + ", upperBand: " + outUpperBand[i] + ", middleBand: " + outMiddleBand[i] + ", lowerBand: " + outLowerBand[i] + " at price " + closePrice[begin.value + i]);
        //  }

        if (retCode == RetCode.Success && retCode2 == RetCode.Success) {
            double thisFast = outFastEma[length.value - 1];
            double thisSlow = outSlowEma[length.value - 1];

            double thisClose = closePrice[length.value - 1];


            // System.out.println("thisClose: " + thisClose + ", thisUpper: " + thisUpper + ", thisLower: " + thisLower);

            if (thisFast > thisSlow) {
                return 1;
            } else if (thisFast < thisSlow) {
                return -1;
            }
        } else {
            System.out.println("ta-lib could not compute this");
        }

        return 9;
    }

}
