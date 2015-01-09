package src.kiva;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import jade.core.Agent;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

public class Warehouse extends Agent {

	AgentController recAgent;

	// How many Agents do we spawn?
	private Map<String, Integer> agents;

	protected void setup() {
		AgentContainer container = getContainerController();
		agents = new HashMap<String, Integer>();

		// How many of which agents do we want to have?
		agents.put("DeliveryRobot", 1);
		agents.put("Picker", 1);
		agents.put("Shelf", 1);

		// the request dummy spawns a recipient
		agents.put("Recipient", 0);
		agents.put("Requestdummy", 1);

		// Spawn Agents
		for (Entry<String, Integer> agent : agents.entrySet()) {
			for (int i = 0; i < agent.getValue(); i++) {
				try {
					recAgent = container.createNewAgent(agent.getKey()
							+ Integer.toString(i),
							"src.kiva." + agent.getKey(), null);
					recAgent.start();
					System.out.println("Spawning " + agent.getKey()
							+ Integer.toString(i));
				} catch (StaleProxyException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
		takeDown();
	}

	protected void takeDown() { 
		System.out.println("Warehouse agent " + getAID().getName()
				+ " terminating");
	}
}
