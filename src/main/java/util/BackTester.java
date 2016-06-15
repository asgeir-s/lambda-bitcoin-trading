package util;

import systems.Aroon;
import systems.Macd1;
import util.model.Tick;

import java.sql.Connection;
import java.util.List;

public class BackTester {
    public static void main(String[] args) {
        Connection database = Database.getConnection();
        List<Tick> ticks = Database.getTicks(database, 1800, 300000);
        int position = 0;
        double positionPrice = ticks.get(40).getClose();
        double money = 1000;

        for (int i = 40; i <= ticks.size(); i++) {
            int result = Aroon.compute(position, ticks.subList(0, i));

            if (result >= -1 && result <= 1 && position != result) {
                if (position == 0) {
                    positionPrice = ticks.get(i).getClose();
                    position = result;
                } else if (position == 1) {
                    double newMoney = (money / ticks.get(i).getClose()) * positionPrice;
                    System.out.printf("LONG  CLOSED: open: %.2f, close: %.2f, result: %.4f\n", positionPrice, ticks.get(i).getClose(), (newMoney - money));

                    money = newMoney;
                    positionPrice = ticks.get(i).getClose();
                    position = result;


                } else if (position == -1) {
                    double newMoney = (money / positionPrice) * ticks.get(i).getClose();
                    System.out.printf("SHORT CLOSED: open: %.2f, close: %.2f, result: %.4f\n", positionPrice, ticks.get(i).getClose(), (newMoney - money));

                    money = newMoney;
                    positionPrice = ticks.get(i).getClose();
                    position = result;
                }

            }
        }

        System.out.println("result money: " + money);


    }
}
