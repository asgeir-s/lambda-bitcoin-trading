package systems;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MInteger;
import com.tictactec.ta.lib.RetCode;
import util.Database;
import util.TradersBit;
import util.model.Tick;
import util.model.TradesBitTrade;

import java.sql.Connection;
import java.util.List;

public class Aroon {

    // JUMO
    final static String apiKey = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdHJlYW1JZCI6ImI5MmU5MzEzLTNiMTgtNDIzYi05OGVmLTQ4ZDQxZDdmOWMyOCIsImFwaUtleUlkIjoiYjBlOTNiYzctNDhhZi00ZDdjLWJmOTAtM2EyMWQzYWM1NWM3IiwidXNlcklkIjoiYXV0aDB8NTc1MDUzMTU0ZTVhMTg5NzcwMmE4MDhiIiwiaWF0IjoxNDY2MDAzMDMwLCJhdWQiOiI3Vk5TMlRjMklpUUIyUHZqVUJjYjU3NDRxSDllWTdpQiIsImlzcyI6InRyYWRlcnNiaXQuY29tIiwic3ViIjoiYXV0aDB8NTc1MDUzMTU0ZTVhMTg5NzcwMmE4MDhiIn0.kFm7TxbvGFflo3CtcrYBuL2IWlgabx1y56FBrY-O03s";
    final static String streamId = "b92e9313-3b18-423b-98ef-48d41d7f9c28";

    final static double thresholdLong = 25;
    final static double thresholdShort = -25;
    final static double thresholdCloseLong = 0;
    final static double thresholdCloseShort = 0;
    final static int periods = 11;


    public String handler(SNSEvent event, Context context) {
        //List<Tick> ticks = Sns2Tick.sns2Ticks(event);

        Connection database = Database.getConnection();
        List<Tick> ticks = Database.getTicks(database, 7200, 30);

        TradesBitTrade status = TradersBit.getStatus(apiKey, streamId);

        TradersBit.postSignal(apiKey, streamId, compute(status.getSignal(), ticks));
        return "ok";
    }


    public static int compute(int status, List<Tick> ticks) {

        //System.out.println("number of ticks: " + ticks.size());

        double[] highPrice = new double[ticks.size()];
        double[] lowPrice = new double[ticks.size()];
        double[] closePrice = new double[ticks.size()];

        for (int i = 0; i < ticks.size(); i++) {
            highPrice[i] = ticks.get(i).getHigh();
            lowPrice[i] = ticks.get(i).getLow();
            closePrice[i] = ticks.get(i).getClose();
        }

        double[] aroonOsc = new double[ticks.size()];

        MInteger begin = new MInteger();
        MInteger length = new MInteger();


        Core c = new Core();
        RetCode retCode = c.aroonOsc(0, ticks.size() - 1, highPrice, lowPrice, periods, begin, length, aroonOsc);


    //    for (int i = 0; i < length.value; i++) {
    //        System.out.println(begin.value + i + ": time: " + ticks.get(begin.value + i).getTickEndTime() + ", aroonOsc: " + aroonOsc[i] + " at price " + closePrice[begin.value + i]);
    //    }

        if (retCode == RetCode.Success) {
            double lastAroonOsc = aroonOsc[length.value - 1];
            //System.out.println("lastAroonOsc: " + lastAroonOsc);

            if (lastAroonOsc > thresholdLong) {
                return 1;
            } else if (lastAroonOsc < thresholdShort) {
                return -1;
            } else if (!(status == 0)) {
                if ((lastAroonOsc < thresholdCloseLong) && status == 1) {
                    return 0;
                } else if ((lastAroonOsc > thresholdCloseShort) && status == -1) {
                    return 0;
                }
            }
        } else {
            System.out.println("ta-lib could not compute this");
        }
        return 9;
    }
}
