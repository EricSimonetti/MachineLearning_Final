class Tuple {
	private int x, y, xf, yf;

	Tuple(int x, int y) {
		this.x = x;
	    this.y = y; 
	}
	void ChangeData(int x, int y){
		this.x = x;
		this.y = y;
	}

	public boolean equals(Object o) {
		if(o == this) return true;
		if (!(o instanceof Tuple)) {
			return false;
		}
		Tuple t = (Tuple) o;
		return t.getX() == x && t.getY() == y;
	}
	double distance(Tuple other){
	    return Math.sqrt(Math.pow(other.x - x, 2)+Math.pow(other.y - y, 2));
    }
	int getX(){ return x; }
	int getY(){ return y; }
	int getXf(){ return xf; }
	int getYf(){ return yf; }
} 
