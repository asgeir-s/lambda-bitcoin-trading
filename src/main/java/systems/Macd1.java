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
import java.sql.SQLException;
import java.util.List;

public class Macd1 {
    //Star
    final static String apiKey = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdHJlYW1JZCI6ImVlMWNjMTRiLTNhZDYtNDg2OC05MjllLTYzNjVkMTcxN2U5YSIsImFwaUtleUlkIjoiMzdiZWQxNDktMDY5ZS00YWMyLTlhMjMtZmM2NDBmOTdhMjMyIiwidXNlcklkIjoiYXV0aDB8NTZiNzY1ZjBlYzljZjgyNDNjODFkYTM2IiwiaWF0IjoxNDY1OTczMTY1LCJhdWQiOiI3Vk5TMlRjMklpUUIyUHZqVUJjYjU3NDRxSDllWTdpQiIsImlzcyI6InRyYWRlcnNiaXQuY29tIiwic3ViIjoiYXV0aDB8NTZiNzY1ZjBlYzljZjgyNDNjODFkYTM2In0.UK9YGepSwOL0-ZcmynYsYvx8fq0hnA-vNh4rtZ1TZXg";
    final static String streamId = "ee1cc14b-3ad6-4868-929e-6365d1717e9a";

    public String handler(SNSEvent event, Context context) {
        //List<Tick> ticks = Sns2Tick.sns2Ticks(event);
        Connection database = Database.getConnection();
        List<Tick> ticks = Database.getTicks(database, 1800, 30);

        TradersBit.postSignal(apiKey, streamId, Macd1.compute(9, ticks));

        try {
            database.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "ok";
    }


    public static int compute(int status, List<Tick> ticks) {

        //System.out.println("number of ticks: " + ticks.size());

        double[] closePrice = new double[ticks.size()];

        for (int i = 0; i < ticks.size(); i++) {
            closePrice[i] = ticks.get(i).getClose();
        }

        double[] out = new double[ticks.size()];
        MInteger begin = new MInteger();
        MInteger length = new MInteger();

        double[] macd = new double[ticks.size()];
        double[] macdSignal = new double[ticks.size()];
        double[] macdHist = new double[ticks.size()];

        Core c = new Core();
        RetCode retCode = c.macd(0, closePrice.length - 1, closePrice, 26, 6, 9, begin, length, macd, macdSignal, macdHist);

      //    for (int i = 0; i < length.value; i++) {
      //        System.out.println(begin.value+i + ": time: " + ticks.get(begin.value+i).getTickEndTime() + ", macd: " + macd[i] + ", signal: " + macdSignal[i] + " at price " + closePrice[begin.value+i]);
      //    }

        double lastMacd = macd[length.value-1];
        //System.out.println("lastMacd: " + lastMacd);


        if (retCode == RetCode.Success) {
            if (lastMacd > 1) {
                return 1;
            } else if (lastMacd < -1) {
                return -1;
            } else if (lastMacd < 1 && lastMacd > -1) {
                return 0;
            }

        } else {
            System.out.println("Error");
            return 9;
        }
        return 9;
    }
}
