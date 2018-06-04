package cisc3320.davidtan.spring2017;
/**
 * Dispatcher
 * Sets CPU registers before context switches
 * 
 * @author David Tan
 */
public class Dispatcher {

	/** Constructor **/
	private Dispatcher() {
	}

	/** Singleton **/
	public static Dispatcher getInstance() {
		if(instance == null)
			instance = new Dispatcher();
		return instance;
	}

	/** Runs a job **/
	public void runJob(Job job, int[] a, int[] p) {
		//Job can only run if it is in memory
		if(!job.inCore || job.partition() == null) {
			a[0] = Config.CPU_STATE_IDLE;
			return;
		}
		a[0] = Config.CPU_STATE_RUN;
		p[2] = job.partition().base();
		p[3] = job.partition().length();

		if(!os.schedule.TIME_SLICE_MODE)
			p[4] = job.getRemainingCPUTime();
		else {
			if(job.getRemainingCPUTime() >= os.schedule.TIME_SLICE)
				p[4] = os.schedule.TIME_SLICE;
			else
				p[4] = job.getRemainingCPUTime();
		}
		Config.println_debug("[Dispatcher]: Running "+job);
		return;
	}

	/** Decide if a job can run **/
	public boolean canRunJob(Job job) {
		//Invalid job
		if(!Job.isValid(job) || !os.jobtable.contains(job))
			return false;
		
		//Job has finished running
		if(job.terminated || job.getRemainingCPUTime() <= 0)
			return false;
		
		//Memory issues
		if(!job.inCore)
			return false;
		
		//Doing IO
		if(job.blocked)
			return false;
		
		return true;
	}

	/* variables */
	private static Dispatcher instance = null;
}
