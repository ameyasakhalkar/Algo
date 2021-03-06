Reads an input Sudoku problem, and tries to solve it using rule-based approach (as opposed to<br>
backtracking). Accepts input in either of two ways:<br>
<br>
- If a command-line paramter is specified, treats it as path of a file on local filesystem, and<br>
reads the problem from that file<br>
- Otherwise, reads the problem on STDIN<br>
<br>
Input format: 81 integers separated by space/tab/new-lines. For an unsolved cell, it expects a <br>
zero (0). Please refer to the 'input' directory for some sample inputs.<br>
<br>
e.g.<br>
<b>java SudokuSolver input.txt</b> expects a text-file named input.txt in the current working directory, with 81<br>
integers separated by whitespace, denoting the initial puzzle, whereas<br>
<b>java SudokuSolver</b> expects to read 81 integers from STDIN denoting the initial puzzle<br>
