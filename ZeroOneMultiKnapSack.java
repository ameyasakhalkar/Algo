import java.util.Scanner;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;

public class ZeroOneMultiKnapSack {
 	private static final int BYTE_MASK = 0xFFFFFFF8;
	private static class MaxWeightForIndex {
		int[] weights;
		int idx;

		MaxWeightForIndex(int[] weights, int idx) {
		}
	}

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

	public static boolean isSmallerThanOrEqualToAnyOne(int[] arr, int val) {
		for (int idx = 0; idx < arr.length; idx++) {
			if (val <= arr[idx]) {
				return true;
			}
		}
		return false;
	}

	public static int remainingWeightsMinusGiven(int[] arr, int val) {
		for (int idx = 0; idx < arr.length; idx++) {
			if (val <= arr[idx]) {
				arr[idx] -= val;
				return idx;
			}
		}
		return arr.length;
	}

	public static int maxVal(int[] weights, int[] remainingWeights, int idx, int[] values, Map<MaxWeightForIndex, Integer> maxVals) {
		if (idx == weights.length) {
			return 0;
		}
		if (!isArraySorted(remainingWeights)) {
			Arrays.sort(remainingWeights);
		}
		int[] tmpArray = new int[remainingWeights.length];
		System.arraycopy(remainingWeights, 0, tmpArray, 0, remainingWeights.length);

		MaxWeightForIndex maxWeightForIndex = new MaxWeightForIndex(tmpArray, idx);
		if (maxVals.containsKey(maxWeightForIndex)) {
			return maxVals.get(maxWeightForIndex);
		}

		int candidateWithCurrent = 0;
		int modifiedIdx = -1;
		if (isSmallerThanOrEqualToAnyOne(tmpArray, weights[idx])) {
			modifiedIdx = remainingWeightsMinusGiven(tmpArray, weights[idx]);
			candidateWithCurrent = values[idx] + maxVal(weights, tmpArray, idx + 1, values, maxVals);
		}	
		int candidateWithOutCurrent = maxVal(weights, remainingWeights, idx + 1, values, maxVals);
		maxVals.put(maxWeightForIndex, new Integer((candidateWithCurrent > candidateWithOutCurrent) ? candidateWithCurrent : candidateWithOutCurrent));
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
