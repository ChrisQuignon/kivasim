package src.kiva;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.ReceiverBehaviour;
import jade.core.behaviours.ReceiverBehaviour.NotYetReady;
import jade.core.behaviours.ReceiverBehaviour.TimedOut;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * The deliverer responds to product requests and delivers shelf to the picker
 * **/

public class DeliveryRobot extends Agent {

	// accept whenever the message arrives
	long timeout = -1;
	boolean answer;
	ReceiverBehaviour informing_free;
	ReceiverBehaviour informing_shelf;

	protected void setup() {

		// Informing_free behaviour informs the picker that it is free.
		informing_free = new ReceiverBehaviour(this, timeout,
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));

		// Informing_shelf behaviour informs the picker that the shelf is picked
		informing_shelf = new ReceiverBehaviour(this, timeout,
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));

		// Adding the behaviours.
		addBehaviour(informing_free);
		addBehaviour(informing_shelf);

		// Add cyclic behaviour for informing_free behaviour
		addBehaviour(new CyclicBehaviour(this) {
			public void action() {

				/*
				 * The delivery robots respond by informing the picker that they
				 * are ready/free to carry the shelves.
				 */
				if (!answer && informing_free.done()) {
					AID picker;
					try {
						picker = informing_free.getMessage().getSender();
						ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
						msg.setContent("READY");
						msg.addReceiver(picker);
						send(msg);
						System.out.println("Informed by picker to be: "
								+ informing_free.getMessage());
						System.out.println("Informed the picker that I am: " + msg.getContent());
						System.out.println("working..");
						answer = true;
					} catch (TimedOut e) {
						// TODO Auto-generated catch block
						// e.printStackTrace();
					} catch (NotYetReady e) {
						// TODO Auto-generated catch block
						// e.printStackTrace();
					}

				}
			};

		});
	}

	// takeDown()
	protected void takeDown() {
		System.out.println("Delivery Robots" + getAID().getName()
				+ "terminating");
	}
}
