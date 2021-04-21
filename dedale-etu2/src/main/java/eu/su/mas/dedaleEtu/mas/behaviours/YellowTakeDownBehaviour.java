package eu.su.mas.dedaleEtu.mas.behaviours;

import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;

/**
 * This  behaviour take down the Agent in Yellow Pages
 * @author dl
 *
 */
public class YellowTakeDownBehaviour extends SimpleBehaviour {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1841225686153895084L;
	private boolean finished = false;

	/**
	 * Setup the yellow pages for an agent
	 * @param myagent the agent who posses the behaviour
	 *  
	 */
	public YellowTakeDownBehaviour (Agent myagent) {
		super(myagent);
	}

	@Override
	public void action() {
		//Deregister the service
		try {
			DFService.deregister(this.myAgent);
			//System.out.println("\n\n\n hello");
		} catch (FIPAException fe) {
			fe.printStackTrace(); 
		}
		finished = true;
	}

	@Override
	public boolean done() {
		return finished;
	}
}
