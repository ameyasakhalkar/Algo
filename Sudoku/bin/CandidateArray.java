public class CandidateArray {
	private static final int BYTE_MASK = 0xFFFFFFF8;
	private static final int BITS_IN_BYTE = 8;
	private byte[] candidateBitArray;
	private int numberOfBitsSet;
	private int maxValue;

	CandidateArray(int n) throws IllegalArgumentException {
		if (n <= 0) {
			throw new IllegalArgumentException("Invalid size of candidate array:" + n);
		}
		candidateBitArray = new byte[(n + 7) >> 3];

		for (int idx = 0; idx < candidateBitArray.length; idx++) {
			for (int bitIdx = 0; bitIdx < BITS_IN_BYTE; bitIdx++) {
				candidateBitArray[idx] |= (0x1 << bitIdx);
				numberOfBitsSet++;
				if (numberOfBitsSet == n) {
					break;
				}
			}
			if (numberOfBitsSet == n) {
				break;
			}
		}
		maxValue = n;
	}

	public void reSetBit(int n) {
		if (isBitSet(n)) {
			int bite = ((n - 1) & BYTE_MASK) >> 3;
			candidateBitArray[bite] &= ~(0x1 << ((n - 1) & 0x7));
			numberOfBitsSet--;
		}
	}

	public void setBit(int n) {
		int bite = ((n - 1) & BYTE_MASK) >> 3;
		candidateBitArray[bite] |= (0x1 << ((n - 1) & 0x7));
	}

	public boolean isBitSet(int n) {
		int bite = ((n - 1) & BYTE_MASK) >> 3;
		return (candidateBitArray[bite] & (0x1 << ((n - 1) & 0x7))) != 0;
	}

	public boolean moreThanOneBitsSet() {
		return (numberOfBitsSet > 1);
	}

	public boolean isOnlyBitSet() {
		return numberOfBitsSet == 1;
	}

	public boolean isGivenNumBitsSet(int n) {
		return numberOfBitsSet == n;
	}

	public boolean isNoBitSet() {
		return numberOfBitsSet == 0;
	}

	// Assuming only one bit is set
	public int getNumberForBitSet() {
		for (int val = 1; val <= maxValue; val++) {
			if (isBitSet(val)) {
				return val;
			}
		}
		return -1;
	}

	public int[] getListOfBitsSet() {
		int[] bitsSet = new int[numberOfBitsSet];
		int idx = 0;
		for (int val = 1; val <= maxValue; val++) {
			if (isBitSet(val)) {
				bitsSet[idx] = val;
				idx++;
			}
		}
		return bitsSet;
	}

	public void printCandidateArray() {
		System.out.print("Candidates:");
		for (int val = 1; val <= maxValue; val++) {
			if (isBitSet(val)) {
				System.out.print(val + ",");
			}
		}
		System.out.println();
	}
}
