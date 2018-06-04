package cisc3320.davidtan.spring2017.scheduling;

import java.util.LinkedList;
import java.util.stream.Collectors;

import cisc3320.davidtan.spring2017.Job;
import cisc3320.davidtan.spring2017.os;

/**
 * Multilevel Queue
 * 
 * @author David Tan
 */
public class MultilevelQueue implements SchedulingAlgorithm {

	/** Constructor **/
	private MultilevelQueue(){
		queue0 = new LinkedList<>();
		queue1 = new LinkedList<>();
		queue2 = new LinkedList<>();
		queue3 = new LinkedList<>();
		queue4 = new LinkedList<>();
	}

	/** Singleton **/
	public static MultilevelQueue getInstance() {
		if(instance == null)
			instance = new MultilevelQueue();
		return instance;
	}

	@Override
	public Job schedule() {
		Job result = null;
		populate();
		clear();
		
		if(!queue0.isEmpty()) {
			result = findJob(queue0);
			pushBack(result, queue0);
		}
		if(!queue1.isEmpty() && result == null) {
			result = findJob(queue1);
			pushBack(result, queue1);
		}
		if(!queue2.isEmpty() && result == null) {
			result = findJob(queue2);
			pushBack(result, queue2);
		}
		if(!queue3.isEmpty() && result == null) {
			result = findJob(queue3);
			pushBack(result, queue3);
		}
		if(!queue4.isEmpty() && result == null) {
			result = findJob(queue4);
			pushBack(result, queue4);
		}
		
		os.schedule.TIME_SLICE = 100;
		os.schedule.TIME_SLICE_MODE = true;
		return result;
	}

	/** Searches for a job in a queue that can run on the OS **/
	private Job findJob(LinkedList<Job> queue) {
		Job result = null;
		for(int i=0; i<queue.size(); i++) {
			if(os.dispatcher.canRunJob(queue.get(i))) {
				result = queue.get(i);
				break;
			}
		}
		return result;
	}
	
	/** Finds jobs from jobtable and add to proper queue **/
	private void populate() {
		for(Job job : os.jobtable) {
			if(os.dispatcher.canRunJob(job))
				submit(job);
		}
	}

	/** Add a job to the proper queue **/
	private void submit(Job job) {
		if(!Job.isValid(job))
			return;

		switch(job.Priority()) {
		case 1:
			queue0.add(job);
			break;
		case 2:
			queue1.add(job);
			break;
		case 3:
			queue2.add(job);
			break;
		case 4:
			queue3.add(job);
			break;
		case 5:
			queue4.add(job);
			break;
		}
	}

	/** Removes a job from a queue and push it to the back **/
	private void pushBack(Job job, LinkedList<Job> queue) {
		if(!Job.isValid(job))
			return;
		
		if(!queue.contains(job))
			return;

		queue.remove(job);
		queue.addLast(job);
	}

	/** Remove any unrunnable jobs from the queues **/
	private void clear() {
		queue0.removeAll(queue0.stream().filter(job -> (canRemove(job))).collect(Collectors.toList()));
		queue1.removeAll(queue1.stream().filter(job -> (canRemove(job))).collect(Collectors.toList()));
		queue2.removeAll(queue2.stream().filter(job -> (canRemove(job))).collect(Collectors.toList()));
		queue3.removeAll(queue3.stream().filter(job -> (canRemove(job))).collect(Collectors.toList()));
		queue4.removeAll(queue4.stream().filter(job -> (canRemove(job))).collect(Collectors.toList()));
	}

	/** Determine if a job is able to be removed from scheduling **/
	private boolean canRemove(Job job) {
		if(job == null)
			return true;

		if(Job.isValid(job)) {
			return (!job.inCore
					|| job.terminated 
					|| job.partition() == null
					|| job.getRemainingCPUTime() <= 0
					|| !os.jobtable.contains(job));
		}
		return false;
	}

	/* variables */
	private static MultilevelQueue instance = null;
	private LinkedList<Job> queue0 = null;
	private LinkedList<Job> queue1 = null;
	private LinkedList<Job> queue2 = null;
	private LinkedList<Job> queue3 = null;
	private LinkedList<Job> queue4 = null;
}
