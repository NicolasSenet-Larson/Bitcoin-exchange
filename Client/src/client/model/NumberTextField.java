package client.model;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

/**
 * {@link TextField} implementation that accepts formatted numbers and stores them in a
 * {@link BigDecimal} property.
 * <br/><br/>
 * Inspired from Thomas Bolz's NumberTextField:
 * <br/>{@link https://dzone.com/articles/javafx-numbertextfield-and}
 */
public class NumberTextField extends TextField {

    private final NumberFormat nf;
    private ObjectProperty<BigDecimal> number = new SimpleObjectProperty<>();
    
    /**
     * Get the current value of the BigDecimal property.
     * @return The current value.
     */
    public final BigDecimal getNumber() {
        return number.get();
    }
    
    /**
     * Set the value of the BigDecimal property.
     * @param value - The new value.
     */
    public final void setNumber(BigDecimal value) {
        number.set(value);
    }
    
    /**
     * The BigDecimal property of the NumberTextField.
     * @return The BigDecimal property object.
     */
    public ObjectProperty<BigDecimal> numberProperty() {
        return number;
    }
    
    /**
     * Create a new NumberTextField with starting value 0.
     */
    public NumberTextField() {
        this(BigDecimal.ZERO);
    }
    
    /**
     * Create a new NumberTextField with a chosen starting value.
     * @param value - The starting BigDecimal value.
     */
    public NumberTextField(BigDecimal value) {
        this(value, NumberFormat.getInstance());
        initHandlers();
    }
    
    /**
     * Create a new NumberTextField with a chosen starting value and specific number formatting.
     * @param value - The starting BigDecimal value.
     * @param nf - The number format.
     */
    public NumberTextField(BigDecimal value, NumberFormat nf) {
        super();
        this.nf = nf;
        initHandlers();
        setNumber(value);
    }

    private void initHandlers() {
        // try to parse when focus is lost or RETURN is hit
        setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
                parseAndFormatInput();
            }
        });
        
        focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (!newValue.booleanValue()) {
                    parseAndFormatInput();
                }
            }
        });
        
        this.setOnKeyTyped(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				
			}
        });

        // Set text in field if BigDecimal property is changed from outside.
        numberProperty().addListener(new ChangeListener<BigDecimal>() {
            @Override
            public void changed(ObservableValue<? extends BigDecimal> obserable, BigDecimal oldValue, BigDecimal newValue) {
                setText(nf.format(newValue));
            }
        });
    }

    /**
     * Tries to parse the user input to a number according to the provided
     * NumberFormat
     */
    private void parseAndFormatInput() {
        try {
            String input = getText();
            if (input == null || input.length() == 0) {
                return;
            }
            Number parsedNumber = nf.parse(input);
            BigDecimal newValue = new BigDecimal(parsedNumber.toString());
            setNumber(newValue);
            selectAll();
        } catch (ParseException ex) {
            // If parsing fails keep old number
            setText(nf.format(number.get()));
        }
    }
}