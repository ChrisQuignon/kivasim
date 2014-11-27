package src.kiva;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

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

	private Map<AID, String[]> availability;
	private Map<AID, String[]> requested;
	boolean allRequested;
	private String[] order;
	
	private HashSet<AID> orderAgents;
	private HashSet<AID> shelfAgents;
	private HashSet<AID> deliveryRobots;
	
	private AID[] allOrderAgents;
	private AID[] allShelfAgents;
	int agentPosition;

	ReceiverBehaviour confirm;
	ReceiverBehaviour inform;

	long timeout = 1000;// ms to wait until timeout

	protected void setup() {
		agentPosition = 0;

		confirm = new ReceiverBehaviour(this, timeout,
				MessageTemplate.MatchPerformative(ACLMessage.CONFIRM));
		inform = new ReceiverBehaviour(this, timeout,
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));

		addBehaviour(inform);
		addBehaviour(confirm);

		orderAgents = new HashSet<AID>();
		shelfAgents = new HashSet<AID>();
		deliveryRobots = new HashSet<AID>();
		availability = new HashMap<AID,String[]>();
		requested = new HashMap<AID,String[]>();
		allRequested = false;

		// MAIN
		addBehaviour(new CyclicBehaviour(this) {
			public void action() {
				DFAgentDescription template = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();

				if (order == null && !confirm.done()) {
					// get all agents with a "giveOrder" service
					sd.setType("giveOrder");
					template.addServices(sd);
					try {
						DFAgentDescription[] result = DFService.search(myAgent,
								template);
						allOrderAgents = new AID[result.length];
						for (int i = 0; i < result.length; ++i) {
							allOrderAgents[i] = result[i].getName();
						}
					} catch (FIPAException fe) {
						fe.printStackTrace();
					}

					// send request to first Agent
					if (agentPosition < allOrderAgents.length) {
						ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
						msg.addReceiver(allOrderAgents[agentPosition]);
						send(msg);
					}
					//What if the first agent does not answer?
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
				
				if ( order != null && shelfAgents.isEmpty()) {
					
					// get all agents with a "giveOrder" service
					sd.setType("giveProduct");
					template.addServices(sd);
					try {
						DFAgentDescription[] result = DFService.search(myAgent,
								template);
						allShelfAgents = new AID[result.length];
						for (int i = 0; i < result.length; ++i) {
							allShelfAgents[i] = result[i].getName();
						}
					} catch (FIPAException fe) {
						fe.printStackTrace();
					}

					// send request to all Agents
					ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
					for(AID a: allShelfAgents){
						msg.addReceiver(a);
					}
					for(String s : order) {
						if(msg.getContent() == null){
							msg.setContent(" ");
						}
						msg.setContent(msg.getContent()+ s + ", ");
					}
					send(msg);
				}
				
				//We got an inform
				if(inform.done()){
					try {
						 ACLMessage msg = inform.getMessage();
						
						//Answer from a delivery robot
						if(msg.getContent()=="OK"){
							deliveryRobots.add(msg.getSender());
						}
						//Answer from a Shelf
						else{
							availability.put(msg.getSender(), msg.getContent().split(", "));
						}
						
					} catch (TimedOut e) {
						e.printStackTrace();
					} catch (NotYetReady e) {
						e.printStackTrace();
					}
				}
				
				//We need a driver
				if(!allRequested){
					//TODO request driver
					
					//check what products still need to be requested
					
					//request driver
					//What if the shelf is already carried by another robot?
					
					//possibly wait for answer
					//pop() from availability
					//store request in requested
					
					//set allRequested Flag
					
					System.out.println(this.getAgent().getName()
							+ " needs a driver!");
				}
				
				//wait for inform to pick from which shelf
				//check what was ordered from this shelf
				//decrease products from shelf
				//delete from requested
				
				//if everything is picked: kill order agent
				//reset this agent
				
			}
		});

	}
	//TODO: Write takeDown()
}
