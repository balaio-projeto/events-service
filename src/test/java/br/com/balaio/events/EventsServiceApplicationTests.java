package br.com.balaio.events;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPairGenerator;
import java.util.Base64;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
class EventsServiceApplicationTests {

	/**
	 * O serviço exige configuração JWT na inicialização. O CI não possui arquivo de chave,
	 * então o teste gera um par efêmero e fornece somente a chave pública, como em produção.
	 */
	@DynamicPropertySource
	static void configuracaoJwt(DynamicPropertyRegistry registry) throws IOException {
		try {
			var gerador = KeyPairGenerator.getInstance("RSA");
			gerador.initialize(3072);
			byte[] publica = gerador.generateKeyPair().getPublic().getEncoded();
			String pem = "-----BEGIN PUBLIC KEY-----\n"
					+ Base64.getMimeEncoder(64, new byte[] { '\n' }).encodeToString(publica)
					+ "\n-----END PUBLIC KEY-----\n";
			Path arquivo = Files.createTempFile("jwt-public-", ".pem");
			arquivo.toFile().deleteOnExit();
			Files.writeString(arquivo, pem, StandardCharsets.US_ASCII);
			registry.add("balaio.jwt.public-key-location", () -> arquivo.toString());
		} catch (java.security.GeneralSecurityException exception) {
			throw new IllegalStateException(exception);
		}
		registry.add("balaio.jwt.issuer", () -> "balaio-local");
		registry.add("balaio.jwt.audience", () -> "balaio-api");
	}

	@Test
	void contextLoads() {
	}

}