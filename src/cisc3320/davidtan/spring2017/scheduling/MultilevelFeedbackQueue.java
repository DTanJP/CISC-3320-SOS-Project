package cisc3320.davidtan.spring2017.scheduling;

import java.util.LinkedList;
import java.util.stream.Collectors;

import cisc3320.davidtan.spring2017.Config;
import cisc3320.davidtan.spring2017.Job;
import cisc3320.davidtan.spring2017.os;
/**
 * Multilevel Queue
 * Uses 3 Queues
 * NOTE: So far the most effective scheduler, but a lot of overhead
 * NOTE: Lower the job's priority regardless whether or not it uses up its timeslice -> Most effective so far
 * 
 * @author David Tan
 */
public class MultilevelFeedbackQueue implements SchedulingAlgorithm {

	/** Constructor **/
	private MultilevelFeedbackQueue() {
		queue0 = new LinkedList<>();
		queue1 = new LinkedList<>();
		queue2 = new LinkedList<>();
	}

	/** Singleton **/
	public static MultilevelFeedbackQueue getInstance() {
		if(instance == null)
			instance = new MultilevelFeedbackQueue();
		return instance;
	}

	@Override
	public Job schedule() {
		clearQueues();
		Job result = null;

		//Note: TIME_SLICE: 63 is most optimal for queue0
		if(!queue0.isEmpty()) {
			result = os.schedule.cherryPick(queue0, Config.JOB_MAX_CPU_TIME);
			lowerPriority(result, queue0, queue1);
			os.schedule.TIME_SLICE = 60;
			os.schedule.TIME_SLICE_MODE = true;
		}
		//Note: TIME_SLICE: 150 is most optimal for queue1
		if(!queue1.isEmpty() && result == null) {
			result = os.schedule.cherryPick(queue1, Config.JOB_MAX_CPU_TIME);
			lowerPriority(result, queue1, queue2);
			os.schedule.TIME_SLICE = 150;
			os.schedule.TIME_SLICE_MODE = true;
		}
		if(!queue2.isEmpty() && result == null) {
			result = os.schedule.cherryPick(queue2, Config.JOB_MAX_CPU_TIME);
			os.schedule.TIME_SLICE_MODE = false;
		}
		return result;
	}

	/** Adds a job to be scheduled to be run **/
	public void submit(Job job) {
		if(!Job.isValid(job))
			return;

		//Check to make sure the job is not already in queue
		if(queue0.contains(job) && queue1.contains(job) && queue2.contains(job))
			return;

		queue0.addFirst(job);
	}

	/** Clear the queues of dead jobs **/
	public void clearQueues() {
		queue0.removeAll(queue0.stream().filter(job -> (canRemove(job))).collect(Collectors.toList()));
		queue1.removeAll(queue1.stream().filter(job -> (canRemove(job))).collect(Collectors.toList()));
		queue2.removeAll(queue2.stream().filter(job -> (canRemove(job))).collect(Collectors.toList()));
	}

	/** Determine if a job is able to be removed from scheduling **/
	private boolean canRemove(Job job) {
		if(Job.isValid(job)) {
			return (!job.inCore
					|| job.terminated 
					|| job.partition() == null
					|| job.getRemainingCPUTime() <= 0
					|| !os.jobtable.contains(job));
		}
		return false;
	}

	/** Changes the job's queue **/
	private void lowerPriority(Job job, LinkedList<Job> queueT, LinkedList<Job> queueL) {
		if(job == null)
			return;

		if(queueT == queue2 || queueT == queueL)
			return;

		//If Top Queue contains the job then move it to the bottom
		if(queueT.contains(job))
			queueL.addLast(job);
		queueT.remove(job);
	}
	
	/* variables */
	private static MultilevelFeedbackQueue instance = null;
	private LinkedList<Job> queue0 = null;
	private LinkedList<Job> queue1 = null;
	private LinkedList<Job> queue2 = null;
}
