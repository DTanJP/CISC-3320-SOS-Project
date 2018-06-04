package cisc3320.davidtan.spring2017;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import cisc3320.davidtan.spring2017.scheduling.*;
/**
 * Config
 * Contains important constants and global variables
 * 
 * @author David Tan
 */
public class Config {
	/* Constants */
	/* Constants for int[] a */
	public static final int CPU_STATE_IDLE = 1; //No jobs to run, CPU waiting for interrupts: *Ignore p values
	public static final int CPU_STATE_RUN = 2; //CPU set to run mode: p[0,1,5] are ignored. p[2]: base address, p[3]: size, p[4]: time slice
	public static final int CPU_STATE_JOB_TERMINATE_REQUEST = 5; //Job is requesting termination
	public static final int CPU_STATE_DISKIO = 6; //Job requesting another disk IO operation
	public static final int CPU_STATE_JOB_BLOCK_REQUEST = 7; //Job requesting to be blocked until IO requests are fulfilled.

	/* Constants for int[] p */
	public static final int JOB_UNUSED = 0;
	public static final int JOB_ID = 1;
	public static final int JOB_PRIORITY = 2;
	public static final int JOB_SIZE = 3;
	public static final int JOB_MAX_CPU_TIME = 4;
	public static final int JOB_CURRENT_TIME = 5;

	/* Constants for the direction of jobs */
	public static final int DIRECTION_IN = 0;//Drum -> Memory
	public static final int DIRECTION_OUT = 1; //Memory -> Drum

	/* Variables */
	/** DEBUG_MODE: Used to display information only when set to true **/
	public static boolean DEBUG_MODE = false; //Print out debug information?
	public final static boolean RECORD_MODE = false; //Writes output to a .txt file
	public final static boolean CRASH_DEBUG = false; //Enable debug information when OS crash?

	/* CRASH DEBUG VARIABLES */
	public static int LOADED_JOBS = 0; //How many jobs have entered the OS?
	public static int COMPLETED_JOBS = 0;//How many jobs have terminated?
	public static int COMPLETED_IO = 0;//How many IO requests have been processed?

	/* Scheduling Algorithms */

	//Multilevel Feedback Queue: Completes 449/458 jobs.
	public final static MultilevelFeedbackQueue MultilevelFeedbackQueue_Algorithm = MultilevelFeedbackQueue.getInstance();

	//Shortest Job First: Completes 447/458 jobs.
	public final static SchedulingAlgorithm SJF_Algorithm = SJF.getInstance();

	//Round Robin: Completes 439/458 jobs.
	public final static SchedulingAlgorithm RoundRobin_Algorithm = RoundRobin.getInstance();

	//Shortest Remaining Time First: Completes 431/458 jobs.
	public final static SchedulingAlgorithm SRTF_Algorithm = SRTF.getInstance();

	//Priority Scheduling: Completes 441/458 jobs.
	public final static SchedulingAlgorithm Priority_Algorithm = PriorityScheduling.getInstance();

	//Multilevel Queue: Completes 325/375 jobs.
	public final static MultilevelQueue MultilevelQueue_Algorithm = MultilevelQueue.getInstance();

	//First Come First Serve: Completes 159/209 jobs.
	public final static SchedulingAlgorithm FCFS_Algorithm = FCFS.getInstance();

	/* Methods */
	/** When DEBUG_MODE is enabled, prints out debug information **/
	public static void print_debug(Object line) {
		if(DEBUG_MODE) {
			System.out.print(line.toString());

			if(RECORD_MODE)
				record(line);
		}
	}

	/** When DEBUG_MODE is enabled, prints out debug information **/
	public static void println_debug(Object line) {
		if(DEBUG_MODE) {
			System.out.println(line.toString());

			if(RECORD_MODE)
				recordln(line);
		}
	}

	/** Prints under every handler call **/
	public static void separator() {
		Config.println_debug("========================================================");
	}

	public static String SCHEDULE_NAME(SchedulingAlgorithm style) {
		if(style == null)
			return "NULL";
		if(style == Config.FCFS_Algorithm)
			return "First Come First Serve";
		else if(style == Config.SJF_Algorithm)
			return "Shortest Job First";
		else if(style == Config.SRTF_Algorithm)
			return "Shortest Remaining Time First";
		else if(style == Config.RoundRobin_Algorithm)
			return "Round Robin";
		else if(style == Config.MultilevelFeedbackQueue_Algorithm)
			return "Multilevel Feedback Queue";
		else if(style == Config.MultilevelQueue_Algorithm)
			return "Multilevel Queue";
		else if(style == Config.Priority_Algorithm)
			return "Priority";
		return "Unknown";
	}

	/** Appends to OUTPUT.txt */
	private static void record(Object line) {
		//Appends to text file. Does not start new line
		try(BufferedWriter bw = new BufferedWriter(new FileWriter(new File("./"+SCHEDULE_NAME(os.schedule.style)+".txt"), true))) {
			bw.write(line.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void recordln(Object line) {
		//Appends to text file. Does not start new line
		try(BufferedWriter bw = new BufferedWriter(new FileWriter(new File("./"+SCHEDULE_NAME(os.schedule.style)+".txt"), true))) {
			bw.write(line.toString());
			bw.newLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
