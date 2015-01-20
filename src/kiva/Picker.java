package src.kiva;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import sun.security.util.Length;
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
	private Map<AID, List<String>> availableShelfs;

	// A map of all shelfs and what products we requested from there
	private Map<AID, List<String>> requestedShelfs;
	boolean allProductsRequested;

	// The order of this shelf
	protected List<String> order;

	private HashSet<AID> orderAgents;
	private HashSet<AID> requestedDeliveryRobots;
	private HashSet<AID> availableDeliveryRobots;

	ACLMessage confirm;
	ACLMessage inform;
	ACLMessage request;
	

	protected void setup() {

		order = new ArrayList<String>();
		orderAgents = new HashSet<AID>();
		requestedDeliveryRobots = new HashSet<AID>();
		availableDeliveryRobots = new HashSet<AID>();
		availableShelfs = new HashMap<AID, List<String>>();
		requestedShelfs = new HashMap<AID, List<String>>();
		allProductsRequested = false;

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
			request = myAgent.receive(MessageTemplate.MatchPerformative(ACLMessage.REQUEST));

			if (order.size() == 0) {
				System.out.println("requestOrders");
				requestOrder();
				// TODO define frequency
			}
			
			if(availableDeliveryRobots.size() == 0){
				requestAllDeliveryRobots();
			}

			if (confirm != null) {

				/*
				 * If the delivery robots CONFIRM "available", then add all of
				 * them to availableDeliveryRobots hashset
				 */
				
				
				if (confirm.getContent().equals("Available")) {
					availableDeliveryRobots.add(confirm.getSender());
					System.out
							.println("Delivery robots are ready to pick shelves!");
					//Pick one out of the robots that answered
					//Request the contents of the shelf to the first delivery robot that answered.
						
						System.out.printf("Requesting order to the Delivery robot ", confirm.getSender().getName());
					}
				else{

					System.out.println("GetOrder");
					setOrder();
				}
					
				

				/*
				 * If the delivery robots CONFIRM "CarryingShelf" then put them
				 * into requestedDeliveryRobots hashset
				 */

				if (confirm.getContent() == "CarryingShelf") {
					System.out
							.println("Delivery robot is busy carrying a shelf!");
				}
			}

			if (order.size() != 0 && availableShelfs.isEmpty()) {
				requestShelfAgents();
				System.out.println("request Shelf Agent");
			}

			// We got a inform
			if (inform != null) {

				// inform from a Shelf

				System.out.println("Answer from shelf");

				List<String> products = new ArrayList<String>();
				for (String product : inform.getContent().split(", ")) {
					products.add(product);
				}
				availableShelfs.put(inform.getSender(), products);

				// TODO check if allRequested
				// allRequested = (availableShelfs.size() ==
				// requestedShelfs.size());
				updateAllRequested();
				//System.out.println(allProductsRequested);

			}

			if (order != null && allProductsRequested) {

				Map<AID, List<String>> shelfToBePicked = mapRequests();
				requestShelfDelivery(shelfToBePicked);

				// wait for inform to pick from which shelf
				// check what was ordered from this shelf
				// decrease products from shelf
				// delete from requested

				// if everything is picked: kill order agent
				// reset this agent
			}
		}

		private Map<AID, List<String>> mapRequests() {

			// decide which product need to be brought from which shelf

			// copy order
			Map<AID, List<String>> requests = new HashMap<AID, List<String>>();

			String checkOrder = "";
			for (String product : order) {
				checkOrder = checkOrder + product;
			}

			// iterate over availableshelves
			for (AID shelf : availableShelfs.keySet()) {
				List<String> request = new ArrayList<String>();

				// iterate over products
				for (String available : availableShelfs.get(shelf)) {

					if (checkOrder.contains(available)) {
						// add to request
						request.add(available);
						// remove available from checkorder
						int i = checkOrder.indexOf(available);
						checkOrder = checkOrder.substring(0, i)
								+ checkOrder.substring(i + available.length(),
										checkOrder.length());
					}

					if (request.size() > 0) {
						requests.put(shelf, request);
					}
				}
			}

			// What if there are products left?

			return requests;
		}

		// ACTIONS
		// Requesting all delivery robot
		private void requestAllDeliveryRobots(){
			
						DFAgentDescription template = new DFAgentDescription();
						ServiceDescription sd = new ServiceDescription();
						sd.setType("shelfPicking");
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
		
		//Map one delivery robot per shelf.
		private void requestShelfDelivery(Map<AID, List<String>> requests) {
			System.out.println("Requesting one delivery robot to bring the shelf");
			for (AID shelf : requests.keySet()) {
				System.out.println(shelf.getName());
				//choosing the delivery robots
				System.out.println(availableDeliveryRobots.size());
				for(AID DeliveryRobot : availableDeliveryRobots){

					ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
					msg.setContent(shelf.getName());
					msg.addReceiver(DeliveryRobot);
					send(msg);
					availableDeliveryRobots.remove(DeliveryRobot);
					requestedDeliveryRobots.add(DeliveryRobot);
					break;
				}
				
				
				
			}

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
		
//Check if all shelves have the products that were requested
		private void updateAllRequested() {

			// copy order in String
			String checkOrder = "";
			for (String product : order) {
				checkOrder = checkOrder + product;
			}

			for (List<String> available : availableShelfs.values()) {
				for (String product : available) {
					// Strip order
					if (checkOrder.contains(product)) {
						int i = checkOrder.indexOf(product);
						checkOrder = checkOrder.substring(0, i)
								+ checkOrder.substring(i + product.length(),
										checkOrder.length());
					}
				}
			}

			allProductsRequested = (checkOrder.length() == 0);
			// System.out.println(allRequested);
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
						msg.setContent(s);
					} else {
						msg.setContent(msg.getContent() + ", " + s);
					}
				}
				send(msg);
			} catch (FIPAException fe) {
				fe.printStackTrace();
			}
		}

		private void setOrder() {

			for (String product : confirm.getContent().split((", "))) {
				order.add(product);
			}

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
			doDelete();
		}
	}
}
