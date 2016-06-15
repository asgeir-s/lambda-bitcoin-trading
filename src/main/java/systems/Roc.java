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

public class Roc {

    // Rocky
    final static String apiKey = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdHJlYW1JZCI6IjYxNmZkZTQ4LTk3NjYtNDNlMi04MDlmLTNlMDRjM2Q4MGU4YSIsImFwaUtleUlkIjoiYjhiN2I0MWMtMDUyMy00OTAzLWEyNDgtOGMyMWNkMGI3YjkyIiwidXNlcklkIjoiYXV0aDB8NTc1MDUzMTU0ZTVhMTg5NzcwMmE4MDhiIiwiaWF0IjoxNDY2MDA1MDQ4LCJhdWQiOiI3Vk5TMlRjMklpUUIyUHZqVUJjYjU3NDRxSDllWTdpQiIsImlzcyI6InRyYWRlcnNiaXQuY29tIiwic3ViIjoiYXV0aDB8NTc1MDUzMTU0ZTVhMTg5NzcwMmE4MDhiIn0.Wbtkg6gc8BBjEmjIFRUeeeipDCZt71ni83jtQT6Muhs";
    final static String streamId = "616fde48-9766-43e2-809f-3e04c3d80e8a";

    final static double thresholdLong = 0;
    final static double thresholdShort = 0;
    final static double thresholdCloseLong = 0;
    final static double thresholdCloseShort = 0;
    final static int periods = 10;


    public String handler(SNSEvent event, Context context) {
        //List<Tick> ticks = Sns2Tick.sns2Ticks(event);

        Connection database = Database.getConnection();
        List<Tick> ticks = Database.getTicks(database, 21600, 30);

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

        double[] roc = new double[ticks.size()];

        MInteger begin = new MInteger();
        MInteger length = new MInteger();


        Core c = new Core();
        RetCode retCode = c.roc(0, ticks.size()-1, closePrice, periods, begin, length, roc);

        for (int i = 0; i < length.value; i++) {
            System.out.println(begin.value + i + ": time: " + ticks.get(begin.value + i).getTickEndTime() + ", aroonOsc: " + roc[i] + " at price " + closePrice[begin.value + i]);
        }

        if (retCode == RetCode.Success) {
            double lastRoc = roc[length.value - 1];
            System.out.println("lastRoc: " + lastRoc);

            if (lastRoc < thresholdLong) {
                return 1;
            } else if (lastRoc > thresholdShort) {
                return -1;
            } else if (!(status.getSignal() == 0)) {
                if ((lastRoc > thresholdCloseLong) && status.getSignal() == 1) {
                    return 0;
                } else if ((lastRoc < thresholdCloseShort) && status.getSignal() == -1) {
                    return 0;
                }
            }
        } else {
            System.out.println("ta-lib could not compute this");
        }
        return 9;
    }

}
