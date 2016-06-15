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

public class WilliamR {

    //Takazi
    final static String apiKey = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdHJlYW1JZCI6ImI1YjQ0OGRmLWFmYWItNGNlNC1hNjk5LTEzMDY0YmM1YmIzMSIsImFwaUtleUlkIjoiYzg0ZGZiNTQtYzhkMi00OGNiLWI1Y2MtZGFhNmE4MTg4ZTg3IiwidXNlcklkIjoiYXV0aDB8NTc1MDUzMTU0ZTVhMTg5NzcwMmE4MDhiIiwiaWF0IjoxNDY2MDAzMTUxLCJhdWQiOiI3Vk5TMlRjMklpUUIyUHZqVUJjYjU3NDRxSDllWTdpQiIsImlzcyI6InRyYWRlcnNiaXQuY29tIiwic3ViIjoiYXV0aDB8NTc1MDUzMTU0ZTVhMTg5NzcwMmE4MDhiIn0.XUd8IbFN1LmPZIWlL2QoaSAdzp-3xjXp1xgND2KaJ0I";
    final static String streamId = "b5b448df-afab-4ce4-a699-13064bc5bb31";

    final static double thresholdLong = -10;
    final static double thresholdShort = -90;
    final static double thresholdCloseLong = -50;
    final static double thresholdCloseShort = -50;
    final static int periods = 14;


    public String handler(SNSEvent event, Context context) {
        //List<Tick> ticks = Sns2Tick.sns2Ticks(event);

        Connection database = Database.getConnection();
        List<Tick> ticks = Database.getTicks(database, 7200, 30);

        TradesBitTrade status = TradersBit.getStatus(apiKey, streamId);

        TradersBit.postSignal(apiKey, streamId, compute(status, ticks));
        return "ok";
    }


    private static int compute(TradesBitTrade status, List<Tick> ticks) {

        System.out.println("number of ticks: " + ticks.size());

        double[] highPrice = new double[ticks.size()];
        double[] lowPrice = new double[ticks.size()];
        double[] closePrice = new double[ticks.size()];

        for (int i = 0; i < ticks.size(); i++) {
            highPrice[i] = ticks.get(i).getHigh();
            lowPrice[i] = ticks.get(i).getLow();
            closePrice[i] = ticks.get(i).getClose();
        }

        double[] out = new double[ticks.size()];
        MInteger begin = new MInteger();
        MInteger length = new MInteger();


        Core c = new Core();
        RetCode retCode = c.willR(0, ticks.size() - 1, highPrice, lowPrice, closePrice, periods, begin, length, out);
        //RetCode retCode = c.macd(0, closePrice.length - 1, closePrice, 26, 6, 9, begin, length, macd, macdSignal, macdHist);

     //   for (int i = 0; i < length.value; i++) {
     //       System.out.println(begin.value + i + ": time: " + ticks.get(begin.value + i).getTickEndTime() + ", willR: " + out[i] + " at price " + closePrice[begin.value + i]);
     //   }

        double lastWill = out[length.value-1];
        System.out.println("lastWill: " + lastWill);

        if (lastWill < thresholdLong) {
            return 1;
        } else if (lastWill > thresholdShort) {
            return -1;
        } else if (!(status.getSignal() == 0)) {
            if ((lastWill > thresholdCloseLong) && status.getSignal() == 1) {
                return 0;
            } else if ((lastWill < thresholdCloseShort) && status.getSignal() == -1) {
                return 0;
            }
        }
        return 9;
    }

}
