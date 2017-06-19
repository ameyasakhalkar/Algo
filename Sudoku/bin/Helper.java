import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;

public class Helper {
	private Helper() {
	}

	private static final int SIZE = 9;
	public static boolean isValidFormation(Sudoku sudoku) {
		boolean isValid = true;

		// Check validity of each row. Note: a row might be incomplete but still valid SO FAR
		for (int row = 0; row < SIZE; row++) {
			CandidateArray candidateArray = new CandidateArray(SIZE);
			int numberOfValuesNotSet = 0;
			for (int col = 0; col < SIZE; col++) {
				int value = sudoku.getValue(row, col);
				if (value == 0) {
					numberOfValuesNotSet++;
					continue;
				}
				candidateArray.reSetBit(value);
			}
			if (!candidateArray.isGivenNumBitsSet(numberOfValuesNotSet)) {
				isValid = false;
				System.out.println("Not valid on row " + row);
				return isValid;
			}
		}	

		// Check validity of each column
		for (int col = 0; col < SIZE; col++) {
			CandidateArray candidateArray = new CandidateArray(SIZE);
			int numberOfValuesNotSet = 0;
			for (int row = 0; row < SIZE; row++) {
				int value = sudoku.getValue(row, col);
				if (value == 0) {
					numberOfValuesNotSet++;
					continue;
				}
				candidateArray.reSetBit(value);
			}
			if (!candidateArray.isGivenNumBitsSet(numberOfValuesNotSet)) {
				isValid = false;
				System.out.println("Not valid on column " + col);
				return isValid;
			}
		}	

		// Check validity of each 3x3 block
		for (int blockIndex = 0; blockIndex < SIZE; blockIndex++) {
			CandidateArray candidateArray = new CandidateArray(SIZE);
			int numberOfValuesNotSet = 0;
			for (int row = blockIndex / 3 * 3; row < (blockIndex + 3) / 3 * 3; row++) {
				for (int col = (blockIndex % 3) * 3; col < (blockIndex % 3) * 3 + 3; col++) {
					int value = sudoku.getValue(row, col);
					if (value == 0) {
						numberOfValuesNotSet++;
						continue;
					}
					candidateArray.reSetBit(value);
				}
			}	
			if (!candidateArray.isGivenNumBitsSet(numberOfValuesNotSet)) {
				isValid = false;
				System.out.println("Not valid on block " + blockIndex);
				return isValid;
			}
		}	
		return isValid;
	}

	/**
	 * Takes union of the values of the sets in the given lists, and returns true if the size of 
	 * the union is same as the size of the input list of sets
	 */
	public static boolean isSetListComplete(List<Set<Integer>> listOfSets) {
		Set<Integer> unionSet = new HashSet<Integer>();

		for (Set<Integer> candidateSet : listOfSets) {
			unionSet.addAll(candidateSet);
		}
		return unionSet.size() == listOfSets.size();
	}

	/**
	 * For the given blockIndex and cellIndex of a cell in that block return its row index
	 * in the grid. Row indexes as well as the cell indexes start from 0. 
	 * e.g. for a 9x9 grid with 3x3 blocks, for cellIndex 7 and blockIndex 6, the row index 
	 * would be 8
	 */
	public static int getRowInGridForCellAndBlock(int cellIndex, int blockIndex, int gridRowSize, int blockRowSize) {
		if (cellIndex < 0 || blockIndex < 0 || cellIndex >= gridRowSize || blockIndex >= gridRowSize) {
			return -1;
		}
		int numBlocksInARow = (gridRowSize / blockRowSize);
		int blockStartRow = (blockIndex / numBlocksInARow) * numBlocksInARow;
		int cellRowWithinBlock = getRowForCellIndexInBlock(cellIndex, blockRowSize);
		return blockStartRow + cellRowWithinBlock;
	}

	public static int getColInGridForCellAndBlock(int cellIndex, int blockIndex, int gridRowSize, int blockRowSize) {
		if (cellIndex < 0 || blockIndex < 0 || cellIndex >= gridRowSize || blockIndex >= gridRowSize) {
			return -1;
		}
		int numBlocksInARow = (gridRowSize / blockRowSize);
		int blockStartCol  = (blockIndex % numBlocksInARow) * numBlocksInARow;
		int cellColWithinBlock = getColForCellIndexInBlock(cellIndex, blockRowSize);
		return blockStartCol + cellColWithinBlock;
	}

	/**
	 * For the given index of cell within nxn block, return its row index relative
	 * to the block. Row indexes as well as the cell indexes start from 0. 
	 * e.g. for a 3x3 block, for cellIndex 7, the row index would be 2 (cells 0,1,2
	 * on 0th row, cells 3,4,5 on 1st row and cells 6,7,8 on 2nd row)
	 * If cellIndex is invalid, returns -1
	 */
	public static int getRowForCellIndexInBlock(int cellIndex, int blockSize) {
		if (cellIndex < 0 || cellIndex >= (blockSize * blockSize)) {
			return -1;
		}
		return cellIndex / blockSize;
	}

	/**
	 * For the given index of cell within nxn block, return its column index relative
	 * to the block. Column indexes as well as the cell indexes start from 0. 
	 * e.g. for a 3x3 block, for cellIndex 7, the column index would be 1 (cells 0,3,6
	 * on 0th column, cells 1,4,7 on 1st column and cells 2,5,8 on 2nd column)
	 * If cellIndex is invalid, returns -1
	 */
	public static int getColForCellIndexInBlock(int cellIndex, int blockSize) {
		if (cellIndex < 0 || cellIndex >= (blockSize * blockSize)) {
			return -1;
		}
		return cellIndex % blockSize;
	}

	/**
	 * Given the row and column of a cell, identifies the block index. Block indexes
	 * start from 0, and increase in row-major order. If the row or column are invalid,
	 * then returns -1
	 * Assumption: blockRowSize is integral multiple of gridRowSize
	 */
	public static int getBlockIndexFromRowCol(int row, int col, int gridRowSize, int blockRowSize) {
		if (row < 0 || col < 0 || row >= gridRowSize || col >= gridRowSize) {
			return -1;
		}
		return (row / blockRowSize) * blockRowSize + (col / blockRowSize);	
	}

	/**
	 * Given the row and column of a cell, identifies the cell index within its block. Cell indexes
	 * start from 0, and increase in row-major order. If the row or column are invalid,
	 * then returns -1
	 * Assumption: blockRowSize is integral multiple of gridRowSize
	 */
	public static int getCellIndexWithinBlockFromRowCol(int row, int col, int gridRowSize, int blockRowSize) {
		if (row < 0 || col < 0 || row >= gridRowSize || col >= gridRowSize) {
			return -1;
		}
		return (row % blockRowSize) * blockRowSize + (col % blockRowSize);	
	}

	/**
	 * For the given value (that is not set yet), and the block index in the grid, return the candidate rows for that
	 * value in the block. The rows indexes start from 0 and run up to blockSize.
	 */
	// TBD: Use primitives instead of generics
	public static Set<Integer> getCandidateRowsForDigitInBlock(CandidateArray[][] candidateArray, int blockIndex, int value, int blockSize) {
		int[] bitsSetForBlock = candidateArray[blockIndex][value].getListOfBitsSet();
		Set<Integer> rowsForBlock = new HashSet<Integer>();
		for (int val : bitsSetForBlock) {
			rowsForBlock.add(getRowForCellIndexInBlock(val - 1, blockSize));
			System.out.print(getRowForCellIndexInBlock(val - 1, blockSize));
		}
		return rowsForBlock;
	}

	/**
	 * For the given value (that is not set yet), and the block index in the grid, return the candidate columns for that
	 * value in the block. The column indexes start from 0 and run up to blockSize.
	 */
	// TBD: Use primitives instead of generics
	public static Set<Integer> getCandidateColsForDigitInBlock(CandidateArray[][] candidateArray, int blockIndex, int value, int blockSize) {
		int[] bitsSetForBlock = candidateArray[blockIndex][value].getListOfBitsSet();
		Set<Integer> colsForBlock = new HashSet<Integer>();
		for (int val : bitsSetForBlock) {
			colsForBlock.add(getColForCellIndexInBlock(val - 1, blockSize));
			System.out.print(getColForCellIndexInBlock(val - 1, blockSize));
		}
		return colsForBlock;
	}	
}
