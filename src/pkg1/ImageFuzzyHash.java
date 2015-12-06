package pkg1;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
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
	protected ImageFyzzyHashSum fuzzyHash;

	public ImageFuzzyHash(Mat srcImage){
		assert srcImage != null;
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
				Imgproc.rectangle(testImage, rect.tl(), rect.br(), new Scalar(128, 128, 0));
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
	
	protected ImageFyzzyHashSum calcImageHash() throws IOException{
		CharactersStatistics charStats = this.getFrequency();
		List<Integer> frequences = charStats.getFrequencies();
		//charStats.dump(new File("chars/"));
		return new ImageFyzzyHashSum(frequences);
	}
	
	public ImageFyzzyHashSum getImageFuzzyHash() throws IOException{
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
		List<Rect> characterRectangles = this.getPerCharacterRectangles();
		List<CharacterMatrix> characters = new ArrayList<>();
		
		for(Rect rect : characterRectangles){
			CharacterMatrix currentChar = new CharacterMatrix(new Mat(this.srcImage, rect));
			characters.add(currentChar);
		}
		
		PrintWriter writer = null;
		try{
			writer = new PrintWriter("differences.txt", "UTF-8");
		}
		catch(FileNotFoundException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch(UnsupportedEncodingException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for(int first = 0; first < characters.size() - 1; ++first){
			for(int second = first + 1; second < characters.size(); ++second){
				double similarity = characters.get(first).compare(characters.get(second));
				writer.println(similarity);
			}
		}
		writer.close();
	}
}
