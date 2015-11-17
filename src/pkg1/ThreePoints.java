package pkg1;


import org.opencv.core.Point;

public class ThreePoints{
	protected Point first;
	protected Point between;
	protected Point last;
	
	public ThreePoints(Point p1, Point p2, Point p3){
		double dist12 = ThreePoints.getDistance(p1, p2);
		double dist23 = ThreePoints.getDistance(p2, p3);
		double dist31 = ThreePoints.getDistance(p3, p1);
		
		if(dist12 > dist23 && dist12 > dist31){
			this.first		= p1.clone();
			this.between 	= p3.clone();
			this.last		= p2.clone();
		}
		else if(dist23 > dist12 && dist23 > dist31){
			this.first		= p2.clone();
			this.between 	= p1.clone();
			this.last		= p3.clone();
		}
		else{
			this.first		= p1.clone();
			this.between 	= p2.clone();
			this.last		= p3.clone();
		}
		
		double dist1 = ThreePoints.getDistance(this.first, this.between);
		double dist2 = ThreePoints.getDistance(this.between, this.last);
		if(dist1 < dist2){//first ---------------------- between ----- last
			Point tmp = this.first;
			this.first = this.last;
			this.last = tmp;
		}
	}
		
	public static double getDistance(Point p1, Point p2){
		return Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
	}
	
	public static Point getMiddle(Point p1, Point p2){
		double xCoord = (p1.x + p2.x) / 2;
		double yCoord = (p1.y + p2.y) / 2;
		return new Point(xCoord, yCoord);
	}
	
	public double getAngle(){//угол к OX (-pi / 2; pi / 2)
		Point normalized = new Point();
		normalized.x = this.getLast().x - this.getFirst().x;
		normalized.y = this.getLast().y - this.getFirst().y;
		return Math.atan2(normalized.y, normalized.x);
	}
	
	public static double getAngle(ThreePoints rhs, ThreePoints lhs) throws Exception{
		double rhsAngle = rhs.getAngle();
		double lhsAngle = lhs.getAngle();
		
		double angle = rhsAngle - lhsAngle;
		return angle;
	}
	
	public Point getFirst(){
		return this.first.clone();
	}
	
	public Point getBetween(){
		return this.between.clone();
	}
	
	public Point getLast(){
		return this.last.clone();
	}
	
	public double getDistance(){
		return ThreePoints.getDistance(this.getFirst(), this.getLast());
	}
	
	public String toString(){
		String res = new String();
		res += "First" + String.format("[%.0f; %.0f]\n", this.getFirst().x, this.getFirst().y);
		res += "Between" + String.format("[%.0f; %.0f]\n", this.getBetween().x, this.getBetween().y);
		res += "Last" + String.format("[%.0f; %.0f]\n", this.getLast().x, this.getLast().y);
		
		return res;
	}
}