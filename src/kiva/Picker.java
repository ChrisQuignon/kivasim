package src.kiva;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

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

	// A map of all shelfs and their products
	private Map<AID, String[]> availableShelfs;

	// A map of all shelfs and what products we requested from there
	private Map<AID, String[]> requestedShelfs;
	boolean allRequested;

	// The order of this shelf
	private String[] order;

	private HashSet<AID> orderAgents;
	private HashSet<AID> deliveryRobots;

	ReceiverBehaviour confirm;
	ReceiverBehaviour inform;

	long timeout = -1;// We always answer

	DFAgentDescription template;
	ServiceDescription sd;

	protected void setup() {

		confirm = new ReceiverBehaviour(this, timeout,
				MessageTemplate.MatchPerformative(ACLMessage.CONFIRM));
		inform = new ReceiverBehaviour(this, timeout,
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));

		addBehaviour(inform);
		addBehaviour(confirm);

		orderAgents = new HashSet<AID>();
		deliveryRobots = new HashSet<AID>();
		availableShelfs = new HashMap<AID, String[]>();
		requestedShelfs = new HashMap<AID, String[]>();
		allRequested = false;

		template = new DFAgentDescription();
		sd = new ServiceDescription();

		// MAIN
		addBehaviour(new CyclicBehaviour(this) {
			public void action() {

				if (order == null && !confirm.done()) {
					System.out.println("requestOrders");
					requestOrder();
				}

				if (order == null && confirm.done()) {
					System.out.println("GetOrder");
					setOrder();
				}

				if (order != null && availableShelfs.isEmpty()) {
					requestShelfAgents();
					System.out.println("reQuestShelfAgent");
				}

				// We got an inform
				if (inform.done()) {
					
					try {
						ACLMessage msg = inform.getMessage();

						// Answer from a delivery robot
						if (msg.getContent() == "OK") {
							System.out.println("Answer from deliveryRobot");
							deliveryRobots.add(msg.getSender());
						}
						// Answer from a Shelf
						else {
							System.out.println("Answer from shelf");
							availableShelfs.put(msg.getSender(), msg.getContent()
									.split(", "));
						}

					} catch (TimedOut e) {
						e.printStackTrace();
					} catch (NotYetReady e) {
						e.printStackTrace();
					}
				}

				//
				if(order != null && !allRequested){
					// check what products still need to be requested
					
					//find products we need that are not requested yet
					
					//order delivery
					requestDeliveryRobot();
					
					allRequested = true;
				}
				
				if (order != null && allRequested) {
					
				

				// wait for inform to pick from which shelf
				// check what was ordered from this shelf
				// decrease products from shelf
				// delete from requested

				// if everything is picked: kill order agent
				// reset this agent
				
				}

			}

			private void requestDeliveryRobot() {

				//TODO request delivery robot
				
				// What if the shelf is already carried by another robot?

				// possibly wait for answer
				// pop() from availability
				// store request in requested

				// set allRequested Flag
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			private void requestShelfAgents() {

				sd.setType("giveProduct");
				template.addServices(sd);
				ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);

				try {
					DFAgentDescription[] result = DFService.search(myAgent,
							template);
					for (DFAgentDescription agent : result) {
						msg.addReceiver(agent.getName());
					}
					for (String s : order) {
						if (msg.getContent() == null) {
							msg.setContent(" ");
						}
						msg.setContent(msg.getContent() + s + ", ");
					}
					send(msg);

				} catch (FIPAException fe) {
					fe.printStackTrace();
				}
			}

			private void setOrder() {
				// Set Order
				try {
					ACLMessage msg = confirm.getMessage();
					order = msg.getContent().split((", "));
					orderAgents.add(msg.getSender());
					
					System.out.println(this.getAgent().getName()
							+ " has order: " + msg.getContent());
				} catch (TimedOut e) {
				} catch (NotYetReady e) {
				}

			}

			private void requestOrder() {

				sd.setType("giveOrder");
				template.addServices(sd);

				try {
					DFAgentDescription[] result = DFService.search(myAgent,
							template);
					ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
					for (DFAgentDescription agent : result) {
						msg.addReceiver(agent.getName());
						send(msg);
					}
				} catch (FIPAException fe) {
					fe.printStackTrace();
				}
			}
		});

	}
	// TODO: Write takeDown()
}
