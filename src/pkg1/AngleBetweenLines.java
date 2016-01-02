package pkg1;

public class AngleBetweenLines implements Comparable<AngleBetweenLines>{
	public final ThreePoints line1;
	public final ThreePoints line2;
	public double angle;
	
	public AngleBetweenLines(ThreePoints line1, ThreePoints line2, double angle){
		this.line1 = line1;
		this.line2 = line2;
		while(angle < 0){
			angle += Math.PI * 2 ;
		}
		this.angle = angle; 
	}

	@Override
	public int compareTo(AngleBetweenLines o){
		if(Math.abs(this.angle) < Math.abs(o.angle))
			return 1;
		else if(Math.abs(this.angle) == Math.abs(o.angle))
			return 0;
		else
			return -1;
	}
}
