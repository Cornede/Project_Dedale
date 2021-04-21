package eu.su.mas.dedaleEtu.mas.behaviours;

import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.FSMExploAgent;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * The agent periodically share its map.
 * It blindly tries to send all its graph to its friend(s)  	
 * If it was written properly, this sharing action would NOT be in a ticker behaviour and only a subgraph would be shared.

 * @author hc
 *
 */
public class ExchangeMapExploBehaviour extends SimpleBehaviour{
	
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
	public ExchangeMapExploBehaviour(Agent myAgent) {
		super(myAgent);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -568863390879327961L;

	@Override
	public void action() {
		//FSMExchange.pdf in the files to see it globally
		
		/************************************************
		 * 
		 * CREATE the states names and FSM
		 * 
		 ************************************************/
				
		final String SendPosition = "SendPosition";
		final String SendMap = "SendMap";
		final String ReceiveNewMap = "ReceiveNewMap";
		final String ReceiveNewPaths = "ReceiveNewPaths";
		final String End = "End";
		
		FSMBehaviour fsm = new FSMBehaviour(this.myAgent);
				
		/************************************************
		 * 
		 * ADD the states and behaviours
		 * 
		 ************************************************/
		
		AID cartoNearby = ((FSMExploAgent) this.myAgent).getServices("Cartographer")[0];
		
		fsm.registerFirstState(new SendBehaviour(this.myAgent, cartoNearby, ACLMessage.ACCEPT_PROPOSAL, "SHARE-POSITION", 1), SendPosition);
		fsm.registerState(new SendBehaviour(this.myAgent, cartoNearby, ACLMessage.INFORM, "SHARE-MAP", 2), SendMap);
		fsm.registerState(new ReceiveBehaviour(this.myAgent, cartoNearby, ACLMessage.INFORM, "SHARE-PATHS", 1), ReceiveNewPaths);
		fsm.registerState(new ReceiveBehaviour(this.myAgent, cartoNearby, ACLMessage.CONFIRM, "SHARE-NEWMAP", 2), ReceiveNewMap);
		fsm.registerLastState(new EndBehaviour(this.myAgent), End);
		
		
		
		/************************************************
		 * 
		 * ADD the transitions
		 * 
		 ************************************************/
		
		fsm.registerDefaultTransition(SendPosition, SendPosition);
		fsm.registerTransition(SendPosition, SendMap, 1);
		fsm.registerDefaultTransition(SendMap, SendMap);
		fsm.registerTransition(SendMap, ReceiveNewPaths, 2);
		fsm.registerDefaultTransition(ReceiveNewPaths, ReceiveNewPaths);
		fsm.registerTransition(ReceiveNewPaths, ReceiveNewMap, 3);
		fsm.registerDefaultTransition(ReceiveNewMap, ReceiveNewMap);
			
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
