

/**
  VisibleField class
  This is the data that's being displayed at any one point in the game (i.e., visible field, because it's what the
  user can see about the minefield). Client can call getStatus(row, col) for any square.
  It actually has data about the whole current state of the game, including  
  the underlying mine field (getMineField()).  Other accessors related to game status: numMinesLeft(), isGameOver().
  It also has mutators related to actions the player could do (resetGameDisplay(), cycleGuess(), uncover()),
  and changes the game state accordingly.
  
  It, along with the MineField (accessible in mineField instance variable), forms
  the Model for the game application, whereas GameBoardPanel is the View and Controller, in the MVC design pattern.
  It contains the MineField that it's partially displaying.  That MineField can be accessed (or modified) from 
  outside this class via the getMineField accessor.  
 */

public class VisibleField {
   
     /**
      Representation invariant:
      The VisibleField class must share the same MineField object with GameBoardPanel in a game. 
      This way when the GameBoardPanel mutates that MineField, the VisibleField can see those changes. 
    */
   
   // ----------------------------------------------------------   
   // The following public constants (plus numbers mentioned in comments below) are the possible states of one
   // location (a "square") in the visible field (all are values that can be returned by public method 
   // getStatus(row, col)).
   
   // The following are the covered states (all negative values):
   public static final int COVERED = -1;   // initial value of all squares
   public static final int MINE_GUESS = -2;
   public static final int QUESTION = -3;

   // The following are the uncovered states (all non-negative values):
   
   // values in the range [0,8] corresponds to number of mines adjacent to this square
   public static final int MINE = 9;      // this loc is a mine that hasn't been guessed already (end of losing game)
   public static final int INCORRECT_GUESS = 10;  // is displayed a specific way at the end of losing game
   public static final int EXPLODED_MINE = 11;   // the one you uncovered by mistake (that caused you to lose)
   // ----------------------------------------------------------   
  
   // Instance variables
   private MineField mineField;
   private int[][] mineFieldState; // a 2D array that stores states of each square.
   private int numRows;  // Number of rows in minefield.
   private int numCols;  // Number of columns in minefield.
   // Number of guesses. When the user right clicks on a square, it changes the square's color to yellow (mine flag) & increments its count. 
   private int numMineGuess;  
   private boolean isGameOver;  //  Check whether the game is over or not; true => over & false => not over.

    /**
     Create a visible field that has the given underlying mineField.
     The initial state will have all the mines covered up, no mines guessed, and the game
     not over.
     @param mineField  the minefield to use for this VisibleField
     */
   public VisibleField(MineField mineField) {
      this.mineField = mineField;
      numRows = mineField.numRows();
      numCols = mineField.numCols();      
      isGameOver = false;  
      numMineGuess = 0;  

      mineFieldState = new int[numRows][numCols];

      // Initial state - all squares are covered.
      coverSquares();
   }


   /**
      Reset the object to its initial state (see constructor comments), using the same underlying MineField.
   */
   public void resetGameDisplay() {

      numMineGuess = 0;  // No mine guessing at the beginning.
      isGameOver = false;  // Game is not over at the begining.

      // Initial state - all squares are covered.
      coverSquares();
   }
   
   /**
      Initial state - all the squares in the minefield will be covered.
   */
   public void coverSquares() {
      for(int i = 0; i < numRows; i++) {
         for(int j = 0; j < numCols; j++) {
            mineFieldState[i][j] = COVERED;
         }
      }
   }
        
   
    /**
     Returns a reference to the mineField that this VisibleField "covers"
     @return the minefield
     */
   public MineField getMineField() {
      return mineField;
   }


    /**
     Returns the visible status of the square indicated.
     @param row  row of the square.
     @param col  col of the square.
     @return the status of the square at location (row, col).  See the public constants at the beginning of the class
     for the possible values that may be returned, and their meanings.
     PRE: getMineField().inRange(row, col)
     */
   public int getStatus(int row, int col) {
      assert getMineField().inRange(row, col);
      return mineFieldState[row][col];  
   }


    /**
     Return the the number of mines left to guess.  This has nothing to do with whether the mines guessed are correct
     or not.  Just gives the user an indication of how many more mines the user might want to guess.  So the value can
     be negative, if they have guessed more than the number of mines in the minefield.
     @return the number of mines left to guess.
     */
   public int numMinesLeft() {
      // Number of mines left = Number of total mines - Number of mines guessed.
      return ((getMineField().numMines()) - numMineGuess);
   }


    /**
     Cycles through covered states for a square, updating number of guesses as necessary.  Call on a COVERED square
     changes its status to MINE_GUESS; call on a MINE_GUESS square changes it to QUESTION;  call on a QUESTION square
     changes it to COVERED again; call on an uncovered square has no effect.
     @param row  row of the square.
     @param col  col of the square.
     PRE: getMineField().inRange(row, col)
     */
   public void cycleGuess(int row, int col) {
      
      assert getMineField().inRange(row,col);
      
      if(mineFieldState[row][col] == COVERED) {
         mineFieldState[row][col] = MINE_GUESS;      // COVERED => MINE_GUESS.
         numMineGuess++;                                    
      } 
      else if(mineFieldState[row][col] == MINE_GUESS) {
         mineFieldState[row][col] = QUESTION;         // MINE_GUESS => QUESTION.
         numMineGuess--;                                
         if(numMineGuess < 0){
            numMineGuess = 0;
         }
      }
      else if(mineFieldState[row][col] == QUESTION){ 
         mineFieldState[row][col] = COVERED;        // QUESTION => COVERED.
      }
   }

    /**
     Uncovers this square and returns false if and only if you uncover a mine here.
     If the square wasn't a mine or adjacent to a mine it also uncovers all the squares in
     the neighboring area that are also not next to any mines, possibly uncovering a large region.
     Any mine-adjacent squares you reach will also be uncovered, and form
     (possibly along with parts of the edge of the whole field) the boundary of this region.
     Does not uncover, or keep searching through, squares that have the status MINE_GUESS.
     @param row  row of the square.
     @param col  col of the square.
     @return false if and only if you uncover a mine at (row, col).
     PRE: getMineField().inRange(row, col)
     */
   public boolean uncover(int row, int col) {

      if (getMineField().inRange(row, col)) {
         
         // Uncover a square in which there is a mine. 
         if (getMineField().hasMine(row, col)) {

            // Set the game as over.
            isGameOver = true;

            // Update states of each square when losing.
            loseGame(row, col);

            return false;
         }

         // Uncover a square in which there is no mine on it.
         // The game will keep uncovering adjacent squares until it gets to the boundary of the field, squares that are adjacent to other mines or 
         // the state of squares are MINE_GUESS or QUESTION.
         uncoverAdjacent(row, col);

         // Check if each square is covered (true => game is over => player wins).
         if (allCovered()) {

            // Update states of each square when winning.
            winGame(row, col);
            isGameOver = true;
         }

         return true;
      }
      return false; // If row and col is in the minefield => true; else => false.
   }


    /**
     Returns whether the game is over.
     @return whether game over
     */
   public boolean isGameOver() {
      return isGameOver;
   }


    /**
     Return whether this square has been uncovered.  (i.e., is in any one of the uncovered states,
     vs. any one of the covered states).
     @param row of the square
     @param col of the square
     @return whether the square is uncovered
     PRE: getMineField().inRange(row, col)
     */
   public boolean isUncovered(int row, int col) {

      assert getMineField().inRange(row,col);

         // If state of each square is COVERED, MINE_GUESS or QUESTION => return false.
         if (QUESTION <= mineFieldState[row][col] && mineFieldState[row][col] <= COVERED) {
            return false;
         }   

         // If state of each square is [0, 8] => this square is already uncovered.
         if (0 <= mineFieldState[row][col] && mineFieldState[row][col] <= 8) {
            return true;
         }
      

      return false; // If row and col is in the minefield => true; else => false.

   }


    //Private methods


    /**
     Recursively finds and uncovers adjacent squares. Recursion will terminate until it gets to the boundary of the field. 
     Each recursion represents a different direction. 
     Squares that are adjacent to other squares or the state of squares are MINE_GUESS or QUESTION.      
     @param row  row of the square.
     @param col  col of the square.
     PRE: getMineField().inRange(row, col)
     */
   private void uncoverAdjacent(int row, int col) {

      if (getMineField().inRange(row, col)) {

         // If squares are uncovered or its states are MINE_GUESS => stop the recursion.
         if ((0 <= mineFieldState[row][col] && mineFieldState[row][col] <= 8) || mineFieldState[row][col] == MINE_GUESS) {
            return;
         } 
         
         else {
            // Check how many adjacent mines the square has, the value would be in [0, 8] => update the state of this square. 
            mineFieldState[row][col] = getMineField().numAdjacentMines(row, col);

            // If the square has no adjacent mines => keep checking whether its adjacent squares have adjacent mines or not.
            if (mineFieldState[row][col] == 0) {
               uncoverAdjacent(row - 1, col - 1);    // top left
               uncoverAdjacent(row - 1, col);        // top
               uncoverAdjacent(row - 1, col + 1);    // top right
               uncoverAdjacent(row, col - 1);        // left
               uncoverAdjacent(row, col + 1);        // right
               uncoverAdjacent(row + 1, col - 1);    // bottom left
               uncoverAdjacent(row + 1, col);        // bottom
               uncoverAdjacent(row + 1, col + 1);    // bottom right       
            }

            return;

            }
      }
      return;
   }

    /**
     Check if all the squares with no mines are covered. If yes => true. If not => false.
     @return true if all the squares are covered; else false. 
     */
   private boolean allCovered() {

      for (int i = 0; i < numRows; i++) {
      
         for (int j = 0; j < numCols; j++) {

            // All squares should be uncovered, except if there is a mine in it. 
            if (mineFieldState[i][j] == COVERED && !getMineField().hasMine(i, j)) {
               return false;
            }

            // If the square is in QUESTION state, then it must have a mine. 
            if (mineFieldState[i][j] == QUESTION && !getMineField().hasMine(i, j)) {
               return false;
            }
           
            // If the square is in MINE_GUESS state, then it must have a mine.
            if (mineFieldState[i][j] == MINE_GUESS && !getMineField().hasMine(i, j)) {
               return false;
            }
         }
      }      
      return true;
    }  
   
   
   /**
     Updates states of all the squares upon winning. 
     @param row  row of the square.
     @param col  col of the square.
     PRE: getMineField().inRange(row, col).
   */
   private void winGame(int row, int col) {

      assert getMineField().inRange(row,col);

      // When winning, all the squares must be uncovered. If not uncovered, there must be a mine in it so => update its state to MINE_GUESS. 
      for (int i = 0; i < numRows; i++) {
            
         for (int j = 0; j < numCols; j++) {
               
            if (getMineField().hasMine(i, j)) {
                  
               mineFieldState[i][j] = MINE_GUESS;
            }
         }
      }      
   }   
   

    /**
     Upon losing, update all states of all the squares.
     @param row  row of the square.
     @param col  col of the square.
     PRE: getMineField().inRange(row, col)
     */
   private void loseGame(int row, int col) {

      assert getMineField().inRange(row,col);

      for (int i = 0; i < numRows; i++) {
            
         for (int j = 0; j < numCols; j++) {

            // When player clicks on a mine => the state of this square turns to EXPLODED_MINE.
            if (i == row && j == col) {

               mineFieldState[i][j] = EXPLODED_MINE;                                                           
            } 
               
            // If the state of the square is MINE_GUESS & if there is no mine in it => then its state is updated to INCORRECT_GUESS.
            else if ((mineFieldState[i][j] == MINE_GUESS) && (!getMineField().hasMine(i, j))) {

               mineFieldState[i][j] = INCORRECT_GUESS;
            } 
               
            // If the state of the square is COVERED and if there is a mine in it => then its state is updated to MINE. 
            else if ((mineFieldState[i][j] == COVERED) && (getMineField().hasMine(i, j))) {

               mineFieldState[i][j] = MINE;                        
            } 
               
        }
     }
  }
 

}