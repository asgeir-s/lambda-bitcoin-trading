package systems;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MInteger;
import com.tictactec.ta.lib.RetCode;
import util.Database;
import util.model.Tick;
import util.TradersBit;

import java.sql.Connection;
import java.util.List;

public class Macd1 {
    String apiKey = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdHJlYW1JZCI6ImVlMWNjMTRiLTNhZDYtNDg2OC05MjllLTYzNjVkMTcxN2U5YSIsImFwaUtleUlkIjoiMzdiZWQxNDktMDY5ZS00YWMyLTlhMjMtZmM2NDBmOTdhMjMyIiwidXNlcklkIjoiYXV0aDB8NTZiNzY1ZjBlYzljZjgyNDNjODFkYTM2IiwiaWF0IjoxNDY1OTczMTY1LCJhdWQiOiI3Vk5TMlRjMklpUUIyUHZqVUJjYjU3NDRxSDllWTdpQiIsImlzcyI6InRyYWRlcnNiaXQuY29tIiwic3ViIjoiYXV0aDB8NTZiNzY1ZjBlYzljZjgyNDNjODFkYTM2In0.UK9YGepSwOL0-ZcmynYsYvx8fq0hnA-vNh4rtZ1TZXg";
    String streamId = "ee1cc14b-3ad6-4868-929e-6365d1717e9a";

    public String handler(SNSEvent event, Context context) {
        //List<Tick> ticks = Sns2Tick.sns2Ticks(event);

        Connection database = Database.getConnection();
        List<Tick> ticks = Database.getTicks(database, 30);

        TradersBit.postSignal(apiKey, streamId, Macd1.compute(ticks));
        return "ok";
    }


    private static int compute(List<Tick> ticks) {

        System.out.println("number of ticks: " + ticks.size());

        double[] closePrice = new double[ticks.size()];

        for (int i = 0; i < ticks.size(); i++) {
            closePrice[i] = ticks.get(i).getClose();
        }


        double[] out = new double[ticks.size()];
        MInteger begin = new MInteger();
        MInteger length = new MInteger();


        double[] out1 = new double[ticks.size()];
        double[] out2 = new double[ticks.size()];
        double[] out3 = new double[ticks.size()];


        Core c = new Core();
        //RetCode retCode = c.sma(0, closePrice.length - 1, closePrice, PERIODS_AVERAGE, begin, length, out);
        RetCode retCode = c.macd(0, closePrice.length - 1, closePrice, 26, 6, 9, begin, length, out1, out2, out3);


        //  for (int i = 0; i < length.value; i++) {
        //      System.out.println(begin.value+i + ": time: " + ticks.get(begin.value+i).getTickEndTime() + ", macd: " + out1[i] + ", signal: " + out2[i] + " at price " + closePrice[begin.value+i]);
        //  }

        if (retCode == RetCode.Success) {
            if (out2[out2.length - 1] > 1) {
                return 1;
            } else if (out2[out2.length - 1] < -1) {
                return -1;
            } else if (out2[out2.length - 1] < 1 && out2[out2.length - 1] > -1) {
                return 0;
            }

        } else {
            System.out.println("Error");
            return 9;
        }
        return 9;
    }
}
