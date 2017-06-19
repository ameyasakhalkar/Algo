public class Sudoku {
	private int[][] grid;
	private static final int GRID_ROW_SIZE = 9;
	private int unsolvedCount;

	public Sudoku() {
		grid = new int[GRID_ROW_SIZE][GRID_ROW_SIZE];
		for (int row = 0; row < GRID_ROW_SIZE; row++) {
			for (int col = 0; col < GRID_ROW_SIZE; col++) {
				grid[row][col] = 0;
			}
		}
		unsolvedCount = GRID_ROW_SIZE * GRID_ROW_SIZE;
	}

	public Sudoku(int[][] grid) {
		this.grid = new int[GRID_ROW_SIZE][GRID_ROW_SIZE];
		unsolvedCount = GRID_ROW_SIZE * GRID_ROW_SIZE;
		for (int row = 0; row < GRID_ROW_SIZE; row++) {
			for (int col = 0; col < GRID_ROW_SIZE; col++) {
				this.grid[row][col] = grid[row][col];
				if (grid[row][col] != 0) {
					unsolvedCount--;
				}
			}
		}
	}

	public void printGrid() {
		for (int row = 0; row < GRID_ROW_SIZE; row++) {
			for (int col = 0; col < GRID_ROW_SIZE; col++) {
				if (grid[row][col] == 0) {
					System.out.print("  ");
				} else {
					System.out.print(grid[row][col] + " ");
				}
			}
			System.out.println();
		}
	}

	public boolean isSolved() {
		return (unsolvedCount == 0);
	}

	public int getValue(int row, int col) {
		return grid[row][col];
	}

	public void setValue(int row, int col, int value) {
		if (grid[row][col] == 0 && value != 0) {
			unsolvedCount--;
		}
		grid[row][col] = value;
	}

	public int getUnsolvedCount() {
		return unsolvedCount;
	}
}
