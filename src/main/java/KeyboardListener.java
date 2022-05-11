import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

 public class KeyboardListener extends KeyAdapter{
 	
 		public void keyPressed(KeyEvent e){
 		    switch(e.getKeyCode()){
				//if it's not the opposite direction
		    	case 39:	// -> Right
					if(ThreadsController.getDirectionSnake() != 1)
						ThreadsController.setDirectionSnake(0);
					break;
		    	case 38:	// -> Top
					if(ThreadsController.getDirectionSnake() != 3)
						ThreadsController.setDirectionSnake(2);
					break;
		    				
		    	case 37: 	// -> Left 
					if(ThreadsController.getDirectionSnake() != 0)
						ThreadsController.setDirectionSnake(1);
					break;
		    				
		    	case 40:	// -> Bottom
					if(ThreadsController.getDirectionSnake() != 2)
						ThreadsController.setDirectionSnake(3);
					break;

				case 27:  // -> Escape Key
					ThreadsController.pauseGame();

		    	
		    	default: 	break;
 		    }
 		}
 	
 }
