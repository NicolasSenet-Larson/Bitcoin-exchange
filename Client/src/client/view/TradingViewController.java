package client.view;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import client.MainApp;
import client.model.Order;
import client.util.NumberField;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

/**
 * The exchange page is where the user can place orders, and see current buyers and sellers for a chosen currency pair.
 * @author Nicolas Senet-Larson;
 */
public class TradingViewController {
	@FXML
	private GridPane inputsGrid;
	@FXML
	private RadioButton sendLabel;
	@FXML
    private ComboBox<Object> sendCurrency;
    @FXML
    private ComboBox<Object> receiveCurrency;
    @FXML
    private Button submit;
    @FXML
    private TableView<Order> buyers;
    @FXML
    private TableColumn<Order, BigDecimal> ordered;
    @FXML
    private TableColumn<Order, BigDecimal> buyRate;
    @FXML
    private TableView<Order> sellers;
    @FXML
    private TableColumn<Order, BigDecimal> available;
    @FXML
    private TableColumn<Order, BigDecimal> sellRate;
    
    private final NumberField send = new NumberField();
    private final NumberField receive = new NumberField();
    private final NumberField rate = new NumberField();
    private final ObservableList<Order> sellOrders = FXCollections.observableArrayList();
    private final ObservableList<Order> buyOrders = FXCollections.observableArrayList();
    
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
    	
    	/** Initialise currency drop downs */
    	
    	// Populate drop downs.
    	ObservableList<Object> currencies = FXCollections.observableArrayList(MainApp.properties.keySet());
    	sendCurrency.setItems(currencies);
    	sendCurrency.setValue(currencies.get(0));
    	receiveCurrency.getItems().addAll(currencies.subList(1, currencies.size()));
    	receiveCurrency.setValue(currencies.get(1));
    	
    	// Remove selected first currency from the second currency list.
    	sendCurrency.valueProperty().addListener((observable, oldValue, newValue) -> {
        	receiveCurrency.getItems().add(currencies.indexOf(oldValue), oldValue);
        	receiveCurrency.getItems().remove(currencies.indexOf(newValue));
        	
        	fetchCurrentBuyersAndSellers(); // Update tables.
        });
    	
    	// If selected currency is null, select first currency in the list.
    	receiveCurrency.valueProperty().addListener((observable, oldValue, newValue) -> {
    		if (newValue == null)
	    		receiveCurrency.setValue(receiveCurrency.getItems().get(0));
        	
    		fetchCurrentBuyersAndSellers(); // Update tables.
        });

    	/** Add change listener to radio buttons */
    	
    	sendLabel.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
				send.setDisable(false);
				receive.setDisable(true);
			} else {
				send.setDisable(true);
				receive.setDisable(false);
			}
    	});
    	
    	/** Initialise input fields */
    	
    	// Initialise and add NumberFields to the inputs GridPane.
    	send.setPrefWidth(250);
    	rate.setPrefWidth(250);
    	receive.setPrefWidth(250);
    	receive.setDisable(true);
    	inputsGrid.getChildren().add(0, send);
    	inputsGrid.getChildren().add(2, rate);
    	inputsGrid.getChildren().add(3, receive);
    	GridPane.setConstraints(send, 1, 0);
    	GridPane.setConstraints(rate, 1, 2);
    	GridPane.setConstraints(receive, 1, 4);
    	send.setId("send");
    	rate.setId("rate");
    	receive.setId("receive");
    	
    	// Calculate receive quantity, from send quantity and rate, on send quantity change.
    	send.numberProperty().addListener(new ChangeListener<BigDecimal>() {
            @Override
            public void changed(ObservableValue<? extends BigDecimal> observable, BigDecimal oldValue, BigDecimal newValue) {
            	if (sendLabel.isSelected()) {
            		receive.setNumber(newValue.multiply(rate.getNumber()).setScale(8, RoundingMode.HALF_UP));
            	}
            }
        });
    	
    	// Calculate send quantity, from receive quantity and rate, on receive quantity change.
    	receive.numberProperty().addListener(new ChangeListener<BigDecimal>() {
            @Override
            public void changed(ObservableValue<? extends BigDecimal> observable, BigDecimal oldValue, BigDecimal newValue) {
            	if (!sendLabel.isSelected() && rate.getNumber().compareTo(BigDecimal.ZERO) > 0) {
            		send.setNumber(newValue.divide(rate.getNumber(), 8, RoundingMode.HALF_UP));
            	}
            }
        });
    	
    	// Calculate send OR receive quantity, from send OR receive quantity and rate, on rate change.
    	rate.numberProperty().addListener(new ChangeListener<BigDecimal>() {
            @Override
            public void changed(ObservableValue<? extends BigDecimal> observable, BigDecimal oldValue, BigDecimal newValue) {
            	if (sendLabel.isSelected()) {
            		receive.setNumber(send.getNumber().multiply(newValue).setScale(8, RoundingMode.HALF_UP));
            	} else if (newValue.compareTo(BigDecimal.ZERO) > 0) {
            		send.setNumber(receive.getNumber().divide(newValue, 8, RoundingMode.HALF_UP));
            	}
            }
        });

    	/** Initialise tables */
    	
    	// Current buying rates.
    	ordered.setCellValueFactory(new PropertyValueFactory<Order, BigDecimal>("sendAmount"));
		buyRate.setCellValueFactory(new PropertyValueFactory<Order, BigDecimal>("rate"));
		buyers.setItems(buyOrders);
		
		// Current selling rates.
		available.setCellValueFactory(new PropertyValueFactory<Order, BigDecimal>("sendAmount"));
		sellRate.setCellValueFactory(new PropertyValueFactory<Order, BigDecimal>("rate"));
		sellers.setItems(sellOrders);
		
		// Change inputs when a current buying or selling rate is selected.
		buyers.getSelectionModel().selectedItemProperty().addListener((observable, oldSelection, newSelection) -> {
		    if (newSelection != null)
		        rate.setNumber(newSelection.getRate());
		});
		sellers.getSelectionModel().selectedItemProperty().addListener((observable, oldSelection, newSelection) -> {
		    if (newSelection != null)
		        rate.setNumber(newSelection.getRate());
		});
		
		// Clear selection from a table when the other table is selected.
		buyers.focusedProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue)
				sellers.getSelectionModel().clearSelection();
		});
		sellers.focusedProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue)
				buyers.getSelectionModel().clearSelection();
		});
		
		// Populate tables.
		fetchCurrentBuyersAndSellers();
    }
    
    /**
     * Update the current buyers and sellers tables.
     */
    private void fetchCurrentBuyersAndSellers() {
    	// Change table headers.
    	ordered.setText("Ordered " + sendCurrency.getValue());
		buyRate.setText(receiveCurrency.getValue() + " per " + sendCurrency.getValue());
		available.setText("Available " + sendCurrency.getValue());
		sellRate.setText(receiveCurrency.getValue() + " per " + sendCurrency.getValue());
    	
		// Clear previous table data.
		buyOrders.clear();
		sellOrders.clear();
		
		// Get fresh data from the database.
    	try {
    		// Initialise connection to server.
			Connection con = DriverManager
					.getConnection("jdbc:mysql://db4free.net:3306/cryptoexchange?useSSL=false", "nicosenetlarson", "Nico.1994");
			
			// Fetch current buyers and populate table.
			ResultSet rs = con.createStatement().executeQuery(
				"SELECT SUM(CAST(SendAmount * MinRate AS DECIMAL(20,8))), 1.0000/MinRate FROM orders " +
				"WHERE SendCurrency = '" + receiveCurrency.getValue() + "' " +
				"AND ReceiveCurrency = '" + sendCurrency.getValue() + "' GROUP BY 1.0000/MinRate ORDER BY MinRate;");
			while (rs.next())
				buyOrders.add(new Order(rs.getBigDecimal(1), rs.getBigDecimal(2)));
			
			// Fetch current sellers and populate table.
			rs = con.createStatement().executeQuery(
				"SELECT SUM(SendAmount), MinRate FROM orders " +
				"WHERE SendCurrency = '" + sendCurrency.getValue() + "' " +
				"AND ReceiveCurrency = '" + receiveCurrency.getValue() + "' GROUP BY MinRate ORDER BY MinRate;");
			while (rs.next())
				sellOrders.add(new Order(rs.getBigDecimal(1), rs.getBigDecimal(2)));
		} catch (SQLException e) {
			new Alert(AlertType.NONE, e.getClass().getSimpleName() + ": " + e.getMessage(), ButtonType.CLOSE).show();
			e.printStackTrace();
		}
    }
    
    /**
     * Get wallet balance and address, and send order to the DBMS.
     */
    @FXML
    private void placeOrder() {
    	// Validate input.
    	if (send.getNumber().compareTo(BigDecimal.ZERO) > 0 && rate.getNumber().compareTo(BigDecimal.ZERO) > 0) {
    		// Loading message.
    		Alert loading = new Alert(Alert.AlertType.NONE, "Placing your order...", ButtonType.CLOSE);
    		loading.show();
    		
    		try {
				// Initialise connection to DBMS and prepare statement.
				PreparedStatement stmt = DriverManager
				.getConnection("jdbc:mysql://db4free.net:3306/cryptoexchange?useSSL=false", "nicosenetlarson", "Nico.1994")
				.prepareStatement("CALL PlaceOrder(?,?,?,?,?,?,?);");
				
				// Get sending wallet balance.
				String[] properties = MainApp.properties.getProperty(sendCurrency.getValue().toString()).split(", ");
				String balance = MainApp.rpc(properties[0], loading, "-regtest getbalance")[0];
				
				if (balance != null) {
	    			// Add parameters to statement. (Balance, Send Address, Receive Address, Send Currency, Receive Currency, Send Amount, Minimum Rate)
	    			stmt.setBigDecimal(1, new BigDecimal(balance));
	    			stmt.setString(2, properties[1]);
	    			stmt.setString(3, MainApp.properties.getProperty(receiveCurrency.getValue().toString()).split(", ")[1]);
	    			stmt.setString(4, sendCurrency.getValue().toString());
	    			stmt.setString(5, receiveCurrency.getValue().toString());
	    			stmt.setBigDecimal(6, send.getNumber());
	    			stmt.setBigDecimal(7, rate.getNumber());
	    			
					// Execute query.
					ResultSet rs = stmt.executeQuery();
					if (rs.next())
						new Alert(Alert.AlertType.NONE, rs.getString(1), ButtonType.CLOSE).show(); // Result message.
				}
			} catch (IOException | SQLException e) {
				new Alert(AlertType.NONE, e.getClass().getSimpleName() + ": " + e.getMessage(), ButtonType.CLOSE).show();
				e.printStackTrace();
			}
    		
    		loading.close(); // Close loading message.
    	} else {
    		new Alert(Alert.AlertType.NONE, "Please specify an amount and rate.", ButtonType.CLOSE).show();
    	}
    	
		fetchCurrentBuyersAndSellers(); // Update tables.
    }
    
    /**
     * Reset all input fields to zero.
     */
    @FXML
    private void clear() {
    	rate.setNumber(BigDecimal.ZERO);
    	send.setNumber(BigDecimal.ZERO);
    	receive.setNumber(BigDecimal.ZERO);
    }
}
