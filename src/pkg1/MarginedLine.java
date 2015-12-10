package pkg1;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MarginedLine{
	private final List<MarginedCharacter> characters;
	private final double relativeWidth;
	
	public MarginedLine(final List<MarginedCharacter> characters, final double relativeWidth){
		assert characters != null;
		assert relativeWidth > 0;
		
		this.characters = characters;
		this.relativeWidth = relativeWidth;
	}
	
	public List<MarginedCharacter> getCharacters(){
		return this.characters;
	}
	
	public double getRelativeWidth(){
		return this.relativeWidth;
	}
	
	public String toString(final int groupSize){
		String result = "";
		for(final MarginedCharacter character : this.characters){
			try{
				result += character.toString(groupSize);
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		result += String.format("%0" + groupSize + "d", 0) + String.format("%0" + groupSize + "d", (int)this.getRelativeWidth());
		return result;
	}
	
	private static double calcPositionError(final List<MarginedCharacter> line1, final List<MarginedCharacter> line2){
		assert line1.size() == line2.size();
		
		double errorSum = 0;
		for(int pos = 0; pos < line1.size(); ++pos){
			errorSum += Math.abs(line1.get(pos).getPosition() - line2.get(pos).getPosition());
		}
		return errorSum / line1.size();
	}
	
	private List<CharMatch> getCharacterPairs(final MarginedLine o){
		final double approxCharSize = (this.relativeWidth / this.characters.size() + o.relativeWidth / o.characters.size()) / 2; 
		final double widthDiff = this.relativeWidth - o.relativeWidth;
		
		double charDiffSum = 0;
		if(Math.abs(widthDiff) > approxCharSize * 0.75){
			//строки отличаются по размеру больше чем на 1 символ
			if(widthDiff > 0){
				//потерян крайний символ в нижней строке
				
				double lostPart = widthDiff / o.relativeWidth;//на какую долю сократилась строка из за потери 
				//предположим, что он был в начале:
				int i1 = 0;
				double skipped = 0;
				while(Math.abs(lostPart - skipped) > approxCharSize * 0.75){
					++i1;
					assert i1 != this.characters.size();
				}
				
				
				for(int i1 = 0, i2 = 0; i1 < this.characters.size() && i2 < o.characters.size(); ++i1, ++i2){
					MarginedCharacter char1 = this.characters.get(i1);
					MarginedCharacter char2 = o.characters.get(i2);
					
					
				}
				
			}
		}
		
	}
	
	public Map<CharacterId, List<CharacterId>> matchLines(final MarginedLine o){
		//double sizeDiff = (this.getRelativeWidth() - o.getRelativeWidth()) * 1000 / o.getRelativeWidth();
		
		Map<CharacterId, List<CharacterId>> result = new TreeMap<>();
		
		final double approxCharSize = (this.relativeWidth / this.characters.size() + o.relativeWidth / o.characters.size()) / 2;
		
		for(int i1 = 0, i2 = 0; i1 < this.characters.size() && i2 < o.characters.size(); ++i1, ++i2){
			MarginedCharacter char1 = this.characters.get(i1);
			MarginedCharacter char2 = this.characters.get(i2);
			
			double posDiff = char1.getRelativePosition() - char2.getRelativePosition();
			if(Math.abs(posDiff) > approxCharSize){
				//символ выпал
				if(posDiff > 0){
					//выпал из this
					--i2;
				}
				else{
					//выпал из o
					--i1;
				}
			}
			else{
				//всё ок, символы рядом
				List<CharacterId> currentIds = result.get(char1);
				currentIds.add(char2.getId());
				//result.put(char1.getId(), currentIds);//если не работает - раскоментить.
			}
		}
		
		return result;
	}
	
}






