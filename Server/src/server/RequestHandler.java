package server;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import javafx.collections.ObservableList;
import javafx.scene.control.TextArea;
import server.model.Order;

/**
 * The HTTP request handler.
 * @author Nicolas Senet-Larson
 */
public class RequestHandler implements HttpHandler {
	private ObservableList<Order> orders;
	private TextArea textArea;
	
	public RequestHandler(ObservableList<Order> orders, TextArea textArea) {
		this.orders = orders;
		this.textArea = textArea;
	}
	
	@Override
	public void handle(HttpExchange exchange) throws IOException {
		switch (exchange.getRequestMethod()) {
		
		// POST requests.
		case "POST":
			DataInputStream in = new DataInputStream(exchange.getRequestBody());
			String[] params = in.readUTF().split(",");
			orders.add(new Order(params[0], params[1], new BigDecimal(params[2]), new BigDecimal(params[3]), params[4]));
    		exchange.sendResponseHeaders(200, 0);
    		
		// GET requests.
		case "GET":
			orders.get(orders.indexOf(new Order("BTC", "", BigDecimal.ZERO, BigDecimal.ZERO, "")));
			textArea.appendText(exchange.getRequestURI().getQuery() + "\n");
			
			Headers responseHeaders = exchange.getResponseHeaders();
    	    responseHeaders.set("Content-Type", "text/plain");
    	    exchange.sendResponseHeaders(200, 0);
			
			OutputStream responseBody = exchange.getResponseBody();
			Headers requestHeaders = exchange.getRequestHeaders();
			
			Set<String> keySet = requestHeaders.keySet();
			
			Iterator<String> iter = keySet.iterator();
			while (iter.hasNext()) {
				String key = iter.next();
				List<String> values = requestHeaders.get(key);
				String s = key + " = " + values.toString() + "\n";
				responseBody.write(s.getBytes());
			}
			responseBody.close();
			
		// Unknown requests.
		default:
			exchange.sendResponseHeaders(400, 0);
		}
	}
}