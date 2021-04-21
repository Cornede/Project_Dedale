package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.FSMExploAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import javafx.util.Pair;


/***
 * This behaviour allows the agent who possess it to send nb random int within [0-100[ to another agent whose local name is given in parameters
 * 
 * There is not loop here in order to reduce the duration of the behaviour (an action() method is not preemptive)
 * The loop is made by the behaviour itslef
 * 
 * @author Cédric Herpson
 *
 */

public class SendBehaviour extends SimpleBehaviour{

	private static final long serialVersionUID = 9088209402507795289L;

	private boolean finished = false;

	private int condition;

	private int performative;

	private String protocol;

	private AID receiverAID;

	private String contentString = null;

	private int mode;

	public SendBehaviour(final Agent myAgent, AID receiverAID, int performative, String protocol, String content) {
		super(myAgent);
		this.performative = performative;
		this.protocol = protocol;
		this.receiverAID = receiverAID;
		this.contentString = content;
		this.mode = -1;
	}

	public SendBehaviour(final Agent myAgent, AID receiverAID, int performative, String protocol, int mode) {
		super(myAgent);
		this.performative = performative;
		this.protocol = protocol;
		this.receiverAID = receiverAID;
		this.mode = mode;
	}

	public void action() {
		final ACLMessage msg = new ACLMessage(performative);
		//1°Create the message
		switch(this.mode) {
		/************************************************
		 * 
		 * Explorer : Sending my position
		 * 
		 ************************************************/
		case 1:
			msg.setSender(this.myAgent.getAID());
			msg.setProtocol(protocol);
			msg.addReceiver(receiverAID);
			msg.setContent(((AbstractDedaleAgent)this.myAgent).getCurrentPosition());
			System.out.println("Explorer : I send my position to the cartographer : [ " + ((AbstractDedaleAgent)this.myAgent).getCurrentPosition() + " ]");
			this.condition = 1;
			break;
		/************************************************
		 * 
		 * Explorer : Sending my map 
		 * 
		 ************************************************/
		case 2:
			msg.setSender(this.myAgent.getAID());
			msg.setProtocol(protocol);
			msg.addReceiver(receiverAID);
			try {
				msg.setContentObject(((FSMExploAgent) this.myAgent).getMyMapSerial());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Explorer : I send my map to the cartographer ");
			this.condition = 2;
			break;
		/************************************************
		 * 
		 * Cartographer : Sending new Agent's paths 
		 * 
		 ************************************************/
		case 3:
			msg.setSender(this.myAgent.getAID());
			msg.setProtocol(protocol);
			msg.addReceiver(receiverAID);
			List<String> paths = new ArrayList<String>();
			List<Pair<List<String>, AID>> pathsToTake = new ArrayList<Pair<List<String>, AID>>();
			pathsToTake = ((FSMExploAgent) this.myAgent).getMyMap().getPathsToTake(((FSMExploAgent) this.myAgent).getPositions());
			for (Pair<List<String>, AID> pathsNAID : pathsToTake) {
				if (receiverAID.equals(pathsNAID.getValue())) {
					paths = pathsNAID.getKey();
					break;
				}
			}
			String pathsString = ((FSMExploAgent) this.myAgent).dataToString(paths);
			System.out.println("Cartographer : I send the paths to the explorer : [ " + pathsString + " ]");
			msg.setContent(pathsString);
			this.condition = 5;
			break;
		/************************************************
		 * 
		 * Cartographer : Sending new Agent's map 
		 * 
		 ************************************************/
		case 4:
			msg.setSender(this.myAgent.getAID());
			msg.setProtocol(protocol);
			msg.addReceiver(receiverAID);
			SerializableSimpleGraph<String, MapAttribute> mapReceived = null;
			for (Pair<SerializableSimpleGraph<String, MapAttribute>, AID> couple : ((FSMExploAgent) this.myAgent).getMapsReceived()) {
				if (couple.getValue() == receiverAID) {
					mapReceived = couple.getKey();
				}
			}
			MapRepresentation mapToSend = new MapRepresentation();
			SerializableSimpleGraph<String, MapAttribute> sgToSend = mapToSend.prepareMap(((FSMExploAgent) this.myAgent).getMyMap(), mapReceived);
			System.out.println("Cartographer : I send the newmap the explorer : " + sgToSend.toString());
			try {
				msg.setContentObject(sgToSend);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (!(((FSMExploAgent) this.myAgent).getCntExchange() == 0)) {
				((FSMExploAgent) this.myAgent).setCntExchange(((FSMExploAgent) this.myAgent).getCntExchange() - 1);
				this.condition = 6;
			}
			break;
		/************************************************
		 * 
		 * DEFAULT CASE : TEMPORARY 
		 * 
		 ************************************************/
		default:
			msg.setContent(contentString);
		}

		this.myAgent.send(msg);
		this.finished=true; 
		//System.out.println(this.myAgent.getLocalName()+" ----> Message sent to "+this.receiverAID+" ,content= "+msg.getContent());

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

