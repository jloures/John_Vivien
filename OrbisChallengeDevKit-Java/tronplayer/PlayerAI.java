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

        private int map_detail[][];
        private ArrayList <Point> power_ups;
        
   
	@Override
	public void newGame(TronGameBoard map,  
			LightCycle playerCycle, LightCycle opponentCycle) {
		power_ups = new ArrayList<>(); //initialize data structure which will hold coordinates to all powerups
                Point point_curr = new Point(); //initialize temp variable (only used in the loop)
                int size = map.length() - 2; //actual board is only (n - 2) ^2 big due to the fact that 2 wall tiles show up on each side
                for(int i = 0; i < size; i++) //for loop for identifying powerups
                    for(int j = 0; j < size; j++)
                        if(map.tileType(i, j).equals(TileTypeEnum.POWERUP)) {
                            point_curr.x = i;
                            point_curr.y = j;
                            power_ups.add(point_curr);
                        }
	}
	
	@Override
	public PlayerAction getMove(TronGameBoard map,
			LightCycle playerCycle, LightCycle opponentCycle, int moveNumber) {
		
                // Dummy variables used for the sole purpose of gathering info on the position and/or possible movements of the player       
                Point point = playerCycle.getPosition();
                TileTypeEnum up = map.tileType((int)point.getX(), (int)point.getY() - 1);
                TileTypeEnum down = map.tileType((int)point.getX(), (int)point.getY() + 1);
                TileTypeEnum right = map.tileType((int)point.getX() + 1, (int)point.getY());
                TileTypeEnum left = map.tileType((int)point.getX() - 1, (int)point.getY());
                
                //Determine your aim
                //if(ClosestPowerUp(map,playerCycle) == -1)
                    
                return StayAlive(point, map, playerCycle);
		 
        }
        
        //Refresh the current state of the PowerUps
        public void RefreshPowerUp(TronGameBoard map) {
        
            for(Point point: power_ups)
                if(!map.tileType((int)point.getX(), (int)point.getY()).equals(TileTypeEnum.POWERUP))
                    power_ups.remove(point);
        }
        
        //This function is used to determine the best way to get somewhere a.k.a AIM
        public PlayerAction GetToAim (TronGameBoard map, LightCycle playerCycle, LightCycle opponentCycle, Point aim) {
        
            Point CurrPos = new Point();
            CurrPos = playerCycle.getPosition(); 
            
            boolean BestPath = false;
            int i = CurrPos.x;
            int j = CurrPos.y;
            int Hdir = 0;
            int Vdir = 0;
            
            if(CurrPos.x > aim.x)   // shall turn left
                Hdir = -1;
            else if(CurrPos.x < aim.x)   // shall turn right
                Hdir = 1;
            //else stay the same
            
            if(CurrPos.y > aim.y)   // shall go down
                Vdir = -1;
            else if(CurrPos.y < aim.y)  //shall go up
                Vdir = 1;
            // else stay the same
            
            //try the best path I
            if(Hdir==-1 & Vdir==-1) {
                for(; i>=aim.x; i--){
                    for(; j>=aim.y; j--) {
                        if(map.tileType(i, j).equals(TileTypeEnum.WALL) 
                                || map.tileType(i, j).equals(TileTypeEnum.TRAIL)) {
                            j++;
                            break;
                        }
                    }
                    if(map.tileType(i, j).equals(TileTypeEnum.WALL) 
                                || map.tileType(i, j).equals(TileTypeEnum.TRAIL)) {
                        i++;
                        break;
                    }
                }
                if(i==aim.x & j== aim.y)   // best path exsists
                    BestPath = true;
            }

            else if(Hdir==-1 & Vdir==1) {
                for(; i>=aim.x; i--){
                    for(; j>=aim.y; j++) {
                        if(map.tileType(i, j).equals(TileTypeEnum.WALL) 
                                || map.tileType(i, j).equals(TileTypeEnum.TRAIL)) {
                            j--;
                            break;
                        }
                    }
                    if(map.tileType(i, j).equals(TileTypeEnum.WALL) 
                                || map.tileType(i, j).equals(TileTypeEnum.TRAIL)) {
                        i++;
                        break;
                    }
                }
                if(i==aim.x & j== aim.y)   // best path exsists
                    BestPath = true;   
            }

            else if(Hdir==1 & Vdir==-1) {
                for(; i>=aim.x; i++){
                    for(; j>=aim.y; j--) {
                        if(map.tileType(i, j).equals(TileTypeEnum.WALL) 
                                || map.tileType(i, j).equals(TileTypeEnum.TRAIL)) {
                            j++;
                            break;
                        }
                    }
                    if(map.tileType(i, j).equals(TileTypeEnum.WALL) 
                                || map.tileType(i, j).equals(TileTypeEnum.TRAIL)) {
                        i--;
                        break;
                    }
                }
                if(i==aim.x & j== aim.y)   // best path exsists
                    BestPath = true;
            }

            else if(Hdir==1 & Vdir ==1) {
                for(; i>=aim.x; i++){
                    for(; j>=aim.y; j++) {
                        if(map.tileType(i, j).equals(TileTypeEnum.WALL) 
                                || map.tileType(i, j).equals(TileTypeEnum.TRAIL)) {
                            j--;
                            break;
                        }
                    }
                    if(map.tileType(i, j).equals(TileTypeEnum.WALL) 
                                || map.tileType(i, j).equals(TileTypeEnum.TRAIL)) {
                        i--;
                        break;
                    }
                }
                if(i==aim.x & j== aim.y)   // best path exsists
                    BestPath = true;
            }
            
            //try the best path II
            else if(Hdir==-1) {   //where Vdir = 0, go straight left
                for(;i>=aim.x;i--){
                    if(map.tileType(i, j).equals(TileTypeEnum.WALL) 
                                || map.tileType(i, j).equals(TileTypeEnum.TRAIL)) {
                            i++;
                            break;
                        }
                }
                if(i==aim.x)   // best path exsists
                    BestPath = true;
            }
            
            else if(Hdir==1) {   //where Vdir = 0, go straight right
                for(;i<=aim.x;i++){
                    if(map.tileType(i, j).equals(TileTypeEnum.WALL) 
                                || map.tileType(i, j).equals(TileTypeEnum.TRAIL)) {
                            i--;
                            break;
                        }
                }
                if(i==aim.x)   // best path exsists
                    BestPath = true;
            }
            
            else if(Vdir==-1) {   //where Vdir = 0, go straight left
                for(;j>=aim.y;j--){
                    if(map.tileType(i, j).equals(TileTypeEnum.WALL) 
                                || map.tileType(i, j).equals(TileTypeEnum.TRAIL)) {
                            j++;
                            break;
                        }
                }
                if(j==aim.y)   // best path exsists
                    BestPath = true;
            }
            
            else if(Vdir==1) {   //where Vdir = 0, go straight left
                for(;j>=aim.y;j++){
                    if(map.tileType(i, j).equals(TileTypeEnum.WALL) 
                                || map.tileType(i, j).equals(TileTypeEnum.TRAIL)) {
                            j--;
                            break;
                        }
                }
                if(j==aim.y)   // best path exsists
                    BestPath = true;
            }
            
            //try some path
            
            return PlayerAction.SAME_DIRECTION;
        
        }
        
        //Method for finding the closest powerup according to the maps features
        //Returns -1 on failure (List is empty, no more powerups) or the index of the closest powerup
        
        public int ClosestPowerUp (TronGameBoard map, LightCycle playerCycle) {
        
            if(power_ups.isEmpty()) //No more powerups return -1
                return -1;
            
            //Refresh your powerup
            RefreshPowerUp(map);
            
            int short_distance = 10000; //The shortest distance to the closest powerup
            int index = 0; //Index of the closest powerup

            //iterate through all powerups in the list
            for(Point point: power_ups) {
                //calculate distance
                int distance = (int)(abs(point.getX() - playerCycle.getPosition().getX()) + abs(point.getY() - playerCycle.getPosition().getY()));
                //check if it is indeed the shortest distance
                if(distance <= short_distance) {
                    short_distance = distance;
                    index = power_ups.indexOf(point);
                }
            }
            
                return index;
        }
        
        /*The only thing this function does is to avoid nearby obstacles by moving the lightcycle to the closest empty tile*/
        
        public PlayerAction StayAlive (Point point, TronGameBoard map, LightCycle playerCycle) {
        
        switch(playerCycle.getDirection()) {
                    case DOWN: if(!map.isOccupied((int)point.getX(),(int)point.getY() + 1))
                                        return PlayerAction.SAME_DIRECTION;
                        else if(!map.isOccupied((int)point.getX() + 1,(int)point.getY()))
                                            return PlayerAction.MOVE_RIGHT;
                        else if(!map.isOccupied((int)point.getX() - 1,(int)point.getY()))
                                            return PlayerAction.MOVE_LEFT;
                        else if(playerCycle.hasPowerup())
                                             return PlayerAction.ACTIVATE_POWERUP;
                        break;
                        
                    case UP: if(!map.isOccupied((int)point.getX(),(int)point.getY() - 1))
                                        return PlayerAction.SAME_DIRECTION;
                        else if(!map.isOccupied((int)point.getX() + 1,(int)point.getY()))
                                            return PlayerAction.MOVE_RIGHT;
                        else if(!map.isOccupied((int)point.getX() - 1,(int)point.getY()))
                                            return PlayerAction.MOVE_LEFT;
                        else if(playerCycle.hasPowerup())
                                             return PlayerAction.ACTIVATE_POWERUP;
                        break;    
                     
                    case RIGHT: if(!map.isOccupied((int)point.getX() + 1,(int)point.getY()))
                                        return PlayerAction.SAME_DIRECTION;
                        else if(!map.isOccupied((int)point.getX(),(int)point.getY() - 1))
                                            return PlayerAction.MOVE_UP;
                        else if(!map.isOccupied((int)point.getX(),(int)point.getY() + 1))
                                            return PlayerAction.MOVE_DOWN;
                        else if(playerCycle.hasPowerup())
                                             return PlayerAction.ACTIVATE_POWERUP;
                        break; 
                        
                    case LEFT: if(!map.isOccupied((int)point.getX() - 1, (int)point.getY()))
                                        return PlayerAction.SAME_DIRECTION;
                        else if(!map.isOccupied((int)point.getX(),(int)point.getY() - 1))
                                            return PlayerAction.MOVE_UP;
                        else if(!map.isOccupied((int)point.getX(),(int)point.getY() + 1))
                                            return PlayerAction.MOVE_DOWN;
                        else if(playerCycle.hasPowerup())
                                             return PlayerAction.ACTIVATE_POWERUP;
                        break;  
                        
                    default: return PlayerAction.SAME_DIRECTION;
                        
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