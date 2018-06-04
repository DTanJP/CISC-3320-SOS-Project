package cisc3320.davidtan.spring2017.scheduling;

import java.util.LinkedList;

import cisc3320.davidtan.spring2017.Config;
import cisc3320.davidtan.spring2017.Job;
import cisc3320.davidtan.spring2017.os;

/**
 * Shortest Remaining Time First
 * 
 * @author David Tan
 */
public class SRTF implements SchedulingAlgorithm {

	/** Constructor **/
	private SRTF() {
	}

	/** Singleton **/
	public static SRTF getInstance() {
		return instance;
	}

	@Override
	public Job schedule() {
		if(os.jobtable.isEmpty())
			return null;
		os.schedule.TIME_SLICE_MODE = false;
		return os.schedule.cherryPick((LinkedList<Job>) os.jobtable, Config.JOB_CURRENT_TIME);
	}

	/* variables */
	private static SRTF instance = new SRTF();
}
