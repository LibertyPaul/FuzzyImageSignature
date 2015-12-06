package pkg1;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class LinePage{
	protected List<CharacterLine> lines;
	
	public LinePage(){
		this.lines = new LinkedList<>();
	}
	
	protected void addLine(CharacterLine line){
		for(int pos = 0; pos < lines.size(); ++pos){
			if(this.lines.get(pos).topY() < line.topY()){
				this.lines.add(pos, line);
				return;
			}
		}
		this.lines.add(this.lines.size(), line);
	}
	
	public void addCharacter(final NumberedCharacter character){
		for(final CharacterLine line : this.lines){
			if(line.isInLine(character)){
				line.add(character);
				return;
			}
		}
		
		CharacterLine line = new CharacterLine(character);
		this.addLine(line);
	}
	
	
	public Mat drawLines(final Mat srcImage){
		Mat result = srcImage.clone();
		for(final CharacterLine line : this.lines){
			Rect rect = new Rect();
			rect.x = 0;
			rect.y = (int)line.topY();
			rect.width = result.cols();
			rect.height = (int)line.bottomY() - (int)line.topY();
			
			Imgproc.rectangle(result, rect.tl(), rect.br(), new Scalar(255, 0,0));
		}
		return result;
	}
	
	public List<CharacterId> gerOrderedCharacters(){
		int charactersCount = 0;
		for(final CharacterLine line : this.lines){
			charactersCount += line.getCharacterCount();
		}
		
		List<CharacterId> result = new ArrayList<>(charactersCount);
		
		for(final CharacterLine line : this.lines){
			for(final NumberedCharacter character : line.getLine()){
				result.add(character.getId());
			}
		}
		
		return result;
	}
}




