package eu.su.mas.dedaleEtu.mas.agents.dummies.explo;

import java.util.ArrayList;
import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.startMyBehaviours;

import eu.su.mas.dedaleEtu.mas.behaviours.ExploCoopBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.HuntCoopBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.ShareMapBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.YellowSetupBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.YellowTakeDownBehaviour;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;

import jade.core.behaviours.Behaviour;
import jade.core.behaviours.FSMBehaviour;

/**
 * <pre>
 * ExploreCoop agent. 
 * Basic example of how to "collaboratively" explore the map
 *  - It explore the map using a DFS algorithm and blindly tries to share the topology with the agents within reach.
 *  - The shortestPath computation is not optimized
 *  - Agents do not coordinate themselves on the node(s) to visit, thus progressively creating a single file. It's bad.
 *  - The agent sends all its map, periodically, forever. Its bad x3.
 *  
 * It stops when all nodes have been visited.
 * 
 * 
 *  </pre>
 *  
 * @author hc
 *
 */


public class ExploreCoopAgent extends AbstractDedaleAgent {

	private static final long serialVersionUID = -7969469610241668140L;
	private MapRepresentation myMap;
	

	// State names
	private static final String A = "A";
	private static final String B = "B";
	private static final String C = "C";
	
	protected void setup(){

		super.setup();
		
		final Object[] args = getArguments();
		
		List<String> list_agentNames=new ArrayList<String>();
		
		if(args.length==0){
			System.err.println("Error while creating the agent, names of agent to contact expected");
			System.exit(-1);
		}else{
			int i=2;// WARNING YOU SHOULD ALWAYS START AT 2. This will be corrected in the next release.
			while (i<args.length) {
				list_agentNames.add((String)args[i]);
				i++;
			}
		}
		
		FSMBehaviour fsm = new FSMBehaviour(this);
		// Define the different states and behaviours
		fsm.registerFirstState (new ExploCoopBehaviour(this,this.myMap), A);
		fsm.registerState(new StateBeha(5),B);
		fsm.registerLastState(new StateBeha2(),C);
		
		// Register the transitions
		fsm.registerDefaultTransition(A,A);//Default
		fsm.registerDefaultTransition(B,A);//Default
		fsm.registerTransition(B,B, 2) ;//Cond 2
		fsm.registerTransition(B,C, 1) ;//Cond 1

		/***
		List<Behaviour> lb=new ArrayList<Behaviour>();
		
		lb.add(new YellowSetupBehaviour(this, 1));
		lb.add(new ExploCoopBehaviour(this,this.myMap));
		lb.add(new YellowTakeDownBehaviour(this));
		lb.add(new YellowSetupBehaviour(this, 2));
		lb.add(new HuntCoopBehaviour(this,this.myMap));
		addBehaviour(new startMyBehaviours(this,lb));
		*/
		
		
		addBehaviour(fsm);
		
		System.out.println("the  agent "+this.getLocalName()+ " is started");

	}
	
	
	
}