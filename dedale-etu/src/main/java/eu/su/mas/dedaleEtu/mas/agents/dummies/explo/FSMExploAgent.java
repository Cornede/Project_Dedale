package eu.su.mas.dedaleEtu.mas.agents.dummies.explo;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.startMyBehaviours;
import eu.su.mas.dedaleEtu.mas.behaviours.DecisionBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.ObservationBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.SignalBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.YellowSetupBehaviour;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.FSMBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

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
 * @author dl
 *
 */


public class FSMExploAgent extends AbstractDedaleAgent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 830885575375128225L;
	private MapRepresentation myMap;
	private int mode = 0; // 0 : Exploration, 1 : Hunt
	private int step = 0;
	
	private boolean interblocage = false;
	private int interblocageState = 0;
	private int communicationState = 0;
	private boolean moved = true;
	private String lastPosition = "";
	
	//Counters
	private int wait;
	
	//Observations
	private List<String> openNextNodes = new ArrayList<String>();
	private List<String> lastStenchNodes = new ArrayList<String>();
	private List<String> stenchNodes = new ArrayList<String>();
	
	//Informations
	private List<String> position = new ArrayList<String>();
	private List<String> stenchDirection = new ArrayList<String>();
	private List<String> insideStench = new ArrayList<String>();
	
	/**
	 * This method is automatically called when "agent".start() is executed.
	 * Consider that Agent is launched for the first time. 
	 * 			1) set the agent attributes 
	 *	 		2) add the behaviours
	 *          
	 */
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
		
		System.out.println("ARGUMENTS : " +list_agentNames.get(0));
		
		List<Behaviour> lb=new ArrayList<Behaviour>();
		
		
		/************************************************
		 * 
		 * CREATE the states names and FSM
		 * 
		 ************************************************/
		
		final String YellowSetup = "YellowSetup";
		final String Observation = "Observation";
		final String Signal = "Signal";

		FSMBehaviour fsm = new FSMBehaviour(this);
		
		/************************************************
		 * 
		 * ADD the states and behaviours
		 * 
		 ************************************************/
		
		fsm.registerFirstState(new YellowSetupBehaviour(this, list_agentNames.get(0)), YellowSetup);
		fsm.registerState(new ObservationBehaviour(this), Observation);
		fsm.registerState(new SignalBehaviour(this), Signal);
		
		/************************************************
		 * 
		 * ADD the transitions
		 * 
		 ************************************************/
		
		fsm.registerDefaultTransition(YellowSetup, Observation);
		fsm.registerDefaultTransition(Observation, Signal); 
		fsm.registerDefaultTransition(Signal, Observation);
		
		//System.out.println("the  agent "+this.getLocalName()+ " is started");
		
		/***
		 * MANDATORY TO ALLOW YOUR AGENT TO BE DEPLOYED CORRECTLY
		 */
		lb.add(fsm);
		
		addBehaviour(new startMyBehaviours(this,lb));
		
	}
	
	/**
	* @param name of the service needed
	* @return list of agents whom can offer the service
	*/
	public AID[] getServices(String service) {
		DFAgentDescription dfd = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType(service); //name of the service
		dfd.addServices(sd);
		AID[] agentsAID = null;
		try {
			DFAgentDescription[] result = DFService.search(this, dfd);
			// System.out.println("result" + result);
			// list of all the agents (AID) offering this service
			agentsAID = new AID[result.length];
			for (int i = 0; i < result.length; ++i) {
				agentsAID[i] = result[i].getName();
			}
			// System.out.println("AID" + agentsAID);		
		} catch (FIPAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return agentsAID;
	}
	
	public String dataToString(List<String> data) {
		String[] dataArray = data.toArray(new String[0]);
		return String.join(",", dataArray);
	}
	
	public List<String> stringToData(String string) {
		List<String> dataList = Stream.of(string.split(",", -1))
					.collect(Collectors.toList());
		return dataList;
	}

	public int getWait() {
		return wait;
	}
	
	public void decreaseWait() {
		this.wait = this.wait - 1;
	}


	public void setWait(int wait) {
		this.wait = wait;
	}

	
	public MapRepresentation getMyMap() {
		return myMap;
	}
	
	public void myMapAddNode(String node, MapAttribute attribute) {
		this.myMap.addNode(node, attribute);
	}
	
	public boolean myMapAddNewNode(String nodeid) {
		return this.myMap.addNewNode(nodeid);
	}
	
	public void myMapAddEdge(String node, String nodeid) {
		this.myMap.addEdge(node, nodeid);
	}
	
	public List<String> myMapShortestPathToClosestOpenNode(String node) {
		return this.myMap.getShortestPathToClosestOpenNode(node);
	}
	
	public SerializableSimpleGraph<String,MapAttribute> getMyMapSerial() {
		return myMap.getSerializableGraph();
	}

	public void setMyMap(MapRepresentation myMap) {
		this.myMap = myMap;
	}
	
	public void initiateMyMap() {
		this.myMap = new MapRepresentation();
	}

	public List<String> getPosition() {
		return position;
	}
	
	public void cleanPosition() {
		this.position.clear();
	}
	
	public void addPosition(String node) {
		this.position.add(node);
	}

	public void setPosition(List<String> position) {
		this.position = position;
	}

	public List<String> getStenchDirection() {
		return stenchDirection;
	}
	
	public void cleanStenchDirection() {
		this.stenchDirection.clear();
	}
	
	public void addStenchDirection(String node) {
		this.stenchDirection.add(node);
	}

	public void setStenchDirection(List<String> stenchDirection) {
		this.stenchDirection = stenchDirection;
	}

	public List<String> getInsideStench() {
		return insideStench;
	}
	
	public void cleanInsideStench() {
		this.insideStench.clear();
	}
	
	public void addInsideStench(String node) {
		this.insideStench.add(node);
	}

	public void setInsideStench(List<String> insideStench) {
		this.insideStench = insideStench;
	}

	public List<String> getOpenNextNodes() {
		return openNextNodes;
	}
	
	public void cleanOpenNextNodes() {
		this.openNextNodes.clear();
	}
	
	public void addOpenNextNodes(String node) {
		this.openNextNodes.add(node);
	}

	public void setOpenNextNodes(List<String> openNextNodes) {
		this.openNextNodes = openNextNodes;
	}

	public List<String> getLastStenchNodes() {
		return lastStenchNodes;
	}
	
	public void cleanLastStenchNodes() {
		this.lastStenchNodes.clear();
	}
	
	public void addLastStenchNodes(String node) {
		this.lastStenchNodes.add(node);
	}

	public void setLastStenchNodes(List<String> lastStenchNodes) {
		this.lastStenchNodes = lastStenchNodes;
	}

	public List<String> getStenchNodes() {
		return stenchNodes;
	}
	
	public void cleanStenchNodes() {
		this.stenchNodes.clear();
	}
	
	public void addStenchNodes(String node) {
		this.stenchNodes.add(node);
	}

	public void setStenchNodes(List<String> stenchNodes) {
		this.stenchNodes = stenchNodes;
	}

	public int getMode() {
		return mode;
	}

	public void setMode(int mode) {
		this.mode = mode;
	}

	public int getStep() {
		return step;
	}

	public void setStep(int step) {
		this.step = step;
	}
	
	
	public void setInterblocage(boolean interblocage) {
		this.interblocage = interblocage;
	}
	
	public boolean isInterblocage() {
		return interblocage;
	}
	
	
	public int getInterblocageState() {
		return interblocageState;
	}

	public void setInterblocageState(int interblocageState) {
		this.interblocageState = interblocageState;
	}
	
	public String getLastPosition() {
		return lastPosition;
	}

	public void setLastPosition(String lastPosition) {
		this.lastPosition = lastPosition;
	}
	
	public boolean getMoved() {
		return moved;
	}
	
	public void setMoved(boolean moved) {
		this.moved = moved;
	}
	
}