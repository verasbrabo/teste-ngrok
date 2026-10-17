package app.auth;

import java.security.SecureRandom;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import app.DTO.NotificationTokenDTO;
import app.DTO.RegisterFuncionarioDTO;
import app.DTO.TrocarSenhaDTO;
import app.DTO.UpdateMeDTO;
import app.DTO.UsuarioResponseDTO;
import app.auth.DTO.EmailDTO;
import app.auth.DTO.RegisterAdminResponseDTO;
import app.auth.DTO.RegisterDTO;
import app.auth.DTO.ResetPasswordDTO;
import app.auth.DTO.RegisterProprietarioDTO;
import app.entity.Cliente;
import app.entity.Funcionario;
import app.entity.Quiosque;
import app.entity.RecoveryToken;
import app.entity.Usuario;
import app.enums.StatusConta;
import app.enums.TipoConta;
import app.enums.UserRole;
import app.repository.PedidoRepository;
import app.repository.FuncionarioRepository;
import app.repository.QuiosqueRepository;
import app.repository.RecoveryTokenRepository;
import app.service.EmailService;
import app.service.ImageService;
import jakarta.transaction.Transactional;



@Service
public class AuthService implements UserDetailsService{
	private static final String PREFIXO_CLIENTE = "CLIENTE::";
	private static final String PREFIXO_QUIOSQUE = "QUIOSQUE::";
	private static final Set<UserRole> ROLES_QUIOSQUE = Set.of(
			UserRole.PROPRIETARIO, UserRole.GERENTE, UserRole.FUNCIONARIO);

	@Autowired
	private AuthRepository usuarioRepository;
	@Autowired
	private FuncionarioRepository funcionarioRepository;
	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	private QuiosqueRepository quiosqueRepository;
	@Autowired
	private ImageService imageService;
	@Autowired
	private PedidoRepository pedidoRepository;
	@Autowired
	private EmailService emailService;
	@Autowired
	private RecoveryTokenRepository recoveryTokenRepository;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		if (username.startsWith(PREFIXO_CLIENTE)) {
			String login = username.substring(PREFIXO_CLIENTE.length());
			return usuarioRepository.findByLoginAndRole(login, UserRole.CLIENTE)
					.orElseThrow(() -> new UsernameNotFoundException("Conta não encontrada"));
		}
		if (username.startsWith(PREFIXO_QUIOSQUE)) {
			String login = username.substring(PREFIXO_QUIOSQUE.length()).trim();
			return usuarioRepository.findByLoginAndRoleIn(login, ROLES_QUIOSQUE)
					.orElseThrow(() -> new UsernameNotFoundException("Conta não encontrada"));
		}
		throw new UsernameNotFoundException("Tipo de conta não informado");
	}

	static String identificadorAutenticacao(String login, TipoConta tipoConta) {
		String prefixo = tipoConta == TipoConta.CLIENTE
				? PREFIXO_CLIENTE
				: PREFIXO_QUIOSQUE;
		return prefixo + login;
	}

	public Usuario register(RegisterDTO data){
		if (data.role() == UserRole.PROPRIETARIO) {
			String cnpj = data.cnpj() != null && !data.cnpj().isBlank()
					? data.cnpj()
					: data.cpf();
			if (cnpj == null || cnpj.isBlank()) {
				throw new ResponseStatusException(
						HttpStatus.BAD_REQUEST, "CNPJ é obrigatório para proprietários");
			}
			return registerProprietario(
					new RegisterProprietarioDTO(
							data.nome(), data.email(), data.telefone(), cnpj, data.password()),
					null);
		}
		if (data.role() != UserRole.CLIENTE) {
			throw new ResponseStatusException(
					HttpStatus.BAD_REQUEST,
					"Funcionários devem ser cadastrados pelo gestor do quiosque");
		}
		if (usuarioRepository.existsByLoginAndRole(data.email(), UserRole.CLIENTE))
			throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado");
		if (usuarioRepository.existsByTelefoneAndRole(data.telefone(), UserRole.CLIENTE))
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Telefone já cadastrado");

		String encryptedPassword = passwordEncoder.encode(data.password());
		Usuario user = new Cliente(data.nome(), data.email(), encryptedPassword,
				normalizarCpf(data.cpf()), data.telefone(), data.role());

		user.setUltimoLogin(LocalDateTime.now());

		return usuarioRepository.save(user);
	}

	@Transactional
	public Funcionario registerProprietario(
			RegisterProprietarioDTO data,
			MultipartFile file) {
		String email = normalizarEmail(data.email());
		if (emailQuiosqueEmUso(email, null))
			throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado");
		if (usuarioRepository.existsByTelefoneAndRoleIn(data.telefone(), ROLES_QUIOSQUE))
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Telefone já cadastrado");

		String cnpj = normalizarCnpj(data.cnpj());
		if (cnpj.length() != 14)
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CNPJ inválido");
		if (funcionarioRepository.existsByCnpj(cnpj))
			throw new ResponseStatusException(HttpStatus.CONFLICT, "CNPJ já cadastrado");

		Funcionario proprietario = new Funcionario(
				data.nome(),
				email,
				passwordEncoder.encode(data.password()),
				"",
				data.telefone(),
				UserRole.PROPRIETARIO);
		// No proprietário, o e-mail de contato também é o login da conta.
		proprietario.setEmail(email);
		proprietario.setCnpj(cnpj);
		proprietario.setUltimoLogin(LocalDateTime.now());
		proprietario = usuarioRepository.save(proprietario);

		if (file != null && !file.isEmpty())
			imageService.uploadImagemUsuario(proprietario, file);

		return proprietario;
	}

	private static String normalizarCnpj(String cnpj) {
		return cnpj.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
	}
	
	@Transactional
	public RegisterAdminResponseDTO registerFuncionario(
			Usuario usuario,
			Long idQuiosque,
			RegisterFuncionarioDTO data) {
		Quiosque quiosque = findQuiosqueForUser(usuario, idQuiosque);
		UserRole role = validarCargoCadastro(usuario.getRole(), data.role());
		String email = normalizarEmail(data.email());
		if (emailQuiosqueEmUso(email, null)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado");
		}
		if (usuarioRepository.existsByTelefoneAndRoleIn(data.telefone(), ROLES_QUIOSQUE)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Telefone já cadastrado");
		}

		String senha = UUID.randomUUID().toString().substring(0, 8);
		String encryptedPassword = passwordEncoder.encode(senha);
		String login = procurarDuplicatas(gerarUsername(data.nome(), quiosque.getCodigo()));

		Funcionario user = new Funcionario(
				data.nome(), login, encryptedPassword, normalizarCpf(data.cpf()),
				data.telefone(), role);
		user.setEmail(email);
		user.setInicioTurno(data.startShift());
		user.setFimTurno(data.finishShift());
		if (data.diasTurno() != null)
			user.setDiasTurno(data.diasTurno());
		user.setDeveTrocarSenha(true);
		user.setSenhaCifrada(null);
		user.setQuiosque(quiosque);
		quiosque.getFuncionarios().add(user);
		user = usuarioRepository.save(user);
		quiosqueRepository.save(quiosque);

		RegisterAdminResponseDTO response = new RegisterAdminResponseDTO(
				user.getPublicId(),
				quiosque.getNome(),
				user.getEmail(),
				user.getNome(),
				user.getDiasTurnoLista(),
				user.getRole(),
				user.getTelefone());

		// A credencial provisória existe apenas durante o envio e nunca é devolvida
		// ao gestor nem persistida de forma reversível.
		emailService.enviarCredenciaisFuncionario(user.getEmail(), login, senha);
		return response;
	}

	/** Garante que somente o proprietário possa conceder o cargo de gerente. */
	static UserRole validarCargoCadastro(UserRole cargoSolicitante, UserRole cargoSolicitado) {
		if (cargoSolicitado != UserRole.FUNCIONARIO && cargoSolicitado != UserRole.GERENTE) {
			throw new ResponseStatusException(
					HttpStatus.BAD_REQUEST, "Cargo inválido para funcionário");
		}

		if (cargoSolicitado == UserRole.GERENTE && cargoSolicitante != UserRole.PROPRIETARIO) {
			throw new ResponseStatusException(
					HttpStatus.FORBIDDEN, "Apenas proprietários podem cadastrar gerentes");
		}

		return cargoSolicitado;
	}

	private String procurarDuplicatas(String username) {
		List<String> usernames = usuarioRepository.findLoginByBase(username);
		if (!usernames.contains(username))
			return username;

		int count = 2;
		while (usernames.contains(username + count))
			count++;
		return username + count;
	}
	
	public void forgotPassword(EmailDTO data) {
		String email = normalizarEmail(data.email());
		Optional<Usuario> user = data.tipoConta() == TipoConta.CLIENTE
				? usuarioRepository.findByLoginAndRole(email, UserRole.CLIENTE)
				: usuarioRepository.findByLoginAndRole(email, UserRole.PROPRIETARIO);

		if (user.isEmpty()) return;

		Usuario usuario = user.get();
		Boolean naoPassou5Minutos = recoveryTokenRepository
				.existsByUsuarioAndEnviadoAfterAndExpiracaoAfter(
						usuario, LocalDateTime.now().minusMinutes(5), LocalDateTime.now());
		if (naoPassou5Minutos) return;

		String codigo = gerarCodigo(4);
		
		RecoveryToken token = new RecoveryToken();
		
		
		
		token.setToken(codigo);
		token.setUsuario(usuario);
		token.setExpiracao(LocalDateTime.now().plusMinutes(30));
		token.setEnviado(LocalDateTime.now());
		
		this.recoveryTokenRepository.save(token);
		
		emailService.enviarEmail(email, "Código de recuperação da sua conta", codigo);
	}
	
	@Transactional
	public void resetPassword(String token, ResetPasswordDTO data) {
		Set<UserRole> roles = data.tipoConta() == TipoConta.CLIENTE
				? Set.of(UserRole.CLIENTE)
				: Set.of(UserRole.PROPRIETARIO);

		RecoveryToken recoveryToken =
				recoveryTokenRepository.findByTokenAndUsuarioRoleInAndExpiracaoAfter(
						token, roles, LocalDateTime.now())
                        .orElseThrow(() ->
                                new RuntimeException("Token inválido"));

        if (recoveryToken.getExpiracao().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expirado");
        }

        Usuario usuario = recoveryToken.getUsuario();

        usuario.setSenhaHash(passwordEncoder.encode(data.password()));
		usuario.setSenhaCifrada(null);
		if (usuario instanceof Funcionario funcionario)
			funcionario.setDeveTrocarSenha(false);

        usuarioRepository.save(usuario);

        recoveryTokenRepository.delete(recoveryToken);
    }
	
	@Transactional
	public void deletarTokensObsoletos() {
		recoveryTokenRepository.deleteByExpiracaoBefore(LocalDateTime.now());
	}
	
	
	private String gerarCodigo(int tamanho) { 
		String CARACTERES = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
	    SecureRandom random = new SecureRandom();
	    StringBuilder codigo = new StringBuilder(tamanho);
	    for (int i = 0; i < tamanho; i++) {
            int index = random.nextInt(CARACTERES.length());
            codigo.append(CARACTERES.charAt(index));
	    }

        return codigo.toString();
	}
	
	
	private static String normalizarEmail(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}

	private String gerarUsername(String nomeCompleto, String codigoQuiosque) {
		String[] partes = nomeCompleto.trim().split("\\s+");
		String nome = normalizar(partes[0]);
		if (partes.length == 1)
			return codigoQuiosque + nome;
		String sobrenome = normalizar(partes[partes.length - 1]);
		return codigoQuiosque + nome + sobrenome;
	}

	private String normalizar(String texto) {
		return Normalizer
				.normalize(texto, Normalizer.Form.NFD)
				.replaceAll("\\p{M}", "")
				.toLowerCase(Locale.ROOT)
				.replaceAll("[^a-z0-9]", "");
	}

	/** E-mail é único dentro do contexto do quiosque, mas pode existir no cliente. */
	private boolean emailQuiosqueEmUso(String email, Long ignorarId) {
		if (ignorarId == null) {
			return funcionarioRepository.existsByEmailIgnoreCaseAndRoleIn(email, ROLES_QUIOSQUE)
					|| usuarioRepository.existsByLoginAndRoleIn(email, ROLES_QUIOSQUE);
		}
		return funcionarioRepository.existsByEmailIgnoreCaseAndRoleInAndIdNot(
				email, ROLES_QUIOSQUE, ignorarId)
				|| usuarioRepository.existsByLoginAndRoleInAndIdNot(
						email, ROLES_QUIOSQUE, ignorarId);
	}

	// CPF é opcional (funcionário/gerente não o informam), mas a coluna
	// usuario.cpf é NOT NULL no banco. Normaliza ausência para string vazia,
	// mesma convenção já usada nos proprietários sem CPF, evitando o
	// "Column 'cpf' cannot be null" (não há unique em cpf, então "" repetido
	// não conflita).
	static String normalizarCpf(String cpf) {
		if (cpf == null || cpf.isBlank())
			return "";

		String cpfNormalizado = cpf.replaceAll("\\D", "");
		return validarCPF(cpfNormalizado) ? cpfNormalizado : "";
	}
	
	private static boolean validarCPF(String cpf) {
		if(cpf == null || cpf.isBlank()) {
			return false;
		}

	    // Remove pontos e hífen
	    cpf = cpf.replaceAll("\\D", "");
	    
	    // CPF deve ter exatamente 11 dígitos
	    if (cpf.length() != 11) {
	        return false;
	    }

	    // Rejeita CPFs com todos os dígitos iguais
	    if (cpf.matches("(\\d)\\1{10}")) {
	        return false;
	    }

	    // Primeiro dígito verificador
	    int soma = 0;
	    for (int i = 0; i < 9; i++) {
	        soma += Character.getNumericValue(cpf.charAt(i)) * (10 - i);
	    }

	    int resto = soma % 11;
	    int digito1 = (resto < 2) ? 0 : 11 - resto;

	    // Segundo dígito verificador
	    soma = 0;
	    for (int i = 0; i < 10; i++) {
	        soma += Character.getNumericValue(cpf.charAt(i)) * (11 - i);
	    }

	    resto = soma % 11;
	    int digito2 = (resto < 2) ? 0 : 11 - resto;

	    // Compara os dígitos calculados com os do CPF
	    return digito1 == Character.getNumericValue(cpf.charAt(9))
	        && digito2 == Character.getNumericValue(cpf.charAt(10));
	}


	@Transactional
	public UsuarioResponseDTO putUsuario(Usuario user, UpdateMeDTO data){
		validarDadosUnicos(user, data);
		user.atualizarPerfil(data);
		if (user instanceof Funcionario funcionario
				&& data.email() != null
				&& !data.email().isBlank()) {
			String email = normalizarEmail(data.email());
			funcionario.setEmail(email);
		}
		usuarioRepository.save(user);
		UsuarioResponseDTO response = UsuarioResponseDTO.from(user);
	    return response;
	}

	private void validarDadosUnicos(Usuario user, UpdateMeDTO data) {
		boolean contaCliente = user.getRole() == UserRole.CLIENTE;
		String emailAtual = user instanceof Funcionario funcionario
				&& funcionario.getEmail() != null && !funcionario.getEmail().isBlank()
						? funcionario.getEmail()
						: user.getLogin();
		if (data.email() != null && !data.email().isBlank()
				&& !data.email().equalsIgnoreCase(emailAtual)) {
			String email = normalizarEmail(data.email());
			boolean emailEmUso = contaCliente
					? usuarioRepository.existsByLoginAndRoleAndIdNot(
							email, UserRole.CLIENTE, user.getId())
					: emailQuiosqueEmUso(email, user.getId());
			if (emailEmUso)
				throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado");
		}

		if (data.telefone() != null && !data.telefone().isBlank()
				&& !data.telefone().equals(user.getTelefone())) {
			boolean telefoneEmUso = contaCliente
					? usuarioRepository.existsByTelefoneAndRoleAndIdNot(
							data.telefone(), UserRole.CLIENTE, user.getId())
					: usuarioRepository.existsByTelefoneAndRoleInAndIdNot(
							data.telefone(), ROLES_QUIOSQUE, user.getId());
			if (telefoneEmUso)
				throw new ResponseStatusException(HttpStatus.CONFLICT, "Telefone já cadastrado");
		}
	}
	
	@Transactional
	public void patchSenha(Usuario usuario, TrocarSenhaDTO data) {
		if (!passwordEncoder.matches(data.senhaAtual(), usuario.getSenhaHash())) {
	        throw new RuntimeException("Senha atual inválida");
	    }
		
		if (data.novaSenha() != null && !data.novaSenha().isBlank()) {
			usuario.setSenhaHash(passwordEncoder.encode(data.novaSenha()));
			usuario.setSenhaCifrada(null);
			if (usuario instanceof Funcionario funcionario)
				funcionario.setDeveTrocarSenha(false);
		}
	}
	
	@Transactional
	public void deleteMe(Funcionario user) {
		// Remove os pedidos do cliente antes de apagar o usuário; sem isso a FK
		// pedido.idCliente -> usuario bloqueia a exclusão (DataIntegrityViolation).
		// Os itemPedidos saem em cascata (Pedido.itemPedidos = CascadeType.ALL).
		var pedidos = pedidoRepository.findByClienteOrderByDataHoraPedido(user);
		if (!pedidos.isEmpty())
			pedidoRepository.deleteAll(pedidos);

		// Proprietário: o quiosque não pode ser apagado em cascata — seus itens
		// permanecem referenciados pelo histórico de pedidos (item_pedido.id_item
		// -> item é FK NO ACTION), então excluir categorias/itens quebraria com
		// DataIntegrityViolation. Em vez disso, desativa o quiosque e remove o
		// vínculo com o proprietário (senão quiosque.proprietario_id -> usuario
		// bloqueia a exclusão do usuário).
		if (user.getRole() == UserRole.PROPRIETARIO) {
				// Set quiosques desativado
				List<Quiosque> qs = quiosqueRepository.findByProprietario(user);
				qs.stream().forEach(q -> q.setStatus(StatusConta.Desativada));
				user.setQuiosques(null);
				quiosqueRepository.saveAll(qs);
			
			// Ainda não testei isso aqui
			
//			quiosqueRepository.findByEmpresaProprietario(user).ifPresent(q -> {
//				q.getEmpresa().setStatus(StatusConta.Desativada);
//				q.setEmpresa(null);
//				q.setStatus(StatusConta.Desativada);
//				quiosqueRepository.save(q);
//			});
		}

		if(user.getImagem() != null)
			deleteImagemUsuario(user);
		usuarioRepository.delete(user);
	}
	
	public void saveToken(Usuario user, NotificationTokenDTO data) {
		user.setDeviceToken(data.token());
		usuarioRepository.save(user);
	}
	
	@Transactional
	public String uploadImagemUsuario(
			Usuario usuario,
			MultipartFile file) {

		return imageService.uploadImagemUsuario(usuario, file);		
	}
	
	@Transactional
	public void deleteImagemUsuario(
			Usuario usuario) {
		
		imageService.deleteImagemUsuario(usuario);
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

}
