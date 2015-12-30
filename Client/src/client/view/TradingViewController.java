package client.view;

import client.MainApp;
import client.model.NumberSpinner;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class TradingViewController {
    // Reference to the main application.
    private MainApp mainApp;
    @FXML
    GridPane inputsGrid;
    @FXML
    ComboBox currencyFrom;
    
	/**
     * The constructor.
     * The constructor is called before the initialize() method.
     */
    public TradingViewController() {
    }

    /**
     * Initialises the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
    	final NumberSpinner quantity = new NumberSpinner();
    	NumberSpinner rate = new NumberSpinner();
    	inputsGrid.getChildren().add(3, quantity);
    	inputsGrid.getChildren().add(4, rate);
    	GridPane.setColumnIndex(quantity, 1);
    	GridPane.setColumnIndex(rate, 1);
    	GridPane.setRowIndex(quantity, 0);
    	GridPane.setRowIndex(rate, 1);
    	
    	Platform.runLater(new Runnable() {
            @Override
            public void run() {
                ((TextField)quantity.getChildren().get(0)).requestFocus();
                mainApp.getPrimaryStage().getScene().lookup("#nav-exchange").requestFocus();
            }
        });
    }
    
    /**
     * Is called by the main application to give a reference back to itself.
     * @param mainApp the main application.
     */
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }
}
