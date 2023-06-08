package uk.ac.ed.inf;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class to create the map for the drone to navigate
 * 
 *
 */
public class Drone_Map {

	// Private constants and variables
	private static final double LNG1 = -3.192473;
	private static final double LNG2 = -3.184319;
	private static final double LAT1 = 55.946233;
	private static final double LAT2 = 55.942617;
	private List<Point> confPoints;
	private List<Geometry> noFlyZones;
	private List<Order> orders;

	/**
	 * Map constructor
	 * 
	 * @param parser - The object needed to parse we need files on the server
	 * @param yyyy   - The flight year
	 * @param mm     - The flight month
	 * @param dd     - The flight day
	 */
	public Drone_Map(infoReader reader, String yyyy, String mm, String dd) {
		this.confPoints = new ArrayList<>();
		this.noFlyZones = new ArrayList<>();
		this.orders = new ArrayList<>();

		// Create map on object creation
		setUpMap(reader, yyyy, mm, dd);
	}

	// Getters
	public List<Point> getConfPoints() {
		return confPoints;
	}

	public List<Geometry> getNoFlyZones() {
		return this.noFlyZones;
	}

	public List<Order> getOrders() {
		return this.orders;
	}

	// Methods
	/**
	 * Set up the map to the given date
	 * 
	 * @param reader - The object needed to parse we need files on the server
	 * @param yyyy   - The flight year
	 * @param mm     - The flight month
	 * @param dd     - The flight day
	 */
	private void setUpMap(infoReader reader, String yyyy, String mm,
						  String dd) {

		// Confinement area points to feature collection
		this.confPoints = new ArrayList<>(Arrays.asList(
				Point.fromLngLat(LNG1, LAT1), Point.fromLngLat(LNG2, LAT1),
				Point.fromLngLat(LNG2, LAT2), Point.fromLngLat(LNG1, LAT2),
				Point.fromLngLat(LNG1, LAT1)));

		// Parse buildings
		reader.readBuildings();
		var buildingsList = reader.getBuildings();
		for (int i = 0; i < buildingsList.size(); i++) {
			this.noFlyZones.add(buildingsList.get(i).geometry());
		}

		// read the map locations.
		reader.readMaps(yyyy, mm, dd);
		System.out.println("What3Words Data fetched!");

		var shops = reader.getShops();
		var prices = reader.getPrices();

		for (int i = 0; i < reader.getOrders().size(); i++) {
			List<String> items = reader.getOrders().get(i);
			String deliverTo = reader.getDeliverTo().get(i);
			List<Location> deliverFroms = new ArrayList<Location>();

			int orderPrice = 50;

			var orderLoc = new Location(reader.readLocation(deliverTo).get(1), reader.readLocation(deliverTo).get(0));

			for (String item: items){
				String shop = shops.get(item);
				var shopLoc = new Location(reader.readLocation(shop).get(1), reader.readLocation(shop).get(0));
				deliverFroms.add(shopLoc);

				orderPrice = orderPrice + prices.get(item);
			}
			
			var order = new Order(reader.getOrderNo().get(i), orderLoc, deliverFroms, orderPrice);
			this.orders.add(order);
		}
	}

	/**
	 * Create the flight path
	 * 
	 * @param dronePoints - The points visited in sequential order
	 * @param bearings    - The bearings chosen in sequential order
	 * @param words       - The 3 word location of the sensors visited
	 * @return An array of strings of all the moves
	 */
	public String[] getFlightPath(List<Point> dronePoints,
			List<Integer> bearings, List<String> words) {
		String[] lines = new String[dronePoints.size()];

		// Add each move to a new entry in the array
		// Loop the size -1 since the last point is the destination, not a move
		for (int i = 0; i < dronePoints.size() - 1; i++) {
			lines[i] = (i + 1) + "," + dronePoints.get(i).longitude() + ","
					+ dronePoints.get(i).latitude() + "," + bearings.get(i)
					+ "," + dronePoints.get(i + 1).longitude() + ","
					+ dronePoints.get(i + 1).latitude() + "," + words.get(i);
		}

		return lines;
	}

	/**
	 * Create the readings GeoJson
	 *
	 * @param dronePoints - The points visited in sequential order
	 * @return The readings GeoJson
	 */
	public String getReadings(List<Point> dronePoints) {
		// Create line string between all visited points and convert to Feature
		var flightLineString = LineString.fromLngLats(dronePoints);
		var flightGeo = (Geometry) flightLineString;
		var flightFt = Feature.fromGeometry(flightGeo);

		// Add the flight path and the sensors to a Feature Collection
		var flightList = new ArrayList<Feature>();
		flightList.add(flightFt);
		var flightFtColl = FeatureCollection.fromFeatures(flightList);

		// Convert to JSON format
		return flightFtColl.toJson();
	}
}
