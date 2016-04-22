package client.model;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.layout.HBox;

/**
 * Model class for an Order.
 * @author Nicolas Senet-Larson
 */
public class Order {
	private final SimpleStringProperty sendCurrency;
	private final SimpleStringProperty receiveCurrency;
	private final SimpleObjectProperty<BigDecimal> sendAmount;
	private final SimpleObjectProperty<BigDecimal> receiveAmount;
	private final SimpleObjectProperty<BigDecimal> rate;
	private SimpleObjectProperty<Date> date;
	private final SimpleObjectProperty<HBox> sendStatus;
	private final SimpleStringProperty receiveStatus;
	
	/**
	 * Constructor for the current buying and selling rates tables on the Exchange page.
	 * @param amount Buying/selling amount.
	 * @param rate Buying/selling rate.
	 */
	public Order(BigDecimal amount, BigDecimal rate) {
		this(null, null, amount, null, rate, null, null, null);
	}
	
	/**
	 * Constructor for the My Orders page.
	 * @param sendCurrency The currency I send.
	 * @param receiveCurrency The currency I receive.
	 * @param sendAmount The amount I send.
	 * @param receiveAmount The amount I receive.
	 * @param rate receiveAmount/sendAmount.
	 * @param date The date and time the order was matched.
	 * @param sendStatus My status of the matched order: sent, accepted or cancelled.
	 * @param receiveStatus The other's status of the matched order: sent, accepted or cancelled.
	 */
	public Order(String sendCurrency, String receiveCurrency, BigDecimal sendAmount, BigDecimal receiveAmount, BigDecimal rate, String date, HBox sendStatus, String receiveStatus) {
		DateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		
		this.sendCurrency = new SimpleStringProperty(sendCurrency);
		this.receiveCurrency = new SimpleStringProperty(receiveCurrency);
		this.sendAmount = new SimpleObjectProperty<BigDecimal>(sendAmount);
		this.receiveAmount = new SimpleObjectProperty<BigDecimal>(receiveAmount);
		this.rate = new SimpleObjectProperty<BigDecimal>(rate);
		if (date != null)
			try {
				this.date = new SimpleObjectProperty<Date>(utcFormat.parse(date));
			} catch (ParseException e) {
				e.printStackTrace();
			}
		this.sendStatus = new SimpleObjectProperty<HBox>(sendStatus);
		this.receiveStatus = new SimpleStringProperty(receiveStatus);
	}
	
	public String getSendCurrency() {
		return sendCurrency.get();
	}
	
	public void setSendCurrency(String string) {
		sendCurrency.set(string);
	}
	
	public String getReceiveCurrency() {
		return receiveCurrency.get();
	}
	
	public void setReceiveCurrency(String string) {
		receiveCurrency.set(string);
	}
	
	public BigDecimal getSendAmount() {
		return sendAmount.get();
	}
	
	public void setSendAmount(BigDecimal bd) {
		sendAmount.set(bd);
	}
	
	public BigDecimal getReceiveAmount() {
		return receiveAmount.get();
	}
	
	public void setReceiveAmount(BigDecimal bd) {
		receiveAmount.set(bd);
	}

	public BigDecimal getRate() {
		return rate.get();
	}
	
	public void setRate(BigDecimal bd) {
		rate.set(bd);
	}
	
	public Date getDate() {
		return date.get();
	}
	
	public void setDate(Date dt) {
		date.set(dt);
	}
	
	public HBox getSendStatus() {
		return sendStatus.get();
	}
	
	public void setSendStatus(HBox hbox) {
		sendStatus.set(hbox);
	}
	
	public String getReceiveStatus() {
		return receiveStatus.get();
	}
	
	public void getReceiveStatus(String string) {
		receiveStatus.set(string);
	}
}