package app.auth;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import app.DTO.FuncionarioResponseDTO;
import app.DTO.NotificationTokenDTO;
import app.DTO.TrocarSenhaDTO;
import app.DTO.UpdateMeDTO;
import app.DTO.UsuarioResponseDTO;
import app.auth.DTO.AuthenticationDTO;
import app.auth.DTO.LoginResponseDTO;
import app.auth.DTO.RegisterDTO;
import app.config.TokenService;
import app.controller.AcompanhamentoController;
import app.entity.Usuario;
import jakarta.validation.Valid;

@RestController
@RequestMapping("")
public class AuthController {

	@Autowired
	TokenService tokenService;
	@Autowired
	AuthenticationManager authenticationManager;
	@Autowired
	AuthService authService;
	@Autowired
	private AuthRepository usuarioRepository;	

	@PostMapping("/auth/login")
	public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid AuthenticationDTO data) {
		var userNamePassword = new UsernamePasswordAuthenticationToken(data.email(), data.password());
		var auth = this.authenticationManager.authenticate(userNamePassword);
		
		var token = tokenService.generateToken((Usuario) auth.getPrincipal());
		
		Usuario usuario = (Usuario) auth.getPrincipal();
		
		usuario.setUltimoLogin(LocalDateTime.now());
		// Colocar a requisição de trocar senha
		/*
		 * if ultimologin is null
		 * criar uma role pra isso????
		 * criar uma role que retorna trocarSenha no getAuthorities do Usuario e filtar no SecurityFilter
		 */
		usuarioRepository.save(usuario);
		return ResponseEntity.ok(new LoginResponseDTO(token));
	}
	
	// Sem @PreAuthorize: o cadastro é feito por usuários anônimos (novo
	// proprietário/cliente). O AuthService restringe os papéis permitidos.
	@PostMapping("/auth/register")
	public ResponseEntity<UsuarioResponseDTO> register(@RequestBody @Valid RegisterDTO data){		
		Usuario user = authService.register(data);
		return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioResponseDTO.from(user));
	}	
	
	@GetMapping("/me")
	@PreAuthorize("hasAnyRole('FUNCIONARIO','CLIENTE')")
	public ResponseEntity<UsuarioResponseDTO> me(@AuthenticationPrincipal Usuario user){
		return ResponseEntity.ok(UsuarioResponseDTO.from(user));
	}
	
	@PutMapping("/me")
	@PreAuthorize("hasAnyRole('FUNCIONARIO','CLIENTE')")
	public ResponseEntity<UsuarioResponseDTO> putUsuario(
			@AuthenticationPrincipal Usuario usuario,
			@RequestBody UpdateMeDTO data){
		UsuarioResponseDTO user = authService.putUsuario(usuario, data);
		return ResponseEntity.status(HttpStatus.OK).body(user);
	}
	
	@PatchMapping("/me")
	@PreAuthorize("authenticated()") // Aqui pra fazer aquela desgraça
	public ResponseEntity<Void> patchSenha(
			@AuthenticationPrincipal Usuario usuario,
			@RequestBody TrocarSenhaDTO senha){		
		authService.patchSenha(usuario, senha);
		return ResponseEntity.ok().build();
	}
	
	@DeleteMapping("/me")
	@PreAuthorize("hasAnyRole('FUNCIONARIO','CLIENTE')")
	public ResponseEntity<Void> deleteMe(@AuthenticationPrincipal Usuario user){
		authService.deleteMe(user);
		return ResponseEntity.noContent().build();
	}
	
	@PreAuthorize("hasAnyRole('FUNCIONARIO','CLIENTE')")
	@PostMapping("/me/notification-token")
	public ResponseEntity<Void> updateToken(
			@AuthenticationPrincipal Usuario usuario,
			@RequestBody NotificationTokenDTO data) {
		
		authService.saveToken(usuario, data);
		
		return ResponseEntity.ok().build();
	}
	
	@PostMapping("/me/imagem")
	@PreAuthorize("hasAnyRole('FUNCIONARIO','CLIENTE')")
	public ResponseEntity<String> updateImagem(
			@AuthenticationPrincipal Usuario usuario,
			@RequestPart("file") MultipartFile file){		
		
		String nomeArquivo = this.authService.uploadImagemUsuario(usuario, file);
		
		return ResponseEntity.ok(nomeArquivo);
	}
	
	@DeleteMapping("/me/imagem")
	@PreAuthorize("hasAnyRole('FUNCIONARIO','CLIENTE')")
	public ResponseEntity<Void> deleteImagem(
			@AuthenticationPrincipal Usuario usuario) {		
		
		this.authService.deleteImagemUsuario(usuario);
		
		return ResponseEntity.noContent().build();
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
//	@PreAuthorize("hasRole('PROPRIETARIO')")
//	@PostMapping("/register/admin")
//	public ResponseEntity<String> registerAdmin(@RequestBody @Valid RegisterDTO data){
//		
//		String encryptedPassword = passwordEncoder.encode(data.password());
//		Administrador newAdmin = new Administrador(data.nome(), data.login(),encryptedPassword,data.cpf(), data.telefone(), UserRole.ADMIN, data.dataNasc());	
//		
//		return authService.registerAdmin(data);
//	}
//	@PreAuthorize("hasRole('PROPRIETARIO')")
//	@PostMapping("/register/entregador")
//	public ResponseEntity<String> registerEntregador(@RequestBody @Valid RegisterDTO data){
//		
//		String encryptedPassword = passwordEncoder.encode(data.password());
//		Entregador newEntregador = new Entregador(data.nome(), data.login(),encryptedPassword,data.cpf(), data.telefone(), UserRole.ENTREGADOR, data.dataNasc());		
//		
//		
//		return authService.registerEntregador(data);
//	}
//	
//	@PostMapping("/register/proprietario")
//	public ResponseEntity<String> registerProprietario(@RequestBody @Valid RegisterDTO data){
//		
//		String encryptedPassword = passwordEncoder.encode(data.password());
//		Proprietario newProprietario = new Proprietario(data.nome(), data.login(),encryptedPassword,data.cpf(), data.telefone(), UserRole.PROPRIETARIO, data.dataNasc());		
//		
//		
//		return authService.registerProprietaio(data);
//	}
	
}