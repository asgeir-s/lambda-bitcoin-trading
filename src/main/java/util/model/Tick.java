package util.model;

/**
 * Created by sogasg on 14/06/16.
 */
public class Tick {

    private int id;
    private Double open;
    private Double close;
    private Double high;
    private Double low;
    private Double volume;
    private int LastOriginID;
    private int TickEndTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Double getOpen() {
        return open;
    }

    public void setOpen(Double open) {
        this.open = open;
    }

    public Double getClose() {
        return close;
    }

    public void setClose(Double close) {
        this.close = close;
    }

    public Double getHigh() {
        return high;
    }

    public void setHigh(Double high) {
        this.high = high;
    }

    public Double getLow() {
        return low;
    }

    public void setLow(Double low) {
        this.low = low;
    }

    public Double getVolume() {
        return volume;
    }

    public void setVolume(Double volume) {
        this.volume = volume;
    }

    public int getLastOriginID() {
        return LastOriginID;
    }

    public void setLastOriginID(int lastOriginID) {
        LastOriginID = lastOriginID;
    }

    public int getTickEndTime() {
        return TickEndTime;
    }

    public void setTickEndTime(int tickEndTime) {
        TickEndTime = tickEndTime;
    }

}