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

public class Ema {

    // good 6500 - 1800
    // emma
    final static String apiKey = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdHJlYW1JZCI6IjA3ZmRmNzUxLTdhOTctNDg5OS05YTU3LTY5NThjMjBmZDY1ZSIsImFwaUtleUlkIjoiNGUzNTdjZjItYWZkOC00ZDI5LWE5ZDItYzE0Nzg1ZDFhNTRkIiwidXNlcklkIjoiYXV0aDB8NTZiYWU2OGRkYWMyYWM1YTUzODk0MzAzIiwiaWF0IjoxNDY2MDYyNDQxLCJhdWQiOiI3Vk5TMlRjMklpUUIyUHZqVUJjYjU3NDRxSDllWTdpQiIsImlzcyI6InRyYWRlcnNiaXQuY29tIiwic3ViIjoiYXV0aDB8NTZiYWU2OGRkYWMyYWM1YTUzODk0MzAzIn0.135z1sXyWPXsFGZuhACHQvwUILQDDFWG7idNrJTvMn4";
    final static String streamId = "07fdf751-7a97-4899-9a57-6958c20fd65e";

    final static int fastEma = 26;
    final static int slowEma = 36;


    public String handler(SNSEvent event, Context context) {
        //List<Tick> ticks = Sns2Tick.sns2Ticks(event);

        Connection database = Database.getConnection();
        List<Tick> ticks = Database.getTicks(database, 1800, 40);

        TradesBitTrade status = TradersBit.getStatus(apiKey, streamId);

        TradersBit.postSignal(apiKey, streamId, compute(status.getSignal(), ticks));

        System.out.println("last tick time: " + ticks.get(ticks.size() - 1).getTickEndTime());
        return "ok";
    }


    public static int compute(int status, List<Tick> ticks) {

        //  System.out.println("number of ticks: " + ticks.size());

        double[] closePrice = new double[ticks.size()];

        for (int i = 0; i < ticks.size(); i++) {
            closePrice[i] = ticks.get(i).getClose();
        }

        double[] outFastEma = new double[ticks.size()];
        double[] outSlowEma = new double[ticks.size()];


        MInteger fastBegin = new MInteger();
        MInteger fastLength = new MInteger();


        MInteger slowBegin = new MInteger();
        MInteger slowLength = new MInteger();


        Core c = new Core();
        RetCode retCode = c.ema(0, ticks.size() - 1, closePrice, fastEma, fastBegin, fastLength, outFastEma);
        RetCode retCode2 = c.ema(0, ticks.size() - 1, closePrice, slowEma, slowBegin, slowLength, outSlowEma);

       //   for (int i = 0; i < slowEma.value; i++) {
       //     System.out.println(slowBegin.value + i + ": time: " + ticks.get(slowBegin.value + i).getTickEndTime() + ", fasetEma: " + outFastEma[i] + ", slowEma: " + outSlowEma[i] + " at price " + closePrice[begin.value + i]);
       // }

        if (retCode == RetCode.Success && retCode2 == RetCode.Success) {
            double thisFast = outFastEma[fastLength.value - 1];
            double thisSlow = outSlowEma[slowLength.value - 1];


             //System.out.println("thisFast: " + thisFast + ", thisSlow: " + thisSlow);

            if (thisFast > thisSlow +4) {
                return 1;
            } else if (thisFast < thisSlow -4) {
                return -1;
            }
        } else {
            System.out.println("ta-lib could not compute this");
        }

        return 9;
    }

}
