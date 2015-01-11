package src.kiva;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * The order agents contains the order and answers the picker agent
 * **/
public class Order extends Agent {

	private String[] order;
	AID picker;
	ACLMessage request;

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

		// register "giveOrder" Service
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("giveOrder");
		sd.setName("kivaOrder");
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}

		// MAIN
		addBehaviour(new CyclicBehaviour(this) {
			public void action() {

				request = myAgent.receive(MessageTemplate
						.MatchPerformative(ACLMessage.REQUEST));

				if (picker == null && request != null) {
					picker = request.getSender();
					ACLMessage msg = new ACLMessage(ACLMessage.CONFIRM);
					msg.setContent("");
					for (String product : order) {
						msg.setContent(msg.getContent() + product + ", ");
					}
					msg.addReceiver(picker);
					send(msg);
				}

				// maybe check if we are still processing the orders.

			};
		});

	}

	protected void takeDown() {
		System.out.println("Order agent " + getAID().getName()
				+ " terminating");
		doDelete();
	}
}
