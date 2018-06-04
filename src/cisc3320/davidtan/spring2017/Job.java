package cisc3320.davidtan.spring2017;

/**
 * Job
 * Represents a Job object
 * Holds important information about the job
 * 
 * @author David Tan
 */
public class Job {

	/** Constructor **/
	public Job(int id, int priority, int size, int max_time, int current_time) {
		this.id = id;
		this.priority = priority;
		this.size = size;
		this.maxCpuTime = max_time;
		this.currentTime = current_time;
		this.initialTime = current_time;
	}

	/** Creates an new instance of job depending on p[] **/
	public static Job createJob(int p[]) {
		return new Job(p[Config.JOB_ID],p[Config.JOB_PRIORITY],p[Config.JOB_SIZE],p[Config.JOB_MAX_CPU_TIME],p[Config.JOB_CURRENT_TIME]);
	}

	/** Determine if a job is valid for processing **/
	public static boolean isValid(Job job) {
		return (job != null && job.id > 0);
	}

	public void updateTime(int currentTime) {
		this.currentTime = currentTime;
	}

	/** Sets the memory for this job **/
	public void setPartition(Partition memory) {
		//Make sure the memory space fits the job and is not in use
		if(memory != null) {
			if(memory.allocated || memory.length() < size) {
				Config.println_debug("[Job.setPartition]: Allocated: "+memory.allocated+"\t"+memory.length()+"/"+size);
				return;
			}
		}

		this.memory = memory;
	}

	/** Decide if this job is fit to be blocked: Decide if this job should move out **/
	public boolean setBlock() {
		blocked = (ioRequest > 0 || latched ? true : false);

		//Blocked + Not doing IO + Existing block jobs in OS => Swap this out
		if(blocked && !latched && os.io.getTotalBlocked() > 0)
			os.swapper.unregisterJob(this);
		
		return blocked;
	}

	//Return the partition associated with this job
	public Partition partition() {
		return memory;
	}

	public int Priority() {
		return priority;
	}

	public int MaxCpuTime() {
		return maxCpuTime;
	}

	public int CurrentTime() {
		return currentTime;
	}

	public int getRemainingCPUTime() {
		return maxCpuTime - (currentTime - initialTime);
	}

	@Override
	public String toString() {
		return "[JOB "+id+"]";
	}

	/* variables */
	public final int id;
	private int priority;
	public final int size;
	private int maxCpuTime;
	private int currentTime;
	public final int initialTime;
	public int ioRequest = 0;

	public boolean blocked = false;
	public boolean terminated = false;
	public boolean latched = false;
	public boolean inCore = false;

	private Partition memory;
}
