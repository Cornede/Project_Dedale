package eu.su.mas.dedaleEtu.mas.behaviours;

import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.FSMExploAgent;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * The agent periodically share its map.
 * It blindly tries to send all its graph to its friend(s)  	
 * If it was written properly, this sharing action would NOT be in a ticker behaviour and only a subgraph would be shared.

 * @author hc
 *
 */
public class ExchangeMapCartoBehaviour extends SimpleBehaviour{

	private boolean finished = false;

	private int condition;

	/**
	 * The agent periodically share its map.
	 * It blindly tries to send all its graph to its friend(s)  	
	 * If it was written properly, this sharing action would NOT be in a ticker behaviour and only a subgraph would be shared.

	 * @param a the agent
	 * @param period the periodicity of the behaviour (in ms)
	 * @param mymap (the map to share)
	 * @param receivers the list of agents to send the map to
	 */
	public ExchangeMapCartoBehaviour(Agent myAgent) {
		super(myAgent);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -568863390879327961L;

	@Override
	public void action() {
		//FSM.pdf in the files to see it globally

		AID[] agentsNearby = ((FSMExploAgent) this.myAgent).getServices("Explorer");
		((FSMExploAgent) this.myAgent).clearMapsReceived();
		((FSMExploAgent) this.myAgent).clearPositions();
		((FSMExploAgent) this.myAgent).setCntExchange(agentsNearby.length);
		int numberAgents = agentsNearby.length;
		int indexAgent = numberAgents - ((FSMExploAgent) this.myAgent).getCntExchange();

		/************************************************
		 * 
		 * CREATE the states names and FSM
		 * 
		 ************************************************/

		final String ReceivePosition = "ReceivePosition";
		final String ReceiveMap = "ReceiveMap";
		final String UpdateCarto = "UpdateCarto";
		final String SendNewMap = "SendNewMap";
		final String SendNewPaths = "SendNewPaths";
		final String End = "End";

		FSMBehaviour fsm = new FSMBehaviour(this.myAgent);

		/************************************************
		 * 
		 * ADD the states and behaviours
		 * 
		 ************************************************/

		fsm.registerFirstState(new ReceiveBehaviour(this.myAgent, agentsNearby[indexAgent], ACLMessage.ACCEPT_PROPOSAL, "SHARE-POSITION", 3), ReceivePosition);
		fsm.registerState(new ReceiveBehaviour(this.myAgent, agentsNearby[indexAgent], ACLMessage.INFORM, "SHARE-MAP", 4), ReceiveMap);
		fsm.registerState(new UpdateCartoBehaviour(this.myAgent, numberAgents), UpdateCarto);
		fsm.registerState(new SendBehaviour(this.myAgent,  agentsNearby[indexAgent], ACLMessage.CONFIRM, "SHARE-PATHS", 3), SendNewMap);
		fsm.registerState(new SendBehaviour(this.myAgent, agentsNearby[indexAgent], ACLMessage.INFORM, "SHARE-NEWMAP", 4), SendNewPaths);
		fsm.registerLastState(new EndBehaviour(this.myAgent), End);

		/************************************************
		 * 
		 * ADD the transitions
		 * 
		 ************************************************/

		// First Loop : Send the CFP and get the positions and maps of all the agents
		fsm.registerTransition(ReceivePosition, ReceiveMap, 1);
		fsm.registerTransition(ReceiveMap, ReceivePosition, 2);

		// Update for Cartographer : update his map and paths
		fsm.registerTransition(ReceiveMap, UpdateCarto, 3);
		fsm.registerTransition(UpdateCarto, SendNewMap, 4);

		// Second Loop : Send the new maps and paths to all the agents
		fsm.registerTransition(SendNewMap, SendNewPaths, 5);
		fsm.registerTransition(SendNewPaths, SendNewMap, 6);

		this.myAgent.addBehaviour(fsm);
	}

	@Override
	public int onEnd() {
		return condition;
	}

	@Override
	public boolean done() {
		return finished;
	}

}
