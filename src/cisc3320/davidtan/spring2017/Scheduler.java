package cisc3320.davidtan.spring2017;

import java.util.LinkedList;

import cisc3320.davidtan.spring2017.scheduling.SchedulingAlgorithm;

/**
 * Scheduler
 * Sees if there is a process to run
 * 
 * @author David Tan
 */
public class Scheduler {

	/** Constructor **/
	private Scheduler() {
		style = Config.MultilevelFeedbackQueue_Algorithm;
	}

	/** Singleton **/
	public static Scheduler getInstance() {
		return instance;
	}

	/** Decide which job to run next **/
	public void process(int[] a, int[] p) {
		
		cpuJob = style.schedule();

		if(!os.dispatcher.canRunJob(cpuJob)) {
			a[0] = Config.CPU_STATE_IDLE;
			cpuJob = null;
			os.io.process();
			return;
		}

		Config.println_debug("[Scheduler.process.Scheduling Algorithm]: "+Config.SCHEDULE_NAME(style)+" -> Scheduled Job: "+cpuJob);
		os.dispatcher.runJob(cpuJob, a, p);
	}

	/** Cherry pick a job from a queue with the best characteristics **/
	public Job cherryPick(LinkedList<Job> queue, int type) {
		if(queue == null)
			return null;
		
		Job result = null;
		//Loop through a queue
		for(Job job : queue) {
			//Determine if the job can run
			if(os.dispatcher.canRunJob(job) && os.jobtable.contains(job)) {
				if(result == null)
					result = job;
				else {
					switch(type) {
					case Config.JOB_ID://FCFS
						if(job.id < result.id)
							result = job;
						break;
					case Config.JOB_CURRENT_TIME://SRTF
						if(job.getRemainingCPUTime() < result.getRemainingCPUTime())
							result = job;
						break;
					case Config.JOB_PRIORITY://Deal with higher priority
						if(job.Priority() < result.Priority())
							result = job;
						break;
					case Config.JOB_SIZE://Reduce external fragmentation
						if(job.size < result.size)
							result = job;
						break;
					case Config.JOB_MAX_CPU_TIME://SJF
						if(job.MaxCpuTime() < result.MaxCpuTime())
							result = job;
						break;
					}
				}
			}
		}
		return result;
	}
	
	/* variables */
	private static Scheduler instance = new Scheduler();
	public SchedulingAlgorithm style;
	public Job cpuJob = null;//Job that is doing CPU
	
	/* This is used only when time slice mode is enabled */
	public int TIME_SLICE = 200; //Time slice for Round robin scheduling
	public boolean TIME_SLICE_MODE = true;

}
