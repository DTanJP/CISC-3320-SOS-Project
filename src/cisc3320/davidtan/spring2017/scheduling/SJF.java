package cisc3320.davidtan.spring2017.scheduling;

import java.util.LinkedList;

import cisc3320.davidtan.spring2017.Config;
import cisc3320.davidtan.spring2017.Job;
import cisc3320.davidtan.spring2017.os;

/**
 * Shortest Job First
 * (Quickest job first)
 * 
 * @author David Tan
 */
public class SJF implements SchedulingAlgorithm {

	/** Constructor **/
	private SJF() {
	}

	/** Singleton **/
	public static SJF getInstance() {
		return instance;
	}

	@Override
	public Job schedule() {
		if(os.jobtable.isEmpty())
			return null;
		os.schedule.TIME_SLICE_MODE = false;
		return os.schedule.cherryPick((LinkedList<Job>) os.jobtable, Config.JOB_MAX_CPU_TIME);
	}

	/* variables */
	private static SJF instance = new SJF();
}
