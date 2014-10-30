package src.kiva;

import jade.core.Agent;
import jade.wrapper.AgentContainer;
import jade.wrapper.StaleProxyException;

import java.util.Random;

/**
 * This requests dummy will randomly request orders
 */
public class Requestdummy extends Agent {

	private int max_parts_per_order = 12;
	private int max_amount_per_part = 12;
	private double max_sec_delay = 5;

	protected void setup() {

		// say hello
		System.out.println("request dummy is here");

		Object[] args = getArguments();
		if (args != null && args.length > 2) {
			max_parts_per_order = Integer.parseInt(args[0].toString());
			max_amount_per_part = Integer.parseInt(args[1].toString());
			max_sec_delay = Double.parseDouble(args[2].toString());
		}

		// spawn recipient
		AgentContainer container = getContainerController();
		try {
			container.createNewAgent("recipient", "src.kiva.Recipient", null);
		} catch (StaleProxyException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// generate orders
		while (true) {

			Random rand = new Random();

			String order = " ";

			// choose a random number of parts
			for (int parts = 0; parts < rand.nextInt(max_parts_per_order); parts++) {

				// The part name is a string of a random number
				int random = rand.nextInt(1000);
				String product = Integer.toString(random);

				// Order the part at most 12 times

				for (int amount = 0; amount < rand.nextInt(max_amount_per_part); amount++) {
					order = order + product + ", ";
				}

			}

			// TODO: publish order
			System.out.println(order);

			// wait some time
			try {
				Thread.sleep(rand.nextInt((int) (max_sec_delay * 1000)));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

}
