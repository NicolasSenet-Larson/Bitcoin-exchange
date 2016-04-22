package server.view;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpServer;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.WindowEvent;
import server.MainApp;
import server.RequestHandler;
import server.model.Order;

public class RootLayoutController {
	@FXML
    private ToggleButton startToggle;
    @FXML
    private TableView<Order> orderTable;
    @FXML
    private TableColumn<Order, String> fromColumn;
    @FXML
    private TableColumn<Order, String> toColumn;
    @FXML
    private TableColumn<Order, String> quantityColumn;
    @FXML
    private TableColumn<Order, String> rateColumn;
    @FXML
    private TableColumn<Order, String> addressColumn;
    @FXML
    private TextArea textArea;
	
    private MainApp mainApp; // Reference to the main application.
    private ObservableList<Order> orders = FXCollections.observableArrayList();
    private HttpServer server;
    
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
		// Initialise order table.
		fromColumn.setCellValueFactory(new PropertyValueFactory<Order, String>("from"));
		toColumn.setCellValueFactory(new PropertyValueFactory<Order, String>("to"));
		quantityColumn.setCellValueFactory(new PropertyValueFactory<Order, String>("quantity"));
		rateColumn.setCellValueFactory(new PropertyValueFactory<Order, String>("rate"));
		addressColumn.setCellValueFactory(new PropertyValueFactory<Order, String>("address"));
    	orderTable.setItems(orders);
    	
		// Start/stop server on toggle.
    	startToggle.selectedProperty().addListener(new ChangeListener<Boolean>(){
			@Override
			public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
				try {
					if (newValue) {
						server = HttpServer.create(new InetSocketAddress(8080), 0);
						server.createContext("/", new RequestHandler(orders, textArea));
						server.setExecutor(Executors.newCachedThreadPool());
						server.start();
						textArea.appendText("Server is listening on port 8080.\n");

						mainApp.getPrimaryStage().setOnCloseRequest(new EventHandler<WindowEvent>(){
							public void handle(WindowEvent event) {
								server.stop(0);
							}
						});
					} else {
						server.stop(0);
						textArea.appendText("Server stopped.\n");
					}
				} catch (IOException e) {
					startToggle.setSelected(oldValue);
					textArea.appendText(e.getClass().getName() + ": " + e.getMessage() + ".\n");
				}
			}
		});
    }
    
    /**
     * Is called by the main application to give a reference back to itself.
     * @param mainApp
     */
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }
}
