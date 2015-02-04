package src.kiva;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;

/**
 * The picker agent requests orders. From these orders, it requests the
 * products. Afterwards it returns the order.
 **/
public class Picker extends Agent {

	// A map of all shelves and their products
	private Map<AID, List<String>> availableshelves;

	// A map of all shelves and what products we requested from there
	private Map<AID, List<String>> requestedshelves;
	private Map<AID, List<String>> shelvesToBePicked;
	boolean allProductsRequested;
	boolean allshelvesRequested;

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
		availableshelves = new HashMap<AID, List<String>>();
		requestedshelves = new HashMap<AID, List<String>>();
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
			request = myAgent.receive(MessageTemplate
					.MatchPerformative(ACLMessage.REQUEST));

			if (order.isEmpty()) {
				System.out.println("Picker: Requesting for an order.");
				requestOrder();
				// TODO define frequency
			}

			if (!allProductsRequested && ! order.isEmpty()) {
				requestAllDeliveryRobots();
			}

			if (confirm != null) {

				/*
				 * If the delivery robots CONFIRM "available", then add all of
				 * them to availableDeliveryRobots hashset
				 */

				if (confirm.getContent().equals("Available")) {
					availableDeliveryRobots.add(confirm.getSender());
					// Pick one out of the robots that answered
					// Request the contents of the shelf to the first delivery
					// robot that answered.

				} else {
					setOrder();
				}
			}

			if (! order.isEmpty() && availableshelves.isEmpty()) {
				requestShelfAgents();
				System.out
						.println("Picker: Requesting for products from the Shelves");
			}

			// We got a inform
			if (inform != null) {

				// We can pick something
				if(inform.getContent().startsWith("PICK ")){
					
					//we got an inform to pick
					String aidString = inform.getContent().substring("PICK".length()+1, inform.getContent().length());
					AID shelf = new AID(aidString, true);
					System.out.println("Picker picks " + requestedshelves.get(shelf)+ " from " + shelf.getName());

					
					//kill order
					//Assumption: we only have one product to pick
					AgentContainer controller = myAgent.getContainerController();
					for(AID order: orderAgents){
						try {
							AgentController orderAgent = controller.getAgent(order.getLocalName());
							orderAgent.kill();
						} catch (ControllerException e) {
							e.printStackTrace();
						}
					}
					
					
					//Confirm picking to Delivery Robot
					ACLMessage msg = new ACLMessage(ACLMessage.CONFIRM);
					for (AID agent : requestedDeliveryRobots) {
						msg.addReceiver(agent);
						send(msg);
					}
					
					//reset this Agent
					setup();
				}
				else{
					//we have an Answer from a shelf
					System.out.println("Picker: Receiving answers from the shelves");

					List<String> products = new ArrayList<String>();
					for (String product : inform.getContent().split(", ")) {
						products.add(product);
					}
					availableshelves.put(inform.getSender(), products);
					
					updateAllProductsRequested();
				}

			}

			if (order != null) {

				shelvesToBePicked = mapRequests();
				requestShelfDelivery(shelvesToBePicked);
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
			for (AID shelf : availableshelves.keySet()) {
				List<String> request = new ArrayList<String>();

				// iterate over products
				for (String available : availableshelves.get(shelf)) {

					if (checkOrder.contains(available)) {
						// add to request
						request.add(available);
						// remove available from checkorder
						int i = checkOrder.indexOf(available);
						checkOrder = checkOrder.substring(0, i)
								+ checkOrder.substring(i + available.length(),
										checkOrder.length());
					}

					if (! request.isEmpty()) {
						requests.put(shelf, request);
					}
				}
			}

			// What if there are products left?

			return requests;
		}

		// ACTIONS
		// Requesting all delivery robot
		private void requestAllDeliveryRobots() {

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
			System.out
					.println("Picker: Asking all the delivery robots if they are available.");
		}

		// Map one delivery robot per shelf.
		private void requestShelfDelivery(Map<AID, List<String>> requests) {

			for (AID shelf : requests.keySet()) {
				// choosing the delivery robots
				for (AID DeliveryRobot : availableDeliveryRobots) {
					System.out
							.println("Picker: Requesting one delivery robot to bring the shelf "
									+ shelf.getName());

					ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
					msg.setContent(shelf.getName());
					msg.addReceiver(DeliveryRobot);
					send(msg);
					
					requestedshelves.put(shelf, requests.get(shelf));
					
					availableDeliveryRobots.remove(DeliveryRobot);

					//we trust the delivery robot that he will do as told
					shelvesToBePicked.remove(shelf);
					requestedDeliveryRobots.add(DeliveryRobot);
					break;
				}

			}

			// What if the shelf is already carried by another robot?

		}

		// Check if all shelves have the products that were requested
		private void updateAllProductsRequested() {

			// copy order in String
			String checkOrder = "";
			for (String product : order) {
				checkOrder = checkOrder + product;
			}

			for (List<String> available : availableshelves.values()) {
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

		// Request for products from the Shelves.
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

		// Picker confirms getting an order
		private void setOrder() {

			for (String product : confirm.getContent().split((", "))) {
				order.add(product);
			}

			orderAgents.add(confirm.getSender());

			System.out.println(this.getAgent().getName()
					+ " confirms he has order: " + confirm.getContent());
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
