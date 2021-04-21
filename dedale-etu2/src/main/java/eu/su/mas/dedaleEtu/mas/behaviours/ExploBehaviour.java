package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.Iterator;
import java.util.List;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;

import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.FSMExploAgent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;


/**
 * <pre>
 * This behaviour allows an agent to explore the environment and learn the associated topological map.
 * The algorithm is a pseudo - DFS computationally consuming because its not optimised at all.
 * 
 * When all the nodes around him are visited, the agent randomly select an open node and go there to restart its dfs. 
 * This (non optimal) behaviour is done until all nodes are explored. 
 * 
 * Warning, this behaviour does not save the content of visited nodes, only the topology.
 * Warning, the sub-behaviour ShareMap periodically share the whole map
 * </pre>
 * @author dl
 *
 */
public class ExploBehaviour extends SimpleBehaviour {

	private static final long serialVersionUID = 8567689731496787661L;

	private boolean finished = false;

	private int condition;

   /**
 	* @param myAgent
 	*/
	public ExploBehaviour(final AbstractDedaleAgent myAgent) {
		super(myAgent);
	}

	@Override
	public void action() {
		System.out.println("Hello I'm " + this.myAgent.getLocalName() + " and I'm exploring");
		
		/************************************************
		 * 
		 * Map's Initialisation 
		 * 
		 ************************************************/
		
		if (((FSMExploAgent) this.myAgent).getMyMap() == null) {
			((FSMExploAgent) this.myAgent).setWait(10); // Set wait to explore 10 nodes before the first exchange
			((FSMExploAgent) this.myAgent).initiateMyMap(); // Initiate the map of the agent
			System.out.println("Initialisation");
		}
		
		/************************************************
		 * 
		 * Check the Surrounding Area
		 * 
		 ************************************************/
	
		// Choose the services for the agent role
		String services = null;
		if (((FSMExploAgent) this.myAgent).getMode().equals("Cartographer")) {
			services = "Explorer";
		}
		else if (((FSMExploAgent) this.myAgent).getMode().equals("Explorer")) {
			services = "Cartographer";
		}
		
		// ( CHECK 1 )
		// Check if they are "Explorer"/"Cartographer" to exchange with
		if ((((FSMExploAgent) this.myAgent).getServices(services).length != 0) && (((FSMExploAgent) this.myAgent).getWait() == 0)) {
			System.out.println("Hello I'm " + this.myAgent.getLocalName() + " and I'm starting an exchange");
			((FSMExploAgent) this.myAgent).setWait(10);
			String agentMode = ((FSMExploAgent) this.myAgent).getMode();
			if (agentMode.equals("Explorer")) {
				this.condition = 2; // Start ExchangeExplo
			}
			else if (agentMode.equals("Cartographer")) {
				this.condition = 3; // Start ExchangeCarto
			}
			this.finished = true;
		}
		
		// ( CHECK 2 )
		// Check if they are "Hunter"/"Camper" to finish the exploration
		//if ((((FSMExploAgent) this.myAgent).getServices("Camper")!=null) || (((FSMExploAgent) this.myAgent).getServices("Hunter")!=null)) {
		//	System.out.println("Hello I'm " + this.myAgent.getLocalName() + " and I'm starting a donation");
		//	this.condition = 4; // Start GiveMapExplo
		//	this.finished = true; 
		//}
		
		/************************************************
		 * 
		 * Explore the Next Node
		 * 
		 ************************************************/
		
		// ( 1 )
		// If wait > 0 decrease the wait
		if (((FSMExploAgent) this.myAgent).getWait() != 0) {
			((FSMExploAgent) this.myAgent).decreaseWait();
		}
		
		// ( 2 )
		// Get the current position
		String nextNode = null;
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		// If our position was a path to explore, delete it
		if (!((FSMExploAgent) this.myAgent).getPathsToExplore().isEmpty()) {
			if (myPosition.equals(((FSMExploAgent) this.myAgent).getPathsToExplore().get(0))) {
				((FSMExploAgent) this.myAgent).delFirstPathsToExplore();
			}
		}
		
		// ( 3 )
		// Check if the position is not null
		if (myPosition!=null) {
			
			// Observe around the current position
			List<Couple<String,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();
			
			/*****************************************************
			 * Wait to see what happen on the graph
			 *****************************************************/
			try {
				this.myAgent.doWait(250);
			} catch (Exception e) {
				e.printStackTrace();
			}
			/*****************************************************/
			
			// Update informations about the current nodes and observations
			// Remove the current node from openlist and add it to closedNodes
			((FSMExploAgent) this.myAgent).getMyMap().addNode(myPosition, MapAttribute.closed);
			// Get the surrounding nodes and, if not in closedNodes, add them to open nodes.
			Iterator<Couple<String, List<Couple<Observation, Integer>>>> iter=lobs.iterator();
			while(iter.hasNext()){
				String nodeId=iter.next().getLeft();
				boolean isNewNode=((FSMExploAgent) this.myAgent).getMyMap().addNewNode(nodeId);
				// The node may exist, but not necessarily the edge
				if (myPosition!=nodeId) {
					((FSMExploAgent) this.myAgent).getMyMap().addEdge(myPosition, nodeId);
					if (nextNode==null && isNewNode) nextNode=nodeId;
				}
			}
			
			// ( 0 )
			// Select next move
			if (nextNode==null){ // If next node is null
				// ( Check 1 )
				// Check if you have a path to explore
				if (!((FSMExploAgent) this.myAgent).getPathsToExplore().isEmpty()) {
					// 
					nextNode = ((FSMExploAgent) this.myAgent).getMyMap().getShortestPath(myPosition, ((FSMExploAgent) this.myAgent).getPathsToExplore().get(0)).get(0);
				}
				// ( Check 2 )
				// If not get the shortest path in the closest open node
				else {
					nextNode = ((FSMExploAgent) this.myAgent).getMyMap().getShortestPathToClosestOpenNode(myPosition).get(0);
				}
			}
			
			((AbstractDedaleAgent)this.myAgent).moveTo(nextNode);
		}
		
		/************************************************
		 * 
		 * Check if the Exploration is Done
		 * 
		 ************************************************/
		
		// ( CHECK 1 )
		// Check if the exploration is finished
		if (!((FSMExploAgent) this.myAgent).getMyMap().hasOpenNode()){
			System.out.println(this.myAgent.getLocalName()+" - Exploration successfully done, behaviour removed.");
			this.condition = 1; // Go Hunting
			this.finished = true;
		}
		// ( CHECK 2 )
		// If not continue the exploration
		this.finished = true;
		System.out.println(condition);
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
