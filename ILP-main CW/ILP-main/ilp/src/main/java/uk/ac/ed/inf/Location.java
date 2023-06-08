package uk.ac.ed.inf;

import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;

import java.awt.geom.Line2D;

/**
 * Class to track location of drone and specific shops.
 * 
 *
 */
public class Location {
	// Private variables
	private double lat;
	private double lng;
	private int bearing;

	/**
	 * Location constructor
	 * 
	 * @param lat - Latitude of item on map
	 * @param lng - Longitude of item on map
	 */
	public Location(Double lat, Double lng) {
		this.lat = lat;
		this.lng = lng;
		
		// Initial bearing of 0 for first move so lastValidBearing!=null
		this.bearing = 0;
	}

	// Getters
	public double getLat() {
		return this.lat;
	}

	public double getLng() {
		return this.lng;
	}

	public Point getPoint() {
		return Point.fromLngLat(this.lng, this.lat);
	}

	public int getBearing() {
		return this.bearing;
	}

	// Setters
	public void setLat(double lat) {
		this.lat = lat;
	}

	public void setLng(double lng) {
		this.lng = lng;
	}

	// Methods
	/**
	 * Make a single move for the drone ,and also checks the validation of the move
	 * 
	 * @param drone_map       - The map the drone will be navigating
	 * @param targetPos - where the drone is aiming to go
	 * @return The Point the drone will move to
	 */
	public Point moveDrone(Drone_Map drone_map, Location targetPos) {
		// Keep track of bearing of last valid move and find a new target
		// bearing
		var lastValidBearing = this.bearing;
		this.bearing = bearing(this, targetPos);
		var move = false;
		var incrementBearing = true;
		Point nextPos = null;
		var counter = 1;

		// Recalculate the target bearing until it results in a valid move
		do {
			nextPos = destination(this, this.bearing);
			if (this.validDroneMove(drone_map, nextPos)) {
				move = true;
				lastValidBearing = this.bearing;
			} else {
				//System.out.println("Readjusting bearing...");
				// To recalculate the bearing, alternate +10 and -10 degrees
				// from the last valid bearing.
				if (incrementBearing) {
					this.bearing += 10 * counter;

					// Ensure valid bearing
					this.bearing = this.validBearing(this.bearing);

					// If the drone is about to go back on it self, adjust
					// bearing again but in the opposite direction
					if (lastValidBearing - this.bearing == 180
							|| this.bearing - lastValidBearing == 180) {
						this.bearing -= 10;
						this.bearing = this.validBearing(this.bearing);
					}
					incrementBearing = false;
				} else {
					this.bearing -= 10 * counter;
					this.bearing = this.validBearing(this.bearing);

					if (lastValidBearing - this.bearing == 180
							|| this.bearing - lastValidBearing == 180) {
						this.bearing += 10;
						this.bearing = this.validBearing(this.bearing);
					}
					incrementBearing = true;
				}
				counter++;
			}
		} while (!move);

		return nextPos;
	}

	/**
	 * Calculate the bearing angle between the previous point and the target Location
	 * 
	 * @param prevPos   - Previous drone Location
	 * @param targetPos - Target drone Location
	 * @return The bearing to target Location
	 */
	private int bearing(Location prevPos, Location targetPos) {
		// Find change in latitude and longitude between the two Locations
		var deltaLat = targetPos.getLat() - prevPos.getLat();
		var deltaLng = targetPos.getLng() - prevPos.getLng();

		// Find bearing and ensure its validity
		var radius = Math.atan2(deltaLat, deltaLng);
		if (radius < 0) {
			radius = 2 * Math.PI + radius;
		}
		var bearingToTarget = (int) Math
				.toDegrees(radius);
		bearingToTarget = validBearing(bearingToTarget);

		return (int) Math.round(bearingToTarget / 10) * 10;
	}

	/**
	 * Find the point of the next move
	 * 
	 * @param prevPos         - Previous Location of the drone
	 * @param bearingToTarget - The target found to next Point
	 * @return The Point the drone will move to
	 */
	private Point destination(Location prevPos, int bearingToTarget) {
		// Rudimentary trig to calculate new longitude and latitude
		var nextPosLng = prevPos.getLng()
				+ Drone.MOVE_DIST * Math.cos(Math.toRadians(bearingToTarget));
		var nextPosLat = prevPos.getLat()
				+ Drone.MOVE_DIST * Math.sin(Math.toRadians(bearingToTarget));

		return Point.fromLngLat(nextPosLng, nextPosLat);
	}

	/**
	 * Ensure the proposed move doesn't fly over restricted areas
	 * 
	 * @param drone_map     - The map the drone is navigating
	 * @param nextPos - The next proposed Point to move to
	 * @return The truth value of the validity of the move
	 */
	private boolean validDroneMove(Drone_Map drone_map, Point nextPos) {
		// Proposed move
		var linePath = new Line2D.Double(this.lat, this.lng, nextPos.latitude(),
				nextPos.longitude());

		// List of buildings
		String[] buildings = { "McEwan Hall Complex", "Teviot",
				"Wilkie Building", "Psychology and Neuroscience", "Chrystal Macmillan and Hugh Robson"};

		// Check intersections of proposed move path and borders
		// Crossing confinement area border
		var crossConfinements = false;
		var confPoints = drone_map.getConfPoints();

		for (int i = 0; i < confPoints.size() - 1; i++) {
			int j = (i + 1) % confPoints.size();
			Line2D barrier = new Line2D.Double(
					confPoints.get(i).coordinates().get(1),
					confPoints.get(i).coordinates().get(0),
					confPoints.get(j).coordinates().get(1),
					confPoints.get(j).coordinates().get(0));

			if (linePath.intersectsLine(barrier)) {
				System.out.println("Illegal move, Attempted to fly out of confinemeant area.");
				crossConfinements = true;
				break;
			}
			if (crossConfinements) {
				break;
			}
		}

		// Crossing a no-fly-zone border
		var crossNoFlyZone = false;
		var noFlyZones = drone_map.getNoFlyZones();
		for (int i = 0; i < noFlyZones.size(); i++) {
			var nfzPoly = (Polygon) noFlyZones.get(i);
			var nfzPoints = nfzPoly.coordinates().get(0);

			for (int j = 0; j < nfzPoints.size() - 1; j++) {
				int k = (j + 1) % nfzPoints.size();
				Line2D barrier = new Line2D.Double(nfzPoints.get(j).latitude(),
						nfzPoints.get(j).longitude(),
						nfzPoints.get(k).latitude(),
						nfzPoints.get(k).longitude());
				if (linePath.intersectsLine(barrier)) {
					System.out.println(
							"Illegal move, Attempted to fly through building '" + buildings[i] + "'");
					crossNoFlyZone = true;
					break;
				}
			}
			if (crossNoFlyZone) {
				break;
			}
		}
		var validMove = !crossConfinements && !crossNoFlyZone;
		return validMove;
	}

	/**
	 * Ensure validity of the bearing
	 *
	 * @param bearing - The bearing we are working with
	 * @return An equivalent bearing within the range 0 <= bearing < 360 degrees
	 */
	private int validBearing(int bearing) {
		if (bearing < 0)
			bearing += 360;
		if (bearing >= 360)
			bearing -= 360;

		return bearing;
	}

	public boolean closeTo(Location loc) {
		var dist = Math.sqrt(
				Math.pow((this.getLng() - loc.getLng()), 2) + Math
						.pow((this.getLat()- loc.getLat()), 2));
		var inRange = dist < Drone.MOVE_DIST;

		return inRange;
	}
}