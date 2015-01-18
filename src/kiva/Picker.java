package src.kiva;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
	private Map<AID, List<String>> availableShelfs;

	// A map of all shelfs and what products we requested from there
	private Map<AID, List<String>> requestedShelfs;
	boolean allRequested;

	// The order of this shelf
	protected List<String> order;

	private HashSet<AID> orderAgents;
	private HashSet<AID> deliveryRobots;

	ACLMessage confirm;
	ACLMessage inform;

	protected void setup() {

		order = new ArrayList<String>();
		orderAgents = new HashSet<AID>();
		deliveryRobots = new HashSet<AID>();
		availableShelfs = new HashMap<AID, List<String>>();
		requestedShelfs = new HashMap<AID, List<String>>();
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

			if (order.size() == 0 ) {
				System.out.println("requestOrders");
				requestOrder();
				//TODO define frequencey
			}

			if (confirm != null) {
				System.out.println("GetOrder");
				setOrder();
			}

			if (order.size() != 0 && availableShelfs.isEmpty()) {
				requestShelfAgents();
				System.out.println("request Shelf Agent");
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
					
					List<String> products = new ArrayList<String>();
					for(String product: inform.getContent().split(", ")){
						products.add(product);
						}
					availableShelfs.put(inform.getSender(), products);
					
					
					//TODO check if allRequested
					//allRequested = (availableShelfs.size() == requestedShelfs.size());
					updateAllRequested();
					//System.out.println(allRequested);
				}
			}

			if (order != null && allRequested) {
				
				Map <AID, List<String>> requests = mapRequests();
				
				requestDeliveryRobot(requests);
				
				// wait for inform to pick from which shelf
				// check what was ordered from this shelf
				// decrease products from shelf
				// delete from requested

				// if everything is picked: kill order agent
				// reset this agent
			}
		}

		private Map<AID, List<String>> mapRequests() {
			
			//decide which product need to be brought from which shelf
			
			//copy order
			Map<AID, List<String>> requests = new HashMap<AID, List<String>>();
			
			String checkOrder = "";
			for(String product:order){
				checkOrder = checkOrder + product;
			}
			

			//iterate over availableshelves
			for(AID shelf: availableShelfs.keySet()){
					List<String> request = new ArrayList<String>();
				
					//iterate over products
					for(String available:availableShelfs.get(shelf)){
						
						if(checkOrder.contains(available)){
							//add to request
							request.add(available);
							//remove available from checkorder
							int i = checkOrder.indexOf(available);
							checkOrder = checkOrder.substring(0, i) + 
									checkOrder.substring(i+available.length(), checkOrder.length());
							}
						 
						if(request.size() > 0){
							requests.put(shelf, request);
						}
					}
				}
			
			//What if there are products left?
			
			return requests;
		}

		// ACTIONS
		private void requestDeliveryRobot(Map<AID, List<String>> requests) {

			// Requesting a delivery robot
			
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
			System.out.println("Requesting Delivery");
			for(AID agent:requests.keySet()){
				System.out.println(agent.getName());
				for(String product:requests.get(agent)){
					System.out.println(product);
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
		
		private void updateAllRequested(){
			
			//copy order in String
			String checkOrder = "";
			for(String product: order){
				checkOrder = checkOrder + product;
			}
			
			for(List<String> available : availableShelfs.values()){
				for(String product:available){
					//Strip order
					if(checkOrder.contains(product)){
						int i = checkOrder.indexOf(product);
						checkOrder = checkOrder.substring(0, i) + 
								checkOrder.substring(i+product.length(), checkOrder.length());
					}
				}
			}
			
			allRequested = (checkOrder.length() == 0);
			//System.out.println(allRequested);
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
					}
					else{
						msg.setContent(msg.getContent() + ", " + s);
					}
				}
				send(msg);
			} catch (FIPAException fe) {
				fe.printStackTrace();
			}
		}

		private void setOrder() {
			
			for(String product:confirm.getContent().split((", "))){
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
