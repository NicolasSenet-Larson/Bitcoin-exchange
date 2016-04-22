package client.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;

/**
 * Textfield implementation that accepts formatted number and stores them in a BigDecimal property.
 * The user input is formatted when the focus is lost or the user hits RETURN.
 *
 * @author Nicolas Senet-Larson
 */
public class NumberField extends TextField {
	private final ObjectProperty<BigDecimal> number = new SimpleObjectProperty<>();
	
    /**
     * Constructor: create a new NumberSpinner with default decimal format "#,##0.00000000".
     */
    public NumberField() {
    	super("0.00000000");
    	number.set(BigDecimal.ZERO);
    	setAlignment(Pos.CENTER_RIGHT);
        setPrefWidth(250);
        
        // Allow input of positive decimal numbers only.
        textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("^[\\d,]*\\.?\\d{0,8}$"))
               setText(oldValue);
        });
        
        // Try to parse when focus is lost or RETURN is hit.
        setOnAction(event ->
        	setNumber(new BigDecimal("0" + getText().replaceAll(",", "")))
		);
        focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.booleanValue())
        		setNumber(new BigDecimal("0" + getText().replaceAll(",", "")));
        });
    }
    
    /**
     * Set the number value of the NumberSpinner.
     * @param value - The new value.
     */
    public void setNumber(BigDecimal value) {
        number.set(value);
        setText(new DecimalFormat("#,##0.00000000").format(value));
    }
    
    /**
     * Get the number value of the NumberSpinner.
     * @return The number value of the NumberSpinner.
     */
    public BigDecimal getNumber() {
        return number.get();
    }
    
    /**
     * The BigDecimal property of the NumberSpinner.
     * @return The BigDecimal property object.
     */
    public ObjectProperty<BigDecimal> numberProperty() {
        return number;
    }
}
