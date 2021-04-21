package eu.su.mas.dedaleEtu.mas.behaviours;

import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;

/**
 * end the FSM
 * @author dl
 *
 */
public class EndBehaviour extends SimpleBehaviour {

	
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
	public EndBehaviour (Agent myagent) {
		super(myagent);
	}

	@Override
	public void action() {
		//Deregister the service
		try {
			DFService.deregister(this.myAgent);
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

