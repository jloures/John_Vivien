import com.orbischallenge.tron.api.PlayerAction;
import com.orbischallenge.tron.client.api.LightCycle;
import com.orbischallenge.tron.client.api.TileTypeEnum;
import com.orbischallenge.tron.client.api.TronGameBoard;
import com.orbischallenge.tron.protocol.TronProtocol;
import com.orbischallenge.tron.protocol.TronProtocol.Direction;
import com.orbischallenge.tron.protocol.TronProtocol.PowerUpType;
import java.awt.Point;
import static java.lang.Math.abs;
import java.util.ArrayList;
import java.util.Random;

public class PlayerAI implements Player {

        private Point power_ups[];
        private int maze_const;
        private int power_num;
        private enum Directions {
            UP, DOWN, LEFT, RIGHT, NONE
        }
        
   
	@Override
	public void newGame(TronGameBoard map,  
			LightCycle playerCycle, LightCycle opponentCycle) {
                power_num = 0;
                Point point_curr = new Point(); //initialize temp variable (only used in the loop)
                maze_const = 8; //within how many tiles should the recursive algorithm look for a solution
                int size = map.length() - 2; //actual board is only (n - 2) ^2 big due to the fact that 2 wall tiles show up on each side
                for(int i = 0; i < size; i++) //for loop for identifying powerups
                    for(int j = 0; j < size; j++)
                        if(map.tileType(i, j).equals(TileTypeEnum.POWERUP)) {
                            power_num++;
                        }
                power_ups = new Point[power_num];
                power_num = 0;
                for(int i = 0; i < size; i++) //for loop for identifying powerups
                    for(int j = 0; j < size; j++)
                        if(map.tileType(i, j).equals(TileTypeEnum.POWERUP)) {
                            power_ups[power_num].x = i;
                            power_ups[power_num].y = j;
                            power_num++;
                        }
        }
	@Override
	public PlayerAction getMove(TronGameBoard map,
			LightCycle playerCycle, LightCycle opponentCycle, int moveNumber) {
            
                // Dummy variables used for the sole purpose of gathering info on the position and/or possible movements of the player       
                Point point = playerCycle.getPosition();  
               //Determine your aim
               Point dummy = Aim(map,playerCycle,opponentCycle);
                if(dummy.equals(new Point(-1,-1))) {
                    System.out.println("Going straight to StayAlive!");
                    return StayAlive(point, map, playerCycle);
                }
                
                System.out.println("Going for GetToAim");
                return GetToAim(map,playerCycle,opponentCycle,dummy);
        }
        
        //Refresh the current state of the PowerUps
        public void RefreshPowerUp(TronGameBoard map) {
        
            for(int i = 0; i < power_num; i++) {
                if(!map.tileType((int)power_ups[i].getX(), (int)power_ups[i].getY()).equals(TileTypeEnum.POWERUP)) {
                    power_ups[i].x = -1;
                    power_ups[i].y = -1;
                }
            //System.out.println("One power up less. Total: " + power_num);
            }
        }     
        //This function will determine what the Point for which the AI should Aim for
        public Point Aim (TronGameBoard map, LightCycle playerCycle, LightCycle opponentCycle) {
        
            //See if there are any powerups left (Top priority)
            int index = ClosestPowerUp(map,playerCycle);
            if(index != -1)
                return power_ups[index];
            
            //Call function StayAlive
                return new Point(-1,-1);
            
        
        }
        //This function is used to determine the best way to get somewhere a.k.a AIM
        public PlayerAction GetToAim (TronGameBoard map, LightCycle playerCycle, LightCycle opponentCycle, Point aim) {
        
            System.out.println("GetToAim is called");
            Point CurrPos = new Point();
            CurrPos = playerCycle.getPosition(); 
            
            int i = CurrPos.x;
            int j = CurrPos.y;
            int k = CurrPos.x;
            int Hdir = 0;
            int Vdir = 0;
            boolean BestPath = false;
            
            System.out.println("aim: (" + aim.x +"," + aim.y +")");
            System.out.println("current location: (" + i + "," + j + ")");
            
            if(CurrPos.x > aim.x) {  // shall turn left
                System.out.println("shall turn left");
                Hdir = -1;
            }
            else if(CurrPos.x < aim.x) {  // shall turn right
                System.out.println("shall turn right");
                Hdir = 1;
            }
            //else stay the same
            
            if(CurrPos.y < aim.y) {  // shall go down
                System.out.println("shall go down");
                Vdir = -1;
            }
            else if(CurrPos.y > aim.y) { //shall go up
                System.out.println("shall go up");
                Vdir = 1;
            }
            // else stay the same
            
            //try the best path I
            if(Hdir==-1 & Vdir==-1) { // left, down
                for(;k>aim.x;k--){
                    j = CurrPos.y;
                    for(i=k; i>aim.x; i--){
                        if(map.tileType(i, j).equals(TileTypeEnum.WALL) 
                                    || map.tileType(i, j).equals(TileTypeEnum.TRAIL)) {
                            i++;
                            break;
                        }
                        for(; j<aim.y; j++) {
                            if(map.tileType(i, j).equals(TileTypeEnum.WALL) 
                                    || map.tileType(i, j).equals(TileTypeEnum.TRAIL)) {
                                j--;
                                break;
                            }
                            if(i==aim.x & j== aim.y)
                                BestPath = true;
                        }   
                        if(BestPath)
                            break;
                    }
                    if(BestPath)
                        break;
                }
                
                if(BestPath) {  // best path exsists
                    System.out.println("BestPath is calculated");
                    switch(playerCycle.getDirection()) {
                        case DOWN:
                            System.out.println("down or left");
                            return BestChoice(CurrPos,map,playerCycle,Directions.DOWN,Directions.LEFT);
                        case LEFT:
                            System.out.println("left or down");
                            return BestChoice(CurrPos,map,playerCycle,Directions.LEFT,Directions.DOWN);
                        case UP:
                            System.out.println("left or up");
                            return BestChoice(CurrPos,map,playerCycle,Directions.LEFT, Directions.UP);
                        case RIGHT:
                            System.out.println("down or right");
                            return BestChoice(CurrPos,map,playerCycle,Directions.DOWN, Directions.RIGHT);
                    default: 
                        System.out.println("default");
                        return BestChoice(CurrPos,map,playerCycle,Directions.DOWN,Directions.LEFT);  
                    }
                }
            }

            else if(Hdir==-1 & Vdir==1) { //left, up
                for(;k>aim.x;k--){
                    j = CurrPos.y;
                    for(; i>aim.x; i--){
                        if(map.tileType(i, j).equals(TileTypeEnum.WALL) 
                                    || map.tileType(i, j).equals(TileTypeEnum.TRAIL)) {
                            i++;
                            break;
                        }
                        for(; j>aim.y; j--) {
                            if(map.tileType(i, j).equals(TileTypeEnum.WALL) 
                                    || map.tileType(i, j).equals(TileTypeEnum.TRAIL)) {
                                j++;
                                break;
                            }
                            if(i==aim.x & j== aim.y)
                                BestPath = true;
                        }   
                        if(BestPath)
                            break;
                    }
                    if(BestPath)
                        break;
                }
                if(BestPath) {  // best path exsists
                    System.out.println("BestPath is calculated");
                    switch(playerCycle.getDirection()) {
                        case UP:
                            System.out.println("up or left");
                            return BestChoice(CurrPos,map,playerCycle,Directions.UP,Directions.LEFT);
                        case LEFT:
                            System.out.println("left or up");
                            return BestChoice(CurrPos,map,playerCycle,Directions.LEFT,Directions.UP);
                        case DOWN:
                            System.out.println("left or down");
                            return BestChoice(CurrPos,map,playerCycle,Directions.LEFT, Directions.DOWN);
                        case RIGHT:
                            System.out.println("up or right");
                            return BestChoice(CurrPos,map,playerCycle,Directions.UP, Directions.RIGHT);
                    default: 
                        System.out.println("default");
                        return BestChoice(CurrPos,map,playerCycle,Directions.UP,Directions.LEFT); 
                    }
                }
            }

            else if(Hdir==1 & Vdir==-1) { //right, down
                for(;k<aim.x;k++){
                    j = CurrPos.y;
                    for(; i<aim.x; i++){
                        if(map.tileType(i, j).equals(TileTypeEnum.WALL) 
                                    || map.tileType(i, j).equals(TileTypeEnum.TRAIL)) {
                            i--;
                            break;
                        }
                        for(; j<aim.y; j++) {
                            if(map.tileType(i, j).equals(TileTypeEnum.WALL) 
                                    || map.tileType(i, j).equals(TileTypeEnum.TRAIL)) {
                                j--;
                                break;
                            }
                            if(i==aim.x & j== aim.y)
                                BestPath = true;
                        }   
                        if(BestPath)
                            break;
                    }
                    if(BestPath)
                        break;
                }
                if(BestPath)  { // best path exsists
                    System.out.println("BestPath is calculated");
                    switch(playerCycle.getDirection()) {
                        case DOWN:
                            System.out.println("down or right");
                            return BestChoice(CurrPos,map,playerCycle,Directions.DOWN,Directions.RIGHT);
                        case RIGHT:
                            System.out.println("right or down");
                            return BestChoice(CurrPos,map,playerCycle,Directions.RIGHT,Directions.DOWN);
                        case UP:
                            System.out.println("right or up");
                            return BestChoice(CurrPos,map,playerCycle,Directions.RIGHT,Directions.UP);
                        case LEFT:
                            System.out.println("down or left");
                            return BestChoice(CurrPos,map,playerCycle,Directions.DOWN,Directions.LEFT);
                    default: 
                        System.out.println("default");
                        return BestChoice(CurrPos,map,playerCycle,Directions.DOWN,Directions.RIGHT);
                    }
                }
            }

            else if(Hdir==1 & Vdir ==1) { //right, up
                for(;k<aim.x;k++){
                    j = CurrPos.y;
                    for(; i<aim.x; i++){
                        if(map.tileType(i, j).equals(TileTypeEnum.WALL) 
                                    || map.tileType(i, j).equals(TileTypeEnum.TRAIL)) {
                            i--;
                            break;
                        }
                        for(; j>aim.y; j--) {
                            if(map.tileType(i, j).equals(TileTypeEnum.WALL) 
                                    || map.tileType(i, j).equals(TileTypeEnum.TRAIL)) {
                                j++;
                                break;
                            }
                            if(i==aim.x & j== aim.y)
                                BestPath = true;
                        }   
                        if(BestPath)
                            break;
                    }
                    if(BestPath)
                        break;
                }
                if(BestPath) {  // best path exsists
                    System.out.println("BestPath is calculated");
                    switch(playerCycle.getDirection()) {
                        case UP:
                            System.out.println("up or right");
                            return BestChoice(CurrPos,map,playerCycle,Directions.UP,Directions.RIGHT);
                        case RIGHT:
                            System.out.println("right or up");
                            return BestChoice(CurrPos,map,playerCycle,Directions.RIGHT,Directions.UP);
                        case DOWN:
                            System.out.println("right or down");
                            return BestChoice(CurrPos,map,playerCycle,Directions.RIGHT,Directions.DOWN);
                        case LEFT:
                            System.out.println("up or left");
                            return BestChoice(CurrPos,map,playerCycle,Directions.UP,Directions.LEFT);
                    default: 
                        System.out.println("up or right");
                        return BestChoice(CurrPos,map,playerCycle,Directions.UP,Directions.RIGHT);
                    }
                }
            }
            
            //try the best path II
            else if(Hdir==-1 && Vdir==0) {   //where Vdir = 0, go straight left
                for(;i>aim.x;i--){
                    if(map.tileType(i, j).equals(TileTypeEnum.WALL) 
                                || map.tileType(i, j).equals(TileTypeEnum.TRAIL)) {
                            i++;
                            break;
                        }
                }
                if(i==aim.x) {  // best path exsists
                    System.out.println("BestPath is calculated");
                    switch(playerCycle.getDirection()){
                        case RIGHT:
                            System.out.println("up or down");
                            return BestChoice(CurrPos,map,playerCycle,Directions.UP,Directions.DOWN);
                        default: 
                            System.out.println("left straight");
                            return BestChoice(CurrPos,map,playerCycle,Directions.LEFT,Directions.NONE);
                    }
                }
            }
            
            else if(Hdir==1 && Vdir==0) {   //where Vdir = 0, go straight right
                for(;i<aim.x;i++){
                    if(map.tileType(i, j).equals(TileTypeEnum.WALL) 
                                || map.tileType(i, j).equals(TileTypeEnum.TRAIL)) {
                            i--;
                            break;
                        }
                }
                if(i==aim.x) {  // best path exsists
                    System.out.println("BestPath is calculated");
                    switch(playerCycle.getDirection()){
                        case LEFT:
                            System.out.println("up or down");
                            return BestChoice(CurrPos,map,playerCycle,Directions.UP,Directions.DOWN);
                        default: 
                            System.out.println("right straight");
                            return BestChoice(CurrPos,map,playerCycle,Directions.RIGHT,Directions.NONE);
                    }
                }
            }
            
            else if(Vdir==-1 && Hdir==0) {   //where Vdir = 0, go straight down
                for(;j<aim.y;j++){
                    if(map.tileType(i, j).equals(TileTypeEnum.WALL) 
                                || map.tileType(i, j).equals(TileTypeEnum.TRAIL)) {
                            j--;
                            break;
                        }
                }
                if(j==aim.y) {  // best path exsists
                    System.out.println("BestPath is calculated");
                    switch(playerCycle.getDirection()){
                        case DOWN:
                            System.out.println("left or right");
                            return BestChoice(CurrPos,map,playerCycle,Directions.LEFT,Directions.RIGHT);
                        default: 
                            System.out.println("down straight");
                            return BestChoice(CurrPos,map,playerCycle,Directions.DOWN,Directions.NONE);
                    }
                }
            }
            
            else if(Vdir==1 && Hdir==0) {   //where Vdir = 0, go straight up
                for(;j>aim.y;j--){
                    if(map.tileType(i, j).equals(TileTypeEnum.WALL) 
                                || map.tileType(i, j).equals(TileTypeEnum.TRAIL)) {
                            j++;
                            break;
                        }
                }
                if(j==aim.y) {  // best path exsists
                    System.out.println("BestPath is calculated");
                    switch(playerCycle.getDirection()){
                        case UP:
                            System.out.println("left or right");
                            return BestChoice(CurrPos,map,playerCycle,Directions.LEFT,Directions.RIGHT);
                        default: 
                            System.out.println("up straight");
                            return BestChoice(CurrPos,map,playerCycle,Directions.UP,Directions.NONE);
                    }
                }
            }
                   
            // no best path, go random
            System.out.println("no best path, go randomly");
            return BestChoice(CurrPos,map,playerCycle,Directions.NONE,Directions.NONE);
        }
        
        //Method for finding the closest powerup according to the maps features
        //Returns -1 on failure (List is empty, no more powerups) or the index of the closest powerup
        
        public int ClosestPowerUp (TronGameBoard map, LightCycle playerCycle) {
        
            boolean empty = true;
            for(int i = 0; i < power_num && empty; i++)
                if(power_ups[i].x != -1)
                    empty = false;
            
            if(empty)
                return -1;
            
            //Refresh your powerup
            RefreshPowerUp(map);
            
            int short_distance = 10000; //The shortest distance to the closest powerup
            int index = 0; //Index of the closest powerup

            //iterate through all powerups in the list
            for(int i = 0; i < power_num; i++) {
                //calculate distance
                int distance = (int)(abs(power_ups[i].getX() - playerCycle.getPosition().getX()) + abs(power_ups[i].getY() - playerCycle.getPosition().getY()));
                //check if it is indeed the shortest distance
                if(distance <= short_distance) {
                    short_distance = distance;
                    index = i;
                }
            }
                return index;
        }
        
        public boolean isSafe (TronGameBoard map, int x, int y) {
        
            if(map.tileType(x, y).equals(TileTypeEnum.LIGHTCYCLE) || map.tileType(x, y).equals(TileTypeEnum.WALL) || map.tileType(x, y).equals(TileTypeEnum.TRAIL))
                return false;
            return true;
        }
        
        /*This is the most import function of the code, it will treat the game as a maze*/
        public PlayerAction StayAlive (Point point, TronGameBoard map, LightCycle playerCycle) {
        
            int maze[][] = new int[map.length()][map.length()];
            for(int i = maze_const; i >= 1; i--) {
                if(SolveMaze(maze, playerCycle.getPosition().x + 1, playerCycle.getPosition().y + 1,map,playerCycle,i))
                    if(maze[playerCycle.getPosition().x][playerCycle.getPosition().y + 1] == 2)
                        return PlayerAction.MOVE_DOWN;
                    else if(maze[playerCycle.getPosition().x][playerCycle.getPosition().y - 1] == 2)
                        return PlayerAction.MOVE_UP;
                    else if(maze[playerCycle.getPosition().x - 1][playerCycle.getPosition().y] == 2)
                        return PlayerAction.MOVE_LEFT;
                    else if(maze[playerCycle.getPosition().x + 1][playerCycle.getPosition().y] == 2)
                        return PlayerAction.MOVE_RIGHT;
            }

            return BestChoice(new Point(playerCycle.getPosition().x,playerCycle.getPosition().y),map,playerCycle,Directions.NONE,Directions.NONE);
        }
        
        /*This function actually solves the maze recursively*/
        public boolean SolveMaze (int maze[][], int x, int y, TronGameBoard map, LightCycle playerCycle, int num) {
        
            //tried = 1, good_path = 2
            boolean successful = false;
            if (num == 0) {
                maze[x][y] = 2;
                successful = true;
            }
            else {
                maze[x][y] = 1;
                //try moving south
                if(isSafe(map, x + 1, y))
                    successful = SolveMaze(maze, x + 1, y, map, playerCycle, num - 1);
                if(!successful)
                    //try moving east
                    if(isSafe(map, x, y + 1))
                        successful = SolveMaze (maze, x, y + 1, map, playerCycle, num - 1);
                if(!successful)
                    //try moving north
                    if(isSafe(map, x - 1, y))
                        successful = SolveMaze(maze, x - 1, y, map, playerCycle, num - 1);
                if(!successful)
                    //try moving west
                    if(isSafe(map, x, y - 1))
                        successful = SolveMaze(maze, x, y -1, map, playerCycle, num - 1);
                if(successful)
                  //this is the correct path
                    maze[x][y] = 2;
            }
            return successful;
        }
        
        /*The only thing this function does is to avoid nearby obstacles by moving the lightcycle to the closest empty tile*/   
        public PlayerAction BestChoice (Point point, TronGameBoard map, LightCycle playerCycle, Directions first, Directions second) {
        
            switch(first) {
                case DOWN:  
                    if(isSafe(map,point.x,point.y + 1) && isSafe(map,point.x,point.y + 2))
                        return PlayerAction.MOVE_DOWN;
                    break;
                case UP: 
                    if(isSafe(map,point.x,point.y - 1) && isSafe(map,point.x,point.y - 2))
                                return PlayerAction.MOVE_UP;
                    break;    
                case RIGHT:
                    if(isSafe(map,point.x + 1,point.y) && isSafe(map,point.x + 2,point.y))
                                return PlayerAction.MOVE_RIGHT;
                    break; 
                case LEFT: 
                    if(isSafe(map,point.x,point.y) && isSafe(map,point.x,point.y + 2))
                                return PlayerAction.MOVE_LEFT;
                    break; 
                default: switch(playerCycle.getDirection()) { 
                    case LEFT: 
                        if(isSafe(map,point.x - 1,point.y) && isSafe(map,point.x - 2, point.y))
                            return PlayerAction.SAME_DIRECTION;
                        if(isSafe(map,point.x,point.y - 1) && isSafe(map,point.x, point.y - 2))
                            return PlayerAction.MOVE_UP;
                        if(isSafe(map,point.x,point.y + 1) && isSafe(map,point.x, point.y + 2))
                            return PlayerAction.MOVE_DOWN;
                        if(playerCycle.hasPowerup())
                            return PlayerAction.ACTIVATE_POWERUP;
                        break;
                    case RIGHT: 
                        if(isSafe(map,point.x + 1,point.y) && isSafe(map,point.x + 2, point.y))
                            return PlayerAction.SAME_DIRECTION;
                        if(isSafe(map,point.x,point.y - 1) && isSafe(map,point.x, point.y - 2))
                            return PlayerAction.MOVE_UP;
                        if(isSafe(map,point.x,point.y + 1) && isSafe(map,point.x, point.y + 2))
                            return PlayerAction.MOVE_DOWN;
                        if(playerCycle.hasPowerup())
                            return PlayerAction.ACTIVATE_POWERUP;
                        break;
                    case UP: 
                        if(isSafe(map,point.x,point.y - 1) && isSafe(map,point.x, point.y - 2))
                            return PlayerAction.SAME_DIRECTION;
                        if(isSafe(map,point.x - 1,point.y) && isSafe(map,point.x - 2, point.y))
                            return PlayerAction.MOVE_LEFT;
                        if(isSafe(map,point.x + 1,point.y) && isSafe(map,point.x + 2, point.y))
                            return PlayerAction.MOVE_RIGHT;
                        if(playerCycle.hasPowerup())
                            return PlayerAction.ACTIVATE_POWERUP;
                        break;
                    case DOWN: 
                        if(isSafe(map,point.x,point.y + 1) && isSafe(map,point.x, point.y + 2))
                            return PlayerAction.SAME_DIRECTION;
                        if(isSafe(map,point.x - 1,point.y) && isSafe(map,point.x - 2, point.y))
                            return PlayerAction.MOVE_LEFT;
                        if(isSafe(map,point.x + 1,point.y) && isSafe(map,point.x + 2, point.y))
                            return PlayerAction.MOVE_RIGHT;
                        if(playerCycle.hasPowerup())
                            return PlayerAction.ACTIVATE_POWERUP;
                        break;
                    }
                }

            switch(second) {
                case DOWN:  
                    if(isSafe(map,point.x,point.y + 1) && isSafe(map,point.x,point.y + 2))
                        return PlayerAction.MOVE_DOWN;
                    break;
                case UP: 
                    if(isSafe(map,point.x,point.y - 1) && isSafe(map,point.x,point.y - 2))
                                return PlayerAction.MOVE_UP;
                    break;    
                case RIGHT:
                    if(isSafe(map,point.x + 1,point.y) && isSafe(map,point.x + 2,point.y))
                                return PlayerAction.MOVE_RIGHT;
                    break; 
                case LEFT: 
                    if(isSafe(map,point.x,point.y) && isSafe(map,point.x,point.y + 2))
                                return PlayerAction.MOVE_LEFT;
                    break; 
                default: switch(playerCycle.getDirection()) { 
                    case LEFT: 
                        if(isSafe(map,point.x - 1,point.y) && isSafe(map,point.x - 2, point.y))
                            return PlayerAction.SAME_DIRECTION;
                        if(isSafe(map,point.x,point.y - 1) && isSafe(map,point.x, point.y - 2))
                            return PlayerAction.MOVE_UP;
                        if(isSafe(map,point.x,point.y + 1) && isSafe(map,point.x, point.y + 2))
                            return PlayerAction.MOVE_DOWN;
                        if(playerCycle.hasPowerup())
                            return PlayerAction.ACTIVATE_POWERUP;
                        break;
                    case RIGHT: 
                        if(isSafe(map,point.x + 1,point.y) && isSafe(map,point.x + 2, point.y))
                            return PlayerAction.SAME_DIRECTION;
                        if(isSafe(map,point.x,point.y - 1) && isSafe(map,point.x, point.y - 2))
                            return PlayerAction.MOVE_UP;
                        if(isSafe(map,point.x,point.y + 1) && isSafe(map,point.x, point.y + 2))
                            return PlayerAction.MOVE_DOWN;
                        if(playerCycle.hasPowerup())
                            return PlayerAction.ACTIVATE_POWERUP;
                        break;
                    case UP: 
                        if(isSafe(map,point.x,point.y - 1) && isSafe(map,point.x, point.y - 2))
                            return PlayerAction.SAME_DIRECTION;
                        if(isSafe(map,point.x - 1,point.y) && isSafe(map,point.x - 2, point.y))
                            return PlayerAction.MOVE_LEFT;
                        if(isSafe(map,point.x + 1,point.y) && isSafe(map,point.x + 2, point.y))
                            return PlayerAction.MOVE_RIGHT;
                        if(playerCycle.hasPowerup())
                            return PlayerAction.ACTIVATE_POWERUP;
                        break;
                    case DOWN: 
                        if(isSafe(map,point.x,point.y + 1) && isSafe(map,point.x, point.y + 2))
                            return PlayerAction.SAME_DIRECTION;
                        if(isSafe(map,point.x - 1,point.y) && isSafe(map,point.x - 2, point.y))
                            return PlayerAction.MOVE_LEFT;
                        if(isSafe(map,point.x + 1,point.y) && isSafe(map,point.x + 2, point.y))
                            return PlayerAction.MOVE_RIGHT;
                        if(playerCycle.hasPowerup())
                            return PlayerAction.ACTIVATE_POWERUP;
                        break;
                    }
                }
            
            
            return PlayerAction.SAME_DIRECTION;
        
    }
}

/**

8888888 8888888888 8 888888888o.      ,o888888o.     b.             8 
      8 8888       8 8888    `88.  . 8888     `88.   888o.          8 
      8 8888       8 8888     `88 ,8 8888       `8b  Y88888o.       8 
      8 8888       8 8888     ,88 88 8888        `8b .`Y888888o.    8 
      8 8888       8 8888.   ,88' 88 8888         88 8o. `Y888888o. 8 
      8 8888       8 888888888P'  88 8888         88 8`Y8o. `Y88888o8 
      8 8888       8 8888`8b      88 8888        ,8P 8   `Y8o. `Y8888 
      8 8888       8 8888 `8b.    `8 8888       ,8P  8      `Y8o. `Y8 
      8 8888       8 8888   `8b.   ` 8888     ,88'   8         `Y8o.` 
      8 8888       8 8888     `88.    `8888888P'     8            `Yo
      
                                Quick Guide
                --------------------------------------------

        1. THIS IS THE ONLY .JAVA FILE YOU SHOULD EDIT THAT CAME FROM THE ZIPPED STARTER KIT
        
        2. Any external files should be accessible from this directory

        3. newGame is called once at the start of the game if you wish to initialize any values
       
        4. getMove is called for each turn the game goes on

        5. map represents the game field. map.isOccupied(2, 2) returns whether or not something is at position (2, 2)
        								  map.tileType(2, 2) will tell you what is at (2, 2). A TileTypeEnum is returned.
        
        6. playerCycle is your lightcycle and is what the turn you respond with will be applied to.
                playerCycle.getPosition() is a Point object representing the (x, y) position
                playerCycle.getDirection() is the direction you are travelling in. can be compared with Direction.DIR where DIR is one of UP, RIGHT, DOWN, or LEFT
                playerCycle.hasPowerup() is a boolean representing whether or not you have a powerup
                playerCycle.isInvincible() is a boolean representing whether or not you are invincible
                playerCycle.getPowerupType() is what, if any, powerup you have
        
        7. opponentCycle is your opponent's lightcycle.

        8. You ultimately are required to return one of the following:
                                                PlayerAction.SAME_DIRECTION
                                                PlayerAction.MOVE_UP
                                                PlayerAction.MOVE_DOWN
                                                PlayerAction.MOVE_LEFT
                                                PlayerAction.MOVE_RIGHT
                                                PlayerAction.ACTIVATE_POWERUP
                                                PlayerAction.ACTIVATE_POWERUP_MOVE_UP
                                                PlayerAction.ACTIVATE_POWERUP_MOVE_DOWN
                                                PlayerAction.ACTIVATE_POWERUP_MOVE_LEFT
                                                PlayerAction.ACTIVATE_POWERUP_MOVE_RIGHT
      	
     
        9. If you have any questions, contact challenge@orbis.com
        
        10. Good luck! Submissions are due Sunday, September 21 at noon. 
            You can submit multiple times and your most recent submission will be the one graded.
 */