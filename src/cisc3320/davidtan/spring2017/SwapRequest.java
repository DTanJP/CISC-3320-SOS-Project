package cisc3320.davidtan.spring2017;

/**
 * SwapRequest
 * Contains a job and direction
 * 
 * @author David Tan
 */
public class SwapRequest {

	/** Constructor **/
	public SwapRequest(Job job, int direction) {
		this.job = job;
		this.direction = direction;
	}

	/** Makes this swap request into a block swap request? **/
	public SwapRequest setBlock(boolean block) {
		this.block = block;
		return this;
	}

	/** Make sure this request is valid **/
	public static boolean isValid(SwapRequest request) {
		if(request == null)
			return false;

		if(!Job.isValid(request.job))
			return false;

		if(request.direction > 1 || request.direction < 0)
			return false;

		if(!os.jobtable.contains(request.job))
			return false;

		if(request.job.terminated)
			return false;

		return true;
	}

	/** Check to make sure this operation is possible **/
	public boolean canSwap() {
		if(!isValid(this))
			return false;

		if(job.terminated)
			return false;

		if(job.latched)
			return false;

		if(job == os.schedule.cpuJob && !job.blocked)
			return false;

		if(job.inCore && direction == Config.DIRECTION_IN)
			return false;

		if(!job.inCore && direction == Config.DIRECTION_OUT)
			return false;

		return true;
	}

	/** Normal handling of swapping of jobs **/
	public boolean execute() {
		boolean success = false;

		if(block)
			return handleBlockSwap();

		switch(direction) {
		//Drum -> memory
		case Config.DIRECTION_IN:
			if(os.memory.registerJob(job)) {
				sos.siodrum(job.id, job.size, job.partition().base(), Config.DIRECTION_IN);
				success = true;
			}
			break;
			//Memory -> drum
		case Config.DIRECTION_OUT:
			sos.siodrum(job.id, job.size, job.partition().base(), Config.DIRECTION_OUT);
			os.memory.unregisterJob(job);
			success = true;
			break;
		}
		return success;
	}

	/** Move a block job out of memory **/
	private boolean handleBlockSwap() {
		boolean success = false;
		Config.println_debug("[BLOCK SwapRequest]: "+job);
		switch(direction) {
			//Memory -> drum
		case Config.DIRECTION_OUT:
			if(job.inCore && job.partition() != null) {
				sos.siodrum(job.id, job.size, job.partition().base(), Config.DIRECTION_OUT);
				os.memory.unregisterJob(job);
				job.inCore = false;
				success = true;
			}
			break;
		}
		return success;
	}

	public Job job() {
		return job;
	}

	public int direction() {
		return direction;
	}

	public String toString() {
		return "<"+job.id+", "+(direction == Config.DIRECTION_IN ? "IN" : "OUT") + ">";
	}

	/* variables */
	private Job job = null;
	private int direction = Config.DIRECTION_IN;
	private boolean block = false;//Is this a (former)block job swap?
}
