import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.chen0040.rl.actionselection.*;
import com.github.chen0040.rl.learning.qlearn.QAgent;
import com.github.chen0040.rl.learning.qlearn.QLambdaLearner;
import com.github.chen0040.rl.learning.qlearn.QLearner;

class ThreadsController extends Thread {
	private long delay = 0;
	private Tuple headSnakePos;
	private int sizeSnake;
	private Tuple positionDepart;
	private ArrayList<ArrayList<DataOfSquare>> Squares;
	private ArrayList<Tuple> positions;
	private Tuple tailPosition;
	private Tuple foodPosition;
	private static boolean pauseGame = false;
	private static int directionSnake;
	private boolean willEat = false;
	private boolean closer = false;
	private int gridSize;
	private int network;
	private int score;

	ThreadsController(Tuple positionDepart, int gridSize, int network){
		this.gridSize = gridSize;
		this.positionDepart = positionDepart;
		this.network = network;
		initialize();
		foodPosition = getValAreaNotInSnake();
		spawnFood(foodPosition);
	}

	private void initialize(){
        score = 0;
		Squares = new ArrayList<>();
		positions = new ArrayList<>();
		sizeSnake=5;

		Squares = Window.getGrid();
		headSnakePos = new Tuple(positionDepart.getX(),positionDepart.getY());
		directionSnake = 1;
        willEat = false;

        tailPosition = new Tuple(headSnakePos.getX()+sizeSnake, headSnakePos.getY());
        for(int i = sizeSnake-1; i>0; i--)
            positions.add(new Tuple(headSnakePos.getX()+i, headSnakePos.getY()));
		Tuple headPos = new Tuple(headSnakePos.getX(),headSnakePos.getY());
		positions.add(headPos);
		redraw();
	}
	 
	//delay between each memory of the snake
	private void pause(){
		try {
			sleep(delay);
		} catch (InterruptedException e) {
				e.printStackTrace();
		}
	}

	private void eatFood(){
        score++;
        System.out.println("ate");
        sizeSnake++;
        foodPosition = getValAreaNotInSnake();
        spawnFood(foodPosition);
    }
	 
	//Stops The Game
	private void reset(){
		//deletes the old snake
		for (Tuple t: positions)
			Squares.get(t.getY()).get(t.getX()).lightMeUp(2);
		//System.out.println("\nGameOver\n");
		initialize();
	}

	 
	//Put food in a position and displays it
	private void spawnFood(Tuple foodPositionIn){
		Squares.get(foodPositionIn.getY()).get(foodPositionIn.getX()).lightMeUp(1);
	}
	 
	//return a position not occupied by the snake
	private Tuple getValAreaNotInSnake(){
		Tuple p ;
		int ranX = (int) (Math.random() * (gridSize-1));
		int ranY = (int) (Math.random() * (gridSize-1));
		p = new Tuple(ranX,ranY);
		for(int i = 0; i < positions.size(); i++){
			if(p.getX() == positions.get(i).getX() && p.getY() == positions.get(i).getY()){
				ranX = (int) (Math.random() * (gridSize-1));
				ranY = (int) (Math.random() * (gridSize-1));
				p = new Tuple(ranX,ranY);
				i = 0;
			}
		}
		return p;
	}

	private boolean wouldDie(int dir){
        Tuple newHead = null;
	    switch (dir){
            case 0: //right
                newHead = new Tuple(headSnakePos.getX()+1, headSnakePos.getY());
                if(newHead.getX() >= gridSize)
                    return true;
                break;
            case 1: //left
                newHead = new Tuple(headSnakePos.getX()-1, headSnakePos.getY());
                if(newHead.getX() < 0)
                    return true;
                break;
            case 2: //up
                newHead = new Tuple(headSnakePos.getX(), headSnakePos.getY()-1);
                if(newHead.getY() < 0)
                    return true;
                break;
            default: //down
                newHead = new Tuple(headSnakePos.getX(), headSnakePos.getY()+1);
                if(newHead.getY() >= gridSize)
                    return true;
        }
        if(newHead.equals(foodPosition)) willEat = true;
        else willEat = false;
        closer = newHead.distance(foodPosition) < headSnakePos.distance(foodPosition);
        for(int i = 1; i<positions.size()-1; i++){
            if(newHead.equals(positions.get(i)))
                return true;
        }
        return false;
    }

	//adds new head, removes tail
	//0:right 1:left 2:top 3:bottom
	private void moveSnake(int dir){
	 	switch(dir){
	 		case 3: //down
	 			headSnakePos.ChangeData(headSnakePos.getX(), (headSnakePos.getY() + 1));
	 			directionSnake = 3;
		 		break;
		 	case 2: //up
		 		headSnakePos.ChangeData(headSnakePos.getX(),headSnakePos.getY()-1);
                directionSnake = 2;
		 		break;
		 	case 1: //left
		 		headSnakePos.ChangeData(headSnakePos.getX()-1,headSnakePos.getY());
                directionSnake = 1;
		 		break;
		 	case 0: //right
		 		headSnakePos.ChangeData(headSnakePos.getX() + 1, headSnakePos.getY());
                directionSnake = 0;
				break;
	 	}
	 	positions.add(new Tuple(headSnakePos.getX(),headSnakePos.getY()));
	 	if(headSnakePos.equals(foodPosition)){
	 	    eatFood();
	 	}
	 	else{
	 	    tailPosition = positions.remove(0);
	 	}
	}
	 
	//redraws snake
	private void redraw(){
        int y = tailPosition.getY();
        int x = tailPosition.getX();
        Squares.get(y).get(x).lightMeUp(2);

        for(Tuple snakePiece : positions){
            y = snakePiece.getY();
            x = snakePiece.getX();
            Squares.get(y).get(x).lightMeUp(0);
        }
	}

	static void pauseGame(){ pauseGame = !pauseGame;}
	static int getDirectionSnake() { return directionSnake; }
	static void setDirectionSnake(int directionSnake) { ThreadsController.directionSnake = directionSnake; }


	//<------------------- neural net stuff -------------------------->
    private int stateId;
	private ArrayList<Memory> replayMemory = new ArrayList<>();
	private ArrayList<Integer> state = new ArrayList<>(Arrays.asList(0,0,0,0,0,0,0,0,0,0,0,0));
	private final int ACTION_COUNT = 3;   //number of possible actions
	//private final int STATE_COUNT = 4095; //number of possible states
    private final int STATE_COUNT = 256; //number of possible states
	private final int ITERATIONS = 1000000; //number of iterations
    private final double LAMBDA = .001;
    private ArrayList<Integer> scores = new ArrayList<>();


	private void update(int action) {
		if(!pauseGame) {
		    boolean wouldDie = wouldDie(action);
		    if(wouldDie){
		        directionFromAction(action);
		        reset();
            }
		    else{
                moveSnake(action);
                redraw();
            }
		}
		pause();
	}

	public void run(){
		if(network == 0){
			while (true) update(directionSnake);
		}


        ObjectMapper mapper = new ObjectMapper();
        QLearner agent = new QLearner(STATE_COUNT, ACTION_COUNT);
        //agent.getLearner().setActionSelection(GibbsSoftMaxActionSelectionStrategy.class.getCanonicalName());
        //agent.setLambda(LAMBDA);
		Random random = new Random();

		//agent.start(random.nextInt(STATE_COUNT));

		for(int i = 0; i < ITERATIONS; i++){
		    //stateId = getStateId(state);
            int actionId;
		    if(i < ITERATIONS/2){
		        actionId = random.nextInt(3);
                //System.out.println("Random action performed: " + actionId);
            }
		    else {
                actionId = Math.abs(agent.selectAction(random.nextInt(256)).getIndex());
                //System.out.println("Action performed: " + actionId);
            }
            //System.out.println("Direction From Action: " + directionFromAction(actionId));
		    //System.out.println("Snake Direction: " + directionSnake);
            //System.out.println("Score: " + score);
		    //System.out.println("Games Played: " + scores.size());

            state = getCurrentState();
            double reward = getReward(actionId);
			update(directionFromAction(actionId));

			//int newStateId = getStateId(state);
            int newStateId = differentState();
			//System.out.println("New State: " + newStateId);
			System.out.println("Reward: " + reward);

			agent.update(actionId, newStateId, stateId, reward);
		}

        for(Memory memory : replayMemory){
            agent.update(memory.oldState, memory.action, memory.newState, memory.reward);
        }
        delay = 50;
        for(int i = 0; i < ITERATIONS; i++){
            //stateId = getStateId(state);
            stateId = differentState();
            int actionId = Math.abs(agent.selectAction(stateId).getIndex());
            System.out.println("Action id:" + actionId);
            update(directionFromAction(actionId));
        }

		scores.add(score);
		System.out.println("added final score of: " + score);
		int gamesPlayed = scores.size();
		double totalScore = scores.stream().reduce(0, Integer::sum);
		System.out.println("average: " + totalScore / gamesPlayed);
        System.out.println("Total score: " + totalScore);

	}

	private int differentState(){
	    StringBuilder newState = new StringBuilder();
        switch(directionSnake){
            case (0): //right
                newState.append("00");
                break;
            case (1): //left
                newState.append("01");
                break;
            case (2): //up
                newState.append("10");
                break;
            default: //down (sake dir = 3)
                newState.append("11");
                break;
        }
        if(wouldDie(directionFromAction(2))) newState.append("1");
        else newState.append("0");
        if(wouldDie(directionFromAction(1))) newState.append("1");
        else newState.append("0");
        if(wouldDie(directionFromAction(0))) newState.append("1");
        else newState.append("0");

        int foodX = headSnakePos.getX()-foodPosition.getX(),
                foodY = headSnakePos.getY()-foodPosition.getY();
        if(foodX == 0 && foodY>0) newState.append("000");
        else if(foodX == 0 && foodY<0) newState.append("001");
        else if(foodY == 0 && foodX<0) newState.append("010");
        else if(foodY == 0 && foodX>0) newState.append("011");
        else if(foodX > 0 && foodY>0) newState.append("100");
        else if(foodX < 0 && foodY>0) newState.append("101");
        else if(foodX > 0) newState.append("110");
        else newState.append("111");


        return Integer.parseInt(newState.toString(),2);
    }

	private ArrayList<Integer> getCurrentState(){
		ArrayList<Integer> currentState = new ArrayList<>(Arrays.asList(0,0,0,0,0,0,0,0,0,0,0,0));
		//get direction
		switch(directionSnake){
			case (0): //right
				currentState.set(0,1);
				break;
			case (1): //left
				currentState.set(1,1);
				break;
			case (2): //up
				currentState.set(2,1);
				break;
			default: //down (sake dir = 3)
				currentState.set(3,1);
				break;
		}

		//get danger
        if(wouldDie(directionFromAction(2))) currentState.set(4,1);
        if(wouldDie(directionFromAction(1))) currentState.set(5,1);
        if(wouldDie(directionFromAction(0))) currentState.set(6,1);

		if(foodPosition.getX() > headSnakePos.getX()){
			currentState.set(7,1);
		}
		else if(foodPosition.getX() < headSnakePos.getX()){
			currentState.set(8,1);
		}
		if(foodPosition.getY() < headSnakePos.getY()){
			currentState.set(9,1);
		}
		else if(foodPosition.getY() > headSnakePos.getY()){
			currentState.set(10,1);
		}

		int foodX = Math.abs(headSnakePos.getX()-foodPosition.getX());
		int foodY = Math.abs(headSnakePos.getY()-foodPosition.getY());
		if((foodX == 1 && foodY == 0) || (foodX == 0 && foodY == 1)) currentState.set(11,1);
		return currentState;
	}

	private int getReward(int action){
		int reward = 0;
		boolean willDie = wouldDie(directionFromAction(action));
		if (willEat){
			reward = -10;
		}
		else if (willDie){
		    scores.add(score);
            System.out.println("added score of: " + score);
		    reward = 10;
        }
        //else if(closer){
            //reward = 1;
            else if(action == 0){
                reward = -2;
            }
        //}
        //else if(action == 1){
        //    reward = -10;
        //}
		return reward;
	}

	private int getStateId(ArrayList<Integer> stateArray){ //converts state from binary to decimal
		StringBuilder currentState = new StringBuilder();
		for(Integer elemet : state)
			currentState.append(elemet);
		return Integer.parseInt(currentState.toString(),2);
	}

	static int directionFromAction(int action){
	    if(action == 0){
	        return directionSnake;
        }
	    switch(directionSnake){
	        case 0: //right
                if(action == 1)
                    return 2;
                return 3;
            case 1: //left
                if(action == 1)
                    return 3;
                return 2;
            case 2: //up
                if(action == 1)
                    return 1;
                return 0;
            case 3: //down
                if(action == 1)
                    return 0;
                return 1;
        }
	    return -1;
    }

	private class Memory{
		int oldState;
		int newState;
		int action;
		double reward;

		public Memory(int oldState, int action, int newState, double reward) {
			this.oldState = oldState;
			this.newState = newState;
			this.reward = reward;
			this.action = action;
		}
	}
}
