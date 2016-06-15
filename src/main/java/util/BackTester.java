package util;

import systems.Aroon;
import systems.Macd1;
import systems.Roc;
import systems.WilliamR;
import util.model.Tick;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

public class BackTester {

    final static int startTime = 1387404468;

    public static void main(String[] args) {
        Connection database = getTestConnection();
        List<Tick> ticks = Database.getTicksSince(database, 7200, startTime);

        System.out.println("first time: " + ticks.get(0).getTickEndTime());
        System.out.println("last time: " + ticks.get(ticks.size()-1).getTickEndTime());

        int numOfTrades = 0;
        int position = 0;

        int goodTrades = 0;
        int badTrades = 0;

        double positionPrice = ticks.get(40).getClose();
        double money = 1000;

        for (int i = 40; i <= ticks.size()-1; i++) {
            int result = Aroon.compute(position, ticks.subList(0, i));

            if (result >= -1 && result <= 1 && position != result) {
                if (position == 0) {
                    positionPrice = ticks.get(i).getClose();
                    position = result;
                } else if (position == 1) {
                    double newMoney = (money / ticks.get(i).getClose()) * positionPrice;

                    if(newMoney >= money) goodTrades++;
                    else badTrades++;
                    //System.out.printf("LONG  CLOSED: open: %.2f, close: %.2f, result: %.4f\n", positionPrice, ticks.get(i).getClose(), (newMoney - money));

                    money = newMoney*0.998;
                    positionPrice = ticks.get(i).getClose();
                    position = result;
                    numOfTrades++;

                } else if (position == -1) {
                    double newMoney = (money / positionPrice) * ticks.get(i).getClose();

                    if(newMoney >= money) goodTrades++;
                    else badTrades++;
                    //System.out.printf("SHORT CLOSED: open: %.2f, close: %.2f, result: %.4f\n", positionPrice, ticks.get(i).getClose(), (newMoney - money));

                    money = newMoney*0.998;
                    positionPrice = ticks.get(i).getClose();
                    position = result;
                    numOfTrades++;
                }

            }
        }

        System.out.printf("result money: %.2f, number of trades: %d, good: %d, bad: %d\n",money, numOfTrades, goodTrades, badTrades);


    }


    public static Connection getTestConnection() {
        // get history fram database
        Connection c = null;
        try {
            Class.forName("org.postgresql.Driver");
            c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/timeseries", "testuser", "Password123");


            c.setAutoCommit(false);
            System.out.println("Opened database successfully");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        return c;
    }
}
