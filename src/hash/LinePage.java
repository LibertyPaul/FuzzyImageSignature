package hash;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
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
	
	public List<MarginedLine> getOrderedCharacterLines(final double pageSize) throws Exception{
		List<MarginedLine> result = new ArrayList<>(this.lines.size());
		
		for(final CharacterLine line : this.lines){
			List<MarginedCharacter> characters = new ArrayList<>();
			NumberedCharacter previous = line.getLine().get(0);
			final MarginedCharacter first = new MarginedCharacter(0, previous.getId(), previous.getRect());
			characters.add(first);
			
			double lineWidth_abs = line.width();
			
			for(int i = 1; i < line.getLine().size(); ++i){
				NumberedCharacter current = line.getLine().get(i);
				double fromStart = current.leftX() - line.leftX();
				double fromEnd 	 = line.rightX() - current.leftX();
				double position_rel = fromStart / (fromStart + fromEnd); 
				
				final MarginedCharacter currentCharacter = new MarginedCharacter(position_rel, line.getLine().get(i).getId(), current.getRect());
				characters.add(currentCharacter);
				
				previous = current;
			}
			
			double lineWidth_rel = lineWidth_abs / pageSize;
			MarginedLine currentLine = new MarginedLine(characters, lineWidth_rel);
			result.add(currentLine);
		}
		
		return result;
	}
}




