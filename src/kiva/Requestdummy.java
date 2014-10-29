package src.kiva;

import jade.core.Agent;
import jade.wrapper.AgentContainer;
import jade.wrapper.StaleProxyException;

import java.util.Random;

/**
 * This requests dummy will randomly request orders
 */
public class Requestdummy extends Agent {

	protected void setup() {

		// say hello
		System.out.println("request dummy is here");

		/** **/
		// spawn recipient
		while (true) {

			Random rand = new Random();

			String order = " ";

			// choose a random number of parts
			for (int parts = 0; parts < rand.nextInt(12); parts++) {

				// The part name is a string of a random number
				int random = rand.nextInt(1000);
				String product = Integer.toString(random);

				// Order the part at most 12 times

				for (int amount = 0; amount < rand.nextInt(13); amount++) {
					order = order + product + ", ";
				}

			}

			// spawn agent
			AgentContainer container = getContainerController();
			try {
				container.createNewAgent("rec:" + order, "src.kiva.Recipient",
						null);
			} catch (StaleProxyException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			// TODO: publish order
			System.out.println(order);

			// wait some time
			try {
				Thread.sleep(rand.nextInt(5000));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
}
