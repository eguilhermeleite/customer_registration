package com.anna.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.anna.domain.User;
import com.anna.repository.UserRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/cusreg")
public class UserController extends HttpServlet implements Serializable {
	private static final long serialVersionUID = 1L;

	// chaves de desencriptar e encriptar
	private String decKey = "NV2M5TnBxtHznZiBF85yNEP1FbnPPqvD";
	private String encKey = "lgmsTAiDqINHDQgu58gM2d3AKpPwV/tM";

	@Autowired
	private UserRepository userRepository;

	public UserController() {
		super();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		try {
			// variaveis vindas no corpo da requisicao
			String ivReceived = request.getParameter("ANNAEXEC");// esse é o iv dinâmico vindo do AnnA
			String cpfRec = request.getParameter("cpf");// cep digitado pelo usuario
			String namRec = request.getParameter("name");// nome digitado pelo usuario
			String emaRec = request.getParameter("email");// email digitado pelo usuario

			// obter os bytes das chaves ANNAEXC, chave de desencriptar e chave de encriptar
			byte[] ivDecoded = Base64.getDecoder().decode(ivReceived);
			byte[] decKeyDecoded = Base64.getDecoder().decode(decKey);
			byte[] encKeyDecoded = Base64.getDecoder().decode(encKey);

			// associar as chaves aos tipos necessarios para os metodos de encrip./desencr.
			IvParameterSpec iv = new IvParameterSpec(ivDecoded);
			SecretKey dKey = new SecretKeySpec(decKeyDecoded, "DESede");
			SecretKey eKey = new SecretKeySpec(encKeyDecoded, "DESede");

			// agora e possivel desencriptar os valores digitados
			String cpf = decrypt(cpfRec, dKey, iv);
			String name = decrypt(namRec, dKey, iv);
			String email = decrypt(emaRec, dKey, iv);

			User user = new User(cpf, name, email);
			userRepository.save(user);

			String finalResponse = "[{";
			finalResponse += "\"PropName\":\"EXECFUNCTION002\",";
			finalResponse += "\"PropValue\":";
			finalResponse += "[";
			finalResponse += "{\"";
			finalResponse += "PropName\":\"Type\",";
			finalResponse += "\"PropValue\":\"EXECFUNCTION\"";
			finalResponse += "},";

			// o AnnA captura o valor das variaveis aqui
			finalResponse += "{";
			finalResponse += "\"PropName\":\"Expression\",";
			finalResponse += "\"PropValue\":\"AddParm(CPF," + cpf + ")AddParm(NOME," + name + ")AddParm(EMAIL," + email
					+ ")\"";
			finalResponse += "},";

			finalResponse += "]";
			finalResponse += "}]";

			// Gerando um novo IV
			byte[] randomBytes = new byte[8];
			new Random().nextBytes(randomBytes);
			final IvParameterSpec newIV = new IvParameterSpec(randomBytes);
			String newIVEncoded = new String(Base64.getEncoder().encode(randomBytes));

			// Encriptação do "IV Novo" com a chave de desencriptação e o "IV Recebido"
			finalResponse = encrypt(finalResponse, eKey, newIV);
			String newIVEncrip = encrypt(newIVEncoded, dKey, iv);

			// Concatenação do JSON de resposta encriptado, o "IV Recebido" e o "IV Novo
			// Encriptado"
			finalResponse = finalResponse + ivReceived + newIVEncrip;

			// Envio das informações ao AnnA
			PrintWriter out = response.getWriter();
			out.print(finalResponse);

		} catch (Exception e) {

		}

	}// doPost

	// metodos de encrypt e decrypt
	public String encrypt(String message, SecretKey key, IvParameterSpec iv) throws NoSuchAlgorithmException,
			NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException,
			UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException {
		final Cipher cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, key, iv);
		byte[] plainTextBytes = message.getBytes("utf-8");
		byte[] buf = cipher.doFinal(plainTextBytes);
		byte[] base64Bytes = Base64.getEncoder().encode(buf);
		String base64EncryptedString = new String(base64Bytes);
		return base64EncryptedString;
	}

	public String decrypt(String encMessage, SecretKey key, IvParameterSpec iv)
			throws UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
			IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		byte[] message = Base64.getDecoder().decode(encMessage.getBytes("utf-8"));
		final Cipher decipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
		decipher.init(Cipher.DECRYPT_MODE, key, iv);
		byte[] plainText = decipher.doFinal(message);
		return new String(plainText, "UTF-8");
	}

	/*
	 * @PostMapping public User save(@RequestBody User user) { return
	 * userRepository.save(user); }
	 */

}
