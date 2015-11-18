
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import pkg1.ImageFuzzyHash;
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
			ir = new ImageRecognizer("Photos/testPage5.jpg");
			
			Mat infoPart = ir.getInfoPart();
			Imgcodecs.imwrite("9.infoPart.bmp", infoPart);
			Mat codePart = ir.getCodePart();
			Imgcodecs.imwrite("10.codePart.bmp", codePart);
			

			ImageFuzzyHash hasher = new ImageFuzzyHash(infoPart);
			hasher.generateLevels();
			
			/*
			ssdeep hasher = new ssdeep();
			String hash = hasher.fuzzy_hash_file("10.result.bmp");
			System.out.println(hash);
			
			
			BufferedImage img = ImageIO.read(new File("9.codePart.bmp"));
			LuminanceSource source = new BufferedImageLuminanceSource(img);
			BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
			Reader reader = new MultiFormatReader();
			Result result = reader.decode(bitmap);
			System.out.println(result.getText());
			*/
			
		}
		catch(Exception e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		System.out.println("done.");
		
		
	}

}