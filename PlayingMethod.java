public class PlayingMethod {
    private static final int ROWS = 9;
    private static final int COLS = 7;
    private static final String[][] board = new String[ROWS][COLS];
    private static final String[][] pieces = new String[ROWS][COLS]; // 存储棋子
    
    // 动物等级定义
    private static final String[] ANIMALS = {"鼠", "猫", "狗", "狼", "豹", "虎", "狮", "象"};
    private static final int[] RANKS = {1, 2, 3, 4, 5, 6, 7, 8};
    
    private boolean blueTurn = true; // 蓝方先手

    public PlayingMethod() {
        initializeBoard();
    }

    private void initializeBoard() {
        // Initialize the board with default values
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                board[i][j] = " ";
                pieces[i][j] = " ";
            }
        }

        // Set special tiles
        board[0][3] = "兽穴"; // 红方兽穴
        board[8][3] = "兽穴"; // 蓝方兽穴
        board[0][2] = board[0][4] = board[1][3] = "陷阱"; // 红方陷阱
        board[8][2] = board[8][4] = board[7][3] = "陷阱"; // 蓝方陷阱
        
        // Set river tiles (两个3x2的河流区域)
        for (int i = 3; i <= 5; i++) {
            for (int j = 1; j <= 2; j++) {
                board[j+2][i] = "河";
            }
        }
        
        // Initialize pieces positions
        initializePieces();
    }
    
    private void initializePieces() {
        // 红方棋子 (上方，第0、1、2行)
        pieces[0][0] = "红狮"; pieces[0][6] = "红虎";
        pieces[1][1] = "红狗"; pieces[1][5] = "红猫";
        pieces[2][0] = "红鼠"; pieces[2][2] = "红豹"; pieces[2][4] = "红狼"; pieces[2][6] = "红象";
        
        // 蓝方棋子 (下方，第6、7、8行)
        pieces[6][0] = "蓝象"; pieces[6][2] = "蓝狼"; pieces[6][4] = "蓝豹"; pieces[6][6] = "蓝鼠";
        pieces[7][1] = "蓝猫"; pieces[7][5] = "蓝狗";
        pieces[8][0] = "蓝虎"; pieces[8][6] = "蓝狮";
    }

    public boolean isValidMove(int startX, int startY, int endX, int endY) {
        String piece = pieces[startX][startY];
        if (piece.equals(" ")) return false;
        
        // 检查是否是当前玩家的棋子
        boolean isPieceBlue = piece.startsWith("蓝");
        if (isPieceBlue != blueTurn) return false;
        
        // 检查边界
        if (endX < 0 || endX >= ROWS || endY < 0 || endY >= COLS) {
            return false;
        }
        
        // 检查目标位置是否有己方棋子
        String targetPiece = pieces[endX][endY];
        if (!targetPiece.equals(" ") && targetPiece.startsWith(piece.substring(0, 1))) {
            return false; // 不能攻击己方棋子
        }
        
        // 不能进入己方兽穴
        if (board[endX][endY].equals("兽穴")) {
            if ((endX == 0 && piece.startsWith("红")) || (endX == 8 && piece.startsWith("蓝"))) {
                return false;
            }
        }
        
        String animal = piece.substring(1);
        
        // 狮子和老虎的跳跃移动
        if (animal.equals("狮") || animal.equals("虎")) {
            return isValidJumpMove(startX, startY, endX, endY, animal) || 
                   isValidNormalMove(startX, startY, endX, endY, animal);
        }
        
        return isValidNormalMove(startX, startY, endX, endY, animal);
    }
    
    private boolean isValidNormalMove(int startX, int startY, int endX, int endY, String animal) {
        // 普通移动：水平或垂直一格
        if (Math.abs(startX - endX) + Math.abs(startY - endY) != 1) {
            return false;
        }
        
        // 只有鼠能进入河流
        if (board[endX][endY].equals("河") && !animal.equals("鼠")) {
            return false;
        }
        
        return true;
    }
    
    private boolean isValidJumpMove(int startX, int startY, int endX, int endY, String animal) {
        boolean isVerticalJump = (startX != endX && startY == endY);
        boolean isHorizontalJump = (startX == endX && startY != endY);
        
        // 狮子可以水平和垂直跳，老虎只能垂直跳
        if (animal.equals("虎") && isHorizontalJump) {
            return false;
        }
        
        if (!isVerticalJump && !isHorizontalJump) {
            return false;
        }
        
        // 检查是否跳过河流
        if (isVerticalJump) {
            // 垂直跳跃河流
            if ((startX <= 2 && endX >= 6) || (startX >= 6 && endX <= 2)) {
                return !isRatBlockingVerticalJump(startY);
            }
        } else {
            // 水平跳跃河流（仅狮子）
            if (startX >= 3 && startX <= 5 && 
                ((startY <= 0 && endY >= 6) || (startY >= 6 && endY <= 0))) {
                return !isRatBlockingHorizontalJump(startX);
            }
        }
        
        return false;
    }
    
    private boolean isRatBlockingVerticalJump(int col) {
        for (int row = 3; row <= 5; row++) {
            if (pieces[row][col].endsWith("鼠")) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isRatBlockingHorizontalJump(int row) {
        for (int col = 1; col <= 5; col++) {
            if (pieces[row][col].endsWith("鼠")) {
                return true;
            }
        }
        return false;
    }
    
    public boolean canCapture(String attacker, String defender, int defenderX, int defenderY) {
        if (defender.equals(" ")) return true;
        
        // 获取动物名称
        String attackerAnimal = attacker.substring(1);
        String defenderAnimal = defender.substring(1);
        
        // 获取等级
        int attackerRank = getAnimalRank(attackerAnimal);
        int defenderRank = getAnimalRank(defenderAnimal);
        
        // 鼠可以吃象（但不能从河中吃陆地上的象）
        if (attackerAnimal.equals("鼠") && defenderAnimal.equals("象")) {
            return !board[defenderX][defenderY].equals("河"); // 鼠在河中不能吃陆地上的象
        }
        
        // 象不能吃鼠
        if (attackerAnimal.equals("象") && defenderAnimal.equals("鼠")) {
            return false;
        }
        
        // 河中的鼠在陆地上无敌（只能被河中的鼠吃）
        if (defenderAnimal.equals("鼠") && board[defenderX][defenderY].equals("河")) {
            return attackerAnimal.equals("鼠") && 
                   board[defenderX][defenderY].equals("河");
        }
        
        // 陷阱中的棋子等级变为0
        if (board[defenderX][defenderY].equals("陷阱")) {
            // 检查是否是对方陷阱
            boolean isDefenderRed = defender.startsWith("红");
            boolean isRedTrap = (defenderX <= 1); // 红方陷阱在上方
            if (isDefenderRed != isRedTrap) {
                return true; // 在对方陷阱中，任何棋子都能吃
            }
        }
        
        // 正常等级比较
        return attackerRank >= defenderRank;
    }
    
    private int getAnimalRank(String animal) {
        for (int i = 0; i < ANIMALS.length; i++) {
            if (ANIMALS[i].equals(animal)) {
                return RANKS[i];
            }
        }
        return 0;
    }

    public void printBoard() {
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                System.out.print(board[i][j] + "\t");
            }
            System.out.println();
        }
    }
    
    public boolean makeMove(int startX, int startY, int endX, int endY) {
        if (!isValidMove(startX, startY, endX, endY)) {
            return false;
        }
        
        String piece = pieces[startX][startY];
        String targetPiece = pieces[endX][endY];
        
        // 检查是否可以吃掉目标棋子
        if (!targetPiece.equals(" ") && !canCapture(piece, targetPiece, endX, endY)) {
            return false;
        }
        
        // 执行移动
        pieces[endX][endY] = piece;
        pieces[startX][startY] = " ";
        
        // 切换回合
        blueTurn = !blueTurn;
        
        return true;
    }
    
    public String checkWin() {
        // 检查是否有棋子进入了对方兽穴
        if (!pieces[0][3].equals(" ")) {
            return pieces[0][3].startsWith("蓝") ? "蓝方获胜！" : null;
        }
        if (!pieces[8][3].equals(" ")) {
            return pieces[8][3].startsWith("红") ? "红方获胜！" : null;
        }
        
        // 检查是否所有敌方棋子都被吃掉
        boolean hasRed = false, hasBlue = false;
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                if (!pieces[i][j].equals(" ")) {
                    if (pieces[i][j].startsWith("红")) hasRed = true;
                    if (pieces[i][j].startsWith("蓝")) hasBlue = true;
                }
            }
        }
        
        if (!hasRed) return "蓝方获胜！";
        if (!hasBlue) return "红方获胜！";
        
        return null; // 游戏继续
    }
    
    public boolean getCurrentTurn() {
        return blueTurn;
    }
    
    public String getCurrentPlayer() {
        return blueTurn ? "蓝方" : "红方";
    }
    
    public String getPiece(int row, int col) {
        return pieces[row][col];
    }
    
    public String getBoardTile(int row, int col) {
        return board[row][col];
    }
    
    public static void main(String[] args) {
        PlayingMethod game = new PlayingMethod();
        game.printBoard();
    }
}