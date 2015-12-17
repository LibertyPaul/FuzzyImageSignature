package pkg1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import org.opencv.core.*;
import org.opencv.imgcodecs.*;
import org.opencv.imgproc.*;
import org.opencv.utils.Converters;

public class ImageRecognizer{
	protected Mat srcImage;
	protected ThreePoints leftBound  = null;
	protected ThreePoints rightBound = null;
	
	public ImageRecognizer(String path){
		srcImage = Imgcodecs.imread(path);
		assert srcImage.empty() == false;
	}
	
	private Mat getPreparedImage(){
		Mat bnw = new Mat();
		Imgproc.cvtColor(this.srcImage, bnw, Imgproc.COLOR_BGR2GRAY);
		Imgcodecs.imwrite("1.bnw.png", bnw);
		
		Mat blurred = new Mat();
		Imgproc.GaussianBlur(bnw, blurred, new Size(3, 3), 5.0);
		Imgcodecs.imwrite("2.blurred.png", blurred);
		
		Mat edges = new Mat();
		Imgproc.Canny(blurred, edges, 50, 175);
		Imgcodecs.imwrite("3.edges.png", edges);
		
		Mat dilated = new Mat();
		Imgproc.dilate(edges, dilated, new Mat(), new Point(-1, -1), 2);
		Imgcodecs.imwrite("4.dilated.png", dilated);
		
		return dilated;
	}
	
	static protected Point getCenter(MatOfPoint2f approxCounter){
		//тут dirty-hack из за того, что класс Moments тупо вырезан из Java либы
		//ищем центр области, описывая минимальную окружность
		RotatedRect ellipse = Imgproc.fitEllipse(approxCounter);
		return ellipse.center;
	}
	
	static protected Point getCenter(MatOfPoint contour){
		MatOfPoint2f countor2f = new MatOfPoint2f(contour.toArray());
		return getCenter(countor2f);
	}
	
	
	protected List<MatOfPoint> getFilteredCountors(){
		Mat image = this.getPreparedImage();
		
		List<MatOfPoint> contours = new ArrayList<>();
		Imgproc.findContours(image, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
		if(contours.size() < 6){//похоже, что найден внешний контур, а не квадратики
			System.out.println("contours.size() < 6");
			Imgproc.findContours(image, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
		}
		
		List<MatOfPoint> acceptedCountors = new ArrayList<>();

		Mat src = this.srcImage.clone();
		for(int i = 0; i < contours.size(); ++i){
			MatOfPoint current = contours.get(i);
			MatOfPoint2f current2f = new MatOfPoint2f(current.toArray());
			MatOfPoint2f approxCurve2f = new MatOfPoint2f();
			
			Imgproc.approxPolyDP(current2f, approxCurve2f, Imgproc.arcLength(current2f, true) * 0.02, true);
			
			double contourArea = Imgproc.contourArea(approxCurve2f);
			List<MatOfPoint> dummy = new ArrayList<>();
			dummy.add(new MatOfPoint(approxCurve2f.toArray()));
			
			if(
		//		approxCurve2f.size().area() == 4.0 	&&
				Math.abs(contourArea)		 > 1000	&&
				Imgproc.isContourConvex(new MatOfPoint(approxCurve2f.toArray()))
			){
				acceptedCountors.add(current);
				Imgproc.drawContours(src, dummy, dummy.size() - 1, new Scalar(255, 255, 0), 2);
			}
		}
		Imgcodecs.imwrite("6.with_contours.png", src);
		
		return acceptedCountors;
	}
	
	//эта функция считает сколько примерно должна занимать 1 метка в зависимости от разрешения фото
	protected static double getAnchorSize(double imageWidhth, double imageHeight){
		return Math.min(imageWidhth, imageHeight) * 0.025;
	}
	
	protected double getAnchorSize(){
		return ImageRecognizer.getAnchorSize(this.srcImage.width(), this.srcImage.height());
	}
	
	protected boolean isSameLine(Point p1, Point p2, Point p3){
		//находим коэффициенты уравнения прямой Ax + By + C = 0
		double A = p3.y - p1.y;
		double B = p1.x - p3.x;
		double C = p1.y * (p3.x - p1.x) - p1.x * (p3.y - p1.y);
		
		//находим длинну высоты, брошенной от точки к прямой
		double D = Math.abs(A * p2.x + B*p2.y + C) / Math.sqrt(A * A + B * B);
		
		return D < this.getAnchorSize() / 2;
	}
	
	protected boolean isSameLine(ThreePoints threePoints){
		return this.isSameLine(threePoints.getFirst(), threePoints.getBetween(), threePoints.getLast());
	}
	
	protected static boolean isCorrectLineProportion(ThreePoints threePoints){
		double distance1 = ThreePoints.getDistance(threePoints.getFirst(), threePoints.getBetween());
		double distance2 = ThreePoints.getDistance(threePoints.getBetween(), threePoints.getLast());
		
		if(distance2 > distance1){
			double temp = distance2;
			distance2 = distance1;
			distance1 = temp;
		}
		
		double proportion = distance1 / distance2;
		
		return proportion > 3 && proportion < 7;
	}
	
	protected double getRelativeSize(Point p1, Point p2){
		double xPart = p2.x - p1.x;
		double yPart = p2.y - p1.y;
		
		double angle = Math.atan(yPart / xPart);
		
		if(angle < 0)
			angle += Math.PI;
		
		double lMax1 = Math.abs(this.srcImage.rows() / Math.cos(angle));
		double lMax2 = Math.abs(this.srcImage.cols() / Math.sin(angle));
		
		double lMax = Math.min(lMax1, lMax2);		
		double size = ThreePoints.getDistance(p1, p2);
		
		double ratio = size / lMax;
		return ratio;
	}
	
	protected void dumpImageWithLines(String dstPath, List<ThreePoints> lines){
		Mat image = this.srcImage.clone();
		Random rg = new Random();
		
		for(ThreePoints line : lines){
			Scalar color = new Scalar(rg.nextInt(255), rg.nextInt(255), rg.nextInt(255));
			Imgproc.line(image, line.getFirst(), line.getLast(), color, 2, Core.LINE_4, 0);
			
			int radius = (int)this.getAnchorSize();
			Imgproc.circle(image, line.getFirst(), radius, color, 5, Core.FILLED, 0);
			Imgproc.circle(image, line.getBetween(), radius, color, 5, Core.FILLED, 0);
			Imgproc.circle(image, line.getLast(), radius, color, 5, Core.FILLED, 0);
			
			Point middle12 = ThreePoints.getMiddle(line.getFirst(), line.getBetween());
			double distance12 = ThreePoints.getDistance(line.getFirst(), line.getBetween());
			Imgproc.putText(image, String.format("%.2f", distance12), middle12, Core.FONT_HERSHEY_COMPLEX, 2, color);
			
			Point middle23 = ThreePoints.getMiddle(line.getLast(), line.getBetween());
			double distance23 = ThreePoints.getDistance(line.getLast(), line.getBetween());
			Imgproc.putText(image, String.format("%.2f", distance23), middle23, Core.FONT_HERSHEY_COMPLEX, 2, color);
		}
		
		Imgcodecs.imwrite(dstPath, image);
	}
	
	//getPageCoords ищет на изображении метки, по которым определяет границы
	protected void recognize() throws Exception{
		List<MatOfPoint> countors = this.getFilteredCountors();
		
		List<Point> contourCenters = new ArrayList<>();
		for(MatOfPoint contour : countors){
			Point current = null;
			try{
				current = ImageRecognizer.getCenter(contour);
			}
			catch(CvException cvEx){
				System.out.printf("getCenter(%s)Exception: %s\n", contour.toString(), cvEx.getMessage());
				continue;
			}
			contourCenters.add(current);
		}
		
		if(contourCenters.size() < 6)
			throw new Exception("Squares detection failed");
		
		
		List<ThreePoints> allLines = new ArrayList<>();
		
		for(int first = 0; first < contourCenters.size() - 2; ++first){
			for(int second = first + 1; second < contourCenters.size() - 1; ++second){
				for(int third = second + 1; third < contourCenters.size(); ++third){//n^3 - УЖС, как так можно...
					ThreePoints currentPoints = new ThreePoints(
						contourCenters.get(first),
						contourCenters.get(second),
						contourCenters.get(third)
					);
					
					allLines.add(currentPoints);
				}
			}
		}

		this.dumpImageWithLines("7.withAllLines.jpg", allLines);
		
		List<ThreePoints> lines = new ArrayList<>();
		for(ThreePoints line : allLines){
			if(this.isSameLine(line) == false){
				continue;
			}
			
			if(this.getRelativeSize(line.getFirst(), line.getLast()) < 0.75){//линия занимает < 75% длины фото
				continue;
			}
			
			if(ImageRecognizer.isCorrectLineProportion(line) == false){
				continue;
			}
			
			lines.add(line);
		}
		
		this.dumpImageWithLines("8.withLines.jpg", lines);
		
		List<AngleBetweenLines> angles = new ArrayList<>();
		for(int first = 0; first < lines.size() - 1; ++first){
			for(int second = first + 1; second < lines.size(); ++second){
				AngleBetweenLines current = new AngleBetweenLines(lines.get(first), lines.get(second), ThreePoints.getAngle(lines.get(first), lines.get(second)));
				angles.add(current);
			}
		}
		
		if(angles.size() == 0){
			throw new Exception("All incorrect lines was filtered, none left. Aborting.");
		}
		
		Collections.sort(angles, Collections.reverseOrder());
		
		AngleBetweenLines winner = angles.get(0);
		ThreePoints line1 = winner.line1;
		ThreePoints line2 = winner.line2;
		
		final double relativeAngle = ThreePoints.getAngle(line1, line2) % Math.PI;
		final double maxAngleDiff = Math.PI / 8;
		if(relativeAngle > maxAngleDiff && relativeAngle < -maxAngleDiff){
			throw new Exception("Lines are not parallel");
		}

		double angle = line1.getAngle();
		int degrees = ((int)Math.toDegrees(angle) + 360) % 360;
		int direction = (degrees - 45) / 90;//0 - up, 1 - left, 2 - down, 3 - right
		
		if(
			direction == 0 && line1.getFirst().x < line2.getFirst().x ||
			direction == 1 && line1.getFirst().y < line2.getFirst().y ||
			direction == 2 && line1.getFirst().x > line2.getFirst().x ||
			direction == 3 && line1.getFirst().y > line2.getFirst().y
		){
			this.leftBound  = line1;
			this.rightBound = line2;
		}
		else{
			this.leftBound  = line2;
			this.rightBound = line1;
		}
		
	}
	
	protected static Mat getImagePart(Mat srcImage, Point topLeft, Point topRight, Point bottomRight, Point bottomLeft){
		List<Point> srcQuad = new ArrayList<>();
		srcQuad.add(topLeft);
		srcQuad.add(topRight);
		srcQuad.add(bottomRight);
		srcQuad.add(bottomLeft);
		
		Size imgSize = new Size();
		imgSize.height = Math.max(
			ThreePoints.getDistance(topLeft, bottomLeft),
			ThreePoints.getDistance(topRight, bottomRight)
		);
		imgSize.width = Math.max(
			ThreePoints.getDistance(topLeft, topRight),
			ThreePoints.getDistance(bottomLeft, bottomRight)
		);
		
		Point dstTopLeft		= new Point(0, 0);
		Point dstTopRight		= new Point(imgSize.width, 0);
		Point dstBottomRight	= new Point(imgSize.width, imgSize.height);
		Point dstBottomLeft		= new Point(0, imgSize.height);
		
		List<Point> dstQuad = new ArrayList<>();
		dstQuad.add(dstTopLeft);
		dstQuad.add(dstTopRight);
		dstQuad.add(dstBottomRight);
		dstQuad.add(dstBottomLeft);
		
		Mat srcQuadMat = Converters.vector_Point2f_to_Mat(srcQuad);
		Mat dstQuadMat = Converters.vector_Point2f_to_Mat(dstQuad);
		
		Mat lambda = Imgproc.getPerspectiveTransform(srcQuadMat, dstQuadMat);
		
		Mat dst = new Mat();
		Imgproc.warpPerspective(srcImage, dst, lambda, imgSize);
		
		Rect rect = new Rect(dstTopLeft, dstBottomRight);
		Mat cropped = new Mat(dst, rect);
		
		
		
		return cropped;
	}
	
	protected static Mat trim(Mat image) throws Exception{
		//удаляет белые поля вокруг текста
		Mat bnw = new Mat();
		Imgproc.cvtColor(image, bnw, Imgproc.COLOR_BGR2GRAY);
		
		Mat dilated = new Mat();
		Imgproc.dilate(bnw, dilated, new Mat(), new Point(-1, -1), 30);

		List<MatOfPoint> contours = new ArrayList<>();
		Imgproc.findContours(dilated, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
		
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
		if(rectangles.size() < 1){
			throw new Exception("No contours found");
		}
		
		int maxRectPos = 0;
		for(int i = 1; i < rectangles.size(); ++i){
			if(rectangles.get(maxRectPos).area() < rectangles.get(i).area()){
				maxRectPos = i;
			}
		}
		
		Mat cropped = new Mat(image, rectangles.get(maxRectPos));
		return cropped;
	}
	
	protected static Mat cutSides(Mat image, int width){
		//удаляет по несколько десятков пикселей с 4х сторон
		Rect rect = new Rect();
		rect.x = width;
		rect.y = width;
		
		rect.width = image.cols() - width * 2;
		rect.height = image.rows() - width * 2;
		
		
		Mat withoutAnchors = new Mat(image, rect);
		return withoutAnchors;
	}
	
	
	public Mat getInfoPart() throws Exception{
		if(this.leftBound == null || this.rightBound == null){
			this.recognize();
		}		
		Mat withAnchors = ImageRecognizer.getImagePart(this.srcImage,
			this.leftBound.getFirst(), this.rightBound.getFirst(),
			this.rightBound.getBetween(), this.leftBound.getBetween()
		);
		Imgcodecs.imwrite("12.withAnchors.png", withAnchors);
		
		int anchorSize = (int) ImageRecognizer.getAnchorSize(withAnchors.cols(), withAnchors.rows());
		Mat withoutAnchors = ImageRecognizer.cutSides(withAnchors, anchorSize);
		Imgcodecs.imwrite("13.withoutAnchors.png", withoutAnchors);
		
		//Mat trimmed = ImageRecognizer.trim(withoutAnchors);
		//return trimmed;
		return withoutAnchors;
	}
	
	public Mat getCodePart() throws Exception{
		if(this.leftBound == null || this.rightBound == null){
			this.recognize();
		}
		Mat withAnchors = ImageRecognizer.getImagePart(this.srcImage,
			this.leftBound.getBetween(), this.rightBound.getBetween(),
			this.rightBound.getLast(), this.leftBound.getLast()
		);
		int anchorSize = (int) ImageRecognizer.getAnchorSize(withAnchors.cols(), withAnchors.rows());
		Mat withoutAnchors = ImageRecognizer.cutSides(withAnchors, anchorSize);
		
		//Mat trimmed = ImageRecognizer.trim(withoutAnchors);
		//return trimmed;
		return withoutAnchors;
	}
}


