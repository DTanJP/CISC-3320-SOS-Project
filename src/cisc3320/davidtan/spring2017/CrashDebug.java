package cisc3320.davidtan.spring2017;
/**
 * CrashDebug
 * Once the program ends/crashes
 * Display information
 * 
 * @author David Tan
 */
public class CrashDebug extends Thread {

	/** Constructor **/
	private CrashDebug() {
	}
	
	/** Singleton **/
	public static CrashDebug getInstance() {
		if(instance == null)
			instance = new CrashDebug();
		return instance;
	}
	
	@Override
	public void run() {
		Config.DEBUG_MODE = true;
		Config.separator();
		Config.println_debug("=======[CRASH DEBUG]=======");
		displayJobs();
		Config.separator();
		Config.println_debug("[CPU JOB]: "+os.schedule.cpuJob);
		Config.println_debug("[IO JOB]: "+os.io.ioJob);
		Config.println_debug("[SWAP JOB]: "+os.swapper.swapOperation.job());
		Config.println_debug("LOADED IN: "+Config.LOADED_JOBS+" jobs.");
		Config.println_debug("COMPLETED: "+Config.COMPLETED_JOBS+" jobs.");
		Config.println_debug("COMPLETED: "+Config.COMPLETED_IO+" IO requests.");
		Config.println_debug("[Scheduling Algorithm]: "+Config.SCHEDULE_NAME(os.schedule.style));
		os.memory.display();
		os.memory.freeSpaceTable();
	}
	
	/** Display the jobtable **/
	private void displayJobs() {
		Config.println_debug("[JOBS]: "+os.jobtable.size());
		Config.println_debug("[IOJOBS]: "+os.io.ioQueue.size());
		for(int i = 0; i<os.jobtable.size(); i++) {
			Job job = os.jobtable.get(i);
			Config.print_debug(job.inCore ? "[" : "(");
			Config.print_debug(job.id+": "+job.partition());
			Config.print_debug(job.inCore ? "] " : ") ");
			Config.print_debug(job.ioRequest > 0 ? "ioRequest: "+job.ioRequest : "");
			Config.print_debug(" "+(job.inCore ? "inCore" : ""));
			Config.print_debug(" "+(job.latched ? "latched" : ""));
			Config.print_debug(" "+(job.terminated ? "TERMINATED" : ""));
			Config.println_debug("");
		}
		Config.println_debug("");
	}
	
	/* variables */
	private static CrashDebug instance = null;
}
