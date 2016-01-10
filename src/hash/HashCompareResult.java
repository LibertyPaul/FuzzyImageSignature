package hash;

import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class HashCompareResult{
	private final int longestError;
	private final List<MarginedCharacter> incorrectCharacters;
	private final List<MarginedCharacter> correctCharacters;
	
	public HashCompareResult(final int characterCount, final int longestError, final List<MarginedCharacter> incorrectCharacters, final List<MarginedCharacter> correctCharacters){
		assert correctCharacters.size() >= 0;
		assert incorrectCharacters.size() >= longestError;
		assert longestError >= 0;
		
		this.longestError = longestError;
		this.incorrectCharacters = incorrectCharacters;
		this.correctCharacters = correctCharacters;
	}
	
	private int getCharacterCount(){
		return this.correctCharacters.size() + this.incorrectCharacters.size();
	}
	
	private int getErrorCount(){
		return this.incorrectCharacters.size();
	}
	
	private double getErrorProbability(){
		return (double)this.getErrorCount() / this.getCharacterCount();
	}
	
	private double getLongestStreakProbability(){
		//считает вероятность возникновения последовательности из N ошибок при вероятности одиночной ошибки
		double streakProb = 1;
		
		for(int i = 0; i < this.longestError; ++i){
			streakProb *= (double)(this.getErrorCount() - i) / (this.getCharacterCount() - i);
		}
		
		return streakProb;
	}
	
	public boolean isGenuine(){
		final double errorProbabilityBound = 0.3;//допускается 30% статистических ошибок
		if(this.getErrorProbability() > errorProbabilityBound){
			return false;
		}
		
		final double longestStreakProb = this.getLongestStreakProbability();
		final double streakProbBound = 0.1;
		
		if(longestStreakProb < streakProbBound){
			return false;
		}
		
		return true;
	}
	
	private static Mat drawCharactersRect(final Mat srcImage, final List<MarginedCharacter> characters, Scalar color){
		final Mat srcCopy = srcImage.clone();
		
		for(final MarginedCharacter mc : characters){
			Imgproc.rectangle(srcCopy, mc.getRect().tl(), mc.getRect().br(), color, 2);
		}
		
		return srcCopy;
	}
	
	public Mat drawIncorrect(final Mat srcImage){
		final Scalar color = new Scalar(0, 0, 255);
		return HashCompareResult.drawCharactersRect(srcImage, this.incorrectCharacters, color);
	}
	
	public Mat drawCorrect(final Mat srcImage){
		final Scalar color = new Scalar(0, 255, 0);
		return HashCompareResult.drawCharactersRect(srcImage, this.correctCharacters, color);
	}
	
	
}
