package com.just.mvnbook.account.captcha;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.InitializingBean;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;

public class AccountCaptchaServiceImpl implements AccountCaptchaService,InitializingBean {

	private DefaultKaptcha producter;
	
	private Map<String,String> captchaMap = new HashMap<String,String>();
	
	private List<String> preDefinedTexts;
	
	private int textCount = 0;
	
	public String generateCaptchaKey() throws AccountCaptchaException {
		String key = RandomGenerator.getRandomString();
		String value = getCaptchaText();
		captchaMap.put(key, value);
		return key;
	}

	private String getCaptchaText() {
		if(preDefinedTexts != null && !preDefinedTexts.isEmpty()) {
			String text = preDefinedTexts.get(textCount);
			textCount = (textCount + 1) % preDefinedTexts.size();
			return text;
		}else {
			return producter.createText();
		}
	}

	public byte[] generateCaptchaImage(String captchaKey)
			throws AccountCaptchaException {
		String text = captchaMap.get(captchaKey);
		if(text == null) {
			throw new AccountCaptchaException("Captch key'"+captchaKey+"'not found!");
		}
		BufferedImage image = producter.createImage(text);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try{
			ImageIO.write(image, "jpg", out);
		}catch(IOException e) {
			throw  new AccountCaptchaException("failed to write captcha stream!");
		}
		return out.toByteArray();
	}

	public boolean validateCaptcha(String captchaKey, String captchaValue)
			throws AccountCaptchaException {
		String text = captchaMap.get(captchaKey);
		if(text == null) {
			throw new AccountCaptchaException("Captch key'"+captchaKey+"'not found!");
		}
		if(text.equals(captchaValue)) {
			captchaMap.remove(captchaKey);
			return true;
		}else {
			return false;
		}
	}

	public List<String> getPreDefinedTexts() {
		return preDefinedTexts;
	}

	public void setPreDefinedTexts(List<String> preDefinedTexts) {
		this.preDefinedTexts = preDefinedTexts;
	}

	public void afterPropertiesSet() throws Exception {
		producter = new DefaultKaptcha();
		producter.setConfig(new Config(new Properties()));
	}

}
