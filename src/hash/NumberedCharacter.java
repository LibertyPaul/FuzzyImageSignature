package hash;

import org.opencv.core.Rect;


public class NumberedCharacter extends PositionedField{
	private final CharacterId id;
	
	public NumberedCharacter(final Rect rect, final CharacterId id){
		super(rect);
		assert id != null && rect != null;
		this.id = id;
	}
	
	public boolean equals(final NumberedCharacter o){
		return this.id.equals(o.id);
	}
	
	public CharacterId getId(){
		return this.id.clone();
	}
}
