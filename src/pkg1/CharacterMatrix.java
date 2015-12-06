package pkg1;

import java.io.File;
import java.io.IOException;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;

public class CharacterMatrix{
	protected Mat character;
	
	public CharacterMatrix(Mat character){
		this.character = character.clone();
	}
	
	protected Mat getImagePart(Rect rect){
		Mat result = new Mat();
		
		int top		= (int)rect.tl().y;
		int bottom	= this.character.rows() - (int)rect.br().y;
		int left	= (int)rect.tl().x;
		int right	= this.character.cols() - (int)rect.br().x;
		
		Core.copyMakeBorder(
			this.character,
			result,
			-top,
			-bottom,
			-left,
			-right,
			Core.BORDER_DEFAULT
		);
		
		return result;
	}
	
	protected static double calcDistance(Mat image1, Mat image2){
		assert image1.type() == image2.type();
		assert image1.size().equals(image2.size());
		
		double distanceSum = 0;
		Size size = image1.size();
		for(int row = 0; row < size.height; ++row){
			for(int col = 0; col < size.width; ++col){
				double[] vec1 = image1.get(row, col);
				double[] vec2 = image2.get(row, col);
				distanceSum += EuclidianDistance.calc(vec1, vec2);
			}
		}
		double minValue = 0;
		double maxValue = 255;
		double rangeSize = maxValue - minValue;
		return distanceSum / (size.area() * rangeSize);
	}

	public double compare(CharacterMatrix characterMatrix){
		//return double [0.0; 1.0]. 0.0 - полное совпадение, 1.0 полное различие
		Size size1 = this.character.size();
		Size size2 = characterMatrix.character.size();
		
		Rect comparingPart = new Rect();//общая для обоих изображений область
		comparingPart.height = (int)Math.max(size1.height, size2.height);
		comparingPart.width  = (int)Math.max(size1.width,  size2.width);
		
		try{
			Mat img1 = this.getImagePart(comparingPart);
			Mat img2 = characterMatrix.getImagePart(comparingPart);
			
			return CharacterMatrix.calcDistance(img1, img2);
		}
		catch(Exception ex){
			System.err.println(ex.getMessage());
			return 1.0;
		}
	}
	
	public void dump(File path){
		path.getParentFile().mkdirs();
		Imgcodecs.imwrite(path.getAbsolutePath(), this.character);
	}
	
}
