package util.model;

/**
 * Created by sogasg on 15/06/16.
 */
public class TradesBitTrade {
    private int timestamp;
    private Double price;
    private Double change;
    private int id;
    private Double valueInclFee;
    private Double changeInclFee;
    private Double value;
    private int signal;

    public int getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public Double getPrice() {
        return this.price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getChange() {
        return this.change;
    }

    public void setChange(Double change) {
        this.change = change;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Double getValueInclFee() {
        return this.valueInclFee;
    }

    public void setValueInclFee(Double valueInclFee) {
        this.valueInclFee = valueInclFee;
    }

    public Double getChangeInclFee() {
        return this.changeInclFee;
    }

    public void setChangeInclFee(Double changeInclFee) {
        this.changeInclFee = changeInclFee;
    }

    public Double getValue() {
        return this.value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public int getSignal() {
        return this.signal;
    }

    public void setSignal(int signal) {
        this.signal = signal;
    }
}
