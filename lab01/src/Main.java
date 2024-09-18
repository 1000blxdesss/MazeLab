import java.awt.Color;
import java.awt.Graphics;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import static java.lang.System.out;

class MazeGenerator extends JPanel implements KeyListener {
    private final int rows = 20; //строки
    private final int cols = 20; //столбцы
    private final int cellSize = 20; //клетки
    private final Cell[][] grid = new Cell[rows][cols];

    private boolean[][] passedCells = new boolean[rows][cols];

    private boolean isAlive;
    private boolean isPaint;
    private Cell spawnPoint;

    public MazeGenerator() {
        setPreferredSize(new Dimension(cols * cellSize, rows * cellSize)); //размер панели = размеру лабиринта
        initializeGrid(); // заполнение сетки
        generateMaze(); // генерация логики лабиринта
        SpawnPoint(5, 5);
        isAlive = false;
        isPaint = false;
        // Make the panel focusable and add the key listener
        setFocusable(true);
        requestFocusInWindow();
        addKeyListener(this);
    }

    //заполняет двумерный массив grid объектами Cell, представляя каждую клетку лабиринта.
    private void initializeGrid() {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                grid[row][col] = new Cell(row, col);
            }
        }
    }

    private void generateMaze() {
        Stack<Cell> stack = new Stack<>();
        Cell start = grid[0][0];
        start.visited = true;
        stack.push(start);

        while (!stack.isEmpty()) {
            Cell current = stack.peek();
            Cell next = getUnvisitedNeighbor(current);

            if (next != null) {
                next.visited = true;
                stack.push(next);
                removeWalls(current, next);
            } else {
                stack.pop();
            }
        }
    }

    private Cell getUnvisitedNeighbor(Cell cell) {
        List<Cell> neighbors = new ArrayList<>();

        int row = cell.row;
        int col = cell.col;

        if (row > 0 && !grid[row - 1][col].visited) neighbors.add(grid[row - 1][col]);
        if (row < rows - 1 && !grid[row + 1][col].visited) neighbors.add(grid[row + 1][col]);
        if (col > 0 && !grid[row][col - 1].visited) neighbors.add(grid[row][col - 1]);
        if (col < cols - 1 && !grid[row][col + 1].visited) neighbors.add(grid[row][col + 1]);

        if (neighbors.isEmpty()) return null;

        Collections.shuffle(neighbors);
        return neighbors.get(0);
    }
    /**Если current находится справа от next, это значит, что мы должны убрать правую стену у current и левую стену у next.
     * <P>
     *Если current находится слева от next, убираем левую стену у current и правую стену у next.
     * <P>
     *Если current находится ниже next, убираем нижнюю стену у current и верхнюю стену у next.
     * <P>
     *Если current находится выше next, убираем верхнюю стену у current и нижнюю стену у next.
     */
    private void removeWalls(Cell current, Cell next) {
        int dx = current.col - next.col;
        int dy = current.row - next.row;

        if (dx == 1) {
            current.walls[3] = false;
            next.walls[1] = false;
        } else if (dx == -1) {
            current.walls[1] = false;
            next.walls[3] = false;
        }

        if (dy == 1) {
            current.walls[0] = false;
            next.walls[2] = false;
        } else if (dy == -1) {
            current.walls[2] = false;
            next.walls[0] = false;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.BLACK);

        // Draw the maze walls
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Cell cell = grid[row][col];
                int x = col * cellSize;
                int y = row * cellSize;
                if (passedCells[row][col]) {
                    g.setColor(Color.YELLOW);
                    g.fillRect(x, y, cellSize, cellSize);
                    g.setColor(Color.BLACK);
                }
                if (cell.walls[0]) g.drawLine(x, y, x + cellSize, y); // Top
                if (cell.walls[1]) g.drawLine(x + cellSize, y, x + cellSize, y + cellSize); // Right
                if (cell.walls[2]) g.drawLine(x, y + cellSize, x + cellSize, y + cellSize); // Bottom
                if (cell.walls[3]) g.drawLine(x, y, x, y + cellSize); // Left


            }
        }

        // Draw the spawn point (robot)
        if (spawnPoint != null) {
            int spawnX = spawnPoint.col * cellSize + cellSize / 2; // Center of the cell
            int spawnY = spawnPoint.row * cellSize + cellSize / 2; // Center of the cell
            System.out.println("x:" + spawnX + "y:" + spawnY);
            System.out.println("Spawn point (row, col): (" + spawnPoint.row + ", " + spawnPoint.col + ")");
            g.setColor(Color.RED);
            g.fillOval(spawnX - 5, spawnY - 5, 10, 10); 
            if (isAlive) g.setColor(Color.GREEN);
            g.fillOval(spawnX - 5, spawnY - 5, 10, 10);
            g.setColor(Color.YELLOW);

            /*if (isPaint) {

                g.fillRect(spawnPoint.col * cellSize, spawnPoint.row * cellSize, cellSize, cellSize);//g.fillRect(spawnPoint.col, spawnPoint.row, cellSize, cellSize);

            }*/
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (spawnPoint == null) return;

        int keyCode = e.getKeyCode();
        int row = spawnPoint.row;
        int col = spawnPoint.col;
        char key = e.getKeyChar();
        byte iz=0;
        if (key == 'g')isAlive = !isAlive;

        if (key == 'h' & isAlive)isPaint=true;

        if(isAlive) {
            switch (keyCode) {
                case KeyEvent.VK_UP:
                    if (row > 0 && !spawnPoint.walls[0] && !grid[row - 1][col].walls[2]) {
                        passedCells[spawnPoint.row][spawnPoint.col] = true;
                        spawnPoint = grid[row - 1][col];
                    }
                    break;
                case KeyEvent.VK_DOWN:
                    if (row < rows - 1 && !spawnPoint.walls[2] && !grid[row + 1][col].walls[0]) {
                        passedCells[spawnPoint.row][spawnPoint.col] = true;
                        spawnPoint = grid[row + 1][col];
                    }
                    break;

                case KeyEvent.VK_LEFT:
                    if (col > 0 && !spawnPoint.walls[3] && !grid[row][col - 1].walls[1]) {
                        passedCells[spawnPoint.row][spawnPoint.col] = true;
                        spawnPoint = grid[row][col - 1];
                    }
                    break;
                case KeyEvent.VK_RIGHT:
                    if (col < cols - 1 && !spawnPoint.walls[1] && !grid[row][col + 1].walls[3]) {
                        passedCells[spawnPoint.row][spawnPoint.col] = true;
                        spawnPoint = grid[row][col + 1];
                    }
                    break;

            }
        }
        repaint(); // Repaint the panel to update the spawn point's position
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // Not used
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Not used
    }

    private static class Cell {
        int row, col;
        boolean[] walls = {true, true, true, true}; // Сверху, справа, снизу, слева
        boolean visited = false;

        Cell(int row, int col) {
            this.row = row;
            this.col = col;
        }
    }

    public void SpawnPoint(int startX, int startY) {
        // проверка на пределы границ
        if (startX >= 0 && startX < cols && startY >= 0 && startY < rows) {
            Cell cell = grid[startY][startX];


            if (cell.visited) {
                spawnPoint = cell;
            } else {
                System.out.println("The specified cell is not a valid spawn point (not visited).");
            }
        } else {
            System.out.println("Invalid coordinates for spawn point.");
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Maze Generator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        MazeGenerator mazePanel = new MazeGenerator();
        frame.add(mazePanel);
        frame.pack();
        frame.setSize(600,440);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // Request focus for the panel to ensure it can receive key events
        mazePanel.requestFocusInWindow();
    }
}
