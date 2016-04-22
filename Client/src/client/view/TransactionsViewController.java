package client.view;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import client.MainApp;
import client.model.Order;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

/**
 * The orders page shows the user's currently placed and matched orders.
 * The user can cancel placed orders and accept/cancel matched orders.
 * Accepted orders are sent by RPC wallet command from this page.
 * @author Nicolas Senet-Larson
 */
public class TransactionsViewController {
	@FXML
    private TableView<Order> placed;
    @FXML
    private TableColumn<Order, String> fromCurrency;
    @FXML
    private TableColumn<Order, BigDecimal> fromAmount;
    @FXML
    private TableColumn<Order, String> toCurrency;
    @FXML
    private TableColumn<Order, BigDecimal> toAmount;
    @FXML
    private TableColumn<Order, BigDecimal> minRate;
    @FXML
    private TableColumn<Order, Date> orderDate;
    @FXML
    private TableColumn<Order, HBox> cancel;
    @FXML
    private TableView<Order> matched;
    @FXML
    private TableColumn<Order, String> sendCurrency;
    @FXML
    private TableColumn<Order, BigDecimal> sendAmount;
    @FXML
    private TableColumn<Order, String> receiveCurrency;
    @FXML
    private TableColumn<Order, BigDecimal> receiveAmount;
    @FXML
    private TableColumn<Order, BigDecimal> rate;
    @FXML
    private TableColumn<Order, Date> matchDate;
    @FXML
    private TableColumn<Order, HBox> sendStatus;
    @FXML
    private TableColumn<Order, String> receiveStatus;
    
    private MainApp mainApp; // Reference to the main application.
    private final ObservableList<Order> placedOrders = FXCollections.observableArrayList();
    private final ObservableList<Order> matchedOrders = FXCollections.observableArrayList();
    
	/**
     * The constructor.
     * The constructor is called before the initialize() method.
     */
    public TransactionsViewController() {
    }

    /**
     * Initialises the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
    	// Initialise the currently placed orders table.
    	fromCurrency.setCellValueFactory(new PropertyValueFactory<Order, String>("sendCurrency"));
		fromAmount.setCellValueFactory(new PropertyValueFactory<Order, BigDecimal>("sendAmount"));
    	toCurrency.setCellValueFactory(new PropertyValueFactory<Order, String>("receiveCurrency"));
		toAmount.setCellValueFactory(new PropertyValueFactory<Order, BigDecimal>("receiveAmount"));
		minRate.setCellValueFactory(new PropertyValueFactory<Order, BigDecimal>("rate"));
		orderDate.setCellValueFactory(new PropertyValueFactory<Order, Date>("date"));
		cancel.setCellValueFactory(new PropertyValueFactory<Order, HBox>("sendStatus"));
		placed.setItems(placedOrders);

    	sendCurrency.setCellValueFactory(new PropertyValueFactory<Order, String>("sendCurrency"));
		sendAmount.setCellValueFactory(new PropertyValueFactory<Order, BigDecimal>("sendAmount"));
    	receiveCurrency.setCellValueFactory(new PropertyValueFactory<Order, String>("receiveCurrency"));
		receiveAmount.setCellValueFactory(new PropertyValueFactory<Order, BigDecimal>("receiveAmount"));
		rate.setCellValueFactory(new PropertyValueFactory<Order, BigDecimal>("rate"));
		matchDate.setCellValueFactory(new PropertyValueFactory<Order, Date>("date"));
		sendStatus.setCellValueFactory(new PropertyValueFactory<Order, HBox>("sendStatus"));
		receiveStatus.setCellValueFactory(new PropertyValueFactory<Order, String>("receiveStatus"));
		matched.setItems(matchedOrders);
		
		// Populate tables.
		try {
    		// Initialise connection to server.
			Connection con = DriverManager.getConnection("jdbc:mysql://db4free.net:3306/cryptoexchange?useSSL=false", "nicosenetlarson", "Nico.1994");
			
			// Get all addresses.
			StringBuilder addresses = new StringBuilder();
			MainApp.properties.values().forEach(value -> {
				if (addresses.length() > 0)
					addresses.append(',');
				addresses.append("'" + value.toString().split(", ")[1] + "'");
			});
			
			// Fetch my currently placed orders and populate table.
			ResultSet rs = con.createStatement().executeQuery(
					"SELECT *, CAST(SendAmount * MinRate AS DECIMAL(20,8)) AS ReceiveAmount, CONVERT_TZ(OrderDate, @@global.time_zone, '+00:00') AS UTCDate "
					+ "FROM orders WHERE ReceiveAddress IN (" + addresses + ");");
			while (rs.next()) {
				// HBox with cancel button for the 'Cancel' column.
				HBox cancelBox = new HBox();
				Button cancelButton = new Button("Cancel");
				cancelButton.setPrefWidth(60);
				cancelBox.getChildren().add(cancelButton);
				cancelBox.setAlignment(Pos.CENTER);
				
				// Cancel action.
				int orderID = rs.getInt("OrderID");
				cancelButton.setOnAction(event -> {
					try {
						con.createStatement().execute("DELETE FROM orders WHERE OrderID = " + orderID + ";");
						mainApp.showPage("Transactions"); // Update page.
					} catch (SQLException e) {
						new Alert(AlertType.NONE, e.getClass().getSimpleName() + ": " + e.getMessage(), ButtonType.CLOSE).show();
						e.printStackTrace();
					}
				});
				
				// Add order to table.
				placedOrders.add(new Order(rs.getString("SendCurrency"), rs.getString("ReceiveCurrency"), rs.getBigDecimal("SendAmount"),
						rs.getBigDecimal("ReceiveAmount"), rs.getBigDecimal("MinRate"), rs.getString("UTCDate"), cancelBox, null));
			}
			
			// Fetch my matched orders and populate table.
			rs = con.createStatement().executeQuery(
				"SELECT *, CAST(Amount2 / Amount1 AS DECIMAL(20,8)) AS Rate1, CAST(Amount1 / Amount2 AS DECIMAL(20,8)) AS Rate2, "
				+ "CONVERT_TZ(MatchDate, @@global.time_zone, '+00:00') AS UTCDate "
				+ "FROM matches WHERE Address1 IN (" + addresses + ") OR Address2 IN (" + addresses + ");");
			while (rs.next()) {
				// Separate my information from other user's.
				int[] user = new int[2];
				
				// If address 2 is mine, then use information set 1.
				if (addresses.toString().contains(rs.getString("Address2"))) {
					user[0] = 1; // Me.
					user[1] = 2; // The other user.
				
					// If address 1 is mine, then use information set 2.
				} else {
					user[0] = 2;
					user[1] = 1;
				}
				
				// HBox with accept/cancel buttons and status label for the 'Status' column.
				HBox statusBox = new HBox();
				Label statusLabel = new Label();
				Button acceptButton = new Button("Accept");
				acceptButton.setPrefWidth(60);
				Button cancelButton = new Button("Cancel");
				cancelButton.setPrefWidth(60);
				statusBox.setAlignment(Pos.CENTER);
				
				// Accept and Cancel action.
				int matchID = rs.getInt("MatchID");
				cancelButton.setOnAction(event -> {
					try {
						// Set all status to cancelled.
						con.createStatement().execute(
								"UPDATE matches SET Status" + user[0] + " = 'Cancelled', Status" + user[1] + " = 'Cancelled' WHERE MatchID = " + matchID + ";");
						mainApp.showPage("Transactions"); // Update page.
					} catch (SQLException e) {
						new Alert(AlertType.NONE, e.getClass().getSimpleName() + ": " + e.getMessage(), ButtonType.CLOSE).show();
						e.printStackTrace();
					}
				});
				acceptButton.setOnAction(event -> {
					try {
						// Set my status to accepted.
						con.createStatement().execute("UPDATE matches SET Status" + user[0] + " = 'Accepted' WHERE MatchID = " + matchID + ";");
						mainApp.showPage("Transactions"); // Update page.
					} catch (SQLException e) {
						new Alert(AlertType.NONE, e.getClass().getSimpleName() + ": " + e.getMessage(), ButtonType.CLOSE).show();
						e.printStackTrace();
					}
				});
				
				// If I haven't done anything yet, show accept and cancel buttons.
				if (rs.getString("Status" + user[0]) == null)
					statusBox.getChildren().addAll(acceptButton, cancelButton);
				
				// If I accepted and other has accepted/already sent, execute transaction.
				else if (rs.getString("Status" + user[0]).equals("Accepted") && rs.getString("Status" + user[1]) != null && !rs.getString("Status" + user[1]).equals("Cancelled")) {
					
					Alert loading = new Alert(Alert.AlertType.NONE, "Sending " + rs.getString("Currency" + user[0]) + "...", ButtonType.CLOSE); // Loading message.
	        		loading.show();
	        		
					MainApp.rpc(MainApp.properties.get(rs.getString("Currency" + user[0])).toString().split(", ")[0], loading,
							"-regtest sendtoaddress " + rs.getString("Address" + user[0]) + " " + rs.getBigDecimal("Amount" + user[0]).toPlainString()
							+ " \"Cryptoexchange\"");
					con.createStatement().execute("UPDATE matches SET Status" + user[0] + " = 'Sent' WHERE MatchID = " + matchID + ";");
					statusLabel.setText("Sent");
					statusBox.getChildren().add(statusLabel);
					
					loading.close(); // Close loading message.
					
				// If only I accepted, still show cancel button.
				} else if(rs.getString("Status" + user[0]).equals("Accepted")) {
					statusLabel.setText("Accepted");
					statusBox.getChildren().addAll(statusLabel, cancelButton);
					
				// If cancelled, show cancelled status.
				} else {
					statusLabel.setText(rs.getString("Status" + user[0]));
					statusBox.getChildren().add(statusLabel);
				}
				
				// Add match to table.
				matchedOrders.add(new Order(rs.getString("Currency" + user[0]), rs.getString("Currency" + user[1]),rs.getBigDecimal("Amount" + user[0]),
						rs.getBigDecimal("Amount" + user[1]), rs.getBigDecimal("Rate" + user[0]), rs.getString("UTCDate"), statusBox,
						rs.getString("Status" + user[1])));
			}	
		} catch (IOException | SQLException e) {
			new Alert(AlertType.NONE, e.getClass().getSimpleName() + ": " + e.getMessage(), ButtonType.CLOSE).show();
			e.printStackTrace();
		}
    }
    
    /**
     * Is called by the main application to give a reference back to itself.
     * @param mainApp the main application.
     */
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }
}
