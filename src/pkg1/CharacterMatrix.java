package pkg1;

import java.io.File;
import java.io.IOException;

import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;

public class CharacterMatrix{
	protected Mat character;
	
	public CharacterMatrix(Mat character){
		this.character = character.clone();
	}
	
	protected Mat getImagePart(Rect rect) throws Exception{
		if(
				rect.width	> this.character.cols() ||
				rect.height	> this.character.rows()
		){
			throw new Exception("Out of matrix");
		}
		
		Mat result = new Mat(this.character, rect);
		return result;
	}
	
	protected static double euclidianDistance(double[] v1, double[] v2){
		assert v1.length == v2.length;
		
		double result = 0;
		for(int i = 0; i < v1.length; ++i){
			double sub = v1[i] - v2[i];
			double squareSub = Math.pow(sub, 2);
			result += squareSub;
		}
		result = Math.pow(result, 0.5);
		return result;
	}
	
	protected static double calcDistance(Mat image1, Mat image2){
		assert image1.size().equals(image2.size());
		assert image1.type() == image2.type();
		
		double distanceSum = 0;
		
		double minValue = 0;
		double maxValue = 255;
		double rangeSize = maxValue - minValue;
		
		Size size = image1.size();
		for(int row = 0; row < size.height; ++row){
			for(int col = 0; col < size.width; ++col){
				double[] point1 = image1.get(row, col);
				double[] point2 = image2.get(row, col);
				assert point1.length == point2.length;
				
				double distanceMaxPossibleValue = rangeSize * Math.pow(point1.length, 0.5);
				double distance = CharacterMatrix.euclidianDistance(point1, point2);
				distanceSum += distance / distanceMaxPossibleValue;
			}
		}
		double area = size.width * size.height;
		return distanceSum / area;
	}

	public double compare(CharacterMatrix characterMatrix){
		//return double [0.0; 1.0]. 0.0 - полное совпадение, 1.0 полное различие
		Size size1 = this.character.size();
		Size size2 = characterMatrix.character.size();
		
		Rect comparingPart = new Rect();//общая для обоих изображений область
		comparingPart.height = (int)Math.min(size1.height, size2.height);
		comparingPart.width  = (int)Math.min(size1.width,  size2.width);
		
		assert comparingPart.size().area() <= size1.area() && comparingPart.size().area() <= size2.area();
		
		Mat img1 = null;
		Mat img2 = null;
		double distance = 0;
		
		try{
			img1 = this.getImagePart(comparingPart);
			img2 = characterMatrix.getImagePart(comparingPart);
			
			distance = CharacterMatrix.calcDistance(img1, img2);
			//score = Core.norm(img1, img2);
		}
		catch(Exception ex){
			System.err.println(ex.getMessage());
			return 1.0;
		}
		
		double unused1 = size1.area() - comparingPart.size().area();
		double unused2 = size2.area() - comparingPart.size().area();
		double unused = unused1 + unused2;
		double unusedAreaCoefficient = unused / (comparingPart.size().area() + unused);
		
		return distance + unusedAreaCoefficient;
	}
	
	public void dump(File path) throws IOException{
		path.getParentFile().mkdirs();
		Imgcodecs.imwrite(path.getAbsolutePath(), this.character);
	}
	
}
