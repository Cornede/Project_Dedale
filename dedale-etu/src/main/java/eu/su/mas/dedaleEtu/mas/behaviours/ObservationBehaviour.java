package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.ArrayList;
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
public class ObservationBehaviour extends OneShotBehaviour {

	private static final long serialVersionUID = 8567689731496787661L;

	private boolean finished = false;

	/**
	 * @param myAgent
	 */
	public ObservationBehaviour(final AbstractDedaleAgent myAgent) {
		super(myAgent);
	}

	@SuppressWarnings("unlikely-arg-type")
	@Override
	public void action() {

		String myPosition;
		String nextNode = null;

		switch (((FSMExploAgent) this.myAgent).getMode()) {

		case 0 : // EXPLORATION MODE

			//System.out.println("Hello I'm " + this.myAgent.getLocalName() + " and I'm exploring");

			/************************************************
			 * 
			 * Map's Initialisation 
			 * 
			 ************************************************/

			if (((FSMExploAgent) this.myAgent).getMyMap() == null) {
				((FSMExploAgent) this.myAgent).initiateMyMap(); // Initiate the map of the agent
				System.out.println("[ " + this.myAgent.getLocalName() + " ]" + " : Initialisation");
			}

			/************************************************
			 * 
			 * Explore the Next Node
			 * 
			 ************************************************/

			myPosition = ((AbstractDedaleAgent)this.myAgent).getCurrentPosition();

			if (myPosition != null) {

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

				/************************************************
				 * 
				 * Update informations about nodes
				 * 
				 ************************************************/

				((FSMExploAgent) this.myAgent).getMyMap().addNode(myPosition, MapAttribute.closed);
				Iterator<Couple<String, List<Couple<Observation, Integer>>>> iter=lobs.iterator();

				while(iter.hasNext()){
					String nodeId=iter.next().getLeft();
					boolean isNewNode=((FSMExploAgent) this.myAgent).getMyMap().addNewNode(nodeId);
					if (myPosition!=nodeId) {
						((FSMExploAgent) this.myAgent).getMyMap().addEdge(myPosition, nodeId);
						if ( (nextNode==null && isNewNode) && (!((FSMExploAgent) this.myAgent).getPosition().contains(nodeId)) ) {
							nextNode=nodeId;
						}
					}
				}
				
				/************************************************
				 * 
				 * Test if exploration's done or continue
				 * 
				 ************************************************/

				if (!((FSMExploAgent) this.myAgent).getMyMap().hasOpenNode()){
					System.out.println("[ " + this.myAgent.getLocalName() + " ]" + " : Exploration successfully done, behaviour removed.");
					((FSMExploAgent) this.myAgent).setMode(1);
					break;
				}

				if (nextNode==null){ 
					nextNode = ((FSMExploAgent) this.myAgent).getMyMap().getShortestPathToClosestOpenNode(myPosition,((FSMExploAgent) this.myAgent).getPosition()).get(0);
				}

				/************************************************
				 * 
				 * Update informations about stenchs
				 * 
				 ************************************************/

				Iterator<Couple<String, List<Couple<Observation, Integer>>>> iter2=lobs.iterator();
				ArrayList<String> NodeStench= new ArrayList<String>();

				while(iter2.hasNext()){
					Couple<String, List<Couple<Observation, Integer>>> i = iter2.next();
					String nodeId=i.getLeft();
					((FSMExploAgent) this.myAgent).addOpenNextNodes(nodeId);
					if (i.getRight().equals("Stench")){
						NodeStench.add(nodeId);
					}
				}

				if (NodeStench.size() == 1) {
					((FSMExploAgent) this.myAgent).addStenchDirection(NodeStench.get(0));
				}
				else if (NodeStench.size() > 1)  {
					((FSMExploAgent) this.myAgent).addInsideStench(myPosition);
				}

			}
			((FSMExploAgent) this.myAgent).decreaseWait();
			((AbstractDedaleAgent)this.myAgent).moveTo(nextNode);
			break;

		case 1 : // HUNT MODE

			//System.out.println("Hello I'm " + this.myAgent.getLocalName() + " and I'm hunting");

			/************************************************
			 * 
			 * Explore the Next Node
			 * 
			 ************************************************/

			myPosition = ((AbstractDedaleAgent)this.myAgent).getCurrentPosition();

			if (myPosition != null) {

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

				Iterator<Couple<String, List<Couple<Observation, Integer>>>> iter=lobs.iterator();

				// We check in the observation if we see the golem

				ArrayList<String> NodeStench= new ArrayList<String>();
				while(iter.hasNext()){
					Couple<String, List<Couple<Observation, Integer>>> i = iter.next();
					String nodeId=i.getLeft();
					((FSMExploAgent) this.myAgent).addOpenNextNodes(nodeId);
					if (i.getRight().equals("Stench")){
						NodeStench.add(nodeId);
					}
				}
				if (NodeStench.size() == 1) {
					((FSMExploAgent) this.myAgent).addStenchDirection(NodeStench.get(0));
				}
				else if (NodeStench.size() > 1)  {
					((FSMExploAgent) this.myAgent).addInsideStench(myPosition);
				}
			}
		}
		this.finished = true;

	}
}
