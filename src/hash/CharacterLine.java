package hash;

import java.util.LinkedList;
import java.util.List;

public class CharacterLine{
	protected List<NumberedCharacter> characters;
	protected double higherBound;//самая верхняя координата среди символов в строке
	protected double lowerBound;//самая нижняя
	//в opencv [0,0] - левый верхний угол, так что higherBound <= lowerBound
	
	public CharacterLine(NumberedCharacter character){
		this.characters  = new LinkedList<>();
		this.higherBound = character.topY();
		this.lowerBound  = character.bottomY();
		this.characters.add(character);
	}
	
	public boolean isInLine(NumberedCharacter character){
		double characterTop = character.topY();
		double characterBottom  = character.bottomY();
		
		return 	(
					(
						characterTop >= this.higherBound && characterTop <= this.lowerBound
					) ||
					(
						characterBottom >= this.higherBound && characterBottom <= this.lowerBound
					)
				) ||
				( 
					characterTop < this.higherBound && characterBottom > this.lowerBound 
				);
	}
	
	private void insertCharacter(NumberedCharacter character){
		for(int pos = 0; pos < this.characters.size(); ++pos){
			if(this.characters.get(pos).leftX() > character.leftX()){
				this.characters.add(pos, character);
				return;
			}
		}
		this.characters.add(this.characters.size(), character);
	}
	
	public void add(NumberedCharacter character){
		assert this.isInLine(character);
		
		this.insertCharacter(character);
		
		this.higherBound = Math.min(this.higherBound, character.topY());
		this.lowerBound = Math.max(this.lowerBound, character.bottomY());
	}
	
	public List<NumberedCharacter> getLine(){
		return this.characters;
	}
	
	public double topY(){
		return this.higherBound;
	}
	
	public double bottomY(){
		return this.lowerBound;
	}
	
	public double leftX(){
		return this.characters.get(0).leftX();
	}
	
	public double rightX(){
		return this.characters.get(this.characters.size() - 1).rightX();
	}
	
	public double width(){
		return this.rightX() - this.leftX();
	}

}
