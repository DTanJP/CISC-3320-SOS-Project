package cisc3320.davidtan.spring2017;

import java.util.LinkedList;

/**
 * IOManager
 * Manages all IO requests
 * 
 * @author David Tan
 */
public class IOManager {

	/** Constructor **/
	private IOManager() {
		ioQueue = new LinkedList<>();
	}

	/** Singleton **/
	public static IOManager getInstance() {
		if(instance == null)
			instance = new IOManager();
		return instance;
	}

	/** Adds a job that requires IO **/
	public void registerJob(Job job) {
		Config.println_debug("[IOManager]: Registering "+job);
		if(!Job.isValid(job))
			return;

		ioQueue.add(job);
		job.ioRequest++;
		process();
	}

	/** Removes a job from doing IO **/
	public void unregisterJob(Job job) {
		if(!Job.isValid(job))
			return;

		//This job is no longer doing IO
		job.latched = false;

		//Remove a IO request for this job
		ioQueue.remove(job);
		job.ioRequest--;
		
		Config.println_debug("[IOManager]: Unregistering "+job);
		//Unblock only when the queue runs out of IO requests from this job
		if(job.blocked && job.ioRequest == 0) {
			job.blocked = false;
			ioJob = null;
		}
		//Continue doing IO for the rest of the queue
		process();
	}
	
	/** Count the amount of blocked inCore jobs **/
	public int getTotalBlocked() {
		return (int)(os.jobtable.stream().filter(job -> Job.isValid(job) && job.blocked && job.inCore).count());
	}
	
	/** Process the jobs **/
	public void process() {
		if(ioQueue.isEmpty() || working)
			return;

		ioJob = null;
		for(Job job : ioQueue) {
			if(job.inCore && job.ioRequest > 0 && os.jobtable.contains(job)) {
				ioJob = job;
				break;
			}
		}
		if(ioJob == null)
			return;
		
		//ioJob doesn't have any ioRequests
		if(ioJob.ioRequest <= 0 || !ioQueue.contains(ioJob))
			return;
		
		Config.println_debug("[IOManager]: Queue: "+ioQueue.toString());
		if(!Job.isValid(ioJob) || !os.jobtable.contains(ioJob)) {
			ioJob = null;
			return;
		}

		ioJob.latched = true;
		Config.COMPLETED_IO++;
		Config.println_debug("[IOManager]: Processing "+ioJob+" [Blocked ? "+ioJob.blocked+"] [inCore ? "+ioJob.inCore+"]");
		sos.siodisk(ioJob.id);
		working = true;
	}

	/** Count how many job requests are in the IO queue **/
	public int countIORequests(Job job) {
		if(!Job.isValid(job) || !os.jobtable.contains(job))
			return 0;
		return (int) ioQueue.stream().filter(j -> j == job).count();
	}

	/* variables */
	private static IOManager instance = null;
	public final LinkedList<Job> ioQueue;
	public Job ioJob = null;//Job that is doing IO
	public boolean working = false;
}
