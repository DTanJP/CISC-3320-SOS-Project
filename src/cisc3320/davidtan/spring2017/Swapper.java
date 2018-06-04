package cisc3320.davidtan.spring2017;

import java.util.LinkedList;

/**
 * Swapper
 * Queues sos.siodrum() requests
 * 
 * @author David Tan
 */
public class Swapper {

	/** Constructor **/
	private Swapper() {
		swapQueue = new LinkedList<>();
	}

	/** Singleton **/
	public static Swapper getInstance() {
		return instance;
	}

	/** Move a job into a queue waiting to be added to core **/
	public void registerJob(Job job) {
		if(!Job.isValid(job))
			return;

		SwapRequest request = new SwapRequest(job, Config.DIRECTION_IN);
		if(SwapRequest.isValid(request))
			swapQueue.add(request);
	}

	/** Move a job into a queue waiting to be removed from core **/
	public void unregisterJob(Job job) {
		if(!Job.isValid(job))
			return;

		//SwapRequest request = new SwapRequest(job, Config.DIRECTION_OUT);
		SwapRequest request = new SwapRequest(job, Config.DIRECTION_OUT).setBlock(job.blocked);
		if(SwapRequest.isValid(request))
			swapQueue.add(request);
	}

	/** Process jobs in the swapQueue **/
	public void process() {
		//Wait until a drum operation is done
		if(working)
			return;

		//Find jobs that waiting to be registered: Long term scheduling
		Job memoryJob = os.memory.findWaitingJob();
		if(Job.isValid(memoryJob) && !memoryJob.terminated)
			registerJob(memoryJob);
		
		if(swapQueue.isEmpty())
			return;

		//Find a valid job ready and waiting to be swapped into memory
		for(SwapRequest request : swapQueue) {
			if(SwapRequest.isValid(request)) {
				if(request.canSwap()) {
					swapOperation = request;
					break;
				}
			}
		}

		//Remove this request
		swapQueue.removeFirstOccurrence(swapOperation);

		//Print out information
		Config.println_debug("[Swapper.process]: [JOB "+swapOperation.job().id
				+"] Partition: "+swapOperation.job().partition()+" [DRUM"
				+(swapOperation.direction() == Config.DIRECTION_IN ? " => " : " <= ")
				+"Memory]");

		//Get the status of swap operation
		working = swapOperation.execute();
	}

	/* variables */
	private static Swapper instance = new Swapper(); //Singleton instance
	public LinkedList<SwapRequest> swapQueue = null; //Queue for swapping jobs
	public SwapRequest swapOperation = null;
	public boolean working = false;
}
