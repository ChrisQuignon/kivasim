package src.kiva;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * The shelf keep yet specified how a shelf is refilled.
 **/
public class Shelf extends Agent {
	ACLMessage request;
	Map<String, Integer> shelves;

	protected void setup() {
		shelves = new HashMap<String, Integer>();
		// Adding 10 items each into each shelves.
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

		// register giveProduct service
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("giveProduct");
		sd.setName("kivaOrder");
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		} catch (FIPAException fe) {
		}

		// MAIN
		addBehaviour(new CyclicBehaviour(this) {
			public void action() {

				request = myAgent.receive(MessageTemplate
						.MatchPerformative(ACLMessage.REQUEST));

				if (request != null) {
					answerRequest();
					// System.out.println("ACK");
				}

				// What if the Shelf is empty or fragmented?
			}

			private void answerRequest() {
				String requestedProducts[] = request.getContent().split(", ");

				String availableProducts[] = available(requestedProducts);
				if (availableProducts.length > 0) {
					// Answer
					ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
					msg.setContent("");
					for (String product : availableProducts) {
						msg.setContent(msg.getContent() + product + ", ");
					}
					msg.addReceiver(request.getSender());
					send(msg);
					// System.out.println("OK, GOT" + msg.getContent());
				}

				// No answer if we do not have any product
			}
		});
	}

	// TODO: write method to check for available products
	protected String[] available(String[] requestedProducts) {
		// puts all the available product from the request into a string
		String availableProducts[] = { "1", "2", "3", "4", "5", "6", "7", "8",
				"9", "0" };

		return availableProducts;
	}

	protected void takeDown() {
		System.out.println("Recipient agent " + getAID().getName()
				+ " terminating");
	}
}
