package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.ArrayList;
import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.FSMExploAgent;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;

/**
 * The agent periodically share its map.
 * It blindly tries to send all its graph to its friend(s)  	
 * If it was written properly, this sharing action would NOT be in a ticker behaviour and only a subgraph would be shared.

 * @author hc
 *
 */
public class DecisionBehaviour extends SimpleBehaviour{

	private boolean finished = false;

	/**
	 * The agent periodically share its map.
	 * It blindly tries to send all its graph to its friend(s)  	
	 * If it was written properly, this sharing action would NOT be in a ticker behaviour and only a subgraph would be shared.

	 * @param a the agent
	 * @param period the periodicity of the behaviour (in ms)
	 * @param mymap (the map to share)
	 * @param receivers the list of agents to send the map to
	 */
	public DecisionBehaviour(Agent myAgent) {
		super(myAgent);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -568863390879327961L;

	@Override
	public void action() {

		switch (((FSMExploAgent) this.myAgent).getMode()) {

		case 0 : // EXPLORATION MODE

			/************************************************
			 * 
			 * Delete possible nodes with agents
			 * 
			 ************************************************/

			List<String> nextNodes = null;

			if (((FSMExploAgent) this.myAgent).getOpenNextNodes() != null) {
				nextNodes = new ArrayList<String>(((FSMExploAgent) this.myAgent).getOpenNextNodes());
				nextNodes.removeAll(((FSMExploAgent) this.myAgent).getPosition());
			}

			/************************************************
			 * 
			 * Take next node open
			 * 
			 ************************************************/

			if (!nextNodes.isEmpty()){ 
				((AbstractDedaleAgent)this.myAgent).moveTo(nextNodes.get(0));
			}

			/************************************************
			 * 
			 * Take the closest open node
			 * 
			 ************************************************/

			else {
				String myPosition = ((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
				//List<String> AgentNodes  = ((FSMExploAgent) this.myAgent).getPosition();
				String nextNode = ((FSMExploAgent) this.myAgent).getMyMap().getShortestPathToClosestOpenNode(myPosition).get(0);
				((AbstractDedaleAgent)this.myAgent).moveTo(nextNode);
			}

			/************************************************
			 * 
			 * Check if the exploration is done
			 * 
			 ************************************************/

			if (!((FSMExploAgent) this.myAgent).getMyMap().hasOpenNode()){
				System.out.println("Hello I'm " + this.myAgent.getLocalName() + " my exploration is successfully done");
				((FSMExploAgent) this.myAgent).setMode(1); // Go Hunting
			}

		case 1 : // HUNT MODE

			/************************************************
			 * 
			 * Estimate wumpus position
			 * 
			 ************************************************/

			/************************************************
			 * 
			 * Check agents near us
			 * 
			 ************************************************/

			/************************************************
			 * 
			 * Take next node
			 * 
			 ************************************************/
				String nextNode = null;
				// ( 0 )
				// Select next move
				if (nextNode==null){ // If next node is null
					nextNode = ((FSMExploAgent)this.myAgent).getOpenNextNodes().get(0);
				}
				((AbstractDedaleAgent)this.myAgent).moveTo(nextNode);
			}
		this.finished = true;

	}

	@Override
	public boolean done() {
		return finished;
	}

}