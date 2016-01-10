package hash;

import java.io.File;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;


public class CharacterMatrix{
	protected final Mat character;
	
	public CharacterMatrix(Mat character){
		Mat charCopy = character.clone();
		if(charCopy.channels() != 1){
			Mat bnw = new Mat();
			Imgproc.cvtColor(character, bnw, Imgproc.COLOR_BGR2GRAY);
			charCopy = bnw;
		}
		
		Mat thresholded = new Mat(); 
		Imgproc.threshold(charCopy, thresholded, 127, 255, Imgproc.THRESH_BINARY);
			
		this.character = thresholded;
	}
	
	private static int countSamePixels(Mat image1, Mat image2){
		Mat samePixels = new Mat();
		Core.bitwise_xor(image1, image2, samePixels);
		return Core.countNonZero(samePixels);
	}
	
	private static double compareByPoints(final Mat img1, final Mat img2){
		final int commonWidth  = Math.min(img1.cols(), img2.cols());
		final int commonHeight = Math.min(img1.rows(), img2.rows());
		
		Rect img1rect = new Rect();
		img1rect.x = (img1.cols() - commonWidth) / 2;
		img1rect.y = (img1.rows() - commonHeight) / 2;
		img1rect.height = commonHeight;
		img1rect.width = commonWidth;
		final Mat part1 = new Mat(img1, img1rect);
		
		Rect img2rect = new Rect();
		img2rect.x = (img2.cols() - commonWidth) / 2;
		img2rect.y = (img2.rows() - commonHeight) / 2;
		img2rect.height = commonHeight;
		img2rect.width = commonWidth;
		final Mat part2 = new Mat(img2, img2rect);
		
		final int commonAreaSize = commonWidth * commonHeight;
		final double image1Aboard = img1.size().area() - commonAreaSize;
		final double image2Aboard = img2.size().area() - commonAreaSize;
			
			
		final double differentPixels = CharacterMatrix.countSamePixels(part1, part2) + image1Aboard + image2Aboard;
		final double score = differentPixels / (commonAreaSize + image1Aboard + image2Aboard);		
		
		return score;
	}
	
	public double compare(CharacterMatrix characterMatrix){
		//return double [0.0; 1.0]. 0.0 - полное совпадение, 1.0 полное различие
		final double result = CharacterMatrix.compareByPoints(this.character, characterMatrix.character);
		return result;
	}
	
	public void dump(File path){
		Mat inv = new Mat();
		Imgproc.threshold(this.character, inv, 127, 255, Imgproc.THRESH_BINARY_INV);
		
		path.getParentFile().mkdirs();
		Imgcodecs.imwrite(path.getAbsolutePath(), inv);
	}
	
}
