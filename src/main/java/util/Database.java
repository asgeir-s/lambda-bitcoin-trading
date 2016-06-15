package util;

import util.model.Tick;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sogasg on 14/06/16.
 */
public class Database {

    public static Connection getConnection() {
        // get history fram database
        Connection c = null;
        try {
            Class.forName("org.postgresql.Driver");
            c = DriverManager.getConnection("jdbc:postgresql://bitfinexdata.c8amlpm3pfhj.eu-central-1.rds.amazonaws.com:5432/bitfinexdata", "bitfinexdata", "ZnRcAZBRCjFFioE5iHkOjiw");

            c.setAutoCommit(false);
            System.out.println("Opened database successfully");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        return c;
    }


    public static List<Tick> getTicks(Connection c, int lastNum) {
        List<Tick> ticks = new ArrayList<Tick>();

        // get history fram database
        Statement stmt = null;
        try {
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM bitfinex_tick_1800;");
            while (rs.next()) {
                Tick tick = new Tick();
                tick.setId(rs.getInt("id"));
                tick.setOpen(rs.getDouble("open"));
                tick.setClose(rs.getDouble("close"));
                tick.setHigh(rs.getDouble("high"));
                tick.setLow(rs.getDouble("low"));
                tick.setVolume(rs.getDouble("volume"));
                tick.setLastOriginID(rs.getInt("last_origin_id"));
                tick.setTickEndTime(rs.getInt("tick_end_time"));
                ticks.add(tick);
            }
            rs.close();
            stmt.close();
            c.close();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        return ticks;
    }

}
