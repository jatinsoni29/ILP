package uk.ac.ed.inf;

import com.mapbox.geojson.Point;
import com.mapbox.turf.TurfClassification;
import com.mapbox.turf.TurfMeasurement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Class to show the status of orders and get the route of drone
 * 
 *
 */

public class Order {
	// Private constants and variables
	private Location deliverTo;
	private List<Location> shops;
	private double price;
	private ArrayList<Integer> route;
	private String orderNo;
	private Location startLoc;

	public Order(String orderNo, Location deliverTo, List<Location> shops, double price) {
		this.orderNo = orderNo;
		this.deliverTo = deliverTo;
		this.shops = shops;
		this.price = price;
		this.route = new ArrayList<Integer>();
		generateRoute();
		this.startLoc = this.shops.get(this.route.get(0));
	}

	// Getters

	public Location getStartLoc() {
		return startLoc;
	}

	public String getOrderNo() {
		return orderNo;
	}

	public Location getDeliverTo() {
		return deliverTo;
	}

	public ArrayList<Integer> getRoute() {
		return route;
	}

	public List<Location> getShops() {
		return shops;
	}

	public double getPrice() {
		return price;
	}

	/**
	 * Determine whether orders are delivered
	 * 
	 * @param droneLoc - Location of the drone
	 * @return Truth value of the drone's Location is within range of the target
	 *         Sensor
	 */
	public boolean delivered(Location droneLoc) {
		var deliverTo= this.deliverTo;
		var dist = Math.sqrt(
				Math.pow((deliverTo.getLng() - droneLoc.getLng()), 2) + Math
						.pow((deliverTo.getLat() - droneLoc.getLat()), 2));
		var inRange = dist < Drone.MOVE_DIST;

		return inRange;
	}

	/**
	 * update the route if the point is visited
	 */
	public void updateRoute() {
		this.route.remove(0);
	}

	/**
	 * Generate the route of delivering orders
	 * This method finds an efficient route to send as many as orders on the map, using a Greedy Best-First algorithm
	 */
	private void generateRoute() {
		// Make two local copies of the sensors so we don't modify the original
		var deliverFromCopy = this.shops;
		var shops = new ArrayList<Point>();
		var shopsCopy = new ArrayList<Point>();

		for (Location location : deliverFromCopy) {
			shops.add(location.getPoint());
			shopsCopy.add(location.getPoint());
		}

		// Find closest sensor and remove it from the list of ones unvisited and
		// add it to the route
		var nextShop = TurfClassification
				.nearestPoint(this.deliverTo.getPoint(), shopsCopy);
		this.route.add(shops.indexOf(nextShop));
		shopsCopy.remove(nextShop);

		// Find the rest of the route
		for (int i = 1; i < this.shops.size(); i++) {
			var lastShop = nextShop;
			nextShop = TurfClassification.nearestPoint(nextShop,
					shopsCopy);
			var distance = TurfMeasurement.distance(lastShop, nextShop);
			this.route.add(shops.indexOf(nextShop));
			shopsCopy.remove(nextShop);
		}
		Collections.reverse(this.route);
	}
}
