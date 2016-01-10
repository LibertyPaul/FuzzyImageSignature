package qrStorage;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class DigitalSignature{
	private static Signature getSignature() throws Exception{
		Signature dsa = null;
		try{
			dsa = Signature.getInstance("SHA1withDSA", "SUN");
		}
		catch(NoSuchAlgorithmException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new Exception("NoSuchAlgorithmException");
		}
		catch(NoSuchProviderException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new Exception("NoSuchProviderException");
		}
		
		return dsa;
	}
	
	private static byte[] sign(final byte[] data, final PrivateKey privateKey) throws Exception{
		Signature dsa = DigitalSignature.getSignature();
		dsa.initSign(privateKey);
		dsa.update(data);
		return dsa.sign();
	}
	
	private static boolean verify(final byte[] data, final byte[] signature, final PublicKey publicKey) throws Exception{
		Signature dsa = DigitalSignature.getSignature();
		dsa.initVerify(publicKey);
		dsa.update(data);
		return dsa.verify(signature);
	}
	
	
	
	
	
	public static byte[] sign(final byte[] data, final File privateKeySrcFile) throws Exception{
		FileInputStream fis = new FileInputStream(privateKeySrcFile);		
		byte[] keyData = new byte[fis.available()];
		fis.read(keyData);
		fis.close();
		
		PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(keyData);
		KeyFactory keyFactory = KeyFactory.getInstance("DSA", "SUN");
		PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);
		
		return DigitalSignature.sign(data, privateKey);
	}
	
	public static boolean verify(final byte[] data, final byte[] signature, final File publicKeySrcFile) throws Exception{
		FileInputStream fis = new FileInputStream(publicKeySrcFile);		
		byte[] keyData = new byte[fis.available()];
		fis.read(keyData);
		fis.close();
		
		X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(keyData);
		KeyFactory keyFactory = KeyFactory.getInstance("DSA", "SUN");
		PublicKey publicKey = keyFactory.generatePublic(pubKeySpec);
		
		return DigitalSignature.verify(data, signature, publicKey);
	}
}














