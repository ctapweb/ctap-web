package com.ctapweb.web.server.user;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import nl.captcha.Captcha;
import nl.captcha.backgrounds.GradiatedBackgroundProducer;
import nl.captcha.servlet.CaptchaServletUtil;
import nl.captcha.servlet.SimpleCaptchaServlet;

public class CTAPCaptchaServlet extends SimpleCaptchaServlet {

	private static final long serialVersionUID = 6560171562324177699L;

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		HttpSession session = req.getSession();
		Captcha captcha = new Captcha.Builder(200, 50)
				.addText()
				.addBackground(new GradiatedBackgroundProducer())
				.gimp()
				.addNoise()
				.build();
		session.setAttribute(Captcha.NAME, captcha);
		CaptchaServletUtil.writeImage(resp, captcha.getImage());
	}
}
