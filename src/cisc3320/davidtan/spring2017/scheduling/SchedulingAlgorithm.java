package cisc3320.davidtan.spring2017.scheduling;

import cisc3320.davidtan.spring2017.Job;
/**
 * Interface template for scheduling style
 * 
 * @author David Tan
 */
public interface SchedulingAlgorithm {

	//Returns the Job that is to be scheduled to run on the CPU
	public Job schedule();
	
}
