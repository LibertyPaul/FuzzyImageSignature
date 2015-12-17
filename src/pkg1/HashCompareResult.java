package pkg1;

import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class HashCompareResult{
	private final int errorCount;
	private final int longestError;
	private final List<MarginedCharacter> incorrectCharacters;
	
	public HashCompareResult(final int errorCount, final int longestError, final List<MarginedCharacter> incorrectCharacters){
		this.errorCount = errorCount;
		this.longestError = longestError;
		this.incorrectCharacters = incorrectCharacters;
	}
	
	public double getScore(){
		final int maxErrorSize = 5;
		if(this.longestError > maxErrorSize){
			return 1.0;
		}
		else{
			return 0.0;
		}
	}
	
	public Mat putErrors(final Mat srcImage){
		final Mat srcCopy = srcImage.clone();
		final Scalar color = new Scalar(0, 0, 255);
		
		for(final MarginedCharacter mc : this.incorrectCharacters){
			Imgproc.rectangle(srcCopy, mc.getRect().tl(), mc.getRect().br(), color);
		}
		
		return srcCopy;
	}
}
