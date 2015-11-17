package pkg1;

import org.opencv.core.Mat;

public class ImageFuzzyHash{
	protected Mat srcImage;
	protected String fuzzyHash;
	
	public ImageFuzzyHash(Mat srcImage) throws Exception{
		if(srcImage == null)
			throw new Exception("srcIamge should not be null");
		
		this.srcImage = srcImage;
	}
	
}
