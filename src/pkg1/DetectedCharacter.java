package pkg1;

import org.opencv.core.Rect;

public class DetectedCharacter extends PositionedField{
	protected final CharacterMatrix matrix;
	
	public DetectedCharacter(final Rect rect, final CharacterMatrix matrix){
		super(rect);
		assert matrix != null;
		
		this.matrix = matrix;
	}
	
	public DetectedCharacter(final DetectedCharacter character){
		super(character.getRect());
		this.matrix = character.matrix;
	}
	
	public CharacterMatrix getMatrix(){
		return this.matrix;
	}
}
