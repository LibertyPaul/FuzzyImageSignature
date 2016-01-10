package hash;

import java.io.File;
import java.io.FileNotFoundException;
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
	protected final Mat srcImage;
	protected final Mat thresholded;
	protected ImageFuzzyHashSum fuzzyHash;

	public ImageFuzzyHash(Mat srcImage){
		assert srcImage != null;
		this.srcImage = srcImage;
		
		Mat bnw = new Mat();
		Imgproc.cvtColor(srcImage, bnw, Imgproc.COLOR_BGR2GRAY);
		
		Mat thresholded = new Mat();
		Imgproc.threshold(bnw, thresholded, 100, 255, Imgproc.THRESH_BINARY_INV);
		
		this.thresholded = thresholded;
		Imgcodecs.imwrite("threshInfoPart.bmp", this.thresholded);
	}
	
	protected boolean isCharacter(Rect rect){
		//функция проверяет размеры фигуры на соответствие примерному размеру символа
		final double verticalCount		= 52.3;
		final double horizontalCount 	= 70;
		assert horizontalCount > 1 && verticalCount > 1;
		
		final double heightError	= rect.height * verticalCount / this.thresholded.rows();
		final double widthError		= rect.width * horizontalCount / this.thresholded.cols();
		//errors - при правильных значениях получаем результат около 1.
		final double acceptableError = 0.66;
		assert acceptableError >= 0 && acceptableError <= 1;
		
		final double lowerBound = 1 - acceptableError;
		final double upperBound = 1 + acceptableError;
		
		return 	heightError > lowerBound && heightError < upperBound &&
				widthError > lowerBound  && widthError < upperBound;
	}
	
	protected List<Rect> getPerCharacterRectangles(){
		Mat testImage = this.srcImage.clone();

		List<MatOfPoint> contours = new ArrayList<>();
		Imgproc.findContours(this.thresholded.clone(), contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
		
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
		Mat result = new Mat(this.thresholded, charField);
		return result;
	}
	
	public CharactersStatistics getCharactersStatistics(){
		List<Rect> characterRectangles = this.getPerCharacterRectangles();
		CharactersStatistics charStat = new CharactersStatistics();
		
		for(final Rect rect : characterRectangles){
			Mat currentMat = new Mat(this.thresholded, rect);
			CharacterMatrix current = new CharacterMatrix(currentMat);
			DetectedCharacter character = new DetectedCharacter(rect, current);
			charStat.add(character);
		}
		
		return charStat;
	}
	
	protected ImageFuzzyHashSum calcImageHash() throws Exception{
		CharactersStatistics charStats = this.getCharactersStatistics();
		
		Mat withGroups = charStats.dumpGroupsToImage(this.srcImage);
		Imgcodecs.imwrite("withGroups.png", withGroups);
		
		LinePage linePage = charStats.createLinePage();
		List<MarginedLine> characterLines = linePage.getOrderedCharacterLines(this.thresholded.width());
		
		return new ImageFuzzyHashSum(characterLines);
	}
	
	public ImageFuzzyHashSum getImageFuzzyHash() throws Exception{
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
	
	public void dumpChars(File path){
		CharactersStatistics cs = this.getCharactersStatistics();
		cs.dump(path);
	}
	
	public void dumpUnique(File path){
		CharactersStatistics cs = this.getCharactersStatistics();
		cs.dumpUnique(path);
	}
	
	
	
	
	public void testCharMatrixComparsion(){
		List<Rect> characterRectangles = this.getPerCharacterRectangles();
		List<CharacterMatrix> characters = new ArrayList<>();
		
		for(Rect rect : characterRectangles){
			CharacterMatrix currentChar = new CharacterMatrix(new Mat(this.thresholded, rect));
			characters.add(currentChar);
		}
		
		PrintWriter densityWriter = null;
		try{
			densityWriter = new PrintWriter("differences.txt", "UTF-8");
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
				densityWriter.println(similarity);
			}
		}
		densityWriter.close();
	}
	
	public void testCharStat(){
		CharactersStatistics charStats = this.getCharactersStatistics();
		System.out.println(charStats.toString());
	}
}
