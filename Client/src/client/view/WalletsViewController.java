package client.view;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import client.MainApp;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

/**
 * The wallets page is used to save/remove wallet information needed to send RPC commands to the wallets.
 * @author Nicolas Senet-Larson
 */
public class WalletsViewController {
	@FXML
	private VBox wallets;
	
    private MainApp mainApp; // Reference to the main application.
    
	/**
     * The constructor.
     * The constructor is called before the initialize() method.
     */
    public WalletsViewController() {
    }

    /**
     * Initialises the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
    	// Display wallets information from properties.
    	MainApp.properties.entrySet().forEach(entry -> {
    		// Remove wallet button.
    		Button removeButton = new Button("Remove");
    		removeButton.setOnAction(event -> removeProperty(entry.getKey()));
    		BorderPane.setAlignment(removeButton, Pos.CENTER);
    		
    		// Wallet name and path.
    		VBox labels = new VBox();
    		Label name = new Label(entry.getKey().toString());
    		Label path = new Label(entry.getValue().toString());
    		labels.getChildren().add(name);
    		labels.getChildren().add(path);
    		labels.setPadding(new Insets(0,0,0,20));
    		
    		BorderPane walletPane = new BorderPane();
    		walletPane.setLeft(removeButton);
    		walletPane.setCenter(labels);
    		walletPane.getStyleClass().add("wallet");
    		
    		wallets.getChildren().add(walletPane);
    	});
    }
    
    @FXML
    private void addWallet() {
    	// Choose the executable file for the wallet.
    	FileChooser fileChooser = new FileChooser();
    	fileChooser.setTitle("Select the executable file for your wallet");
    	fileChooser.getExtensionFilters().add(new ExtensionFilter("Core QT Wallet Application", "*-qt.exe"));
    	File qt = fileChooser.showOpenDialog(mainApp.getPrimaryStage());
    	
    	if (qt != null) {
    		// Check if a wallet of the same currency is already saved.
    		if (MainApp.properties.containsKey(qt.getParentFile().getName()))
    			new Alert(AlertType.NONE, "You already have a saved " + qt.getParentFile().getName() + " wallet. "
    					+ "Remove it first, if you want to replace it.", ButtonType.CLOSE).show();
    		else {
    			Alert loading = new Alert(Alert.AlertType.NONE, "Generating new address...", ButtonType.CLOSE); // Loading message.
        		loading.show();
    			
    			try {
    				//Generate trading address for that wallet.
    				String address = MainApp.rpc(qt.getParent(), loading, "-regtest getnewaddress")[0];
        			if (address != null && !address.equals("")) {
	        			// Write new properties.
	        			MainApp.properties.setProperty(qt.getParentFile().getName(), qt.getParent()+ ", " + address); // Set the properties value.
	        			MainApp.properties.store(new FileOutputStream("config.properties"), null); // Save properties.
	        			
	        			mainApp.showPage("Wallets"); // Reload page.
        			}
	        	} catch(IOException e) {
	        		new Alert(AlertType.NONE, e.getClass().getSimpleName() + ": " + e.getMessage(), ButtonType.CLOSE).show();
	        		e.printStackTrace();
	        	}
    			
    			loading.close(); // Close loading message.
			}
    	}
    }
    
    /**
     * Remove property from the properties file.
     * @param key The property's key.
     */
    private void removeProperty(Object key) {
    	// Confirmation dialog.
    	Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "All orders, placed or matched, with this wallet's address will be removed from the exchange.",
    			ButtonType.YES, ButtonType.CANCEL);
    	confirm.setHeaderText("Are you sure ?");
    	
    	if (confirm.showAndWait().get() == ButtonType.YES) {
	    	try {
				String address = MainApp.properties.get(key).toString().split(", ")[1];
				MainApp.properties.remove(key); // Remove property.
				MainApp.properties.store(new FileOutputStream("config.properties"), null); // Save properties.
				
				// Delete all placed orders and cancel all matches for this address.
				Connection con = DriverManager.getConnection("jdbc:mysql://db4free.net:3306/cryptoexchange?useSSL=false", "nicosenetlarson", "Nico.1994");
				con.createStatement().execute("DELETE FROM orders WHERE SendAddress = '" + address+ "' OR ReceiveAddress = '" + address+ "';");
				con.createStatement().execute("UPDATE matches SET Status1 = 'Cancelled' WHERE Address1 = '" + address + "';");
				con.createStatement().execute("UPDATE matches SET Status2 = 'Cancelled' WHERE Address2 = '" + address + "';");
				
				mainApp.showPage("Wallets"); // Reload page.
	    	} catch(IOException | SQLException e) {
	    		new Alert(AlertType.NONE, e.getClass().getSimpleName() + ": " + e.getMessage(), ButtonType.CLOSE).show();
	    		e.printStackTrace();
	    	}
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
