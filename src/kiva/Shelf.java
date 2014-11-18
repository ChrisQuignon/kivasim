package src.kiva;

import jade.core.Agent;

import java.util.HashMap;
import java.util.Map;

/**
 * The shelf keep yet specified how a shelf is refilled.
 **/
public class Shelf extends Agent {

	protected void setup() {
		Map<String,Integer> shelves = new HashMap<String,Integer>();
		//Adding 10 items each into each shelves. 
		shelves.put("0", 10);
		shelves.put("1", 10);
		shelves.put("2", 10);
		shelves.put("3", 10);
		shelves.put("4", 10);
		shelves.put("5", 10);
		shelves.put("6", 10);
		shelves.put("7", 10);
		shelves.put("8", 10);
		shelves.put("9", 10);
		//TODO : Give a response to the picker about the availability of products
		//TODO : Pass the order to the delivery robots.
	}

}
