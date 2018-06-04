package cisc3320.davidtan.spring2017;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MemoryManager
 * Manages memory space
 * 
 * Only one instance can be created
 * 
 * @author David Tan
 */
public class MemoryManager {

	/** Constructor **/
	private MemoryManager() {
		//initialization
		memorySpace = new LinkedList<>();
		memorySpace.add(Partition.newInstance(0, 100));
	}

	/** Singleton **/
	public static MemoryManager getInstance() {
		return instance;
	}

	/** Searches for available space and returns the address that first fits the job **/
	private int findFirstFitSize(int job_size) {
		//Loop through all of the partitions
		for(Partition index : memorySpace) {
			if(index.length() >= job_size && !index.allocated)
				return index.base();
		}
		return -1;
	}

	/** Creates a partition for the job returns true if it is successful **/
	public boolean registerJob(Job job) {
		//Job exists
		if(!Job.isValid(job) || !os.jobtable.contains(job))
			return false;

		//Job already has a partition
		if(job.partition() != null && job.partition().length() == job.size)
			return false;

		//Make sure all partitions are optimized
		optimizePartitions();

		int baseAddress = findFirstFitSize(job.size);

		//Can't find any space to fit this
		if(baseAddress == -1) {
			Config.println_debug("[MemoryManager.register]: Cannot find base address.");
			return false;
		}

		//The partition this job will be moved inside of
		Partition memory = partitionIndex(findFirstFitSize(job.size));

		//If the job's size takes up the partition size
		if(job.size == memory.length()) {
			job.setPartition(memory);
			memory.allocated = true;
			return true;
		}

		//Create a new partition
		Partition process = Partition.newInstance(baseAddress, job.size);

		//Error: Partition is out of bounds
		if(process == null || memory == null) {
			Config.println_debug("[MemoryManager.register]: Null partition.");
			return false;
		}

		//Set the partition for the job
		job.setPartition(process);

		//Partition is occupied by this job
		process.allocated = true;

		//Adjust the memory
		memory.setBase(memory.base() + process.length());
		memory.setLength(memory.length() - process.length());

		//Add the job process into memory
		memorySpace.add(process);
		if(Config.DEBUG_MODE)
			display();
		return true;
	}

	/** Removes the partition associated with the job not the job itself**/
	public void unregisterJob(Job job) {
		if(!Job.isValid(job) && !job.blocked && os.io.countIORequests(job) > 0) {
			Config.println_debug("[MemoryManager.unregister]: Fail to unregister partition for "+job);
			return;
		}

		if(job.partition() == null) {
			Config.println_debug("[MemoryManager.unregister]: Null partition");
			return;
		}

		Config.println_debug("[MemoryManager.unregisterJob]: "+job);
		//Free the partition up
		job.partition().allocated = false;
		job.setPartition(null);

		display();
		optimizePartitions();
	}

	/** Some jobs are removed, but their partitions still might be allocated **/
	public void refresh() {
		//Reset all partitions
		memorySpace.stream().forEach(partition -> {
			if(partition != null)
				partition.allocated = false;
		});

		//Active jobs keep their partitions
		os.jobtable.stream().forEach(job -> {
			if(Job.isValid(job)) {
				if(job.partition() != null)
					job.partition().allocated = true;
			}
		});

		//Any partition marked unallocated are removed and merged
		optimizePartitions();
	}

	/** Finds available memory space for a job **/
	public boolean foundSpace(Job job) {
		if(!Job.isValid(job))
			return false;

		//Job is already in memory
		if(job.inCore && job.partition() != null)
			return false;

		return findFirstFitSize(job.size) != -1;
	}

	/** Cherry pick the job with the best characteristics that fits the conditions entering memory **/
	public Job findWaitingJob() {
		Job result = null;
		for(Job job : os.jobtable) {
			if(Job.isValid(job)) {
				if(job.partition() == null && !job.terminated && !job.inCore && foundSpace(job)) {
					//Blocked jobs + Existing incore blocked jobs + Certain algorithms => Better performance
					if(job.blocked && os.io.getTotalBlocked() > 0
							&& (os.schedule.style == Config.MultilevelFeedbackQueue_Algorithm
							|| os.schedule.style == Config.SJF_Algorithm
							|| os.schedule.style == Config.Priority_Algorithm
							|| os.schedule.style == Config.FCFS_Algorithm))
						continue;
					
					if(result == null)
						result = job;
					else {
						//Cherry pick the best characteristics for each scheduling algorithm
						switch(Config.SCHEDULE_NAME(os.schedule.style)) {
						case "Multilevel Feedback Queue":
							if(job.MaxCpuTime() < result.MaxCpuTime())
								result = job;
							break;
						case "Round Robin":
						case "Multilevel Queue":
							if(job.Priority() < result.Priority())
								result = job;
							break;
						case "Shortest Remaining Time First":
							if(job.size < result.size)
								result = job;
							break;
						case "Shortest Job First":
							if(job.getRemainingCPUTime() < result.getRemainingCPUTime())
								result = job;
							break;
						case "Priority":
							if(job.Priority() < result.Priority())
								result = job;
							break;
						default:
							if(job.Priority() < result.Priority())
								result = job;
							break;
						}
					}
				}
			}
		}
		return result;
	}

	/** Find a partition by the baseAddress **/
	public Partition partitionIndex(int baseAddress) {
		Partition result = memorySpace.stream()
				//Find a valid partition by address
				.filter(p -> p!= null && p.base() <= baseAddress && p.tail() >= baseAddress)
				.findFirst().get();
		return result;
	}

	/** 
	 * When 2+ contiguous partitions are freed, then merge them together
	 * Start:  (0,18 : 19)(19,24 : 6)[25,29 : 5](30,99 : 70)
	 * Result: (0,24 : 25)[25,29 : 5](30,99 : 70)
	 **/
	public void optimizePartitions() {
		//MemorySpace has only 1 or fewer partition
		if(memorySpace == null || memorySpace.size() == 1)
			return;

		//Put the partitions in order
		organizeMemorySpace();

		//Collect all the free partitions and store them into a list
		List<Partition> partition = memorySpace.stream().filter(p -> p!= null && !p.allocated).collect(Collectors.toList());

		Partition current = null;
		Partition next = null;
		//Start merge process
		for(int index=0; index < partition.size(); index++) {
			current = partition.get(index);

			if(current.length() <= 0)
				continue;

			//Check the next partitions and see if they're contiguous
			for(int x=index+1; x<partition.size(); x++) {
				//Check the next partition
				next = partition.get(x);

				//If the next partition is not contiguous to the current partition
				if(current.tail() + 1 != next.base())
					break;

				//The next partition's size is invalid
				if(next.length() <= 0)
					continue;

				//Update the current partition
				current.setLength(current.length() + next.length());
				next.setLength(0);
			}
		}
		//Remove partitions that were merged
		memorySpace.removeAll(partition.stream().filter(p -> p.length() == 0).collect(Collectors.toList()));
	}

	/** Orders the partitions from smallest base register to largest base register **/
	private void organizeMemorySpace() {
		if(memorySpace.size() < 2 || memorySpace == null)
			return;

		int currentTail = 0;
		int currentBase = 0;
		List<Partition> memory = new LinkedList<>();
		for(int iteration=0; iteration <memorySpace.size(); iteration++) {
			if(currentTail == 99)
				break;

			//Finds the partition with the specified base
			Partition partition = partitionIndex(currentBase);

			//Invalid partition
			if(partition == null)
				continue;

			//If this partition doesn't exist add it to the list
			if(!memory.contains(partition))
				memory.add(partition);

			currentTail = partition.tail();
			currentBase = currentTail + 1;
		}
		//Update memory space only if it is valid
		if(memory != null && memory.size() == memorySpace.size())
			memorySpace = memory;
	}

	/** Display the memory space of free/occupied partitions in a linear way **/
	public void display() {
		optimizePartitions();
		Config.println_debug("\n\t\t[FREE MEMORY SPACE TABLE: "+memorySpace.size()+" partitions] Format: <base,tail : length>(Free)[Occupied]");
		for(Partition partition : memorySpace) {
			if(partition != null) {
				if(partition.allocated)
					Config.print_debug("["+ partition.base()+", "+(partition.tail())+ " : " + partition.length() +"]");//Partition is occupied []
				else
					Config.print_debug("("+ partition.base()+", "+(partition.tail())+ " : " + partition.length() +")");//Partition is free ()
			}
		}
		Config.println_debug("");
	}

	/** Calculates how much space is free in memory **/
	public int calculateFreeSpace() {
		int freePercentage = 0;
		for(Partition partition : memorySpace) {
			if(partition != null) {
				if(!partition.allocated)
					freePercentage += partition.length();
			}
		}
		return freePercentage;
	}

	/** Calculates how much space is used in memory **/
	public int calculateUsedSpace() {
		int usedPercentage = 0;
		for(Partition partition : memorySpace) {
			if(partition != null) {
				if(partition.allocated)
					usedPercentage += partition.length();
			}
		}
		return usedPercentage;
	}

	/** Display free space in a form of a table **/
	public void freeSpaceTable() {
		Config.separator();
		Config.println_debug("==============[FREE SPACE TABLE "+calculateFreeSpace()+"%-"+calculateUsedSpace()+"% ]===============");
		Config.println_debug("Base Address    |    Tail[Length]     |     Job occupied");
		Config.separator();
		organizeMemorySpace();
		for(int i = 0; i<memorySpace.size(); i++) {
			if(memorySpace.get(i) != null) {
				Config.println_debug("||\t"+memorySpace.get(i).base()
						+"\t\t"+memorySpace.get(i).tail()
						+"["+memorySpace.get(i).length()+"]"
						+"\t\t\t"+memorySpace.get(i).allocated);
			}
		}
		Config.println_debug("=========================[END]==========================");
		Config.separator();
	}

	/* variables */
	private List<Partition> memorySpace;
	private static MemoryManager instance = new MemoryManager();
}
