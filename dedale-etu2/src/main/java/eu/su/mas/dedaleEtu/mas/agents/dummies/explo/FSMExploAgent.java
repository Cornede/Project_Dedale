package eu.su.mas.dedaleEtu.mas.agents.dummies.explo;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.startMyBehaviours;
import eu.su.mas.dedaleEtu.mas.behaviours.EndBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.ExchangeMapCartoBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.ExchangeMapExploBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.ExploBehaviour;
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
import javafx.util.Pair;

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
	private List<String> pathsToExplore = new ArrayList<String>();
	private int wait; //To put a timer for the next exchange
	private String mode;
	
	// Only used by the cartographer 
	private List<Pair<SerializableSimpleGraph<String, MapAttribute>, AID>> mapsReceived = new ArrayList<Pair<SerializableSimpleGraph<String, MapAttribute>, AID>>();
	private List<Pair<List<String>, AID>> pathsToTake = new ArrayList<Pair<List<String>, AID>>();
	private List<Pair<String, AID>> positions = new ArrayList<Pair<String, AID>>();
	private int cntExchange;
	
	// Only used by the explorer
	private MapRepresentation lastMapSent = null;
	
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
		
		//FSM.pdf in the files to see it globally
		
		/************************************************
		 * 
		 * CREATE the states names and FSM
		 * 
		 ************************************************/
		
		final String YellowUpdateExplo = "YellowUpdateExplo";
		final String Explo = "Explo";
		final String ExchangeMapCarto = "ExchangeMapCarto";
		final String ExchangeMapExplo = "ExchangeMapExplo";
		//final String GiveMap = "GiveMap";
		//final String YellowUpdateHunt = "YellowUpdateHunt";
		//final String CampNHunt= "CampNHunt";
		final String End = "End";

		FSMBehaviour fsm = new FSMBehaviour(this);
		
		/************************************************
		 * 
		 * ADD the states and behaviours
		 * 
		 ************************************************/
		
		fsm.registerFirstState(new YellowSetupBehaviour(this, list_agentNames.get(0)), YellowUpdateExplo);
		fsm.registerState(new ExploBehaviour(this), Explo);
		//fsm.registerState(new GiveMapBehaviour(this, 1), GiveMap);
		fsm.registerState(new ExchangeMapCartoBehaviour(this), ExchangeMapCarto);
		fsm.registerState(new ExchangeMapExploBehaviour(this), ExchangeMapExplo);
		//fsm.registerState(new YellowSetupBehaviour(this, list_agentNames.get(1)), YellowUpdateHunt);
		//fsm.registerState(new CampNHunt(this, 1), CampNHunt);
		fsm.registerLastState(new EndBehaviour(this), End);
		
		
		/************************************************
		 * 
		 * ADD the transitions
		 * 
		 ************************************************/
		
		fsm.registerDefaultTransition(YellowUpdateExplo, Explo); //First register the "Explorer"/"Cartographer" and start the exploration
		
		fsm.registerDefaultTransition(Explo, Explo); //If nothing happens continue the exploration
		//fsm.registerTransition(Explo, YellowUpdateHunt, 1); //When explo done register hunt
		fsm.registerTransition(Explo, ExchangeMapExplo, 2); //If agent "Explorer" around start exchange
		fsm.registerTransition(Explo, ExchangeMapCarto, 3); //If agent "Cartographer" around start exchange
		//fsm.registerTransition(Explo, GiveMap, 3); //If agent "Hunter" or "Camper" around start exchange
		
		fsm.registerTransition(ExchangeMapExplo, Explo, 2); //If exchange done go back to the exploration
		fsm.registerTransition(ExchangeMapCarto, Explo, 3); //If exchange done go back to the exploration
		
		//fsm.registerTransition(GiveMap, Explo, ); //If give done go back to the exploration
		
		//fsm.registerTransition(YellowUpdateHunt, CampNHunt, 1); //When registered start hunt
		
		fsm.registerTransition(Explo, End, 1); //Temporary
		
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


	public List<String> getPathsToExplore() {
		return pathsToExplore;
	}
	
	public void delFirstPathsToExplore() {
		this.pathsToExplore.remove(0);
	}

	public void setPathsToExplore(List<String> pathsToExplore) {
		this.pathsToExplore = pathsToExplore;
	}


	public String getMode() {
		return mode;
	}


	public void setMode(String mode) {
		this.mode = mode;
	}


	public List<Pair<SerializableSimpleGraph<String, MapAttribute>, AID>> getMapsReceived() {
		return mapsReceived;
	}
	
	public void addMapsReceived(Pair<SerializableSimpleGraph<String, MapAttribute>, AID> mapReceived) {
		this.mapsReceived.add(mapReceived);
	}
	
	public void clearMapsReceived() {
		this.mapsReceived.clear();
	}

	public void setMapsReceived(List<Pair<SerializableSimpleGraph<String, MapAttribute>, AID>> mapsReceived) {
		this.mapsReceived = mapsReceived;
	}
	
	public MapRepresentation getMyMap() {
		return myMap;
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

	public MapRepresentation getLastMapSent() {
		return lastMapSent;
	}

	public void setLastMapSent(MapRepresentation lastMapSent) {
		this.lastMapSent = lastMapSent;
	}

	public List<Pair<String, AID>> getPositions() {
		return positions;
	}
	
	public void addPositions(Pair<String, AID> position) {
		this.positions.add(position);
	}
	
	public void clearPositions() {
		this.positions.clear();
	}
	
	public void removeLastPositions() {
		this.positions.remove(this.positions.size()-1);
	}

	public void setPositions(List<Pair<String, AID>> positions) {
		this.positions = positions;
	}

	public List<Pair<List<String>, AID>> getPathsToTake() {
		return pathsToTake;
	}
	
	public void setPathsToTake(Pair<List<String>, AID> pathToTake) {
		this.pathsToTake.add(pathToTake);
	}

	public void setPathsToTake(List<Pair<List<String>, AID>> pathsToTake) {
		this.pathsToTake = pathsToTake;
	}

	public int getCntExchange() {
		return cntExchange;
	}

	public void setCntExchange(int cntExchange) {
		this.cntExchange = cntExchange;
	}
	
	public void decCntExchange() {
		this.cntExchange = cntExchange - 1;
	}
	
}

