package pkg1;

import org.opencv.core.Point;

public class PageCoords{
	protected Point upperLeft;
	protected Point upperRight;
	protected Point middleLeft;
	protected Point middleRight;
	protected Point lowerLeft;
	protected Point lowerRight;
	
	
	public PageCoords(	Point upperLeft, Point upperRight,
						Point middleLeft, Point middleRight,
						Point lowerLeft, Point lowerRight) throws Exception{
		if(	upperLeft	== null ||
			upperRight	== null ||
			middleLeft	== null ||
			middleRight	== null ||
			lowerLeft	== null ||
			lowerRight	== null)
			throw new Exception("points can't be null");
		
		this.upperLeft		= upperLeft;
		this.upperRight		= upperRight;
		this.middleLeft		= middleLeft;
		this.middleRight	= middleRight;
		this.lowerLeft		= lowerLeft;
		this.lowerRight		= lowerRight;
	}
	
	public Point getUpperLeft(){
		return this.upperLeft.clone();
	}
	
	public Point getUpperRight(){
		return this.upperRight.clone();
	}

	public Point getMiddleLeft(){
		return this.middleLeft.clone();
	}
	
	public Point getMiddleRight(){
		return this.middleRight.clone();
	}
	
	public Point getLowerLeft(){
		return this.lowerLeft.clone();
	}
	
	public Point getLowerRight(){
		return this.lowerRight.clone();
	}
}