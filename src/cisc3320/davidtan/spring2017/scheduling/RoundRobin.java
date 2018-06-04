package cisc3320.davidtan.spring2017.scheduling;

import cisc3320.davidtan.spring2017.Config;
import cisc3320.davidtan.spring2017.Job;
import cisc3320.davidtan.spring2017.os;
/**
 * Round Robin
 * @author David Tan
 */
public class RoundRobin implements SchedulingAlgorithm {

	/** Constructor **/
	private RoundRobin() {
	}
	
	/** Singleton **/
	public static RoundRobin getInstance() {
		return instance;
	}
	
	@Override
	public Job schedule() {
		Config.println_debug("[Round Robin.schedule]");
		Job result = null;
		for(int i=0; i<os.jobtable.size(); i++) {
			if(result != null)
				break;
			
			index++;
			
			if(index >= os.jobtable.size())
				index = 0;
			
			Job job = os.jobtable.get(index);
			if(os.dispatcher.canRunJob(job)) {
				result = os.jobtable.get(index);
				index = (index + 1 >= os.jobtable.size() ? 0 : index + 1);
				break;
			}
		}
		Config.println_debug("[Round Robin.schedule.result]: "+result);
		os.schedule.TIME_SLICE = 100;
		os.schedule.TIME_SLICE_MODE = true;
		return result;
	}

	/* variables */
	private static RoundRobin instance = new RoundRobin();
	private int index = 0;
}
