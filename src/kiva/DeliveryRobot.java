package src.kiva;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

/**
 * The deliverer responds to product requests and delivers shelf to the picker
 * **/

public class DeliveryRobot extends Agent {

	boolean isAvailable = true;
	AID picker;
	ACLMessage request;
	ACLMessage inform;

	protected void setup() {

		// Registered behaviour in yellow pages
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("shelfPicking");
		sd.setName("Delivery robot");
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}

		// Add cyclic behaviour for informing_free behaviour
		addBehaviour(new CyclicBehaviour(this) {
			public void action() {

				request = myAgent.receive(MessageTemplate
						.MatchPerformative(ACLMessage.REQUEST));
				inform = myAgent.receive(MessageTemplate
						.MatchPerformative(ACLMessage.INFORM));

				/*
				 * The delivery robot confirms to the picker's request for
				 * availability.
				 */
				if (request != null) {
					if (isAvailable == true) {
						ACLMessage msg = new ACLMessage(ACLMessage.CONFIRM);
						msg.setContent("Available");
						msg.addReceiver(picker);
						send(msg);
						System.out.println("Delivery Robot: Confirmed "
								+ msg.getContent() + " to picker's request");
					}
					/*
					 * Delivery robot confirms to picker that it is carrying a
					 * shelf
					 */
					else{
					ACLMessage msg = new ACLMessage(ACLMessage.CONFIRM);
					msg.setContent("CarryingShelf");
					msg.addReceiver(picker);
					send(msg);
					System.out.println("Delivery Robot: Confirmed "
							+ msg.getContent() + " to picker's request");
					isAvailable = false;
					}
					
				}

			};
		});
	}

	protected void takeDown() {
		System.out.println("Delivery Robots " + getAID().getName()
				+ " terminating");
		doDelete();
	}
}
