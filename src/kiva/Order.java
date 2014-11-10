package src.kiva;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

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
	}
	
	// TODO: Answer

}
