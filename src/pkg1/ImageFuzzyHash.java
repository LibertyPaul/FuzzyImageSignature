package pkg1;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class ImageFuzzyHash{
	protected Mat srcImage;
	protected String fuzzyHash;

	public ImageFuzzyHash(Mat srcImage) throws Exception{
		if(srcImage == null)
			throw new Exception("srcIamge should not be null");
		
		this.srcImage = srcImage.clone();
	}
	
	protected boolean isCharacter(Rect rect){
		//функция проверяет размеры фигуры на соответствие примерному размеру символа
		double verticalCount	= 70;
		double horizontalCount 	= 70;
		assert horizontalCount > 1 && verticalCount > 1;
		
		double heightError	= rect.height * verticalCount / this.srcImage.rows();
		double widthError	= rect.width * horizontalCount / this.srcImage.cols();
		//errors - при правильных значениях получаем результат около 1.
		
		double acceptableError = 0.50;
		assert acceptableError >= 0 && acceptableError <= 1;
		
		double lowerBound = 1 - acceptableError;
		double upperBound = 1 + acceptableError;
		//если результат в диапазоне от 0.75 до 1.25 - ОК
		
		return 	heightError > lowerBound && heightError < upperBound &&
				widthError > lowerBound  && widthError < upperBound;
	}
	
	protected List<Rect> getPerCharacterRectangles(){
		List<MatOfPoint> contours = new ArrayList<>();
		
		Mat bnw = new Mat();
		Imgproc.cvtColor(srcImage, bnw, Imgproc.COLOR_BGR2GRAY);
		
		Mat thresholded = new Mat();
		Imgproc.threshold(bnw, thresholded, 100, 255, Imgproc.THRESH_BINARY_INV);
		
		Imgproc.findContours(thresholded, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
		
		Mat testImage = this.srcImage.clone();
		
		List<Rect> rectangles = new ArrayList<>();//координаты прямоугольных областей описаных вокруг контуров
		for(MatOfPoint currentContour : contours){
			MatOfPoint2f current2f = new MatOfPoint2f(currentContour.toArray());
			MatOfPoint2f approxCurve2f = new MatOfPoint2f();
			Imgproc.approxPolyDP(current2f, approxCurve2f, Imgproc.arcLength(current2f, true) * 0.02, true);
			
			Rect rect = Imgproc.boundingRect(new MatOfPoint(approxCurve2f.toArray()));
			if(this.isCharacter(rect)){
				rectangles.add(rect);
				Imgproc.rectangle(testImage, rect.tl(), rect.br(), new Scalar(255, 255, 255));
			}
		}
		
		Imgcodecs.imwrite("withContours.png", testImage);
		
		return rectangles;
	}	
	
	
	protected Mat getSubMat(Rect charField){//копирует область, содержащую символ в новое изображение
		Mat result = new Mat(this.srcImage, charField);
		return result;
	}
	
	
	
	protected CharactersStatistics getFrequency() throws IOException{
		List<Rect> characterRectangles = this.getPerCharacterRectangles();
		CharactersStatistics charStat = new CharactersStatistics();
		
		for(Rect rect : characterRectangles){
			CharacterMatrix current = new CharacterMatrix(this.getSubMat(rect));
			charStat.add(current);
		}
		
		return charStat;
	}
	
	protected String calcImageHash() throws IOException{
		CharactersStatistics charStats = this.getFrequency();

		charStats.dump(new File("chars/"));
		System.out.println(charStats);
		
		return "";
	}
	
	public String getImageFuzzyHash() throws IOException{
		if(this.fuzzyHash == null)
			this.fuzzyHash = this.calcImageHash();
		
		return this.fuzzyHash;
	}
	
	
	public Mat getRectedImage(){
		List<Rect> characterRectangles = this.getPerCharacterRectangles();
		Mat imgCopy = this.srcImage.clone();
		for(Rect rect : characterRectangles){
			Imgproc.rectangle(imgCopy, rect.tl(), rect.br(), new Scalar(255, 255, 255));
		}
		System.out.println(characterRectangles.size());
		return imgCopy;
	}
	
	public void test(){
	}
}
