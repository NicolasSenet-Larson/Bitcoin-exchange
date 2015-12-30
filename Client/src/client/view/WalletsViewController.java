package client.view;

import client.MainApp;
import javafx.fxml.FXML;

public class WalletsViewController {
    // Reference to the main application.
    private MainApp mainApp;
    
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
    }
    
    /**
     * Is called by the main application to give a reference back to itself.
     * @param mainApp the main application.
     */
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }
}
