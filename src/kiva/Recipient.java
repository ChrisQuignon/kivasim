package src.kiva;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

/**
 * The recipient is the interface to the client. It receives orders from the
 * client and spawns order agents.
 * **/
public class Recipient extends Agent {

	protected String[] orders;
	AgentController orderAgent;

	protected void setup() {

		// listen to requestdummy
		addBehaviour(new CyclicBehaviour(this) {
			public void action() {
				ACLMessage msg = receive();
				if (msg != null) {
					orders = msg.getContent().split(", ");
					AgentContainer container = getContainerController();
					try {
						// TODO: Fix agent hash
						orderAgent = container.createNewAgent(
								"order:" + msg.hashCode(), "src.kiva.Order",
								orders);
						orderAgent.start();
					} catch (StaleProxyException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
				block();
			}
		});
	}
	//takeDown()
	protected void takeDown() {
		System.out.println("Recipient agent" + getAID().getName() +"terminating" );
	}
}
