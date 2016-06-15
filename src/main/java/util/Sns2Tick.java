package util;

import com.alibaba.fastjson.JSON;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import util.model.Tick;

import java.util.Arrays;
import java.util.List;


public class Sns2Tick {

    public static List<Tick> sns2Ticks(SNSEvent event) {
        Tick eventTick = JSON.parseObject(event.getRecords().get(0).getSNS().getMessage(), Tick.class);
        return Arrays.asList(eventTick);
    }

}
