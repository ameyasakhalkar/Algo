import java.util.Scanner;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.io.File;

public class SudokuSolver {
	private Sudoku sudoku;
	private static final int GRID_ROW_SIZE = 9;
	private static final int BLOCK_SIZE = 3;
	private int[][] grid;
	private CandidateArray[][] candidateDigitsForAllCells;
	private CandidateArray[][] candidateCellArrayForDigitsInRows; // First dimension is for row, second for the digit. Value indicates the Candidate cells for that digit in that row
	private CandidateArray[][] candidateCellArrayForDigitsInCols; // First dimension is for column, second for the digit. Value indicates the Candidate cells for that digit in that column
	public CandidateArray[][] candidateCellArrayForDigitsInBlocks; // First dimension is for block, second for the digit. Value indicates the Candidate cells for that digit in that block (3 x 3)

	private CandidateArray[][] initCandidateArrays(int rowMax, int colMax, int maxValue) {
		CandidateArray[][] candidateCellArray = new CandidateArray[GRID_ROW_SIZE][GRID_ROW_SIZE];
		for (int row = 0; row < rowMax; row++) {
			for (int col = 0; col < colMax; col++) {
				candidateCellArray[row][col] = new CandidateArray(maxValue);
			}
		}
		return candidateCellArray;
	}

	public SudokuSolver(int[][] grid) {
		sudoku = new Sudoku();
		this.grid = grid;
		candidateDigitsForAllCells = initCandidateArrays(GRID_ROW_SIZE, GRID_ROW_SIZE, GRID_ROW_SIZE);
		candidateCellArrayForDigitsInRows = initCandidateArrays(GRID_ROW_SIZE, GRID_ROW_SIZE, GRID_ROW_SIZE);
		candidateCellArrayForDigitsInCols = initCandidateArrays(GRID_ROW_SIZE, GRID_ROW_SIZE, GRID_ROW_SIZE);
		candidateCellArrayForDigitsInBlocks = initCandidateArrays(GRID_ROW_SIZE, GRID_ROW_SIZE, GRID_ROW_SIZE);
	}

	public void printGrid() {
		System.out.println("\n\nGrid:");
		sudoku.printGrid();
	}

	/**
	 * For Hard/Evil combinations: 
	 * If a digit has not been set in any of the 3 horizontal blocks (like 0, 1, 2 OR 6, 7, 8) or any of the 3 vertical
	 * blocks (like 0, 3, 6) OR (1, 4, 7), see if its candidate cells can be restricted.
	 * For each pair of such horizontal blocks (say 0-1, 0-2, 1-2 while considering horizontal blocks 0, 1 and 2), find
	 * the candidate rows for that digit in the pair combined. e.g. if 5 can be placed only on rows 0 and 2 in block 0,
	 * and the same rows in block 1 as well, then the combined candidate rows for 5 across 0 and 1 are two: 0 and 2. That 
	 * means, in block 2, it has to appear in row 1. So all cells in block 2 for rows 0 and 2 can be eliminated as candidates.
	 *
	 * Follow the same for vertical blocks as well.
	 *
	 * Also, if for a digit (not set yet) in a block, if there is just a single row possible, then it cannot appear in the 
	 * same row for the other horizontal neighnouring blocks.
	 */
	private void checkPossiblePositionsForDigitAcrossBlocks() {
		// horizontal
		for (int blockRow = 0; blockRow < BLOCK_SIZE; blockRow++) {
			int blockIndex = blockRow * BLOCK_SIZE;
			for (int value = 0; value < GRID_ROW_SIZE; value++) {
				if (candidateCellArrayForDigitsInBlocks[blockIndex][value].moreThanOneBitsSet() &&
						candidateCellArrayForDigitsInBlocks[blockIndex + 1][value].moreThanOneBitsSet() &&
						candidateCellArrayForDigitsInBlocks[blockIndex + 2][value].moreThanOneBitsSet() ) {

					// For each pair of blocks, check the #distinct rows that have the candidate cells.
					// If its 2, reset all the cells in that row for the third block as from being the candidates
					List<Set<Integer>> candidateRowsForBlocks = new ArrayList<Set<Integer>>();
					for (int count = 0; count < BLOCK_SIZE; count++) {
						Set<Integer> candidateRows = Helper.getCandidateRowsForDigitInBlock(
							candidateCellArrayForDigitsInBlocks, blockIndex + count, value, BLOCK_SIZE);
						candidateRowsForBlocks.add(candidateRows);
					}

					for (int combination = 0; combination < BLOCK_SIZE; combination++) {
						List<Set<Integer>> pairOfCandidateRowSets = new ArrayList<Set<Integer>>();
						pairOfCandidateRowSets.add(candidateRowsForBlocks.get(combination));
						pairOfCandidateRowSets.add(candidateRowsForBlocks.get((combination + 1) % BLOCK_SIZE));
						if (Helper.isSetListComplete(pairOfCandidateRowSets)) {
							for (int row : candidateRowsForBlocks.get(combination)) {
								for (int cell = row * BLOCK_SIZE; cell < (row + 1) * BLOCK_SIZE; cell++) {

									// Find the blockindex 2 hops away horizontally
									// e.g. if the combination is for blocks 4 and 5, it needs the horizontal block
									// that is 2 hops away from 4 i.e. 3.
									int tmpBlockIndex = blockIndex + (combination + 2) % BLOCK_SIZE;
									candidateCellArrayForDigitsInBlocks[tmpBlockIndex][value].reSetBit(cell + 1);
									if (candidateCellArrayForDigitsInBlocks[tmpBlockIndex][value].isOnlyBitSet()) {
										int onlyCandidateCell = candidateCellArrayForDigitsInBlocks[tmpBlockIndex][value].getNumberForBitSet() - 1;
										int rowForCell = Helper.getRowInGridForCellAndBlock(onlyCandidateCell, tmpBlockIndex, GRID_ROW_SIZE, BLOCK_SIZE);	
										int colForCell = Helper.getColInGridForCellAndBlock(onlyCandidateCell, tmpBlockIndex, GRID_ROW_SIZE, BLOCK_SIZE);	
										if (sudoku.getValue(rowForCell, colForCell) == 0) {
											setDigitInCell(rowForCell, colForCell, value + 1);
										}	
									}
								}
							}
						}
					}
				}

				// Check if the digit is not set in at least two blocks in the row
				int numBlocksInWhichNotSet = 0;
				for (int count = 0; count < BLOCK_SIZE; count++) {
					if (candidateCellArrayForDigitsInBlocks[blockIndex + count][value].moreThanOneBitsSet()) {
						numBlocksInWhichNotSet++;
					}
				}

				// At least two blocks should not have that digit set. Otherwise, there is nothing to do
				if (numBlocksInWhichNotSet <= 1) {
					continue;
				}

				// For each pair of blocks, check the #distinct rows that have the candidate cells.
				// If its 2, reset all the cells in that row for the third block as from being the candidates
				List<Set<Integer>> candidateRowsForBlocks = new ArrayList<Set<Integer>>(BLOCK_SIZE);
				int[] numCandidateRowsPerBlock = new int[BLOCK_SIZE];

				// At least one of the blocks should have exactly one candidate row, otherwise nothing can be done here
				int indexOfBlockWithJustOneCandidateRow = -1;
				int indexOfBlockWithTwoOrMoreCandidateRows = -1;
				for (int count = 0; count < BLOCK_SIZE; count++) {
					Set<Integer> candidateRows = Helper.getCandidateRowsForDigitInBlock(candidateCellArrayForDigitsInBlocks,
						blockIndex + count, value, BLOCK_SIZE);
					candidateRowsForBlocks.add(candidateRows);
					numCandidateRowsPerBlock[count] = candidateRows.size();

					if (numCandidateRowsPerBlock[count] == 1) {
						indexOfBlockWithJustOneCandidateRow = count;
					} else if (numCandidateRowsPerBlock[count] >= 2) {
						indexOfBlockWithTwoOrMoreCandidateRows = count;
					}
				}

				if (indexOfBlockWithJustOneCandidateRow == -1 || indexOfBlockWithTwoOrMoreCandidateRows == -1) {
					continue;
				}

				int singleCandidateRow = -1;
				singleCandidateRow = candidateRowsForBlocks.get(indexOfBlockWithJustOneCandidateRow).iterator().next();
				for (int cell = singleCandidateRow * BLOCK_SIZE; cell < (singleCandidateRow + 1) * BLOCK_SIZE; cell++) {
					// Find the blockindex where to reset the candidate cells
					int tmpBlockIndex = blockIndex + indexOfBlockWithTwoOrMoreCandidateRows;
					candidateCellArrayForDigitsInBlocks[tmpBlockIndex][value].reSetBit(cell + 1);
					if (candidateCellArrayForDigitsInBlocks[tmpBlockIndex][value].isOnlyBitSet()) {
						int onlyCandidateCell = candidateCellArrayForDigitsInBlocks[tmpBlockIndex][value].getNumberForBitSet() - 1;
						int rowForCell = Helper.getRowInGridForCellAndBlock(onlyCandidateCell, tmpBlockIndex, GRID_ROW_SIZE, BLOCK_SIZE);	
						int colForCell = Helper.getColInGridForCellAndBlock(onlyCandidateCell, tmpBlockIndex, GRID_ROW_SIZE, BLOCK_SIZE);	

						if (sudoku.getValue(rowForCell, colForCell) == 0) {
							setDigitInCell(rowForCell, colForCell, value + 1);
						}	
					}
				}
			}
		}

		// vertical
		for (int blockCol  = 0; blockCol < BLOCK_SIZE; blockCol++) {
			int blockIndex = blockCol;
			for (int value = 0; value < GRID_ROW_SIZE; value++) {
				if (candidateCellArrayForDigitsInBlocks[blockIndex][value].moreThanOneBitsSet() &&
							candidateCellArrayForDigitsInBlocks[blockIndex + BLOCK_SIZE][value].moreThanOneBitsSet() &&
							candidateCellArrayForDigitsInBlocks[blockIndex + 2 * BLOCK_SIZE][value].moreThanOneBitsSet()) {
						candidateCellArrayForDigitsInBlocks[blockIndex][value].printCandidateArray();	
						candidateCellArrayForDigitsInBlocks[blockIndex+BLOCK_SIZE][value].printCandidateArray();	
						candidateCellArrayForDigitsInBlocks[blockIndex+2*BLOCK_SIZE][value].printCandidateArray();	

						// For each pair of blocks, check the #distinct cols that have the candidate cells.
						// If its 2, reset all the cells in that col for the third block as from being the candidates
						List<Set<Integer>> candidateColsForBlocks = new ArrayList<Set<Integer>>();

						for (int count = 0; count < BLOCK_SIZE; count++) {
							Set<Integer> candidateCols = Helper.getCandidateColsForDigitInBlock(
								candidateCellArrayForDigitsInBlocks, blockIndex + count * BLOCK_SIZE, value, BLOCK_SIZE);
							candidateColsForBlocks.add(candidateCols);
						}

						for (int combination = 0; combination < BLOCK_SIZE; combination++) {
							List<Set<Integer>> pairOfCandidateColSets = new ArrayList<Set<Integer>>();
							pairOfCandidateColSets.add(candidateColsForBlocks.get(combination));
							pairOfCandidateColSets.add(candidateColsForBlocks.get((combination + 1) % BLOCK_SIZE));
							if (Helper.isSetListComplete(pairOfCandidateColSets)) {
								for (int col : candidateColsForBlocks.get(combination)) {
									for (int cell = col; cell < GRID_ROW_SIZE; cell += BLOCK_SIZE) {
										// Find the blockindex 2 hops away vertically.
										// e.g. if the combination is for blocks 4 and 7, it needs the vertical block
										// that is 2 hops away from 4 i.e. 1.
										int tmpBlockIndex = (blockIndex + (combination + 2) * BLOCK_SIZE) % GRID_ROW_SIZE;
										candidateCellArrayForDigitsInBlocks[tmpBlockIndex][value].reSetBit(cell + 1);
										if (candidateCellArrayForDigitsInBlocks[tmpBlockIndex][value].isOnlyBitSet()) {
											int onlyCandidateCell = candidateCellArrayForDigitsInBlocks[tmpBlockIndex][value].getNumberForBitSet() - 1;
											int rowForCell = Helper.getRowInGridForCellAndBlock(onlyCandidateCell, tmpBlockIndex, GRID_ROW_SIZE, BLOCK_SIZE);	
											int colForCell = Helper.getColInGridForCellAndBlock(onlyCandidateCell, tmpBlockIndex, GRID_ROW_SIZE, BLOCK_SIZE);	
											if (sudoku.getValue(rowForCell, colForCell) == 0) {
												setDigitInCell(rowForCell, colForCell, value + 1);
											}	
										}
									}
								}
							}
						}	
				}

				// Check if the digit is not set in at least two blocks in the column
				int numBlocksInWhichNotSet = 0;
				for (int count = 0; count < BLOCK_SIZE; count++) {
					if (candidateCellArrayForDigitsInBlocks[blockIndex + count * BLOCK_SIZE][value].moreThanOneBitsSet()) {
						numBlocksInWhichNotSet++;
					}
				}

				// At least two blocks should not have that digit set. Otherwise, there is nothing to do
				if (numBlocksInWhichNotSet <= 1) {
					continue;
				}

				// For each pair of blocks, check the #distinct columns that have the candidate cells.
				// If its 2, reset all the cells in that column for the third block as from being the candidates
				List<Set<Integer>> candidateColsForBlocks = new ArrayList<Set<Integer>>(BLOCK_SIZE);
				int[] numCandidateColsPerBlock = new int[BLOCK_SIZE];

				// At least one of the blocks should have exactly one candidate column, otherwise nothing can be done here
				int indexOfBlockWithJustOneCandidateCols = -1;
				int indexOfBlockWithTwoOrMoreCandidateCols = -1;

				for (int count = 0; count < BLOCK_SIZE; count++) {
					Set<Integer> candidateCols = Helper.getCandidateColsForDigitInBlock(candidateCellArrayForDigitsInBlocks,
						blockIndex + count * BLOCK_SIZE, value, BLOCK_SIZE);
					candidateColsForBlocks.add(candidateCols);
					numCandidateColsPerBlock[count] = candidateCols.size();

					if (numCandidateColsPerBlock[count] == 1) {
						indexOfBlockWithJustOneCandidateCols = count;
					} else if (numCandidateColsPerBlock[count] >= 2) {
						indexOfBlockWithTwoOrMoreCandidateCols = count;
					}
				}

				if (indexOfBlockWithJustOneCandidateCols == -1 || indexOfBlockWithTwoOrMoreCandidateCols == -1) {
					continue;
				}

				int singleCandidateCol = -1;
				singleCandidateCol = candidateColsForBlocks.get(indexOfBlockWithJustOneCandidateCols).iterator().next();
				for (int cell = singleCandidateCol; cell < GRID_ROW_SIZE; cell += BLOCK_SIZE) {
					// Find the blockindex where to reset the candidate cells
					int tmpBlockIndex = (blockIndex + indexOfBlockWithTwoOrMoreCandidateCols * BLOCK_SIZE) % GRID_ROW_SIZE;
					candidateCellArrayForDigitsInBlocks[tmpBlockIndex][value].reSetBit(cell + 1);
					if (candidateCellArrayForDigitsInBlocks[tmpBlockIndex][value].isOnlyBitSet()) {
						int onlyCandidateCell = candidateCellArrayForDigitsInBlocks[tmpBlockIndex][value].getNumberForBitSet() - 1;
						int rowForCell = Helper.getRowInGridForCellAndBlock(onlyCandidateCell, tmpBlockIndex, GRID_ROW_SIZE, BLOCK_SIZE);	
						int colForCell = Helper.getColInGridForCellAndBlock(onlyCandidateCell, tmpBlockIndex, GRID_ROW_SIZE, BLOCK_SIZE);	

						if (sudoku.getValue(rowForCell, colForCell) == 0) {
							setDigitInCell(rowForCell, colForCell, value + 1);
						}	
					}
				}	
			}
		}	
	}

	private void resetCellsAsCandidate(int row, int col, int value) {
		// Reset this cell from being a candidate for all digits in this row, column and block
		for (int digit = 1; digit <= GRID_ROW_SIZE; digit++) {
			candidateCellArrayForDigitsInRows[row][digit - 1].reSetBit(col + 1);
			if (candidateCellArrayForDigitsInRows[row][digit - 1].isOnlyBitSet()) {
				// Since the numbers set start from 1, subtract 1 to get the column index for the only candidate for this digit
				int onlyCandidateCol = candidateCellArrayForDigitsInRows[row][digit - 1].getNumberForBitSet() - 1;
				if (sudoku.getValue(row, onlyCandidateCol) == 0) {
					setDigitInCell(row, onlyCandidateCol, digit);
				}	
			}
		}

		for (int digit = 1; digit <= GRID_ROW_SIZE; digit++) {
			candidateCellArrayForDigitsInCols[col][digit - 1].reSetBit(row + 1);
			if (candidateCellArrayForDigitsInCols[col][digit - 1].isOnlyBitSet()) {
				// Since the numbers set start from 1, subtract 1 to get the row index for the only candidate for this digit
				int onlyCandidateRow = candidateCellArrayForDigitsInCols[col][digit - 1].getNumberForBitSet() - 1;
				if (sudoku.getValue(onlyCandidateRow, col) == 0) {
					setDigitInCell(onlyCandidateRow, col, digit);
				}	
			}
		}	

		int blockIndex = Helper.getBlockIndexFromRowCol(row, col, GRID_ROW_SIZE, BLOCK_SIZE);
		int cellIndexInBlock = Helper.getCellIndexWithinBlockFromRowCol(row, col, GRID_ROW_SIZE, BLOCK_SIZE);
		for (int digit = 1; digit <= GRID_ROW_SIZE; digit++) {
			candidateCellArrayForDigitsInBlocks[blockIndex][digit - 1].reSetBit(cellIndexInBlock + 1);
			if (candidateCellArrayForDigitsInBlocks[blockIndex][digit - 1].isOnlyBitSet()) {
				// Since the numbers set start from 1, subtract 1 to get the cellIndex for the only candidate for this digit
				int onlyCandidateCell = candidateCellArrayForDigitsInBlocks[blockIndex][digit - 1].getNumberForBitSet() - 1;
				int candidateCellRow = blockIndex / BLOCK_SIZE * BLOCK_SIZE + (onlyCandidateCell / BLOCK_SIZE);
				int candidateCellCol = (blockIndex % BLOCK_SIZE) * BLOCK_SIZE + (onlyCandidateCell % BLOCK_SIZE);
				if (sudoku.getValue(candidateCellRow, candidateCellCol) == 0) {
					setDigitInCell(candidateCellRow, candidateCellCol, digit);
				}	
			}
		}	

		// Reset the cells in the same row in other blocks from being candidate for this digit. Similarly, reset 
		// the cells in the same column in other being from being candidate
	}

	private void resetDigitAsCandidate(int row, int col, int value) {
		// Reset this value/digit from being a candidate for all cells in this row, column and block
		for (int tmpCol = 0; tmpCol < GRID_ROW_SIZE; tmpCol++) {
			if (tmpCol == col) {
				continue;
			}
			candidateDigitsForAllCells[row][tmpCol].reSetBit(value);
			candidateCellArrayForDigitsInRows[row][value-1].reSetBit(tmpCol+1);
			if (sudoku.getValue(row, tmpCol) == 0 && candidateDigitsForAllCells[row][tmpCol].isOnlyBitSet()) {
				int onlyCandidate = candidateDigitsForAllCells[row][tmpCol].getNumberForBitSet();
				setDigitInCell(row, tmpCol, onlyCandidate);
			}
		}

		for (int tmpRow = 0; tmpRow < GRID_ROW_SIZE; tmpRow++) {
			if (tmpRow == row) {
				continue;
			}
			candidateDigitsForAllCells[tmpRow][col].reSetBit(value);
			candidateCellArrayForDigitsInCols[col][value-1].reSetBit(tmpRow+1);
			if (sudoku.getValue(tmpRow, col) == 0 && candidateDigitsForAllCells[tmpRow][col].isOnlyBitSet()) {
				int onlyCandidate = candidateDigitsForAllCells[tmpRow][col].getNumberForBitSet();
				setDigitInCell(tmpRow, col, onlyCandidate);
			}
		}

		for (int tmpRow = (row / BLOCK_SIZE * BLOCK_SIZE); tmpRow < (row + BLOCK_SIZE) / BLOCK_SIZE * BLOCK_SIZE; tmpRow++) {
			for (int tmpCol = (col / BLOCK_SIZE * BLOCK_SIZE); tmpCol < (col + BLOCK_SIZE) / BLOCK_SIZE * BLOCK_SIZE; tmpCol++) {
				if (tmpRow == row && tmpCol == col) {
					continue;
				}
				candidateDigitsForAllCells[tmpRow][tmpCol].reSetBit(value);
				if (sudoku.getValue(tmpRow, tmpCol) == 0 && candidateDigitsForAllCells[tmpRow][tmpCol].isOnlyBitSet()) {
					int onlyCandidate = candidateDigitsForAllCells[tmpRow][tmpCol].getNumberForBitSet();
					setDigitInCell(tmpRow, tmpCol, onlyCandidate);
				}
			}
		}

		int blockIndex = Helper.getBlockIndexFromRowCol(row, col, GRID_ROW_SIZE, BLOCK_SIZE);
		for (int cellIndexInBlock = 0; cellIndexInBlock < GRID_ROW_SIZE; cellIndexInBlock++) {
			candidateCellArrayForDigitsInBlocks[blockIndex][value - 1].reSetBit(cellIndexInBlock + 1);
		}

		// Also for the horizontal and vertical neighbouring blocks, reset this digit as not being a candidate _only_ for
		// the cells that fall in the same row and col respectively
		int cellIndexInBlock = Helper.getCellIndexWithinBlockFromRowCol(row, col, GRID_ROW_SIZE, BLOCK_SIZE);
		for (int tmpBlockIndex = (blockIndex / BLOCK_SIZE) * BLOCK_SIZE; tmpBlockIndex < (blockIndex + BLOCK_SIZE)/BLOCK_SIZE*BLOCK_SIZE; tmpBlockIndex++) {
			if (tmpBlockIndex == blockIndex) {
				continue;
			}
			for (int tmpCellIndex = cellIndexInBlock / BLOCK_SIZE * BLOCK_SIZE; tmpCellIndex < (cellIndexInBlock + BLOCK_SIZE)/BLOCK_SIZE*BLOCK_SIZE; tmpCellIndex++) {
				candidateCellArrayForDigitsInBlocks[tmpBlockIndex][value - 1].reSetBit(tmpCellIndex + 1);
			}
		}

		cellIndexInBlock = Helper.getCellIndexWithinBlockFromRowCol(row, col, GRID_ROW_SIZE, BLOCK_SIZE);
		for (int tmpBlockIndex = blockIndex % BLOCK_SIZE; tmpBlockIndex < GRID_ROW_SIZE; tmpBlockIndex += BLOCK_SIZE) {
			if (tmpBlockIndex == blockIndex) {
				continue;
			}
			for (int tmpCellIndex = cellIndexInBlock % BLOCK_SIZE; tmpCellIndex < GRID_ROW_SIZE; tmpCellIndex += BLOCK_SIZE) {
				candidateCellArrayForDigitsInBlocks[tmpBlockIndex][value - 1].reSetBit(tmpCellIndex + 1);
			}
		}
	}

	private void setDigitInCell(int row, int col, int value) {
		sudoku.setValue(row, col, value);
		printGrid();
		resetDigitAsCandidate(row, col, value);
		resetCellsAsCandidate(row, col, value);
	}

	private void init() {
		for (int row = 0; row < GRID_ROW_SIZE; row++) {
			for (int col = 0; col < GRID_ROW_SIZE; col++) {
				// Even if the input value for given cell is 0, another value might 
				// have been set in the cell due to ruling out other candidate
				// values as a result of entering values in other cells
				if (grid[row][col] == 0 && sudoku.getValue(row, col) != 0) {
					continue;
				}

				sudoku.setValue(row, col, grid[row][col]);

				int value = grid[row][col];
				if (value == 0) {
					continue;
				}
				resetDigitAsCandidate(row, col, value);
				resetCellsAsCandidate(row, col, value);
			}
		}	
	}

        public void solve() throws Exception {
		if (!Helper.isValidFormation(new Sudoku(grid))) {
			System.out.println("Not a valid formation. Not solving");	
			return;
		}

		init();
		printGrid();
		int numIterations = 0;
		while (!sudoku.isSolved()) {
			for (int row = 0; row < GRID_ROW_SIZE; row++) {
				for (int col = 0; col < GRID_ROW_SIZE; col++) {
					int value = sudoku.getValue(row, col);
					if (value == 0) {
						if (candidateDigitsForAllCells[row][col].isOnlyBitSet()) {
							int candidateValue = candidateDigitsForAllCells[row][col].getNumberForBitSet();
							setDigitInCell(row, col, candidateValue);
						}
						continue;
					}
					resetDigitAsCandidate(row, col, value);
					resetCellsAsCandidate(row, col, value);
				}
			}
			checkPossiblePositionsForDigitAcrossBlocks();
			numIterations++;
			System.out.println("\n\n Iteration:" + numIterations);
			printGrid();
			if (numIterations == 1000) {
				break;
			}
		}

		if (Helper.isValidFormation(sudoku)) {
			System.out.println("Its a valid formation");
		}
	}

	private static int[][] readGridFromFile(String filePath) {
		int[][] grid = new int[GRID_ROW_SIZE][GRID_ROW_SIZE];
		Scanner s = null;
		try {
			s = new Scanner(new File(filePath));
			int row = 0;
			int col = 0;

			while (s.hasNextInt()) {
				int value = s.nextInt();
				if (col == GRID_ROW_SIZE) {
					col = 0;
					row++;
				}
				grid[row][col] = value;
				col++;
			}
		} catch (Exception e) {
			System.out.println("Failed reading " + filePath);
		} finally {
			if (s != null) {
				s.close();
			}
		}
		return grid;
	}

	/**
	 * If an argument was provided, treat it as input file, and read the input grid from that
	 * file. If no argument was provided, read the grid from STDIN. The grid (either from file
	 * or from STDIN) is expected to be whitespace separated integers, with the unset cells 
	 * having a value of 0.
	 */
	public static void main(String[] args) throws Exception {
		int[][] grid = null;
		Scanner s = new Scanner(System.in);
		if (args.length > 0) {
			grid = readGridFromFile(args[0]);
			if (grid == null) {
				System.out.println("Failed to read from file. Aborting");
			}
		} else {
			grid = new int[GRID_ROW_SIZE][GRID_ROW_SIZE];
			for (int row = 0; row < GRID_ROW_SIZE; row++) {
				for (int col = 0; col < GRID_ROW_SIZE; col++) {
					grid[row][col] = s.nextInt();
				}
			}	
		}

		SudokuSolver sudokuSolver = new SudokuSolver(grid);
		// Original
		sudokuSolver.printGrid();
		sudokuSolver.solve();
		// After solving (limited to 1000 iterations)
		sudokuSolver.printGrid();
	}
}
