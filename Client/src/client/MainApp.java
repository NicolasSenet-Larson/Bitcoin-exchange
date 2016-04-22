package client;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import client.view.RootLayoutController;
import client.view.TransactionsViewController;
import client.view.WalletsViewController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class MainApp extends Application {
	
	private Stage primaryStage;
	public static Properties properties;
	private BorderPane rootLayout;
	
	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle("Client");
		this.primaryStage.setMinWidth(800);
		this.primaryStage.setMinHeight(600);
		
		// Load properties from file.
		properties = new Properties();
		try {
			properties.load(new FileInputStream("config.properties"));
		} catch (IOException e) {
			new Alert(AlertType.NONE, e.getClass().getSimpleName() + ": " + e.getMessage(), ButtonType.CLOSE).show();
			e.printStackTrace();
		}
		
		// Get the MySql connector class.
    	try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			new Alert(AlertType.NONE, e.getClass().getSimpleName() + ": " + e.getMessage(), ButtonType.CLOSE).show();
			e.printStackTrace();
		}
    	
    	// Load root layout.
		initRootLayout();
	}
	
	/**
     * Initialises the root layout.
     */
    private void initRootLayout() {
        try {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/RootLayout.fxml"));
            rootLayout = (BorderPane) loader.load();
            
            // Give the controller access to the main application.
            RootLayoutController controller = loader.getController();
            controller.setMainApp(this);
            
            // Show the scene containing the root layout.
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
        	new Alert(AlertType.NONE, e.getClass().getSimpleName() + ": " + e.getMessage(), ButtonType.CLOSE).show();
            e.printStackTrace();
        }
    }
    
    /**
     * Displays the chosen page.
     * @param page The page to display.
     */
    public void showPage(String page) {
        try {
            // Load home view from fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/" + page + "View.fxml"));
            Node view = loader.load();
            
            // Set home view into the center of root layout.
	        rootLayout.setCenter(view);
            
            // Give the controller access to the main application.
	        switch (page) {
	        	case "Transactions":
	        		TransactionsViewController ordersController = loader.getController();
		        	ordersController.setMainApp(this);
		        	break;
	        	case "Wallets":
	        		WalletsViewController walletsController = loader.getController();
		        	walletsController.setMainApp(this);
		        	break;
	        }
        } catch (IOException e) {
        	new Alert(AlertType.NONE, e.getClass().getSimpleName() + ": " + e.getMessage(), ButtonType.CLOSE).show();
            e.printStackTrace();
        }
    }
    
    /**
     * Start a wallet RPC service and run some commands.
     * @param dir The path to the directory of the wallet application.
     * @param loading A loading alert if there is one.
     * @param commands The RPC commands to run.
     * @return Array of commands results.
     * @throws IOException
     */
    public static String[] rpc(String dir, Alert loading, String... commands) throws IOException {
    	final String path = "\"" + dir + "\\daemon\\" + dir.substring(dir.lastIndexOf('\\') + 1).toLowerCase();
    	
    	// Start wallet RPC service.
    	Thread start = new Thread() {
    		public void run() {
    			try {
			    	Process pr = Runtime.getRuntime().exec(path + "d.exe\" -regtest -daemon"); // Execute command.
			    	BufferedReader err = new BufferedReader(new InputStreamReader(pr.getErrorStream())); // Get error stream.
			    	
			    	// Build error String.
			    	StringBuilder error = new StringBuilder();
			    	String line;
			    	while ((line = err.readLine()) != null)
						error.append(line);
			    	
			    	// Print error if there is one.
					if (error.length() > 0)
						System.out.println(dir.substring(dir.lastIndexOf('\\') + 1) + " Start: " + error.toString());
    			} catch(IOException e) {
    	            e.printStackTrace();
    			}
    		}
    	};
    	start.start();
    	
    	// Execute RPC commands.
    	Process pr = null;
    	BufferedReader in = null;
    	BufferedReader err = null;
    	StringBuilder result = null;
    	StringBuilder error = null;
    	String[] results = new String[commands.length];
    	
    	for (int i = 0; i < commands.length; i++) {
    		while (true) {
	    		pr = Runtime.getRuntime().exec(path + "-cli.exe\" " + commands[i]); // Execute command.
		    	in = new BufferedReader(new InputStreamReader(pr.getInputStream())); // Result stream.
				err = new BufferedReader(new InputStreamReader(pr.getErrorStream())); // Error stream.
				result = new StringBuilder();
				error = new StringBuilder();
				String line;
				
				// Build result String.
				while ((line = in.readLine()) != null)
					result.append(line);
				
				// Store result in array.
				results[i] = result.toString();
				
				// Build error String.
				while ((line = err.readLine()) != null)
					error.append(line);
				
				if (error.length() > 0)
					if (error.toString().contains("\"code\":-28")) { // Retry if the wallet was verifying blocks.
						continue;
					} else {
						// Display error if there is one.
						System.out.println("Error!");
						new Alert(Alert.AlertType.NONE, dir.substring(dir.lastIndexOf('\\') + 1) + " Wallet: " + error.toString(), ButtonType.CLOSE).show();
						// Close any loading message.
						if (loading != null)
							loading.close();
					}
				break;
    		}
    	}
    	
    	// Stop wallet RPC service.
    	pr = Runtime.getRuntime().exec(path + "-cli.exe\" -regtest stop"); // Execute command.
    	err = new BufferedReader(new InputStreamReader(pr.getErrorStream())); // Get error stream.
    	
    	// Build error String.
    	error = new StringBuilder();
    	String line;
    	while ((line = err.readLine()) != null)
			error.append(line);
    	
    	// Print error if there is one.
		if (error.length() > 0)
			System.out.println(dir.substring(dir.lastIndexOf('\\') + 1) + " Stop: " + error.toString());
		
		// Return results.
		return results;
    }
    
    /**
     * Gets the main stage.
     * @return the main stage.
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }
    
	public static void main(String[] args) {
		launch(args);
	}
}
