package app.auth;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
import app.DTO.TrocarSenhaDTO;
import app.DTO.UpdateMeDTO;
import app.DTO.UsuarioResponseDTO;
import app.auth.DTO.RegisterAdminResponseDTO;
import app.auth.DTO.RegisterDTO;
import app.entity.Quiosque;
import app.entity.Usuario;
import app.enums.StatusConta;
import app.enums.UserRole;
import app.repository.QuiosqueRepository;
import app.service.ImageService;
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
	private app.repository.PedidoRepository pedidoRepository;
	@Autowired
	private app.service.SenhaCriptoService senhaCriptoService;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		// Trocar aqui para Optional<User> findByEmailOrPhone(String email, String phone);
		return usuarioRepository.findByEmail(username);
	}
	
	public Usuario register(RegisterDTO data){

	    if(usuarioRepository.findByEmail(data.email()) != null)
	        throw new RuntimeException("Usuário já cadastrado");
	    if(!data.role().equals(UserRole.PROPRIETARIO) && !data.role().equals(UserRole.CLIENTE)) {
	    	throw new ResponseStatusException(
	                HttpStatus.UNAUTHORIZED, "Apenas gerentes podem cadastrar funcionarios");
	    }
	    String encryptedPassword = passwordEncoder.encode(data.password());
	    Usuario user = new Usuario(data.nome(), data.email(), encryptedPassword, normalizarCpf(data.cpf()), data.telefone(),data.role());
	    user.setUltimoLogin(LocalDateTime.now());
	    return usuarioRepository.save(user);
	}
	
	public RegisterAdminResponseDTO registerFuncionario(Usuario usuario, RegisterDTO data){
	    if(usuarioRepository.findByEmail(data.email()) != null)
	        throw new RuntimeException("Usuário já cadastrado");

	    Quiosque quiosque = findQuiosqueForUser(usuario);
	    
	    String senha = UUID.randomUUID().toString().substring(0, 8);
	    
	    String encryptedPassword = passwordEncoder.encode(senha);
	    
	    // Respeita o cargo pedido (FUNCIONARIO ou GERENTE). Antes era sempre
	    // FUNCIONARIO, então "gerentes" criados pelo app ficavam sem as
	    // permissões de gerente (editar quiosque, banner, funcionários...).
	    UserRole role = data.role() == UserRole.GERENTE
	    		? UserRole.GERENTE
	    		: UserRole.FUNCIONARIO;
	    Usuario user = new Usuario(data.nome(), data.email(), encryptedPassword, normalizarCpf(data.cpf()), data.telefone(), role);
	    user.setSenhaCifrada(senhaCriptoService.cifrar(senha));
	    user.setQuiosque(quiosque);
	    quiosque.getFuncionarios().add(user);
	    user = this.usuarioRepository.save(user);
	    this.quiosqueRepository.save(quiosque);
	    RegisterAdminResponseDTO response = new RegisterAdminResponseDTO(user.getPublicId(), quiosque.getNome(), senha, user.getNome(),user.getEmail(), user.getRole(), user.getTelefone());
	    return response;
	}

	// CPF é opcional (funcionário/gerente não o informam), mas a coluna
	// usuario.cpf é NOT NULL no banco. Normaliza ausência para string vazia,
	// mesma convenção já usada nos proprietários sem CPF, evitando o
	// "Column 'cpf' cannot be null" (não há unique em cpf, então "" repetido
	// não conflita).
	private static String normalizarCpf(String cpf) {
		return (cpf == null) ? "" : cpf;
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
	public void deleteMe(Usuario user) {
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
			quiosqueRepository.findByProprietario(user).ifPresent(q -> {
				q.setProprietario(null);
				q.setStatus(StatusConta.Desativada);
				quiosqueRepository.save(q);
			});
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
	
	private Quiosque findQuiosqueForUser(Usuario usuario) {
		Quiosque quiosque;
		if(usuario.getRole().equals(UserRole.PROPRIETARIO))
			quiosque = quiosqueRepository.findByProprietario(usuario)
	        .orElseThrow(() -> new ResponseStatusException(
	            HttpStatus.NOT_FOUND, "Quiosque não encontrado"));
		else
			quiosque = quiosqueRepository.findByFuncionariosContaining(usuario)
			.orElseThrow(() -> new ResponseStatusException(
		        HttpStatus.NOT_FOUND, "Quiosque não encontrado"));

	    return quiosque;
	}

}
