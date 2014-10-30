package src.kiva;

import jade.core.Agent;

/**
 * The order agents contains the order and answers the picker agent
 * **/
public class Order extends Agent {

	private String[] order;

	protected void setup() {
		// read args
		Object[] args = getArguments();
		if (args != null) {
			order = new String[args.length];
			for (int i = 0; i < args.length; i++) {
				order[i] = args[i].toString();
				// System.out.println(order[i]);
			}
		}
	}

	// TODO: Answer

}
