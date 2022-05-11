import javax.swing.JFrame;

public class Main {
	private static final int GRID_SIZE = 20;
	private static final int INITIAL_SNAKE_SIZE = 3;

	public static void main(String[] args) {

		//Creating the window with all its awesome snaky features
		Window f1= new Window(GRID_SIZE, 1);
		
		//Setting up the window settings
		f1.setTitle("Snake");
		f1.setSize(700,700);
		f1.setVisible(true);
		f1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
