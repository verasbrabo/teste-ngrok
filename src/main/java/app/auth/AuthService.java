package app.auth;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
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

import app.DTO.FuncionarioResponseDTO;
import app.DTO.NotificationTokenDTO;
import app.DTO.RegisterFuncionarioDTO;
import app.DTO.TrocarSenhaDTO;
import app.DTO.UpdateMeDTO;
import app.DTO.UsuarioResponseDTO;
import app.auth.DTO.RegisterAdminResponseDTO;
import app.auth.DTO.RegisterDTO;
import app.entity.Cliente;
import app.entity.Funcionario;
import app.entity.Quiosque;
import app.entity.Usuario;
import app.enums.StatusConta;
import app.enums.UserRole;
import app.repository.PedidoRepository;
import app.repository.QuiosqueRepository;
import app.service.ImageService;
import app.service.SenhaCriptoService;
import jakarta.transaction.Transactional;



@Service
public class AuthService implements UserDetailsService{

	@Autowired
	private AuthRepository usuarioRepository;
	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	private QuiosqueRepository quiosqueRepository;
	@Autowired
	private ImageService imageService;
	@Autowired
	private PedidoRepository pedidoRepository;
	@Autowired
	private SenhaCriptoService senhaCriptoService;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return usuarioRepository.findByLogin(username);
	}
	
	public Usuario register(RegisterDTO data){
	    if(usuarioRepository.findByLogin(data.email()) != null)
	        throw new RuntimeException("Usuário já cadastrado");
	    if(!data.role().equals(UserRole.PROPRIETARIO) && !data.role().equals(UserRole.CLIENTE)) {
	    	throw new ResponseStatusException(
	                HttpStatus.UNAUTHORIZED, "Apenas gerentes podem cadastrar funcionarios");
	    }
	    String encryptedPassword = passwordEncoder.encode(data.password());
	    Usuario user;
	    if(data.role().equals(UserRole.CLIENTE))
	    	user = new Cliente(data.nome(), data.email(), encryptedPassword, normalizarCpf(data.cpf()), data.telefone(),data.role());
	    else
	    	user = new Funcionario(data.nome(), data.email(), encryptedPassword, normalizarCpf(data.cpf()), data.telefone(),data.role());
	    
	    user.setUltimoLogin(LocalDateTime.now());
	    
	    return usuarioRepository.save(user);
	}
	
	public RegisterAdminResponseDTO registerFuncionario(Usuario usuario, Long id_quiosque, RegisterFuncionarioDTO data){
	    if(usuarioRepository.findByLogin(data.email()) != null)
	        throw new RuntimeException("Usuário já cadastrado");

	    Quiosque quiosque = findQuiosqueForUser(usuario, id_quiosque);
	    UserRole role = validarCargoCadastro(usuario.getRole(), data.role());
	    
	    String senha = UUID.randomUUID().toString().substring(0, 8);
	    
	    String encryptedPassword = passwordEncoder.encode(senha);
	    
	    // Gerar um login usando personalizado usando o nome e o quiosque 
	    
	    String login = gerarUsername(data.nome(), quiosque.getCodigo());
	    
	    login = procurarDuplicatas(login);
	    
	    Funcionario user = new Funcionario(data.nome(), login, encryptedPassword, normalizarCpf(data.cpf()), data.telefone(), role);
	    
	    user.setInicioTurno(data.startShift());
	    user.setFimTurno(data.finishShift());
	    user.setSenhaCifrada(senhaCriptoService.cifrar(senha));
	    user.setQuiosque(quiosque);
	    quiosque.getFuncionarios().add(user);
	    user = this.usuarioRepository.save(user);
	    this.quiosqueRepository.save(quiosque);
	    RegisterAdminResponseDTO response = new RegisterAdminResponseDTO(user.getPublicId(), quiosque.getNome(), senha, user.getNome(),user.getLogin(), user.getRole(), user.getTelefone());
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
		
		if(!usernames.contains(username)) 
			return username;
		
		int count = 2;
		while(usernames.contains(username + count))
			count++;
		return username + count;
	}
	
	private String gerarUsername(String nomeCompleto, String codigoQuiosque) {
		String[] partes = nomeCompleto.trim().split("\\s+");
		String nome = normalizar(partes[0]);
		if(partes.length == 1)
			return codigoQuiosque + nome;
		String sobrenome = normalizar(partes[partes.length - 1]);
		
		return codigoQuiosque + nome + sobrenome;
	}
	
	private String normalizar(String texto) {
		return Normalizer
				.normalize(texto, Normalizer.Form.NFD)
				.replaceAll("\\p{M}", "")
				.toLowerCase()
				.replaceAll("[^a-z0-9]", "");
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

		user.atualizarPerfil(data);
		usuarioRepository.save(user);
		UsuarioResponseDTO response = UsuarioResponseDTO.from(user);
	    return response;
	}
	
	@Transactional
	public void patchSenha(Usuario usuario, TrocarSenhaDTO data) {
		if (!passwordEncoder.matches(data.senhaAtual(), usuario.getSenhaHash())) {
	        throw new RuntimeException("Senha atual inválida");
	    }
		
		if (data.novaSenha() != null && !data.novaSenha().isBlank()) 
			usuario.setSenhaHash(passwordEncoder.encode(data.novaSenha()));		
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
