package server.view;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import server.MainApp;

public class RootLayoutController {
    @FXML
    private TextArea textArea;
	
    // Reference to the main application.
    private MainApp mainApp;
    
	/**
     * The constructor.
     * The constructor is called before the initialize() method.
     */
    public RootLayoutController() {
    }

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
    }
    
    /**
     * Is called by the main application to give a reference back to itself.
     * @param mainApp
     */
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }
    
    @FXML
    private void testBitcoin() {
        final String rpcuser = "Nicolas";
        final String rpcpassword = "Nico.1994";
    	
		Authenticator.setDefault(new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication (rpcuser, rpcpassword.toCharArray());
			}
		});
		
		try {
			HttpURLConnection con = (HttpURLConnection) new URL("http://localhost:18332").openConnection();
			// Add request header
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json-rpc");
			
			String urlParameters = "{\"method\": \"getbalance\"}";
			
			// Send post request
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(urlParameters);
			wr.flush();
			wr.close();
			
			textArea.appendText("\n> Sending 'POST' request to URL : " + con.getURL());
			textArea.appendText("\n  Post parameters : " + urlParameters);
			textArea.appendText("\n  Response Code : " + con.getResponseCode());
			
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			StringBuffer response = new StringBuffer();
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			textArea.appendText("\n" + response.toString());
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			textArea.appendText("\n> " + e.getMessage());
			e.printStackTrace();
		}
    }
}
