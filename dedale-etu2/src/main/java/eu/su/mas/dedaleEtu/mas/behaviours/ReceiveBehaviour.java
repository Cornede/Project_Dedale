package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.List;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.FSMExploAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import javafx.util.Pair;

/**
 * This behaviour is a one Shot.
 * It receives a message tagged with an inform performative, print the content in the console and destroy itlself
 * 
 * @author Cédric Herpson
 *
 */
public class ReceiveBehaviour extends SimpleBehaviour{

	private static final long serialVersionUID = 9088209402507795289L;

	private boolean finished = false;

	private int condition;

	private int performative;

	private String protocol;

	//private String conversation;

	private AID senderAID;

	private int mode;

	/**
	 * 
	 * This behaviour is a one Shot.
	 * It receives a message tagged with an inform performative, print the content in the console and destroy itlself
	 * @param myagent
	 */
	public ReceiveBehaviour(final Agent myAgent, int performative) {
		super(myAgent);
		this.performative = performative;
	}

	public ReceiveBehaviour(final Agent myAgent, AID senderAID, int performative, String protocol, int mode) {
		super(myAgent);
		this.performative = performative;
		this.protocol = protocol;
		this.senderAID = senderAID;
		this.mode = mode;
	}


	@SuppressWarnings("unchecked")
	public void action() {
		//1) receive the message
		final MessageTemplate msgTemplate = MessageTemplate.and(MessageTemplate.MatchPerformative(performative), MessageTemplate.MatchProtocol(protocol)) ;			

		final ACLMessage msg = this.myAgent.receive(msgTemplate);

		if (msg != null) {
			/************************************************
			 * 
			 * Choose where the message will be stored
			 * 
			 ************************************************/
			switch (this.mode) {
			/************************************************
			 * 
			 * Explorer : New Paths Received
			 * 
			 ************************************************/
			case 1 : 
				String pathsString = (String) msg.getContent();
				List<String> pathsReceived = ((FSMExploAgent) this.myAgent).stringToData(pathsString);
				System.out.println("Explorer : I received my paths : [ " + pathsString + " ]");
				((FSMExploAgent) this.myAgent).setPathsToExplore(pathsReceived);
				this.condition = 3;
				break;
				/************************************************
				 * 
				 * Explorer : New Map Received
				 * 
				 ************************************************/
			case 2 :
				SerializableSimpleGraph<String, MapAttribute> sgNewMapReceived=null;
				try {
					sgNewMapReceived = (SerializableSimpleGraph<String, MapAttribute>)msg.getContentObject();
				} catch (UnreadableException e) {
					e.printStackTrace();
				}
				System.out.println("Explorer : I received my map : " + sgNewMapReceived);
				((FSMExploAgent) this.myAgent).getMyMap().mergeMap(sgNewMapReceived);
				// No condition needed this fsm is finished
				break;
				/************************************************
				 * 
				 * Cartographer : Agent's Position received
				 * 
				 ************************************************/
			case 3 :
				try {
					String positionReceived = null;
					positionReceived = (String) msg.getContent();
					Pair<String, AID> position = new Pair<String, AID>(positionReceived, senderAID);
					System.out.println("Cartographer : I received the position of the explorer : " + positionReceived);
					((FSMExploAgent) this.myAgent).addPositions(position);
					this.condition = 1;
				} catch (Exception e) {
					e.printStackTrace();
					this.condition = 0;
				}
				break;
				/************************************************
				 * 
				 * Cartographer : Agent's Map received
				 * 
				 ************************************************/
			case 4 :
				SerializableSimpleGraph<String, MapAttribute> sgMapReceived=  null;
				try {
					sgMapReceived = (SerializableSimpleGraph<String, MapAttribute>)msg.getContentObject();
					Pair<SerializableSimpleGraph<String, MapAttribute>, AID> mapReceived = new Pair<SerializableSimpleGraph<String, MapAttribute>, AID>(sgMapReceived, senderAID);
					System.out.println("Cartographer : I received my map : " + mapReceived);
					((FSMExploAgent) this.myAgent).getMyMap().mergeMap(sgMapReceived);
					((FSMExploAgent) this.myAgent).addMapsReceived(mapReceived);
					((FSMExploAgent) this.myAgent).decCntExchange();
					System.out.println(((FSMExploAgent) this.myAgent).getCntExchange());
					if (((FSMExploAgent) this.myAgent).getCntExchange() > 0) {
						System.out.println("Agents left to receive the position and the map : " + ((FSMExploAgent) this.myAgent).getCntExchange());
						this.condition = 2;
					}
					else {
						System.out.println("No agents left  : " + ((FSMExploAgent) this.myAgent).getCntExchange());
						this.condition = 3;
					}
				} catch (UnreadableException e) {
					e.printStackTrace();
					this.condition = 1;
				}
				break;
			}
			// Print the message received
			//System.out.println(this.myAgent.getLocalName()+"<----Result received from "+msg.getSender().getLocalName()+" ,content= "+msg.getContent());
			this.finished=true;
		}else{
			//System.out.println("I'm waiting for a message");
			return;// the behaviour goes to sleep until the arrival of a new message in the agent's Inbox.
				
		}
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


