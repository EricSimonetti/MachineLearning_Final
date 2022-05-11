import java.awt.GridLayout;
import java.awt.event.KeyListener;
import java.util.ArrayList;

import javax.swing.JFrame;


class Window extends JFrame{
	private static ArrayList<ArrayList<DataOfSquare>> Grid;

	Window(int gridSize, int network){
		Grid = new ArrayList<ArrayList<DataOfSquare>>();
		ArrayList<DataOfSquare> data;
		
		// Creates Threads and its data and adds it to the arrayList
		for(int i=0;i<gridSize;i++){
			data= new ArrayList<DataOfSquare>();
			for(int j=0;j<gridSize;j++){
				DataOfSquare c = new DataOfSquare(2);
				data.add(c);
			}
			Grid.add(data);
		}
		
		// Setting up the layout of the panel
		getContentPane().setLayout(new GridLayout(gridSize,gridSize,0,0));
		
		// Start & pauses all threads, then adds every square of each thread to the panel
		for(int i=0;i<gridSize;i++){
			for(int j=0;j<gridSize;j++){
				getContentPane().add(Grid.get(i).get(j).square);
			}
		}
		
		// initial position of the snake
		Tuple position = new Tuple(gridSize/2,gridSize/2);
		// passing this value to the controller
		ThreadsController c = new ThreadsController(position, gridSize, network);
		//Let's start the game now..
		c.start();

		// Links the window to the keyboardlistenner, only if human is selected to play
		if(network == 0)
			this.addKeyListener(new KeyboardListener());
	}

	static ArrayList<ArrayList<DataOfSquare>> getGrid() { return Grid; }
}
