package cisc3320.davidtan.spring2017;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * CISC 3320
 * Operating Systems project
 * os class
 * 
 * @author David Tan
 */
public class os {

	/** New job enters the system **/
	public static void Crint(int[] a, int[] p) {
		Config.separator();
		Config.println_debug("=======[CRINT]=======");
		Config.println_debug("New Job["+p[Config.JOB_ID]+"] entering.");

		BookKeep(p[Config.JOB_CURRENT_TIME]);
		
		Job newJob = Job.createJob(p);
		Config.LOADED_JOBS++;

		if(!jobtable.contains(newJob))
			jobtable.add(newJob);	//Register job to OS jobtable

		clearJobTable();
		//Register job to swapper if there is sufficient memory
		if(memory.foundSpace(newJob))
			swapper.registerJob(newJob);
		else
			Config.println_debug("[Crint]: Unable to register "+newJob+" to memory");

		swapper.process();
		schedule.process(a, p);
		memory.display();
	}

	/** Drum operation finishes operation **/
	public static void Drmint(int[] a, int[] p) {
		Config.separator();
		Config.println_debug("=======[DRUM INTERRUPT]=======");
		BookKeep(p[Config.JOB_CURRENT_TIME]);
		swapper.working = false;

		Job swapJob = swapper.swapOperation.job();
		if(Job.isValid(swapJob)) {
			swapJob.inCore = (swapper.swapOperation.direction() == Config.DIRECTION_IN);
			
			if(schedule.style == Config.MultilevelFeedbackQueue_Algorithm)
				Config.MultilevelFeedbackQueue_Algorithm.submit(swapJob);
		}
		
		//Find a job waiting to move to memory
		Job waitingJob = memory.findWaitingJob();
		if(Job.isValid(waitingJob))
			swapper.registerJob(waitingJob);
		
		swapper.process();
		schedule.process(a, p);
	}

	/** IO request finish **/
	public static void Dskint(int[] a, int[] p) {
		Config.separator();
		Config.println_debug("=======[DISK INTERRUPT]=======");

		BookKeep(p[Config.JOB_CURRENT_TIME]);

		if(Job.isValid(io.ioJob)) {
			io.working = false;
			io.unregisterJob(io.ioJob);
		}

		swapper.process();
		schedule.process(a, p);
	}

	/** Job time runs out **/
	public static void Tro(int[] a, int[] p) {
		Config.separator();
		Config.println_debug("=======[TIMER RUN OUT]=======");

		BookKeep(p[Config.JOB_CURRENT_TIME]);

		if(Job.isValid(schedule.cpuJob) && schedule.cpuJob.getRemainingCPUTime() == 0) {
			Config.COMPLETED_JOBS++;
			
			if(schedule.cpuJob.ioRequest > 0) {
				schedule.cpuJob.terminated = true;
				swapper.process();
				schedule.process(a, p);
				return;
			} else 
				terminateJob(schedule.cpuJob);
		}
		
		clearJobTable();
		swapper.process();
		schedule.process(a, p);
	}

	/** Job requests service **/
	public static void Svc(int[] a, int[] p) {
		Config.separator();
		Config.println_debug("=======[SERVICE CALL]=======");

		BookKeep(p[Config.JOB_CURRENT_TIME]);
		Job svcJob = schedule.cpuJob;

		switch(a[0]) {
		//Terminate request
		case Config.CPU_STATE_JOB_TERMINATE_REQUEST:
			Config.COMPLETED_JOBS++;
			Config.println_debug("[TERMINATING]: Job "+svcJob);
			terminateJob(svcJob);
			break;
			//IO Request
		case Config.CPU_STATE_DISKIO:
			Config.println_debug("[DISK IO]: Job "+svcJob);
			io.registerJob(svcJob);
			break;
			//Block Request
		case Config.CPU_STATE_JOB_BLOCK_REQUEST:
			Config.println_debug("[BLOCK REQUEST]: Job "+svcJob+" -> "+svcJob.setBlock());
			break;
		}
		swapper.process();
		schedule.process(a, p);
	}

	/** Start up **/
	public static void startup() {
		System.out.println("[OS]: Starting up");
		
		//Enable debug info display?
		if(Config.DEBUG_MODE) {
			sos.ontrace();

			//If Enable debug info + Enable recording?
			if(Config.RECORD_MODE) {
				//Clear the output file of previous sessions
				try(BufferedWriter bw = new BufferedWriter(new FileWriter(new File("./"+Config.SCHEDULE_NAME(schedule.style)+".txt")))) {
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else
			sos.offtrace();

		//Print out info that might be crucial for debugging
		if(Config.CRASH_DEBUG)
			Runtime.getRuntime().addShutdownHook(CrashDebug.getInstance());

		//Initializing important components
		jobtable = new LinkedList<>();
		memory = MemoryManager.getInstance();
		schedule = Scheduler.getInstance();
		swapper = Swapper.getInstance();
		dispatcher = Dispatcher.getInstance();
		io = IOManager.getInstance();
	}

	/** Updates the System Time **/
	private static void BookKeep(int currentTime) {
		previousSystemTime = currentSystemTime;
		currentSystemTime = currentTime;

		if(Job.isValid(schedule.cpuJob) && !schedule.cpuJob.blocked) {
			int deltaTime = currentSystemTime - previousSystemTime;
			schedule.cpuJob.updateTime(schedule.cpuJob.CurrentTime() + deltaTime);
		}
	}

	/** Terminates a job **/
	public static void terminateJob(Job job) {
		if(!Job.isValid(job))
			return;

		//Still have IO requests to do
		if(io.ioQueue.contains(job)) {
			job.terminated = true;
			return;
		}

		memory.unregisterJob(job);
		jobtable.remove(job);

		if(schedule.cpuJob == job)
			schedule.cpuJob = null;

		if(io.ioJob == job)
			io.ioJob = null;

		Config.println_debug("[OS]: Terminating "+job);
	}

	/** Removes all jobs that are terminated **/
	public static void clearJobTable() {
		jobtable.removeAll(jobtable
				.stream()
				.filter(job -> job != null && job.terminated && job.ioRequest == 0)
				.collect(Collectors.toList()));
		memory.refresh();
	}

	/* variables */
	public static List<Job> jobtable;

	public static MemoryManager memory;
	public static Scheduler schedule;
	public static Swapper swapper;
	public static Dispatcher dispatcher;
	public static IOManager io;

	private static int currentSystemTime = 0;
	private static int previousSystemTime = 0;
}
