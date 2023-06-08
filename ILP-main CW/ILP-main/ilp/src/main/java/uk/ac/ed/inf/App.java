package uk.ac.ed.inf;

import com.mapbox.geojson.Point;
import com.mapbox.turf.TurfMeasurement;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * App for initiating a drone flight
 * 
 *
 */

public class App {
	// Constant IP
	private static final String IP = "localhost";

	// Methods
	/**
	 * Write files
	 * 
	 * @param yyyy       - The year of the flight
	 * @param mm         - The month of the flight
	 * @param dd         - The day of the flight
	 * @throws IOException If file cannot be written
	 */
	private static void writeFiles(String readings, String yyyy,
			String mm, String dd) {
		// Write the readings file
		try {
			FileWriter myWriter = new FileWriter(
					"drone-" + dd + "-" + mm + "-" + yyyy + ".geojson");
			myWriter.write(readings);
			myWriter.close();
			System.out.println("Readings GeoJson successfully created!");
		} catch (IOException e) {
			System.out.println("Fatal error: Readings GeoJson wasn't created.");
			e.printStackTrace();
		}
	}

	/**
	 * Main
	 * 
	 * @param args
	 *             <ul>
	 *             <li>[0] - Flight day</li>
	 *             <li>[1] - Flight month</li>
	 *             <li>[2] - Flight year</li>
	 *             <li>[3] - Starting latitude of the drone</li>
	 *             <li>[4] - Starting longitude of the drone</li>
	 *             <li>[5] - Seed for flights <b>[UNUSED]</b></li>
	 *             <li>[6] - Port for the server</li>
	 *             </ul>
	 */
	public static void main(String[] args) {
		// Take in arguments for the details of the flight
		var dd = args[0];
		var mm = args[1];
		var yyyy = args[2];
		var webPort = args[3];
		var sqlPort = args[4];

		// Set up a connection to web server and database server and reader with it
		var httpConn = new HttpConnection(IP, webPort);
		var sqlConn = new SQLConnection(IP, sqlPort);
		var reader = new infoReader(httpConn, sqlConn);

		// Create a new flight map and drone by parsing the JSON files and text
		System.out.println("> CONNECTING TO SERVER...");
		var map = new Drone_Map(reader, yyyy, mm, dd);
		System.out.println("> CONNECTION COMPLETE!\n");

		var startPoint = Point.fromLngLat(-3.186874,55.944494);
		var drone = new Drone(map, startPoint);

		// Start flight path
		System.out.println("> FLIGHT BEGINING...");
		for (int i = 0; i < Drone.MAX_MOVES; i++) {
			// Move drone 1 time
			Boolean shouldReturn = ((Drone.MAX_MOVES - i) * Drone.MOVE_DIST) <=
					TurfMeasurement.distance(drone.getDroneLoc().getPoint(), drone.getEndLoc().getPoint())+Drone.MOVE_DIST;
			drone.nextMove(shouldReturn);

			// If we reach within 0.0003 of our starting position before 150
			// moves then end the flight
			if (drone.flightComplete)
				break;
		}
		System.out.println("> FLIGHT COMPLETE!\n");
		System.out.println("[Flight ended in "
				+ (drone.getVisitedPoints().size() - 1) + " moves]\n");

		try {
			sqlConn.getStatement().execute("create table deliveries(orderNo char(8)," +
					"deliveredTo varchar(19)," +
					"costInPence int)");
			PreparedStatement ps = sqlConn.getConn().prepareStatement(
					"insert into deliveries values (?, ?, ?)");
			for (Order order: drone.getDeliveredOrders()) {
				ps.setString(1, order.getOrderNo());
				ps.setString(2, order.getDeliverTo().toString());
				ps.setDouble(3, order.getPrice());
				ps.execute();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		try {
			sqlConn.getStatement().execute("create table flightpath(orderNo char(8),\n" +
					"fromLongitude double," +
					"fromLatitude double," +
					"angle integer," +
					"toLongitude double," +
					"toLatitude double)");
			PreparedStatement ps = sqlConn.getConn().prepareStatement(
					"insert into flightpath values (?, ?, ?, ?, ?)");
			for (int i = 0; i <= drone.getVisitedPoints().size() - 1; i++) {
				ps.setDouble(1, drone.getVisitedPoints().get(i).longitude());
				ps.setDouble(2, drone.getVisitedPoints().get(i).latitude());
				ps.setDouble(3, drone.getBearings().get(i));
				ps.setDouble(1, drone.getVisitedPoints().get(i+1).longitude());
				ps.setDouble(2, drone.getVisitedPoints().get(i+1).latitude());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// Write file:
		// Flight Path - the GeoJson file which collects series of moves taken by the dronn
		// drone for GeoJSON
		System.out.println("> WRITING FILES...");
		var readings = map.getReadings(drone.getVisitedPoints());
		writeFiles(readings, yyyy, mm, dd);
		System.out.println("> WRITING COMPLETE!");
	}
}