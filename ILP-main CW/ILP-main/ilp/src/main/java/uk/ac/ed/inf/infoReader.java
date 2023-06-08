package uk.ac.ed.inf;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;

import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Class to read information from other files that contain the map details
 * 
 *
 */

public class infoReader {
	// Private variables
	private final HttpConnection httpConn;
	private final SQLConnection sqlConn;
	private List<Feature> buildings;
	private final Map<String, String> shops;
	private Map<String, Integer> prices;
	private final List<List<String>> orders;
	private List<String> orderNo;
	private List<String> deliverTo;
	private double wordsLng;
	private double wordsLat;

	public infoReader(HttpConnection httpConn, SQLConnection sqlConn) {
		this.httpConn = httpConn;
		this.sqlConn = sqlConn;
		this.prices = new HashMap<>();
		this.shops = new HashMap<>();
		this.orders = new ArrayList<>();
		this.orderNo = new ArrayList<>();
		this.deliverTo = new ArrayList<>();
	}

	// Getters
	public List<String> getOrderNo() {
		return orderNo;
	}

	public Map<String, String> getShops() {
		return shops;
	}

	public Map<String, Integer> getPrices() {
		return prices;
	}

	public List<List<String>> getOrders() {
		return orders;
	}

	public List<String> getDeliverTo() {
		return deliverTo;
	}

	public List<Feature> getBuildings() {
		return buildings;
	}

	public double getWordsLng() {
		return wordsLng;
	}

	public double getWordsLat() {
		return wordsLat;
	}

// Methods
	/**
	 * Parse buildings and landmarks.
	 */
	public void readBuildings() {
		this.httpConn.connToUrl(httpConn.getServer() + "/buildings/no-fly-zones.geojson");
		System.out.println("No-Fly Zones fetched!");

		this.buildings = FeatureCollection.fromJson(this.httpConn.getJson()).features();
	}

	/**
	 * Parse the maps and the databses which contains orders information.
	 */
	public void readMaps(String yyyy, String mm, String dd) {
		// Set up the connection to the json file and the derby database
		this.httpConn.connToUrl(httpConn.getServer() + "/menus/menus.json");
		this.sqlConn.connToSQL((sqlConn.getServer() + "/derbyDB"));
		System.out.println("Orders Data fetched!");

		// Read Json file into menu object
		Type listType = new TypeToken<ArrayList<Menu>>() {
		}.getType();
		ArrayList<Menu> menus = new Gson().fromJson(this.httpConn.getJson(),
				listType);


		// Save menus for all the shops to our map as well as their corresponding locations
		for (int i = 0; i < menus.size(); i++) {
			for (int j = 0; j < menus.get(i).menu.size(); j++) {
				this.shops.put(menus.get(i).menu.get(j).item, menus.get(i).location);
				this.prices.put(menus.get(i).menu.get(j).item, menus.get(i).menu.get(j).pence);
			}
		}
		try {
			ResultSet orderSet = sqlConn.executeQuery("SELECT orderNo, deliverTo FROM orders WHERE deliveryDate='" +
					yyyy + '-' + mm + '-' + dd + "'");

			while (orderSet.next()) {
				this.orderNo.add(orderSet.getString("orderNo"));
				this.deliverTo.add(orderSet.getString("deliverTo"));
			}

			for (int x = 0; x < this.orderNo.size(); x++) {
				List<String> content = new ArrayList<String>();
				ResultSet itemSet = sqlConn.executeQuery("SELECT item FROM orderDetails WHERE orderNo='"
						+ this.orderNo.get(x) + "'");

				while (itemSet.next()) {
					content.add(itemSet.getString("item"));
				}
				this.orders.add(content);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Parse words, i.e. an area on the map, from what3words location
	 *
	 * @param location ir reads a what3word location and return a location on the map.
	 */
	public List<Double> readLocation(String location) {
		String[] words = location.split("\\.");
		// Get location from given words
		this.httpConn.connToUrl(httpConn.getServer() + "/words/" + words[0] + "/" + words[1] + "/" + words[2]
				+ "/details.json");

		// Assign to Details class
		var loc = new Gson().fromJson(this.httpConn.getJson(), LocationUtils.class);

		// Set coords of the sensors
		return Arrays.asList(loc.coordinates.lng, loc.coordinates.lat);
	}
}
