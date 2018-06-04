package cisc3320.davidtan.spring2017.scheduling;

import java.util.LinkedList;

import cisc3320.davidtan.spring2017.Config;
import cisc3320.davidtan.spring2017.Job;
import cisc3320.davidtan.spring2017.os;

/**
 * First Come First Serve
 */
public class FCFS implements SchedulingAlgorithm {

	/** Constructor **/
	private FCFS() {
	}
	
	/** Singleton **/
	public static FCFS getInstance() {
		return instance;
	}
	
	@Override
	public Job schedule() {
		if(os.jobtable == null || os.jobtable.isEmpty())
			return null;
		os.schedule.TIME_SLICE_MODE = false;
		//Ask the scheduler to cherry pick a smallest job id
		return os.schedule.cherryPick((LinkedList<Job>) os.jobtable, Config.JOB_ID);
	}
	
	
	/* variables */
	private static FCFS instance = new FCFS();
}
