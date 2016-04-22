package server;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import javax.swing.JSpinner;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

/**
 * JavaFX Control that behaves like a {@link JSpinner} known in Swing.
 * The {@link BigDecimal} number in the TextField can be incremented or decremented by a configurable
 * stepWidth using the arrow buttons in the control or the up and down arrow keys.
 *
 * Inspired from Thomas Bolz's NumberSpinner:
 * {@link https://dzone.com/articles/javafx-numbertextfield-and}
 */
public class NumberSpinner extends BorderPane {

    private ObjectProperty<BigDecimal> number = new SimpleObjectProperty<>();
	private BigDecimal stepWidth;
	private DecimalFormat df;
    private TextField numberField;
    private Button incrementButton;
    private Button decrementButton;
    
    /**
     * Create new NumberSpinner with default stepWidth (0.001) and format (#,##0.00000000).
     */
    public NumberSpinner() {
        this(new BigDecimal("0.001"));
    }
    
    /**
     * Create new NumberSpinner with specific stepWidth, and default format (#,##0.00000000).
     * @param stepWidth - The stepWidth for incrementing/decrementing the value.
     */
    public NumberSpinner(BigDecimal stepWidth) {
        this(new BigDecimal("0.001"), new DecimalFormat("#,##0.00000000"));
    }
    
    /**
     * Create new NumberSpinner with specific stepWidth and format.
     * @param stepWidth - The stepWidth for incrementing/decrementing the value.
     * @param df - The decimal format to display the value in.
     */
    public NumberSpinner(BigDecimal stepWidth, DecimalFormat df) {
        super();
        number.set(BigDecimal.ZERO);
        this.stepWidth = stepWidth;
        this.df = df;
        this.getStyleClass().add("NumberSpinner");

        // TextField.
        numberField = new TextField(df.format(number.get()));
        numberField.setId("NumberField");
        numberField.setAlignment(Pos.CENTER_RIGHT);
        numberField.setPrefWidth(250);

        // Enable arrow keys for decrement/increment and escape key to cancel editing.
        numberField.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode() == KeyCode.DOWN) {
                    decrement();
                    keyEvent.consume();
                }
                if (keyEvent.getCode() == KeyCode.UP) {
                    increment();
                    keyEvent.consume();
                }
                if (keyEvent.getCode() == KeyCode.ESCAPE) {
                    numberField.setText(df.format(number.get()));
                    keyEvent.consume();
                }
            }
        });
        
        // Allow input of positive decimal numbers only.
        numberField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
            	if (!newValue.matches("^[\\d,]*\\.?\\d{0,8}$")) {
                   numberField.setText(oldValue);
            	}
            }
        });
        
        // Try to parse when focus is lost or RETURN is hit.
        numberField.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
        		setNumber(new BigDecimal("0" + numberField.getText().replaceAll(",", "")));
            }
        });
        numberField.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (!newValue.booleanValue()) {
            		setNumber(new BigDecimal("0" + numberField.getText().replaceAll(",", "")));
                }
            }
        });
        
        // Painting the up and down arrows.
        Path arrowUp = new Path();
        arrowUp.setId("NumberSpinnerArrow");
        arrowUp.getElements().addAll(new MoveTo(-4, 0), new LineTo(4, 0), new LineTo(0, -4), new LineTo(-4, 0));
        
        Path arrowDown = new Path();
        arrowDown.setId("NumberSpinnerArrow");
        arrowDown.getElements().addAll(new MoveTo(-4, 0), new LineTo(4, 0), new LineTo(0, 4), new LineTo(-4, 0));

        // Increment button.
        incrementButton = new Button("", arrowUp);
        incrementButton.setId("SpinnerButtonUp");
        incrementButton.setFocusTraversable(false);
        incrementButton.setPadding(new Insets(-5,6,-5,6));
        incrementButton.setPrefHeight(13);
        incrementButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent ae) {
                increment();
                ae.consume();
            }
        });
        
        // Decrement button.
        decrementButton = new Button("", arrowDown);
        decrementButton.setId("SpinnerButtonDown");
        decrementButton.setFocusTraversable(false);
        decrementButton.setPadding(new Insets(-5,6,-5,6));
        decrementButton.setPrefHeight(12);
        decrementButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent ae) {
                decrement();
                ae.consume();
            }
        });
        
        // Buttons grid pane.
        GridPane buttons = new GridPane();
        buttons.setId("ButtonsBox");
        buttons.add(incrementButton, 0, 0);
        buttons.add(decrementButton, 0, 1);
        
        // Put buttons and TextField together.
        this.setCenter(numberField);
        this.setRight(buttons);
    }

    /**
     * increment number value by stepWidth
     */
    private void increment() {
        setNumber(number.get().add(stepWidth));
    }

    /**
     * decrement number value by stepWidth
     */
    private void decrement() {
    	if (number.get().compareTo(BigDecimal.ZERO) == 1)
    		setNumber(number.get().subtract(stepWidth));
    }
    
    /**
     * Set the number value of the NumberSpinner.
     * @param value - The new value.
     */
    public void setNumber(BigDecimal value) {
        number.set(value);
        numberField.setText(df.format(value));
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
    
    /**
     * Get the TextField's current text value.
     * @return The textual content of the TextField.
     */
    public String getText() {
        return numberField.getText();
    }
    
}
