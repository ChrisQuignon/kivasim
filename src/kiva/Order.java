package src.kiva;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.ReceiverBehaviour;
import jade.core.behaviours.ReceiverBehaviour.NotYetReady;
import jade.core.behaviours.ReceiverBehaviour.TimedOut;
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
	ReceiverBehaviour pickerRequest;
	long timeout = -1;// We always answer

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

		// register "giveorder" Service
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

		// wait for orders
		pickerRequest = new ReceiverBehaviour(this, timeout,
				MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
		addBehaviour(pickerRequest);

		// MAIN
		addBehaviour(new CyclicBehaviour(this) {
			public void action() {

				if (picker == null && pickerRequest.done()) {
					try {
						picker = pickerRequest.getMessage().getSender();
						ACLMessage msg = new ACLMessage(ACLMessage.CONFIRM);
						msg.setContent("");
						for (String product : order) {
							msg.setContent(msg.getContent() + product + ", ");
						}
						msg.addReceiver(picker);
						send(msg);
					} catch (TimedOut e) {
						//e.printStackTrace();
					} catch (NotYetReady e) {
						//e.printStackTrace();
					}
				}
				
				//maybe check if we are still processing the orders.
				
				
			};
		});
        
	}
	
	//takeDown()
	protected void takeDown() {
		System.out.println("Order agent" + getAID().getName() +"terminating" );
	}
}
