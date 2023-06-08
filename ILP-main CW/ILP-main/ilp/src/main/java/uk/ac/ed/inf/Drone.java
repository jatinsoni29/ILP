package uk.ac.ed.inf;

import com.mapbox.geojson.Point;
import com.mapbox.turf.TurfClassification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class to control the drone
 * 
 *
 */

public class Drone {
	// Constants and variables useful for multiple classes
	public static final int MAX_MOVES = 1500;
	public static final double MOVE_DIST = 0.00015;
	public boolean flightComplete;

	// Private variables
	private final Location endLoc;
	private Location droneLoc;
	private Drone_Map drone_map;
	private List<Point> visitedPoints;
	private ArrayList<Integer> route;
	private List<Integer> bearings;
	private ArrayList<Order> deliveredOrders;
	private int visitedShopsPerOrder;

	/**
	 * Drone constructor
	 * 
	 * @param mapForDrone      - The map created by (Shops, confinement area, no flyzones)
	 * @param startLoc - Point where drone starts its flight
	 */
	public Drone(Drone_Map drone_map, Point startLoc) {
		this.drone_map = drone_map;
		this.endLoc = new Location(startLoc.latitude(), startLoc.longitude());
		this.droneLoc = new Location(startLoc.latitude(), startLoc.longitude());
		this.visitedPoints = new ArrayList<Point>();
		this.route = new ArrayList<Integer>();
		this.bearings = new ArrayList<Integer>();
		this.deliveredOrders = new ArrayList<>();
		this.visitedShopsPerOrder = 0;

		// Initialise drone with a flight route
		this.visitedPoints.add(this.droneLoc.getPoint());
		this.flightComplete = false;
		this.getRoute();
	}

	// Getters
	public List<Point> getVisitedPoints() {
		return this.visitedPoints;
	}

	public List<Integer> getBearings() {
		return this.bearings;
	}

	public ArrayList<Order> getDeliveredOrders() {
		return deliveredOrders;
	}

	public Location getDroneLoc() {
		return droneLoc;
	}

	public Location getEndLoc() {
		return endLoc;
	}

	// Methods
	/**
	 * Move drone to next position
	 */
	public void nextMove(Boolean shouldReturn) {
		// Choose a target for the drone
		var targetIdx = 0;
		var routeIdx = 0;
		Location targetLoc = null;
		Order targetOrder = null;

		// If the the rest of moves can only for the drone to fly back the origin, then start to return
		// Else, find the next closest shop of the order
		if (shouldReturn) {
			targetLoc = this.endLoc;
		} else if (this.deliveredOrders.size() == drone_map.getOrders().size()) {
			if (this.endInRange(this.droneLoc)) {
				this.flightComplete = true;
				return;
			}
			targetLoc = this.endLoc;
		} else {
			targetIdx = this.route.get(this.deliveredOrders.size());
			targetOrder = this.drone_map.getOrders().get(targetIdx);
			if (targetOrder.getRoute().size() == this.visitedShopsPerOrder) {
				targetLoc = targetOrder.getDeliverTo();
			} else {
				targetLoc = targetOrder.getShops().get(targetOrder.getRoute().get(this.visitedShopsPerOrder));
			}
		}
		
		// Move drone and update its current Location
		var destination = this.droneLoc.moveDrone(this.drone_map, targetLoc);

		this.droneLoc.setLat(destination.latitude());
		this.droneLoc.setLng(destination.longitude());

		if (targetOrder != null && targetOrder.delivered(this.droneLoc)) {
			this.deliveredOrders.add(targetOrder);
			this.visitedShopsPerOrder = 0;
		} else if (targetLoc != null && targetLoc != this.endLoc && targetLoc.closeTo(this.droneLoc)) {
			this.visitedShopsPerOrder++;
		}

		this.visitedPoints.add(this.droneLoc.getPoint());
		this.bearings.add(this.droneLoc.getBearing());
	}

	/**
	 * Finds a route of picking up orders and delivering orders using a Greedy Best-First approach
	 * 
	 * @return The route of sensors the drone should visit
	 */
	private ArrayList<Integer> getRoute() {
		// Make two local copies of the sensors so we don't modify the original
		var orderCopy = this.drone_map.getOrders();
		var orderPoints = new ArrayList<Point>();
		var orderPointsCopy = new ArrayList<Point>();

		for (int i = 0; i < orderCopy.size(); i++) {
			orderPoints.add(orderCopy.get(i).getStartLoc().getPoint());
			orderPointsCopy.add(orderCopy.get(i).getStartLoc().getPoint());
		}

		// Find closest shop and remove it from the list of ones unvisited and
		// add it to the route
		var nextOrder = TurfClassification.nearestPoint(this.droneLoc.getPoint(), orderPointsCopy);

		this.route.add(orderPoints.indexOf(nextOrder));
		orderPointsCopy.remove(nextOrder);

		// Find the rest of the route
		for (int i = 1; i < drone_map.getOrders().size(); i++) {
			nextOrder = TurfClassification.nearestPoint(orderCopy.get(orderPoints.indexOf(nextOrder)).getDeliverTo().getPoint(),
					orderPointsCopy);
			this.route.add(orderPoints.indexOf(nextOrder));
			orderPointsCopy.remove(nextOrder);
		}

		System.out.println("[Route of locations to visit: " + Arrays.toString(this.route.toArray()) + "]\n");

		return this.route;
	}

	/**
	 * Determine whether the end location is close enough to terminate the flight
	 * 
	 * @param droneLoc - Current Location of the drone
	 * @return the truth value of whether it's in range of the end point (i.e.
	 *         where it started).
	 */
	private boolean endInRange(Location droneLoc) {
		var dist = Math.sqrt(
				Math.pow((this.endLoc.getLng() - droneLoc.getLng()), 2) + Math
						.pow((this.endLoc.getLat() - droneLoc.getLat()), 2));
		var inRange = dist < MOVE_DIST;

		return inRange;
	}
}
