import java.awt.Color;
import javax.swing.JPanel;

class SquarePanel extends JPanel{
	
	private static final long serialVersionUID = 1L;

	SquarePanel(Color d){
		this.setBackground(d);
	}
	
	void ChangeColor(Color d){
		this.setBackground(d);
		this.repaint();
	}
	
}

