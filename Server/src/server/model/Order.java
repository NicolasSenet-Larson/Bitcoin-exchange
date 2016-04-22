package server.model;

import java.io.Serializable;
import java.math.BigDecimal;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Model class for an Order.
 * @author Nicolas Senet-Larson
 */
@SuppressWarnings("serial")
public class Order implements Serializable {
	private final SimpleStringProperty from;
	private final SimpleStringProperty to;
	private final SimpleObjectProperty<BigDecimal> quantity;
	private final SimpleObjectProperty<BigDecimal> rate;
	private final SimpleStringProperty address;
	
	/**
	 * Default constructor.
	 */
	public Order() {
		this(null, null, null, null, null);
	}
	
	/**
	 * Constructor with initial data.
	 * @param from
	 * @param to
	 * @param quantity
	 * @param rate
	 * @param address
	 */
	public Order(String from, String to, BigDecimal quantity, BigDecimal rate, String address) {
		this.from = new SimpleStringProperty(from);
		this.to = new SimpleStringProperty(to);
		this.quantity = new SimpleObjectProperty<BigDecimal>(quantity);
		this.rate = new SimpleObjectProperty<BigDecimal>(rate);
		this.address = new SimpleStringProperty(address);
	}
	
	public String getFrom() {
		return from.get();
	}
	
	public void setFrom(String value) {
		from.set(value);
	}
	
	public String getTo() {
		return to.get();
	}
	
	public void setTo(String value) {
		to.set(value);
	}
	
	public BigDecimal getQuantity() {
		return quantity.get();
	}
	
	public void setQuantity(BigDecimal value) {
		quantity.set(value);
	}
	
	public BigDecimal getRate() {
		return rate.get();
	}
	
	public void setRate(BigDecimal value) {
		rate.set(value);
	}
	
	public String getAddress() {
		return address.get();
	}
	
	public void setAddress(String value) {
		address.set(value);
	}
}
