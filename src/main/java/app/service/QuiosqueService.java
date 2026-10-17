package app.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import app.DTO.AvaliacaoDTO;
import app.DTO.EmpresaOverviewDTO;
import app.DTO.FuncionarioOverviewDTO;
import app.DTO.PedidoOverviewDTO;
import app.DTO.QuiosqueCreateDTO;
import app.DTO.QuiosqueDTO;
import app.DTO.QuiosqueListaDTO;
import app.DTO.QuiosqueUpdateDTO;
import app.DTO.QuiosqueNearByResponseDTO;
import app.DTO.QuiosqueOverviewDTO;
import app.DTO.QuiosqueViewDTO;
import app.auth.AuthRepository;
import app.entity.AvaliacaoResumo;
import app.entity.Categoria;
import app.entity.Coordenada;
import app.entity.Endereco;
import app.entity.Funcionario;
import app.entity.Item;
import app.entity.ItemPedido;
import app.entity.Pedido;
import app.entity.Quiosque;
import app.entity.Usuario;
import app.enums.StatusConta;
import app.enums.UserRole;
import app.repository.AvaliacaoProjection;
import app.repository.CategoriaRepository;
import app.repository.ItemPedidoRepository;
import app.repository.ItemRepository;
import app.repository.PedidoRepository;
import app.repository.QuiosqueMetricasProjection;
import app.repository.QuiosqueRepository;
import jakarta.transaction.Transactional;

@Service
public class QuiosqueService {

	@Autowired
	private PedidoRepository pedidoRepository;
	@Autowired
    private ItemRepository itemRepository;
	@Autowired
	private QuiosqueRepository quiosqueRepository;
	@Autowired
	private CategoriaRepository categoriaRepository;
	@Autowired
	private ImageService imageService;
	@Autowired
	private AuthRepository funcionarioRepository;
	@Autowired
	ItemPedidoRepository itemPedidoRepository;
	
	@Transactional
	public QuiosqueCreateDTO save(
			Funcionario usuario,
			QuiosqueDTO data) {
		String cnpj = usuario.getCnpj();
		if (cnpj == null || cnpj.isBlank())
			throw new ResponseStatusException(
					HttpStatus.BAD_REQUEST, "Proprietário sem CNPJ cadastrado");

		if(quiosqueRepository.existsByEmail(data.email()))
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Dados inválidos");
		
		Quiosque q = new Quiosque(
				data.nome(),
				data.email(),
				data.openingTime(),
				data.closingTime(),
				data.distAtendimento(),
				new Endereco(data.cep(),data.uf(),data.cidade(), data.referencia(), data.rua(), data.numero()),
				new AvaliacaoResumo(0L,0L),
				new Coordenada(data.latitude(), data.longitude()),
				cnpj,
				usuario,
				StatusConta.Ativa,
				0L,
				0L
				);	
		if (data.diasAtivo() != null) {
			q.setDiasAtivo(data.diasAtivo());
		}
		//usuario.setQuiosque(q);
		
		this.quiosqueRepository.save(q);	
		//this.authRepository.save(usuario);
		QuiosqueCreateDTO responseData = QuiosqueCreateDTO.from(q);
		return responseData;
	}	
	
	@Transactional
	public QuiosqueCreateDTO update(Usuario usuario, Long id_quiosque, QuiosqueUpdateDTO data) {
		
		Quiosque q = findQuiosqueForUser(usuario, id_quiosque);

		// A escala semanal é uma decisão do titular. O gerente mantém acesso às
		// demais edições do quiosque, mas não pode alterar diasAtivo.
		if (data.diasAtivo() != null && !q.pertenceAoProprietario(usuario)) {
			throw new ResponseStatusException(
					HttpStatus.FORBIDDEN,
					"Apenas o proprietário pode alterar os dias de funcionamento");
		}
		
		q.atualizarDados(data);
				
		QuiosqueCreateDTO response = QuiosqueCreateDTO.from(q);
		return response;
		
	}
	
	@Transactional
	public QuiosqueCreateDTO updateStatus(Usuario usuario, Long id_quiosque) {
		
		Quiosque q = findQuiosqueForUser(usuario, id_quiosque);
		q.setStatus(
				q.getStatus() == StatusConta.Ativa ? StatusConta.Desativada : StatusConta.Ativa
				);
		
		QuiosqueCreateDTO response = QuiosqueCreateDTO.from(q);
		return response;
	}
	
	
	
	@Transactional
	public String uploadImagemQuiosque(
			Usuario administrador,
			Long id_quiosque,
			MultipartFile file) {
		
		Quiosque quiosque = findQuiosqueForUser(administrador, id_quiosque);
	
		return imageService.uploadImagemQuiosque(quiosque, file);		
	}
	
	
	
	@Transactional
	public void deleteImagemQuiosque(
			Usuario administrador,
			Long id_quiosque) {
		
		Quiosque quiosque = findQuiosqueForUser(administrador, id_quiosque);
		
		imageService.deleteImagemQuiosque(quiosque);
	}
	
	@Transactional
	public void deleteImagemQuiosque(
			Usuario administrador,
			Quiosque quiosque) {
		
		imageService.deleteImagemQuiosque(quiosque);
	}
	
		
	@Transactional
	public void delete(Usuario usuario, Long id_quiosque) {
		Quiosque q = findQuiosqueForUser(usuario, id_quiosque);
		
		if(q.getImagem() != null)
			deleteImagemQuiosque(usuario, q);
		
		// Colocar uma lógica para apagar as imagens do quisque no delete
		
		// Descobrir uma forma melhor de deletar os quiosques
		
		this.quiosqueRepository.delete(q);
	}
	
	
	
//	public QuiosqueCreateDTO get(Usuario usuario, Long id) {
//		return QuiosqueCreateDTO.from(findQuiosqueForUser(usuario, id));
//	}

	public List<QuiosqueListaDTO> findByProprietario(Funcionario usuario) {
		if (usuario.getRole() != UserRole.PROPRIETARIO) {
			throw new ResponseStatusException(
					HttpStatus.FORBIDDEN, "Apenas proprietários podem listar seus quiosques");
		}

		return quiosqueRepository.findByProprietario(usuario).stream()
				.sorted(Comparator.comparing(
						Quiosque::getNome,
						Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
				.map(QuiosqueListaDTO::from)
				.toList();
	}

	/**
	 * Carrega o quiosque vinculado ao funcionário dentro de uma transação.
	 *
	 * O usuário recebido do SecurityContext pode estar desconectado da sessão que
	 * o autenticou. Reconsultar o vínculo mantém categorias, itens e demais
	 * associações lazy disponíveis durante a montagem do DTO.
	 */
	@Transactional
	public QuiosqueViewDTO findByFuncionario(Funcionario usuario) {
		// Proprietários escolhem explicitamente um quiosque pela lista de vínculos.
		if (usuario.getRole() == UserRole.PROPRIETARIO) {
			throw new ResponseStatusException(
					HttpStatus.FORBIDDEN, "Selecione um quiosque do proprietário");
		}

		// A entidade retornada por esta consulta pertence à transação atual; assim
		// o QuiosqueViewDTO pode percorrer as coleções lazy com segurança.
		Quiosque quiosque = quiosqueRepository.findByFuncionariosContaining(usuario)
				.orElseThrow(() -> new ResponseStatusException(
						HttpStatus.NOT_FOUND, "Quiosque não encontrado"));

		return QuiosqueViewDTO.from(quiosque);
	}

	public QuiosqueViewDTO findById(Usuario usuario, Long idQuiosque) {
		return QuiosqueViewDTO.from(findQuiosqueForUser(usuario, idQuiosque));
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
	
	public EmpresaOverviewDTO overview(Funcionario usuario, Long id_quiosque) {
		Quiosque quiosque = findQuiosqueForUser(usuario, id_quiosque);
		
		Long quiosqueId = quiosque.getId();
		
		QuiosqueOverviewDTO quiosqueOverview = getQuiosqueOverview(quiosque, quiosqueId);
		
		List<FuncionarioOverviewDTO> funcionarioOverview = getFuncionarioOverview(quiosqueId);
		
		List<PedidoOverviewDTO> pedidoOverview = getPedidoOverview(quiosqueId);		
		
		return new EmpresaOverviewDTO(
				List.of(quiosqueOverview), 
				funcionarioOverview, 
				pedidoOverview);
	}
	
	private List<PedidoOverviewDTO> getPedidoOverview(Long quiosqueId) {
		List<Pedido> pedidos = pedidoRepository.findByQuiosqueIdOrderByDataHoraPedidoDesc(quiosqueId);
		
		List<UUID> pedidoIds = pedidos.stream()
		        .map(Pedido::getId)
		        .toList();
		
		List<ItemPedido> itens =
			    itemPedidoRepository.findByPedidoIdInWithDetails(pedidoIds);
		
		Map<UUID, List<ItemPedido>> itensPorPedido =
		        itens.stream()
		                .collect(Collectors.groupingBy(
		                        item -> item.getPedido().getId()
		                ));
		
		List<PedidoOverviewDTO> overview = pedidos.stream()
		        .map(p -> {

		            List<ItemPedido> itensDoPedido =
		                    itensPorPedido.getOrDefault(
		                            p.getId(),
		                            List.of()
		                    );

		            return PedidoOverviewDTO.from(
		                    p,
		                    p.getQuiosque(),
		                    itensDoPedido
		            );
		        })
		        .toList();
		
		return overview;
	}
	
	private List<FuncionarioOverviewDTO> getFuncionarioOverview(Long quiosqueId) {
		List<Funcionario> funcionarios = funcionarioRepository.findByQuiosqueId(quiosqueId);
		List<FuncionarioOverviewDTO> overview =
	                funcionarios.stream()
	                        .map(f ->   FuncionarioOverviewDTO.from(f, f.getQuiosque())).toList();
		return overview;
	}
	
	
	private QuiosqueOverviewDTO getQuiosqueOverview(Quiosque quiosque, Long quiosqueId) {

//      ZoneId zone = ZoneId.of("America/Sao_Paulo");
//      ZonedDateTime agora = ZonedDateTime.now(zone);

		LocalDateTime agora = LocalDateTime.now();
		
      LocalDate hoje = agora.toLocalDate();
      LocalTime horarioAtual = agora.toLocalTime();

      LocalDateTime inicioHoje =
              hoje.atStartOfDay();

      LocalDateTime fimHoje =
              agora;

      LocalDateTime inicioOntem =
              hoje.minusDays(1).atStartOfDay();

      LocalDateTime fimOntem =
              LocalDateTime.of(
                      hoje.minusDays(1),
                      horarioAtual
              );

      List<QuiosqueMetricasProjection> projections =
              pedidoRepository.buscarMetricasOverview(
            		  quiosqueId,
                      inicioHoje,
                      fimHoje,
                      inicioOntem,
                      fimOntem
              );

      Map<Long, QuiosqueMetricasProjection> metricasPorQuiosque =
              projections.stream()
                      .collect(Collectors.toMap(
                              QuiosqueMetricasProjection::getQuiosqueId,
                              Function.identity()
                      ));

      QuiosqueMetricasProjection metricas =
              metricasPorQuiosque.get(quiosqueId);
      
      QuiosqueOverviewDTO overview = QuiosqueOverviewDTO.from(
              quiosque,
              metricas);
             
      
      
      return overview;
  }
	
	
	
	
	public QuiosqueViewDTO findById(
			Long id) {		
		Quiosque q = this.quiosqueRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,("Quiosque não encontrado")));
		
		return QuiosqueViewDTO.from(q);
	}
	
	public List<AvaliacaoDTO> findAvaliacoesById(Long id_quiosque) {
		
		List<AvaliacaoProjection> avaliacoes = quiosqueRepository.buscarAvaliacoesDoRestaurante(id_quiosque);
		
		return avaliacoes.stream().map(AvaliacaoDTO::from).toList();
	}
	
//	public QuiosqueViewDTO findByFuncionario(
//			Usuario usuario) {		
//		Quiosque q = findQuiosqueForUser(usuario);
//		
//		return QuiosqueViewDTO.from(q);
//	}
	
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
	
//	private Quiosque findQuiosqueForUser(Usuario usuario) {
//		Quiosque quiosque;
//		if(usuario.getRole().equals(UserRole.PROPRIETARIO))
//			quiosque = quiosqueRepository.findByProprietario(usuario)
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
