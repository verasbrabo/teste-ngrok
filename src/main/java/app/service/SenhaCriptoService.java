package app.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Component;

/**
 * Criptografia reversível (AES-256/GCM) da senha do funcionário, usada apenas
 * para reexibir a senha ao gestor na tela de gerenciar funcionários.
 *
 * Diferente do {@code senhaHash} (BCrypt, mão única, usado na autenticação),
 * aqui a senha é cifrada de forma reversível: fica ilegível no banco
 * (`senha_cifrada`), mas o servidor consegue decifrá-la com a chave secreta
 * para responder a requisição. A chave/salt ficam em configuração
 * (`application.properties` / variáveis de ambiente), nunca no banco.
 */
@Component
public class SenhaCriptoService {

	private final TextEncryptor encryptor;

	public SenhaCriptoService(
			@Value("${app.senha.cripto.chave}") String chave,
			@Value("${app.senha.cripto.salt}") String salt) {
		// Encryptors.delux: AES-256 GCM com IV aleatório; salt em hexadecimal.
		this.encryptor = Encryptors.delux(chave, salt);
	}

	/** Cifra a senha em texto puro. Retorna `null` para entrada nula. */
	public String cifrar(String texto) {
		if (texto == null) return null;
		return encryptor.encrypt(texto);
	}

	/**
	 * Decifra a senha. Retorna `null` quando não há valor cifrado (ex.: contas
	 * antigas, anteriores a esta funcionalidade).
	 */
	public String decifrar(String cifrado) {
		if (cifrado == null || cifrado.isBlank()) return null;
		return encryptor.decrypt(cifrado);
	}
}
