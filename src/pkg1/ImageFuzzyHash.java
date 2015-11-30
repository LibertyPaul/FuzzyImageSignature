package pkg1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

public class ImageFuzzyHash{
	protected Mat srcImage;
	protected Mat infoPart;
	protected String fuzzyHash;
	
	public ImageFuzzyHash(Mat srcImage) throws Exception{
		if(srcImage == null)
			throw new Exception("srcIamge should not be null");
		
		this.srcImage = srcImage.clone();
		
		Mat bnw = new Mat();
		Imgproc.cvtColor(srcImage, bnw, Imgproc.COLOR_BGR2GRAY);
		
		Mat infoResized = new Mat();
		Imgproc.resize(bnw, infoResized, new Size(1000, 1000));
		
		Mat thresholded = new Mat();
		Imgproc.threshold(infoResized, thresholded, 110, 255, Imgproc.THRESH_BINARY_INV);
		
		this.infoPart = thresholded;
	}
	
	protected boolean isCharacter(Rect rect){
		//функция проверяет размеры фигуры на соответствие примерному размеру символа
		//предположим, что на странице может поместиться 50 символов по вертикали и по горизонтали вплотную
		int verticalCount	= 50;
		int gorizontalCount = 50;
		int approxHeight	= this.infoPart.rows() / verticalCount;
		int aproxWidth		= this.infoPart.cols() / gorizontalCount;
		
		double heightError	= Math.abs(1 - ((double)rect.height / approxHeight));
		double widthError	= Math.abs(1 - ((double)rect.width / aproxWidth));
		
		double acceptableError = 0.75;//+- 25% это ОК
		
		return heightError < acceptableError && widthError < acceptableError;
	}
	
	protected List<Rect> getPerCharacterRectangles(){		
		List<MatOfPoint> contours = new ArrayList<>();
		Imgproc.findContours(this.infoPart, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
		
		int contourCount = contours.size();
		
		List<Rect> rectangles = new ArrayList<>();//координаты прямоугольных областей описаных вокруг контуров
		for(int i = 0; i < contourCount; ++i){
			MatOfPoint currentContour = contours.get(i);
			MatOfPoint2f current2f = new MatOfPoint2f(currentContour.toArray());
			MatOfPoint2f approxCurve2f = new MatOfPoint2f();
			Imgproc.approxPolyDP(current2f, approxCurve2f, Imgproc.arcLength(current2f, true) * 0.02, true);
			
			Rect rect = Imgproc.boundingRect(new MatOfPoint(approxCurve2f.toArray()));
			rectangles.add(rect);
		}
		
		List<Rect> characterRectangles = new ArrayList<>();
		//координаты областей описаных вокруг символов(или объектов, по размеру схожих с ними)
		for(Rect current : rectangles){
			if(this.isCharacter(current)){
				characterRectangles.add(current);
			}
		}
		//в принципе, это не обязательно. TODO: попробовать без этой фильтрации
		
		return characterRectangles;
	}
	
	protected Mat charToMat(Rect charField){//копирует область, содержащую символ в новое изображение
		List<Point> srcQuad = new ArrayList<>();
		srcQuad.add(charField.tl());
		Point srcTR = new Point(charField.x + charField.width, charField.y);
		srcQuad.add(srcTR);
		srcQuad.add(charField.br());
		Point srcBL = new Point(charField.x, charField.y + charField.height);
		srcQuad.add(srcBL);
		
		List<Point> dstQuad = new ArrayList<>();
		dstQuad.add(new Point(0, 0));
		dstQuad.add(new Point(charField.width, 0));
		dstQuad.add(new Point(charField.width, charField.height));
		dstQuad.add(new Point(0, charField.height));
		
		Mat srcQuadMat = Converters.vector_Point2f_to_Mat(srcQuad);
		Mat dstQuadMat = Converters.vector_Point2f_to_Mat(dstQuad);
		
		Mat lambda = Imgproc.getPerspectiveTransform(srcQuadMat, dstQuadMat);
		
		Mat result = new Mat();
		Imgproc.warpPerspective(this.infoPart, result, lambda, charField.size());
		
		Mat dilated = new Mat();
		Imgproc.dilate(result, dilated, new Mat());
		
		result = dilated;
		
		return result;
	}
	
	protected DataHash getDataHash(Rect rect){
		Mat currentChar = this.charToMat(rect);
		MatOfByte buffer = new MatOfByte();
		Imgcodecs.imencode(".bmp", currentChar, buffer);
		
		DataHash result = new DataHash(buffer.toArray());
		return result;
	}
	
	protected List<DataHash> getCharacterHashes(){
		List<Rect> characterRectangles = this.getPerCharacterRectangles();
		List<DataHash> dataHashes = new ArrayList<>(characterRectangles.size());
		
		for(Rect rect : characterRectangles){
			DataHash current = this.getDataHash(rect);
			dataHashes.add(current);
		}
		
		return dataHashes;
	}
	
	protected String calcImageHash() throws IOException{		
		ComplexSsdeep hasher = new ComplexSsdeep();
		hasher.addAll(this.getCharacterHashes());
		
		return hasher.getComplexHash();
	}
	
	public String getImageFuzzyHash() throws IOException{
		if(this.fuzzyHash == null)
			this.fuzzyHash = this.calcImageHash();
		
		return this.fuzzyHash;
	}
	
	
	public Mat getRectedImage(){
		List<Rect> characterRectangles = this.getPerCharacterRectangles();
		Mat imgCopy = this.infoPart.clone();
		for(Rect rect : characterRectangles){
			Imgproc.rectangle(imgCopy, rect.tl(), rect.br(), new Scalar(255, 255, 255));
		}
		System.out.println(characterRectangles.size());
		return imgCopy;
	}
	
	public void test(){
		ComplexSsdeep hasher = new ComplexSsdeep();
		hasher.addAll(this.getCharacterHashes());
		hasher.test();
	}
}
