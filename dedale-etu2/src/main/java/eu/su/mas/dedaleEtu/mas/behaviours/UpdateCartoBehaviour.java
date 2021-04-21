package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.ArrayList;
import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.FSMExploAgent;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import javafx.util.Pair;

/**
 * The agent periodically share its map.
 * It blindly tries to send all its graph to its friend(s)  	
 * If it was written properly, this sharing action would NOT be in a ticker behaviour and only a subgraph would be shared.

 * @author hc
 *
 */
public class UpdateCartoBehaviour extends SimpleBehaviour{
	
	private int numberAgents;
	
	private boolean finished = false;
	
	private int condition;

	/**
	 * The agent periodically share its map.
	 * It blindly tries to send all its graph to its friend(s)  	
	 * If it was written properly, this sharing action would NOT be in a ticker behaviour and only a subgraph would be shared.
	 * @param numberAgents
	 * @param a the agent
	 * @param period the periodicity of the behaviour (in ms)
	 * @param mymap (the map to share)
	 * @param receivers the list of agents to send the map to
	 */
	public UpdateCartoBehaviour(Agent myAgent, int numberAgents) {
		super(myAgent);
		this.numberAgents = numberAgents;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -568863390879327961L;

	@Override
	public void action() {
		List<Pair<List<String>, AID>> pathsToTake = new ArrayList<Pair<List<String>, AID>>();
		Pair<String, AID> cartographer = new Pair<String, AID>(((AbstractDedaleAgent)this.myAgent).getCurrentPosition(), ((AbstractDedaleAgent)this.myAgent).getAID());
		((FSMExploAgent) this.myAgent).addPositions(cartographer);
		pathsToTake = ((FSMExploAgent) this.myAgent).getMyMap().getPathsToTake(((FSMExploAgent) this.myAgent).getPositions());
		((FSMExploAgent) this.myAgent).setPathsToExplore(pathsToTake.get(pathsToTake.size()-1).getKey());
		((FSMExploAgent) this.myAgent).removeLastPositions();
		((FSMExploAgent) this.myAgent).setCntExchange(numberAgents);
		this.condition = 4;
		this.finished=true;
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

