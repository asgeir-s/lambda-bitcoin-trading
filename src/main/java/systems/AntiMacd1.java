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
import java.sql.SQLException;
import java.util.List;

public class AntiMacd1 {
    // MonTac
    // res: 3619.36, period: 3600, trades: 289
    final static String apiKey = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdHJlYW1JZCI6Ijg0ZDcwZjY0LTgzZTgtNGQ0My04MzYzLWExMjUyNTU1MTUzNCIsImFwaUtleUlkIjoiNGQxOWQzN2MtNjMzMi00Y2YxLTgwZjEtZWE1NWE0YWNhM2QzIiwidXNlcklkIjoiYXV0aDB8NTZiYWU2OGRkYWMyYWM1YTUzODk0MzAzIiwiaWF0IjoxNDY2MTE5MTk0LCJhdWQiOiI3Vk5TMlRjMklpUUIyUHZqVUJjYjU3NDRxSDllWTdpQiIsImlzcyI6InRyYWRlcnNiaXQuY29tIiwic3ViIjoiYXV0aDB8NTZiYWU2OGRkYWMyYWM1YTUzODk0MzAzIn0.IpCl90a5WXI4x_3wYzM9fozgQ0gNfpGxlZFccFCG1a0";
    final static String streamId = "84d70f64-83e8-4d43-8363-a12525551534";

    public String handler(SNSEvent event, Context context) {
        //List<Tick> ticks = Sns2Tick.sns2Ticks(event);
        Connection database = Database.getConnection();
        List<Tick> ticks = Database.getTicks(database, 3600, 60);

        TradesBitTrade status = TradersBit.getStatus(apiKey, streamId);


        TradersBit.postSignal(apiKey, streamId, Macd1.compute(status.getSignal(), ticks));

        try {
            database.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println("last tick time: " + ticks.get(ticks.size()-1).getTickEndTime());
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
        RetCode retCode = c.macd(0, closePrice.length - 1, closePrice, 8, 30, 9, begin, length, macd, macdSignal, macdHist);

        //    for (int i = 0; i < length.value; i++) {
        //        System.out.println(begin.value+i + ": time: " + ticks.get(begin.value+i).getTickEndTime() + ", macd: " + macd[i] + ", signal: " + macdSignal[i] + " at price " + closePrice[begin.value+i]);
        //    }

        double lastMacd = macdSignal[length.value - 1];
        //System.out.println("lastMacd: " + lastMacd);


        if (retCode == RetCode.Success) {
            if (lastMacd > 1.9) {
                return -1;
            } else if (lastMacd < -1.9) {
                return 1;
            } else if (status == 1 && lastMacd < 20) {
                return 0;
            } else if (status == -1 && lastMacd > 20) {
                return 0;
            }

        } else {
            System.out.println("Error");
            return 9;
        }
        return 9;
    }
}
