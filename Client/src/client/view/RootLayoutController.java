package client.view;

import client.MainApp;
import javafx.fxml.FXML;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;

public class RootLayoutController {
    @FXML
    private ToggleGroup toggleGroup; // Navigation bar toggle group.
    @FXML
    private ToggleButton exchangeToggle; // Exchange Currency toggle button.
    @FXML
    private ToggleButton transactionsToggle; // My Transactions toggle button.
    @FXML
    private ToggleButton walletsToggle; // My Wallets toggle button.
    
    private MainApp mainApp; // Reference to the main application.
    
	/**
     * The constructor.
     * The constructor is called before the initialize() method.
     */
    public RootLayoutController() {
    }

    /**
     * Initialises the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
    	// Set toggle buttons user data.
    	exchangeToggle.setUserData("Trading");
    	transactionsToggle.setUserData("Transactions");
    	walletsToggle.setUserData("Wallets");
    	
    	// Add change listener to toggle group.
    	toggleGroup.selectedToggleProperty().addListener((observable, oldToggle, newToggle) -> {
			if (newToggle != null)
				if (newToggle == exchangeToggle && MainApp.properties.keySet().size() < 2)
					toggleGroup.selectToggle(walletsToggle); // If not enough saved wallets, go to wallets.
				else
					mainApp.showPage(newToggle.getUserData().toString()); // Show toggled page.
			else
				toggleGroup.selectToggle(oldToggle); // Always keep toggled.
    	});
    }
    
    /**
     * Is called by the main application to give a reference back to itself.
     * @param mainApp the main application.
     */
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
        
        // If enough wallets are saved, go to exchange, otherwise go to wallets.
        if (MainApp.properties.keySet().size() >= 2)
 			toggleGroup.selectToggle(exchangeToggle);
 		else
 			toggleGroup.selectToggle(walletsToggle);
    }
}
