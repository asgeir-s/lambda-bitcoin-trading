package util;

import com.alibaba.fastjson.JSON;
import util.model.TradesBitTrade;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;


public class TradersBit {

    public static String postSignal(String apiKey, String streamId ,int signal) {

        System.out.println("signal: " + signal);

        if(signal < -1 || signal > 1) {
            return "not new signal. Signal was: " + signal;
        }

        HttpURLConnection connection = null;
        try {
            //Create connection
            URL url = new URL("https://api.tradersbit.com/api/streams/" + streamId + "/signal");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("x-authorization", "apikey " + apiKey);


            connection.setUseCaches(false);
            connection.setDoOutput(true);

            OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
            out.write(Integer.toString(signal));
            out.flush();
            out.close();

            int res = connection.getResponseCode();

            System.out.println(res);

            //Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            return response.toString();
        }
        catch (Exception e) {
            if (e.getMessage().contains("response code: 409")) {
                System.out.println("this is the same as the current active position");
                return "OK";
            }
            else {
                e.printStackTrace();
                return null;
            }
        }
        finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public static TradesBitTrade getStatus(String apiKey, String streamId) {

        HttpURLConnection connection = null;
        try {
            //Create connection
            URL url = new URL("https://api.tradersbit.com/api/streams/" + streamId + "/status");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("x-authorization", "apikey " + apiKey);


            connection.setUseCaches(false);
            connection.setDoOutput(true);

            int res = connection.getResponseCode();

            System.out.println(res);

            //Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();

            return JSON.parseObject(response.toString().substring(1, response.toString().length()-2), TradesBitTrade.class);
        }
        catch (Exception e) {
                e.printStackTrace();
                return null;
        }
        finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
