package src.kiva;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

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
	protected String[] order;

	private HashSet<AID> orderAgents;
	private HashSet<AID> deliveryRobots;

	ACLMessage confirm;
	ACLMessage inform;

	protected void setup() {

		orderAgents = new HashSet<AID>();
		deliveryRobots = new HashSet<AID>();
		availableShelfs = new HashMap<AID, String[]>();
		requestedShelfs = new HashMap<AID, String[]>();
		allRequested = false;

		addBehaviour(new ActionPicker());
	}

	public class ActionPicker extends CyclicBehaviour {

		@Override
		public void action() {

			// receive messages
			confirm = myAgent.receive(MessageTemplate
					.MatchPerformative(ACLMessage.CONFIRM));
			inform = myAgent.receive(MessageTemplate
					.MatchPerformative(ACLMessage.INFORM));

			if (order == null) {
				System.out.println("requestOrders");
				requestOrder();
				//TODO define frequencey
			}

			if (confirm != null) {
				System.out.println("GetOrder");
				setOrder();
			}

			if (order != null && availableShelfs.isEmpty()) {
				requestShelfAgents();
				System.out.println("requestShelfAgent");
			}

			// We got an inform
			if (inform != null) {
				// inform from a delivery robot
				if (inform.getContent() == "OK") {
					System.out.println("Answer from deliveryRobot");
					deliveryRobots.add(inform.getSender());
				}
				// inform from a Shelf
				else {
					System.out.println("Answer from shelf");
					availableShelfs.put(inform.getSender(), inform.getContent()
							.split(", "));
					
					//TODO check if allRequested
					//allRequested = (availableShelfs.size() == requestedShelfs.size());
					updateAllRequested();
					//System.out.println(allRequested);
				}
			}

			//
			if (order != null && !allRequested) {
				// check what products still need to be requested

				// find products we need that are not requested yet

				// order delivery
				
				
				requestDeliveryRobot();
				System.out.println("Requesting Delivery");
				
				//TODO Dummy value - removes
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

		// ACTIONS
		private void requestDeliveryRobot() {

			// TODO request delivery robot

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
		
		private void updateAllRequested(){
			
			//serialize order into a HashMap
			Map<String, Integer> ordersAmount = new HashMap<String, Integer>();
			for(String product:order){
				if(!ordersAmount.containsValue(product)){
					ordersAmount.put(product, 1);
				}
				else{
					ordersAmount.put(product, ordersAmount.get(product)+1);
				}
			}
			
			//decrease Hashmap for all available shelves
			for(String[] available : availableShelfs.values()){
				

				for(String product:available){
					if(ordersAmount.containsKey(product)){
						if(ordersAmount.get(product) > 1){
							ordersAmount.put(product, ordersAmount.get(product)-1);
						}
						else{
							ordersAmount.remove(product);
						}
					}
				}
			}
			
			//Iff the Hashmap is decreased to zero
			//everything is available
			allRequested = ordersAmount.isEmpty();		
		}

		private void requestShelfAgents() {

			DFAgentDescription template = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
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
						msg.setContent("");
					}
					msg.setContent(msg.getContent() + s + ", ");
				}
				send(msg);

			} catch (FIPAException fe) {
				fe.printStackTrace();
			}
		}

		private void setOrder() {
			order = confirm.getContent().split((", "));
			orderAgents.add(confirm.getSender());

			System.out.println(this.getAgent().getName() + " has order: "
					+ confirm.getContent());
		}

		private void requestOrder() {

			DFAgentDescription template = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();

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

		protected void takeDown() {
			System.out.println("Picker agent " + getAID().getName()
					+ " terminating");
		}
	}
}
// TODO: Write takeDown()

