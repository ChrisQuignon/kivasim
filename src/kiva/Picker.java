package src.kiva;

import jade.core.AID;
import jade.core.Agent;
import jade.core.ServiceDescriptor;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

/**
 * The picker agent requests orders. From these orders, it requests the
 * products. Afterwards it returns the order.
 **/
public class Picker extends Agent {
	private String[] orderAgents;
	private String[] order;
	private Boolean isBusy;
	private	AID[] sellerAgents;

	protected void setup() {
		isBusy = false;

		addBehaviour(new CyclicBehaviour(this) {
			public void action() {
				if (!isBusy) {
					
					// get all agents with a "giveOrder" service
					DFAgentDescription template = new DFAgentDescription();
					ServiceDescription sd = new ServiceDescription();
					sd.setType("giveOrder");
					template.addServices(sd);
					try {
						DFAgentDescription[] result = DFService.search(myAgent,
								template);
						sellerAgents = new AID[result.length];
						for (int i = 0; i < result.length; ++i) {
							sellerAgents[i] = result[i].getName();
						}
					} catch (FIPAException fe) {
						fe.printStackTrace();
					}
					
					// send message
					for(AID agent:sellerAgents){
						ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
						msg.setContent("Do you have an order for me? ");
						msg.addReceiver(agent);
						send(msg);
						//System.out.println(msg.getContent() + agent.getName());
					}
					
					//TODO: wait for answer
					isBusy = true;
					
				}
			}
		});

	}

}
