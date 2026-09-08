package app.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import app.DTO.FuncionarioResponseDTO;
import app.DTO.RegisterFuncionarioDTO;
import app.DTO.TrocarSenhaDTO;
import app.auth.AuthRepository;
import app.auth.DTO.RegisterDTO;
import app.entity.Funcionario;
import app.entity.Quiosque;
import app.entity.Usuario;
import app.enums.UserRole;
import app.repository.QuiosqueRepository;
import jakarta.transaction.Transactional;

@Service
public class FuncionarioService {

	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	private AuthRepository usuarioRepository;
	@Autowired
	private QuiosqueRepository quiosqueRepository;
	@Autowired
	private ImageService imageService;
	@Autowired
	private SenhaCriptoService senhaCriptoService;

	/** Monta o DTO já com a senha decifrada (AES) para reexibição ao gestor. */
	private FuncionarioResponseDTO toDTO(Usuario u) {
		return FuncionarioResponseDTO.from(u, senhaCriptoService.decifrar(u.getSenhaCifrada()));
	}

	public List<FuncionarioResponseDTO> getFuncionarios(Usuario usuario, Long id_quiosque) {
		Quiosque q = findQuiosqueForUser(usuario, id_quiosque);

		// A consulta usa explicitamente o ID validado para impedir que a coleção
		// carregada de outro quiosque seja reutilizada pela sessão de persistência.
		List<Funcionario> usuarios = usuarioRepository.findByQuiosqueId(q.getId());

		return usuarios.stream().map(this::toDTO).toList();
	}

	public FuncionarioResponseDTO getFuncionario(Usuario usuario, Long id_quiosque, UUID id) {
		Quiosque q = findQuiosqueForUser(usuario, id_quiosque);

		for (Usuario u : q.getFuncionarios()) {
		    if (id.equals(u.getPublicId())) {
		        return toDTO(u);
		    }
		}

		throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Funcionario não encontrada nesse quiosque");
	}
	
	@Transactional
	public FuncionarioResponseDTO putFuncionario(Usuario usuario, Long id_quiosque, UUID id, RegisterFuncionarioDTO data){
		
		Quiosque quiosque = findQuiosqueForUser(usuario, id_quiosque);
		
		Funcionario user = this.usuarioRepository.findByQuiosqueAndPublicId(quiosque, id)
						.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,("Funcionario não encontrada nesse quiosque")));
		
		if (data.nome() != null && !data.nome().isBlank()) {
		    user.setNome(data.nome());
		}

		if (data.cpf() != null && !data.cpf().isBlank()) {
		    user.setCpf(data.cpf());
		}
		
		if (data.role() != null && usuario.getRole().equals(UserRole.PROPRIETARIO) && !data.role().equals(UserRole.PROPRIETARIO) && !data.role().equals(UserRole.CLIENTE)) {
		    user.setRole(data.role());
		}

		if(data.startShift() != null)
			user.setInicioTurno(data.startShift());
		if(data.finishShift() != null)
			user.setFimTurno(data.finishShift());
		

		if (data.telefone() != null && !data.telefone().isBlank()) {
		    user.setTelefone(data.telefone());
		}

	    FuncionarioResponseDTO response = toDTO(user);
	    return response;
	}
	
	@Transactional
	public String resetPassword(Usuario usuario, Long id_quiosque, UUID id) {
		Quiosque quiosque = findQuiosqueForUser(usuario, id_quiosque);
		
		Usuario user = this.usuarioRepository.findByQuiosqueAndPublicId(quiosque, id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,("Funcionario não encontrada nesse quiosque")));
		
		String senha = UUID.randomUUID().toString().substring(0, 8);

		String encryptedPassword = passwordEncoder.encode(senha);

		user.setSenhaHash(encryptedPassword);
		user.setSenhaCifrada(senhaCriptoService.cifrar(senha));
		return senha;
	}
	
	@Transactional
	public String uploadImagemUsuario(
			Usuario usuario,
			Long id_quiosque,
			UUID id,
			MultipartFile file) {

		Quiosque quiosque = findQuiosqueForUser(usuario, id_quiosque);
		
		Usuario funcionario = usuarioRepository.findByQuiosqueAndPublicId(quiosque, id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,("Funcionario não encontrada nesse quiosque")));

		
		return imageService.uploadImagemUsuario(funcionario, file);		
	}
	
	@Transactional
	public void deleteImagemUsuario(
			Usuario usuario,
			Long id_quiosque,
			UUID id) {
		
		Quiosque quiosque = findQuiosqueForUser(usuario, id_quiosque);
		
		Usuario funcionario = usuarioRepository.findByQuiosqueAndPublicId(quiosque, id)
								.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,("Funcionario não encontrada nesse quiosque")));
		
		imageService.deleteImagemUsuario(funcionario);
	}
	
//	@Transactional
//	public void patchFuncionarioSenha(Usuario usuario, Long id_quiosque, TrocarSenhaDTO data) {
//		findQuiosqueForUser(usuario, id_quiosque);		
//
//	    if (!passwordEncoder.matches(data.senhaAtual(), usuario.getSenhaHash())) {
//	        throw new RuntimeException("Senha atual inválida");
//	    }
//		
//		if (data.novaSenha() != null && !data.novaSenha().isBlank()) 
//			usuario.setSenhaHash(passwordEncoder.encode(data.novaSenha()));
//			
//	}
	
	@Transactional
	public void deleteFuncionario(Usuario usuario, Long id_quiosque, UUID id) {
		findQuiosqueForUser(usuario, id_quiosque);

		this.usuarioRepository.deleteByPublicId(id);
	}
	
	private Quiosque findQuiosqueForUser(Usuario usuario, Long id) {

	    Quiosque quiosque = quiosqueRepository.findById(id)
	        .orElseThrow(() -> new ResponseStatusException(
	            HttpStatus.NOT_FOUND, "Quiosque não encontrado"));

	    if (!quiosque.usuarioPossuiAcesso(usuario)) {
	        throw new ResponseStatusException(
	            HttpStatus.FORBIDDEN, "Usuário não tem acesso a esse quiosque");
	    }

	    return quiosque;
	}
	
//	private Quiosque findQuiosqueForUser(Funcionario usuario) {
//		Quiosque quiosque;
//		if(usuario.getRole().equals(UserRole.PROPRIETARIO))
//			quiosque = quiosqueRepository.findByEmpresaProprietario(usuario)
//	        .orElseThrow(() -> new ResponseStatusException(
//	            HttpStatus.NOT_FOUND, "Quiosque não encontrado"));
//		else
//			quiosque = quiosqueRepository.findByFuncionariosContaining(usuario)
//			.orElseThrow(() -> new ResponseStatusException(
//		        HttpStatus.NOT_FOUND, "Quiosque não encontrado"));
//
//	    return quiosque;
//	}
}
