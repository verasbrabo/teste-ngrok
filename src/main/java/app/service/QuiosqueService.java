package app.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import app.DTO.QuiosqueCreateDTO;
import app.DTO.QuiosqueDTO;
import app.DTO.QuiosqueUpdateDTO;
import app.DTO.QuiosqueNearByResponseDTO;
import app.DTO.QuiosqueViewDTO;
import app.DTO.UsuarioResponseDTO;
import app.auth.AuthRepository;
import app.config.NotificationService;
import app.entity.AvaliacaoResumo;
import app.entity.Categoria;
import app.entity.Coordenada;
import app.entity.Endereco;
import app.entity.Item;
import app.entity.Quiosque;
import app.entity.Usuario;
import app.enums.StatusConta;
import app.enums.UserRole;
import app.repository.CategoriaRepository;
import app.repository.ItemRepository;
import app.repository.QuiosqueRepository;
import jakarta.transaction.Transactional;

@Service
public class QuiosqueService {

	@Autowired
    private ItemRepository itemRepository;
	@Autowired
	private QuiosqueRepository quiosqueRepository;
	@Autowired
	private CategoriaRepository categoriaRepository;
	@Autowired
	private AuthRepository authRepository;
	@Autowired
	private ImageService imageService;
	
	public QuiosqueCreateDTO save(
			Usuario usuario,
			QuiosqueDTO data) {
		
		
		Quiosque q = new Quiosque(
				data.nome(),
				data.email(),
				data.cnpj(),
				data.openingTime(),
				data.closingTime(),
				data.distAtendimento(),
				new Endereco(data.cep(),data.uf(),data.cidade()),
				new AvaliacaoResumo(0L,0L),
				new Coordenada(data.latitude(), data.longitude()),
				usuario,
				StatusConta.Ativa,
				0L,
				0L
				);	
		//usuario.setQuiosque(q);
		
		this.quiosqueRepository.save(q);	
		//this.authRepository.save(usuario);
		QuiosqueCreateDTO responseData = QuiosqueCreateDTO.from(q);
		return responseData;
	}	
	
	@Transactional
	public QuiosqueCreateDTO update(Usuario usuario, QuiosqueUpdateDTO data) {
		
		Quiosque q = findQuiosqueForUser(usuario);
		
		q.atualizarDados(data);
				
		QuiosqueCreateDTO response = QuiosqueCreateDTO.from(q);
		return response;
		
	}
	
	
	
	@Transactional
	public QuiosqueCreateDTO updateStatus(Usuario usuario) {
		
		Quiosque q = findQuiosqueForUser(usuario);
		q.setStatus(
				q.getStatus() == StatusConta.Ativa ? StatusConta.Desativada : StatusConta.Ativa
				);
		
		QuiosqueCreateDTO response = QuiosqueCreateDTO.from(q);
		return response;
	}
	
	
	
	@Transactional
	public String uploadImagemQuiosque(
			Usuario administrador,
			MultipartFile file) {
		
		Quiosque quiosque = findQuiosqueForUser(administrador);
	
		return imageService.uploadImagemQuiosque(quiosque, file);		
	}
	
	
	
	@Transactional
	public void deleteImagemQuiosque(
			Usuario administrador) {
		
		Quiosque quiosque = findQuiosqueForUser(administrador);
		
		imageService.deleteImagemQuiosque(quiosque);
	}
	
	
	
	@Transactional
	public void delete(Usuario usuario) {
		Quiosque q = findQuiosqueForUser(usuario);
		
		if(q.getImagem() != null)
			deleteImagemQuiosque(usuario);
		
		// Colocar uma lógica para apagar as imagens do quisque no delete
		
		this.quiosqueRepository.delete(q);
	}
	
	
	
	public QuiosqueCreateDTO get(Usuario usuario, Long id) {
		return QuiosqueCreateDTO.from(findQuiosqueForUser(usuario, id));
	}

	public List<QuiosqueNearByResponseDTO> findByDistancia(
	        double latUsuario,
	        double lonUsuario,
	        double raioM) {

	    record QuiosqueDistancia(Quiosque quiosque, long metros) {}

	    final double R = 6371000.0; // metros

	    double latDiff = Math.toDegrees(raioM / R);
	    double lonDiff = Math.toDegrees(raioM / (R * Math.cos(Math.toRadians(latUsuario))));

	    double minLat = latUsuario - latDiff;
	    double maxLat = latUsuario + latDiff;
	    double minLon = lonUsuario - lonDiff;
	    double maxLon = lonUsuario + lonDiff;

	    List<Quiosque> lista = this.quiosqueRepository
	            .findByDistanciaAtivo(latUsuario, lonUsuario, raioM,
	                    minLat, maxLat, minLon, maxLon);

	    Coordenada usuario = new Coordenada(latUsuario, lonUsuario);

	    return lista.stream()
	            .map(q -> new QuiosqueDistancia(
	                    q,
	                    q.getCoordenada().distanciaAte(usuario)
	            ))
	            .filter(kd -> kd.metros() <= raioM)
	            .sorted(Comparator.comparingLong(QuiosqueDistancia::metros))
	            .map(kd -> {
	                kd.quiosque().setDistancia(kd.metros());
	                return QuiosqueNearByResponseDTO.from(kd.quiosque());
	            })
	            .toList();
	}
	
	public QuiosqueViewDTO findById(
			long id) {		
		Quiosque q = this.quiosqueRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,("Quiosque não encontrado")));
		
//		q.setNota(q.getAvaliacaoResumo().getMedia());
		
		return QuiosqueViewDTO.from(q);
	}
	
	public QuiosqueViewDTO findByFuncionario(
			Usuario usuario) {		
		Quiosque q = findQuiosqueForUser(usuario);
		
//		q.setNota(q.getAvaliacaoResumo().getMedia());
		
		return QuiosqueViewDTO.from(q);
	}
	
	// não acho que va precisar
	public List<Item> findItensByQuiosque(
			long id){
		
		
		Quiosque q = this.quiosqueRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,("Quiosque não encontrado")));
		return this.itemRepository.findByCategoriaQuiosque(q);
	}
	
	
	// não acho que va precisar
	public List<Categoria> findCategoriasByQuiosque(
			long id){
		
		
		Quiosque q = this.quiosqueRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,("Quiosque não encontrado")));
		return this.categoriaRepository.findByQuiosque(q);
	}	
	
	public List<Quiosque> findAll(){
		return this.quiosqueRepository.findAll();
	}
	

	public long calcularDistancia(
	        double lat1, double lon1,
	        double lat2, double lon2) {

	    double dLat = Math.abs(lat1 - lat2);
	    double dLon = Math.abs(lon1 - lon2);

	    // threshold ~11km (0.1 grau)
	    if (dLat < 0.1 && dLon < 0.1) {
	        return distanciaEquiretangular(lat1, lon1, lat2, lon2);
	    }

	    return distanciaHaversine(lat1, lon1, lat2, lon2);
	}
	
	public long distanciaEquiretangular(
	        double lat1, double lon1,
	        double lat2, double lon2) {

	    final double R = 6371000; // metros

	    double x = Math.toRadians(lon2 - lon1) *
	               Math.cos(Math.toRadians((lat1 + lat2) / 2));
	    double y = Math.toRadians(lat2 - lat1);

	    return Math.round(Math.sqrt(x * x + y * y) * R);
	}
	
	
	public long distanciaHaversine(
	        double lat1, double lon1,
	        double lat2, double lon2) {

	    final double R = 6371000; // metros

	    double dLat = Math.toRadians(lat2 - lat1);
	    double dLon = Math.toRadians(lon2 - lon1);

	    double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
	            Math.cos(Math.toRadians(lat1)) *
	            Math.cos(Math.toRadians(lat2)) *
	            Math.sin(dLon/2) * Math.sin(dLon/2);

	    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

	    return Math.round(R * c);
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
