package cisc3320.davidtan.spring2017;
/**
 * Partition
 * Represents a memory object
 * 
 * @author David Tan
 */
public class Partition {

	/** Constructor **/
	private Partition(int base, int length) {
		this.base = base;
		this.length = length;
	}

	/* Methods */
	/** Create a new partition instance **/
	public static Partition newInstance(int base, int length) {
		if(base > -1 && length > 0 && (base + length - 1) < 100)
			return new Partition(base, length);
		return null;
	}

	public void setBase(int base) {
		this.base = base;
	}

	/** The length cannot be greater than 100 and the tail cannot go beyond 99 **/
	public void setLength(int length) {
		this.length = (length <= 100  && (base + length - 1) <= 100 ? length : 100 - base);
		//base: 10 :: length: 30 <= 100 && 39(Tail) < 100 -> length: 30
		//base: 90 :: length: 15 <= 100 && 114(Tail) > 100 -> length: 100 - 90 -> length: 10
	}

	public int base() {
		return base;
	}

	public int tail() {
		return base + (length - 1);
	}

	public int length() {
		return length;
	}

	@Override
	public String toString() {
		return "["+base+", "+tail()+": "+length+"]";
	}

	/* variables */
	public boolean allocated = false;
	private int base = -1;
	private int length = 0;
}
