
import hash.HashCompareResult;
import hash.ImageFuzzyHash;
import hash.ImageFuzzyHashSum;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;

import qrStorage.DigitalSignature;
import qrStorage.QRHashStorage;
import recognition.ImageRecognizer;

public class Main{
	static{
		//System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}
	
	public static void mainTest() throws Exception{
		List<String> testImages = Arrays.asList(
				"testPhotos/10.jpg",
				"testPhotos/11.jpg"
				//"testPhotos/3.jpg",
				//"testPhotos/fake/1.jpg",
				//"testPhotos/fake/2.jpg"
				//"testPhotos/4.jpg",
				//"testPhotos/5.jpg",
				//"testPhotos/6.jpg"
		);
		
		List<Mat> infoParts = new ArrayList<>();
		List<ImageFuzzyHashSum> hashes = new ArrayList<>();
		
		for(String fName : testImages){
			ImageRecognizer ir = new ImageRecognizer(fName);
			
			Mat infoPart = null;
			Mat codePart = null;
			try{
				infoPart = ir.getInfoPart();
				Imgcodecs.imwrite("9.infoPart.bmp", infoPart);
				codePart = ir.getCodePart();
				Imgcodecs.imwrite("10.codePart.bmp", codePart);
			}
			catch(Exception ex){
				ex.printStackTrace();
				System.err.println(ex.getMessage());
				continue;
			}
			
			ImageFuzzyHash hasher = new ImageFuzzyHash(infoPart);
			
			ImageFuzzyHashSum hash = hasher.getImageFuzzyHash();
			infoParts.add(infoPart);
			hashes.add(hash);
			hash.verbose();
			
			
			
			QRHashStorage qrHashStorage = new QRHashStorage(hash.toString(), null);
			List<BufferedImage> images = qrHashStorage.generateQRCodes();
			int i = 0;
			for(final BufferedImage bufferedImage : images){
				ImageIO.write(bufferedImage, "png", new File("./qrcode" + i++ + ".png"));
			}
		}
		
		assert hashes.size() == infoParts.size();
		
		for(int first = 0; first < hashes.size() - 1; ++first){
			for(int second = first + 1; second < hashes.size(); ++second){
				ImageFuzzyHashSum hash1 = hashes.get(first);
				ImageFuzzyHashSum hash2 = hashes.get(second);
				
				System.out.println(hash1.toString());
				/*
				ImageFuzzyHashSum hash2_restored = ImageFuzzyHashSum.fromString(hash2.toString());
				
				System.out.println(hash2_restored.toString());
				System.out.printf("%d <=> %d : %s\n", first, second, res.isGenuine());
				*/
				HashCompareResult res = hash1.compare(hash2);
				Mat withErrors = res.drawIncorrect(infoParts.get(first));
				Mat withAll = res.drawCorrect(withErrors);
				Imgcodecs.imwrite("withErrors" + first + "-" + second + ".png", withAll);
			}
		}
		
		System.out.println("done.");
	}
	
	
	public static void testQR() throws Exception{
		ImageRecognizer ir = new ImageRecognizer("testPhotos/16.jpg");
		
		Mat codePartMat = null;
		try{
			codePartMat = ir.getCodePart();
		}
		catch(Exception ex){
			ex.printStackTrace();
			System.err.println(ex.getMessage());
		}
		
		BufferedImage codePart =  QRHashStorage.matToBufferedImage(codePartMat);
		
		ImageIO.write(codePart, "png", new File("codePartTest.png"));
		
		QRHashStorage qrStorage = QRHashStorage.fromQRCodes(codePart);
		String hash = qrStorage.getHashValue();
		System.out.println(hash);
	}
	
	public static void testQR2() throws Exception{
		BufferedImage codePart = ImageIO.read(new File("qrs2.jpg"));
		QRHashStorage qrStorage = QRHashStorage.fromQRCodes(codePart);
		String hash = qrStorage.getHashValue();
		System.out.println(hash);
	}
	
	
	public static void testMatConverter() throws IOException{
		Mat img = Imgcodecs.imread("testPhotos/1.jpg");
		BufferedImage result = QRHashStorage.matToBufferedImage(img);
		ImageIO.write(result, "jpg", new File("./converted.jpg"));
		
	}
	
	public static void testDS() throws Exception{
		String testData = "298356372181923846298357210341923859124710294729387510245";
		byte[] testDataByte = testData.getBytes();
		
		byte[] signature = DigitalSignature.sign(testDataByte, new File("./keys/dsaprivkey.der"));
		boolean result = DigitalSignature.verify(testDataByte, signature, new File("./keys/dsapubkey.der"));
		
		if(result){
			System.out.println("testDS: OK");
		}
		else{
			System.out.println("testDS: FAIL");
		}
	}
		
	
	public static void subMain(){
		try{
			//Main.mainTest();
			//Main.testMatConverter();
			//Main.testQR();
			Main.testQR2();
			//Main.testDS();
		}
		catch(Exception e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	

	
	
	private static void sign(final File imgPath, final Path qrCodesDstDir, final File privateKey) throws Exception{
		ImageRecognizer ir = new ImageRecognizer(imgPath.getAbsolutePath());
		Mat infoPart = ir.getInfoPart();
		
		ImageFuzzyHash hasher = new ImageFuzzyHash(infoPart);
		ImageFuzzyHashSum hash = hasher.getImageFuzzyHash();
		
		String hash_s = hash.toString();
		byte[] digitalSignature = DigitalSignature.sign(hash_s.getBytes(), privateKey);
		
		QRHashStorage qrStorage = new QRHashStorage(hash_s, digitalSignature);
		
		List<BufferedImage> images = qrStorage.generateQRCodes();
		int i = 0;
		for(final BufferedImage bufferedImage : images){
			qrCodesDstDir.toFile().mkdirs();
			ImageIO.write(bufferedImage, "png", new File(qrCodesDstDir.toFile() + "/" + i++ + ".png"));
		}
		
		System.out.println("QR-codes was successfully generated and stored in " + qrCodesDstDir.toString());
	}
	
	private static void verify(final File imgPath, final File qrPath, final File publicKey) throws Exception{
		ImageRecognizer ir = new ImageRecognizer(imgPath.getAbsolutePath());
		
		Mat infoPart = ir.getInfoPart();
		//Mat codePart = ir.getCodePart();
	
		ImageFuzzyHash hasher = new ImageFuzzyHash(infoPart);
		ImageFuzzyHashSum hash = hasher.getImageFuzzyHash();
		
		BufferedImage codePart = ImageIO.read(qrPath);
		QRHashStorage qrStorage = QRHashStorage.fromQRCodes(codePart);
		
		byte[] digitalSignature = qrStorage.getSignatureValue();
		boolean isSignatireValid = DigitalSignature.verify(qrStorage.getHashValue().getBytes(), digitalSignature, publicKey);
		if(isSignatireValid == false){
			throw new Exception("The digital signature was forged");
		}
		
		
		ImageFuzzyHashSum checkHash = ImageFuzzyHashSum.fromString(qrStorage.getHashValue());
		
		HashCompareResult res = hash.compare(checkHash);
		
		if(res.isGenuine()){
			System.out.println("The document is geniuine");
		}
		else{
			System.out.println("The document was forged");
		}
	}
	
	
	private static void man(){
		String man = "\n"
				   + "\n"
				   + "ImageFuzzyHash 0.03\n"
				   + "Pavel Yazev, mailme@libertypaul.ru, 2015\n"
				   + "-----------------------\n"
				   + "\n"
				   + "Usage: \n"
				   + "java -jar %filename%.jar -sign   <imagePath> <qrCodesDstDir> <privateKeyPath>\n"
				   + "java -jar %filename%.jar -verify <imagePath> <codePartPhoto> <publicKeyPath>\n"
				   + "\n"
				   + "imagePath - path to image with whole page\n"
				   + "qrCodesDstDir - path to directory where generated QR-codes will be stored\n"
				   + "codePartPhoto - path to photo which contains only code part of document (It's not enough resolution of most cameras to properly recognize all QR-codes from whole document photo)\n"
				   + "private or public keyPath - path to file with DSA key(public or private) in DER format\n"
				   + "\n";
		
		System.out.println(man);
	}
	
	public static void cli(String[] args){
		//System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		try{
			String currentPath = new File(".").getCanonicalPath();
			System.load(currentPath + "/libs/OpenCV/native/lib" + Core.NATIVE_LIBRARY_NAME + ".so");
			new File("debug/dummy").mkdirs();
		}
		catch(IOException e1){
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		
		if(args.length < 4){
			System.out.println("Incorrect argList(" + args.length + ")");
			Main.man();
			return;
		}
		
		
		
		try{
			if(args[0].equals("-sign")){
				Main.sign(new File(args[1]), new File(args[2]).toPath(), new File(args[3]));
			}
			else if(args[0].equals("-verify")){
				Main.verify(new File(args[1]), new File(args[2]), new File(args[3]));
			}
			else{
				System.out.println("Wrong mode(" + args[0] + ")");
				Main.man();
				return;
			}
			
			//Main.subMain();
		}
		catch(Exception e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args){
		//long startTime = System.currentTimeMillis();
		
		Main.subMain();
		//Main.cli(args);
		
		//long stopTime = System.currentTimeMillis();
		//double timeMiliSec = stopTime - startTime;
		//System.out.println("Time elapsed: " + timeMiliSec / 1000);
	}

}