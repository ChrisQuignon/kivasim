package src.kiva;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

import java.util.Random;

/**
 * This requests dummy will randomly request orders
 */
public class Requestdummy extends Agent {

	private int maxPartsPerOrder = 1;
	private int maxAmountPerPart = 1;
	private double maxSecDelay = 30;

	private String order;
	private String recipient = "recipient";
	AgentController recAgent;

	protected void setup() {

		// read args
		Object[] args = getArguments();
		if (args != null && args.length > 2) {
			maxPartsPerOrder = Integer.parseInt(args[0].toString());
			maxAmountPerPart = Integer.parseInt(args[1].toString());
			maxSecDelay = Double.parseDouble(args[2].toString());
		}

		// spawn recipient
		AgentContainer container = getContainerController();
		try {
			recAgent = container.createNewAgent(recipient,
					"src.kiva.Recipient", null);
			recAgent.start();

		} catch (StaleProxyException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// generate orders
		while (true) {

			Random rand = new Random();

			// randomly create an order
			order = "";
			for (int parts = 0; parts <= rand.nextInt(maxPartsPerOrder); parts++) {
				int random = rand.nextInt(10);
				String product = Integer.toString(random);

				for (int amount = 0; amount <= rand.nextInt(maxAmountPerPart); amount++) {
					order = order + product + ", ";
				}
			}
			order.substring(0, (order.length() - 2));

			// publish order
			// TODO: change Message Type
			System.out.println("ORDER: " + order);
			AID dest = new AID(recipient, AID.ISLOCALNAME);
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.setContent(order);
			msg.addReceiver(dest);
			send(msg);

			// System.out.println("send msg" + msg + " to " +
			// dest.getLocalName());

			// wait some time
			try {
				Thread.sleep(rand.nextInt((int) (maxSecDelay * 1000)));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// repeat
		}
	}

	protected void takeDown() {
		System.out.println("Request dummy agent " + getAID().getName()
				+ " terminating");
		doDelete();
	}
}
