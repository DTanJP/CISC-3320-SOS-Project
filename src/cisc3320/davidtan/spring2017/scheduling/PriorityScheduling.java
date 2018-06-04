package cisc3320.davidtan.spring2017.scheduling;

import cisc3320.davidtan.spring2017.Job;
import cisc3320.davidtan.spring2017.os;
/**
 * Priority Scheduling
 * 
 * @author David Tan
 */
public class PriorityScheduling implements SchedulingAlgorithm {

	/** Constructor **/
	private PriorityScheduling() {
	}
	
	/** Singleton **/
	public static PriorityScheduling getInstance() {
		if(instance == null)
			instance = new PriorityScheduling();
		return instance;
	}
	
	@Override
	public Job schedule() {
		//JobTable error checking
		if(os.jobtable == null || os.jobtable.isEmpty())
			return null;
		os.schedule.TIME_SLICE_MODE = false;
		Job result = null;
		int priority = 11;//Priority 10 is lowest priority :: Priority 1 is highest priority
		for(Job job : os.jobtable) {
			if(os.dispatcher.canRunJob(job)) {
				if(priority > job.Priority()) {
					priority = job.Priority();
					result = job;
				}
			}
		}
		return result;
	}

	/* variables */
	private static PriorityScheduling instance = null;
}
