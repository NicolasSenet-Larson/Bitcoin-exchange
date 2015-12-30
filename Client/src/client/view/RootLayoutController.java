package client.view;

import java.io.IOException;

import client.MainApp;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;

public class RootLayoutController {
    // Reference to the main application.
    private MainApp mainApp;
    @FXML
    ToggleGroup toggleGroup; // Navigation bar toggle group.
    @FXML
    private ToggleButton homeButton; // Home toggle button.
    @FXML
    private ToggleButton exchangeButton; // Exchange Currency toggle button.
    @FXML
    private ToggleButton transactionsButton; // My Transactions toggle button.
    @FXML
    private ToggleButton walletsButton; // My Wallets toggle button.
    
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
    	homeButton.setUserData("Home");
    	exchangeButton.setUserData("Trading");
    	transactionsButton.setUserData("Transactions");
    	walletsButton.setUserData("Wallets");
    	
    	// Add change listener to toggle group.
    	toggleGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>(){
			@Override
			public void changed(ObservableValue<? extends Toggle> ov, Toggle toggle, Toggle new_toggle) {
				if (toggleGroup.getSelectedToggle() != null) {
					String userData = toggleGroup.getSelectedToggle().getUserData().toString();
					
					try {
	    		    	// Load view from fxml file.
	    		        FXMLLoader loader = new FXMLLoader();
	    		        loader.setLocation(MainApp.class.getResource("view/" + userData + "View.fxml"));
	    		        Node view = loader.load();

	    		        // Set view into the center of root layout.
	    		        BorderPane rootLayout = (BorderPane) mainApp.getPrimaryStage().getScene().getRoot();
	    		        rootLayout.setCenter(view);
	    		        
	    		        // Give the controller access to the main app.
	    		        switch (userData) {
	    		        	case "Home":
	    		        		HomeViewController homeController = loader.getController();
	        		        	homeController.setMainApp(mainApp);
		    		        	break;
	    		        	case "Trading":
		    		        	TradingViewController tradingController = loader.getController();
		    		        	tradingController.setMainApp(mainApp);
		    		        	break;
	    		        	case "Transactions":
		    		        	TransactionsViewController transactionsController = loader.getController();
		    		        	transactionsController.setMainApp(mainApp);
		    		        	break;
	    		        	case "Wallets":
		    		        	WalletsViewController walletsController = loader.getController();
		    		        	walletsController.setMainApp(mainApp);
		    		        	break;
	    		        }
	    	    	} catch (IOException e) {
	    	            e.printStackTrace();
	    	        }
				}
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
