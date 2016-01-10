package hash;

import org.opencv.core.Rect;

public class PositionedField{
	private final Rect rect;
	
	public PositionedField(final Rect rect){
		assert rect != null;
		
		this.rect = rect;
	}
	
	public Rect getRect(){
		return this.rect;
	}
	
	public double leftX(){
		return this.getRect().x;
	}
	
	public double rightX(){
		return this.getRect().x + this.getRect().width;
	}
	
	public double topY(){
		return this.getRect().y;
	}
	
	public double bottomY(){
		return this.getRect().y + this.getRect().height;
	}
}
