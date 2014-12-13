package src.kiva;

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

import java.util.HashMap;
import java.util.Map;

/**
 * The shelf keep yet specified how a shelf is refilled.
 **/
public class Shelf extends Agent {
	ReceiverBehaviour pickerRequest;
	long timeout = -1;// We always answer

	protected void setup() {
		Map<String,Integer> shelves = new HashMap<String,Integer>();
		//Adding 10 items each into each shelves. 
		shelves.put("0", 10);
		shelves.put("1", 10);
		shelves.put("2", 10);
		shelves.put("3", 10);
		shelves.put("4", 10);
		shelves.put("5", 10);
		shelves.put("6", 10);
		shelves.put("7", 10);
		shelves.put("8", 10);
		shelves.put("9", 10);
		
		//register giveProduct service
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("giveProduct");
		sd.setName("kivaOrder");
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		} catch (FIPAException fe) {
		}
			
		
		//Give a response to the picker about the availability of products
		pickerRequest = new ReceiverBehaviour(this, timeout,
				MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
		addBehaviour(pickerRequest);
		
		//MAIN
		addBehaviour(new CyclicBehaviour(this) {
			public void action() {
				
				if(pickerRequest.done()){
					answerRequest();
				}
				
				//What if the Shelf is empty or fragmented?
			}

			private void answerRequest() {
				try {
					String requestedProducts[] = pickerRequest.getMessage().getContent().split(", ");
					
					String availableProducts[] = available(requestedProducts);
					if (availableProducts.length > 0){
						//Answer
						ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
						msg.setContent("");
						for (String product : availableProducts) {
							msg.setContent(msg.getContent() + product + ", ");
						}
						msg.addReceiver(pickerRequest.getMessage().getSender());
						send(msg);
						//System.out.println("OK, GOT" + msg.getContent());
					}
					
					//No answer if we do not have any product
					
				} catch (TimedOut e) {
					e.printStackTrace();
				} catch (NotYetReady e) {
					e.printStackTrace();
				}
				
			};
		});
	}

	//TODO: write method to check for available products
	protected String[] available(String[] requestedProducts) {
		//puts all the available product from the request into a string
		String availableProducts[] = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0"};
		
		
		
		return availableProducts;
	}	
	//TODO: Write takeDown()
}


