import java.util.Scanner;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ZeroOneMultiKnapSack {
 	private static final int BYTE_MASK = 0xFFFFFFF8;

	/**
	 * Class used as the key to keep track of the maximum value that can be obtained with given sequence of
	 * available weights (of items) and the mapping of which items to be kept into which buckets/sacks
	 */
	private static class MaxWeightForIndexKey {
		int[] weights;
		int idx;

		MaxWeightForIndexKey(int[] weights, int idx) {
			this.weights = weights;
			this.idx = idx;
		}
	}

	/**
	 * Class used as the value to keep track of the maximum value that can be obtained with given sequence of
	 * available weights (of items) and the mapping of which items to be kept into which buckets/sacks
	 */
	private static class MaxWeightForIndexValue {
		Map<Integer, List<Integer>> selectedItemsPerBucketMap;
		int maxValue;

		MaxWeightForIndexValue(Map<Integer, List<Integer>> selectedItemsPerBucketMap, int maxValue) {
			this.selectedItemsPerBucketMap = selectedItemsPerBucketMap;
			this.maxValue = maxValue;
		}
	}

	/**
	 * Checks if the input array of integers is sorted in ascending order
	 * @param	arr	Input array of integers
	 * @return	true if the array is sorted in ascending order, false otherwise
	 */
	public static boolean isArraySorted(int[] arr) {
		boolean isSorted = true;
		int prev = arr[0];
		for (int idx = 1; idx < arr.length; idx++) {
			if (arr[idx] < prev) {
				isSorted = false;
				break;
			}
			prev = arr[idx];
		}
		return isSorted;
	}

	/**
	 * @param	arr	Input array of integers
	 * @param	val	value to be compared with each element of the array
	 * @return	true if val is smaller than _any_ of the array elements, false otherwise
	 */
	public static boolean isSmallerThanOrEqualToAnyOne(int[] arr, int val) {
		for (int idx = 0; idx < arr.length; idx++) {
			if (val <= arr[idx]) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Subtracts the given value from the first element of the input array starting from the given index that is at least as 
	 * big as the value. If all elements in the array starting from the given index are smaller than the given value, then no
	 * change is made.
	 *
	 * @param	arr	Input array of integers
	 * @param	weight	weight to be subtracted from the first larger-or-equal element
	 * @param	startIndex	Index in 'arr' from which to look for available weight
	 * @return	index of the element where weight was subtracted from if the subtraction happened, 
	 *			length of the array otherwise
	 */
	public static int subtractGivenWeightFromRemainingWeights(int[] arr, int weight, int startIndex) {
		for (int idx = startIndex; idx < arr.length; idx++) {
			if (weight <= arr[idx]) {
				arr[idx] -= weight;
				return idx;
			}
		}
		return arr.length;
	}

	/**
	 * Returns the maximum value that can be obtained by picking up items from the _remaining_ subarray, subject
	 * to the remaining weights available.
	 *
	 * @param	itemWeights		List of weights of items from which a subset is to be picked up to maximize the value
	 * @param	remainingBucketWeights	List of remaining weights available in the buckets/knapsacks
	 * @param	idx		Starting index of the sub-array for which maximum value is to be identified
	 * @param	values		List of values of items from which a subset is to be picked up to maximize the value
	 * @param	maxVals		A map of <list of remaining weights, values>. Used as memoization to avoid repeating the same sub-problem
	 * @return	An object containing the maximum value that can be obtained by placing the items from the subarray starting 'idx'
	 *			in the buckets/sacks with remaining weights, AND the mapping from the buckets/sacks to the list of selected items
	 */
	public static MaxWeightForIndexValue maxVal(int[] itemWeights, int[] remainingBucketWeights, int idx, int[] values, 
			Map<MaxWeightForIndexKey, MaxWeightForIndexValue> maxVals, boolean debug) {
		if (idx == itemWeights.length) {
			return null;
		}

		// For all the permutations of a given combination of remainingBucketWeights, and the given index into the array of items,
		// the max values are going to be the same.
		//
		// e.g. the max value with remaining weights of <5, 50> with index 0, and remaining weights of <50, 5> with index 0
		// are going to be the same. To avoid repeatedly solving the same sub-problem (with different permutations of the same
		// combination), memoize/query with only the combination with non-descending order of remaining weights (i.e. <5, 50> here)
		int[] tmpRemainingBuckets = remainingBucketWeights;
		if (!isArraySorted(remainingBucketWeights)) {
			tmpRemainingBuckets = new int[remainingBucketWeights.length];
			System.arraycopy(remainingBucketWeights, 0, tmpRemainingBuckets, 0, remainingBucketWeights.length);
			Arrays.sort(tmpRemainingBuckets);
		}

		MaxWeightForIndexKey maxWeightForIndexKey = new MaxWeightForIndexKey(tmpRemainingBuckets, idx);
		if (maxVals.containsKey(maxWeightForIndexKey)) {
			return maxVals.get(maxWeightForIndexKey);
		}

		int candidateValueWithCurrent = 0;
		int maxCandidateValueWithCurrent = 0;
		int modifiedIdx = -1;
		MaxWeightForIndexValue maxWeightForIndexValue = null;

		// A list of maps of <bucket Id, list of item indexes selected> where each outer list corresponds to 
		// each of the buckets having sufficient weight to accommodate the current item
		Map<Integer, List<Integer>> selectedItemsPerBucketMapWithOutCurrent = new HashMap<Integer, List<Integer>>();
		Map<Integer, List<Integer>> selectedItemsPerBucketMapWithCurrentHavingMaxValue = null;
		if (debug) {
			System.out.println("idx: " + idx + ", value:"+ itemWeights[idx]);
		}

		// Current element can be selected only if at least one bucket has remaining weight at least as big as the current
		// element's weight
		if (isSmallerThanOrEqualToAnyOne(remainingBucketWeights, itemWeights[idx])) {
			while (modifiedIdx < remainingBucketWeights.length) {
				modifiedIdx = subtractGivenWeightFromRemainingWeights(remainingBucketWeights, itemWeights[idx], modifiedIdx + 1);
				if (modifiedIdx == remainingBucketWeights.length) {
					break;
				}

				MaxWeightForIndexValue value = maxVal(itemWeights, remainingBucketWeights, idx + 1, values, maxVals, debug);
				candidateValueWithCurrent = values[idx] + (value == null ? 0 : value.maxValue);
				Map<Integer, List<Integer>> selectedItemsPerBucketMapWithCurrent = new HashMap<Integer, List<Integer>>();

				if (value != null) {
					selectedItemsPerBucketMapWithCurrent.putAll(value.selectedItemsPerBucketMap);
				}
				List<Integer> listOfItemsInCurrentBucket = selectedItemsPerBucketMapWithCurrent.get(modifiedIdx);
				if (listOfItemsInCurrentBucket == null) {
					listOfItemsInCurrentBucket = new ArrayList<Integer>();
					selectedItemsPerBucketMapWithCurrent.put(modifiedIdx, listOfItemsInCurrentBucket);
				}
				listOfItemsInCurrentBucket.add(idx);

				if (candidateValueWithCurrent > maxCandidateValueWithCurrent) {
					maxCandidateValueWithCurrent = candidateValueWithCurrent;
					selectedItemsPerBucketMapWithCurrentHavingMaxValue = selectedItemsPerBucketMapWithCurrent;
				}
				if (debug) {
					System.out.println("idx: " + idx + ", value:"+ itemWeights[idx] + ", modifiedIdx:" + modifiedIdx +", candidateValueWithCurrent:"+candidateValueWithCurrent + ", remainingBucketWeights:"+toArray(remainingBucketWeights));
				}

				// Replace the value back in the bucket weight array
				remainingBucketWeights[modifiedIdx] += itemWeights[idx];
			}
		}

		MaxWeightForIndexValue value = maxVal(itemWeights, remainingBucketWeights, idx + 1, values, maxVals, debug);
		int candidateValueWithOutCurrent = (value == null ? 0 : value.maxValue);
		if (value != null) {
			selectedItemsPerBucketMapWithOutCurrent.putAll(value.selectedItemsPerBucketMap);
		}

		if (maxCandidateValueWithCurrent > candidateValueWithOutCurrent) {
			maxWeightForIndexValue = new MaxWeightForIndexValue(selectedItemsPerBucketMapWithCurrentHavingMaxValue, maxCandidateValueWithCurrent);
		} else {
			maxWeightForIndexValue = new MaxWeightForIndexValue(selectedItemsPerBucketMapWithOutCurrent, candidateValueWithOutCurrent);
		}

		if (debug) {
			System.out.println("idx: " + idx + ", value:"+ itemWeights[idx] +", candidateValueWithOutCurrent:"+candidateValueWithOutCurrent);
		}
		maxVals.put(maxWeightForIndexKey, maxWeightForIndexValue);
		return maxWeightForIndexValue;
	}

	private static String toArray(int[] arr) {
		StringBuffer sb = new StringBuffer();
		for (int index = 0; index < arr.length; index++) {
			sb.append(arr[index]).append(", ");
		}
		return sb.toString();
	}

	public static boolean isBitSet(int[] arr, int n) {
		int bite = (n & BYTE_MASK) >> 3;
		int bit = n & 0x7;
		return (arr[bite] & (0x1 << bit)) != 0;
	}

	public static void setBit(int[] arr, int n) {
	}

	public static void main(String[] args) {
		boolean debug = false;
		if (args.length > 0) {
			debug = Boolean.parseBoolean(args[0]);
		}

		Scanner s = new Scanner(System.in);
		int n = s.nextInt();
		int k = s.nextInt();

		int[] itemWeights = new int[n];
		int[] remainingBucketWeights = new int[k];
		int[] values = new int[n];
		for (int idx = 0; idx < n; idx++) {
			itemWeights[idx] = s.nextInt();
		}	

		for (int idx = 0; idx < k; idx++) {
			remainingBucketWeights[idx] = s.nextInt();
		}

		for (int idx = 0; idx < n; idx++) {
			values[idx] = s.nextInt();
		}

		Map<MaxWeightForIndexKey, MaxWeightForIndexValue> maxVals = new HashMap<MaxWeightForIndexKey, MaxWeightForIndexValue>();
		MaxWeightForIndexValue value = maxVal(itemWeights, remainingBucketWeights, 0, values, maxVals, debug);
		System.out.println("max value:"+ value.maxValue + ",\nMapping of buckets/sacks to the indexes of items selected:");

		for (Map.Entry<Integer, List<Integer>> entry : value.selectedItemsPerBucketMap.entrySet()) {
			System.out.print("Bucket id " + entry.getKey() + " : ");
			for (Integer index : entry.getValue()) {
				System.out.print(index + ", ");
			}
			System.out.println();
		}
	}
}
