package client;

import java.io.IOException;

import client.view.HomeViewController;
import client.view.RootLayoutController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class MainApp extends Application {

	private Stage primaryStage;
	private BorderPane rootLayout;
	
	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle("Client");
		
		initRootLayout();
		showHomeView();

		primaryStage.setMinWidth(500);
		primaryStage.setMinHeight(500);
	}
	
	/**
     * Initialises the root layout.
     */
    public void initRootLayout() {
        try {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/RootLayout.fxml"));
            rootLayout = (BorderPane) loader.load();
            
            // Give the controller access to the main app.
            RootLayoutController controller = loader.getController();
            controller.setMainApp(this);
            
            // Show the scene containing the root layout.
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Shows the home view.
     */
    public void showHomeView() {
        try {
            // Load home view from fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/HomeView.fxml"));
            Node homeView = loader.load();
            
            // Set home view into the center of root layout.
	        rootLayout.setCenter(homeView);
            
            // Give the controller access to the main app.
            HomeViewController controller = loader.getController();
            controller.setMainApp(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Gets the main stage.
     * @return the main stage.
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }
    
    /**
     * Gets the main application object.
     * @return the main application object.
     */
    public MainApp getMainApp() {
        return this;
    }
    
	public static void main(String[] args) {
		launch(args);
	}
}
