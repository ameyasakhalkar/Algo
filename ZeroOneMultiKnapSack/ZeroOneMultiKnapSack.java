import java.util.Scanner;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;

public class ZeroOneMultiKnapSack {
 	private static final int BYTE_MASK = 0xFFFFFFF8;

	/**
	 * Class used to keep track of the maximum value that can be obtained with given sequence of
	 * available weights (of items) and 
	 */
	private static class MaxWeightForIndex {
		int[] weights;
		int idx;

		MaxWeightForIndex(int[] weights, int idx) {
			this.weights = weights;
			this.idx = idx;
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
	 * Subtracts the given value from the first element of the input array that is at least as 
	 * big as the value. If all elements in the array are smaller than the given value, then no
	 * change is made.
	 *
	 * @param	arr	Input array of integers
	 * @param	val	value to be subtracted from the first larger-or-equal element
	 * @return	index of the element where val was subtracted from if the subtraction happened, 
	 *			length of the array otherwise
	 */
	public static int remainingWeightsMinusGiven(int[] arr, int val) {
		for (int idx = 0; idx < arr.length; idx++) {
			if (val <= arr[idx]) {
				arr[idx] -= val;
				return idx;
			}
		}
		return arr.length;
	}

	/**
	 * Returns the maximum value that can be obtained by picking up items from the _remaining_ subarray, subject
	 * to the remaining weights available.
	 *
	 * @param	weights		List of weights of items from which a subset is to be picked up to maximize the value
	 * @param	remainingWeights	List of remaining weights available in the buckets/knapsacks
	 * @param	idx		Starting index of the sub-array for which maximum value is to be identified
	 * @param	values		List of values of items from which a subset is to be picked up to maximize the value
	 * @param	maxVals		A map of <list of remaining weights, values>. Used as memoization to avoid repeating the same sub-problem
	 */
	public static int maxVal(int[] weights, int[] remainingWeights, int idx, int[] values, Map<MaxWeightForIndex, Integer> maxVals) {
		if (idx == weights.length) {
			return 0;
		}

		// For all the permutations of a given combination of remainingWeights, and the given index into the array of items,
		// the max values are going to be the same.
		//
		// e.g. the max value with remaining weights of <5, 50> with index 0, and remaining weights of <50, 5> with index 0
		// are going to be the same. To avoid repeatedly solving the same sub-problem (with different permutations of the same
		// combination), memoize/query with only the combination with non-descending order of remaining weights (i.e. <5, 50> here)
		if (!isArraySorted(remainingWeights)) {
			Arrays.sort(remainingWeights);
		}

		// this function makes (max) 2 calls to itself, once by considering the current element and other by not 
		// considering it. remainingWeights array is modified in-place for the first call. To avoid messing up
		// with the other call, copy remainingWeights into a temporary array before the recursive calls
		int[] tmpArray = new int[remainingWeights.length];
		System.arraycopy(remainingWeights, 0, tmpArray, 0, remainingWeights.length);

		MaxWeightForIndex maxWeightForIndex = new MaxWeightForIndex(remainingWeights, idx);
		if (maxVals.containsKey(maxWeightForIndex)) {
			return maxVals.get(maxWeightForIndex);
		}

		int candidateValueWithCurrent = 0;
		int modifiedIdx = -1;

		// Current element can be selected only if at least one bucket has remaining weight at least as big as the current
		// element's weight
		if (isSmallerThanOrEqualToAnyOne(tmpArray, weights[idx])) {
			modifiedIdx = remainingWeightsMinusGiven(tmpArray, weights[idx]);
			candidateValueWithCurrent = values[idx] + maxVal(weights, tmpArray, idx + 1, values, maxVals);
		}

		int candidateValueWithOutCurrent = maxVal(weights, remainingWeights, idx + 1, values, maxVals);
		maxVals.put(maxWeightForIndex, new Integer((candidateValueWithCurrent > candidateValueWithOutCurrent) ? 
			candidateValueWithCurrent : candidateValueWithOutCurrent));
		return maxVals.get(maxWeightForIndex);
	}

	public static boolean isBitSet(int[] arr, int n) {
		int bite = (n & BYTE_MASK) >> 3;
		int bit = n & 0x7;
		return (arr[bite] & (0x1 << bit)) != 0;
	}

	public static void setBit(int[] arr, int n) {
	}

	public static void main(String[] args) {
		Scanner s = new Scanner(System.in);
		int n = s.nextInt();
		int k = s.nextInt();

		int[] weights = new int[n];
		int[] remainingWeights = new int[k];
		int[] values = new int[n];
		for (int idx = 0; idx < n; idx++) {
			weights[idx] = s.nextInt();
		}	

		for (int idx = 0; idx < k; idx++) {
			remainingWeights[idx] = s.nextInt();
		}

		for (int idx = 0; idx < n; idx++) {
			values[idx] = s.nextInt();
		}

		Map<MaxWeightForIndex, Integer> maxVals = new HashMap<MaxWeightForIndex, Integer>();
		System.out.println("max weight:"+ maxVal(weights, remainingWeights, 0, values, maxVals));
	}
}
