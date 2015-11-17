
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import pkg1.ImageRecognizer;
import ssdeep.ssdeep;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

public class Main{
	static{
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	public static void main(String[] args){
		ImageRecognizer ir = null;
		try{
			ir = new ImageRecognizer("Photos/testPage4.jpg");
			
			Mat infoPart = ir.getInfoPart();
			Imgcodecs.imwrite("8.infoPart.bmp", infoPart);
			Mat codePart = ir.getCodePart();
			Imgcodecs.imwrite("9.codePart.bmp", codePart);
			

			Mat bnw = new Mat();
			Imgproc.cvtColor(infoPart, bnw, Imgproc.COLOR_BGR2GRAY);
			
			Mat infoResized = new Mat();
			Imgproc.resize(bnw, infoResized, new Size(1000, 1000));
			
			Mat thresholded = new Mat();
			Imgproc.threshold(infoResized, thresholded, 110, 255, Imgproc.THRESH_BINARY_INV);
			
			Mat dilated = new Mat();
			Imgproc.dilate(thresholded, dilated, new Mat(), new Point(-1, -1), 10);
			Imgcodecs.imwrite("10.result.bmp", dilated);
			
			ssdeep hasher = new ssdeep();
			String hash = hasher.fuzzy_hash_file("10.result.bmp");
			System.out.println(hash);
			
			
			BufferedImage img = ImageIO.read(new File("9.codePart.bmp"));
			LuminanceSource source = new BufferedImageLuminanceSource(img);
			BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
			Reader reader = new MultiFormatReader();
			Result result = reader.decode(bitmap);
			System.out.println(result.getText());
			
		}
		catch(Exception e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		System.out.println("done.");
		
		
	}

}