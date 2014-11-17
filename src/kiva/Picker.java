package src.kiva;

import java.util.HashSet;

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
 * The picker agent requests orders. From these orders, it requests the
 * products. Afterwards it returns the order.
 **/
public class Picker extends Agent {
	private HashSet<AID> orderAgents;
	private String[] order;
	private AID[] sellerAgents;
	int agentPosition;

	ReceiverBehaviour confirm;
	// ReceiverBehaviour disconfirm;
	long timeout = 1000;// ms to wait until timeout

	protected void setup() {
		agentPosition = 0;

		confirm = new ReceiverBehaviour(this, timeout,
				MessageTemplate.MatchPerformative(ACLMessage.CONFIRM));
		// disconfirm = new ReceiverBehaviour(this, timeout,
		// MessageTemplate.MatchPerformative(ACLMessage.DISCONFIRM));

		addBehaviour(confirm);
		// addBehaviour(disconfirm);

		orderAgents = new HashSet<AID>();

		// MAIN
		addBehaviour(new CyclicBehaviour(this) {
			public void action() {

				if (order == null && !confirm.done()) {
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

					// send request to first Agent
					if (agentPosition < sellerAgents.length) {
						ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);

						msg.addReceiver(sellerAgents[agentPosition]);
						send(msg);
					}
				}

				if (order == null && confirm.done()) {

					try {
						ACLMessage msg = confirm.getMessage();
						order = msg.getContent().split((", "));
						orderAgents.add(msg.getSender());
						agentPosition = 0;

						System.out.println(this.getAgent().getName()
								+ " has order: " + msg.getContent());

					} catch (TimedOut e) {
						// switch to next agent
						agentPosition = agentPosition + 1;
					} catch (NotYetReady e) {
						// switch to next agent
						agentPosition = agentPosition + 1;
					}
				}

				// if(order == null && disconfirm.done()){
				// No disconfirm, for now we just wait for timeout
				// }

				if (order != null) {
					// TODO: request products
				}

			}
		});

	}

}
