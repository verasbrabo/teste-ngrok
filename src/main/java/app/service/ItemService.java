package app.service;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import app.DTO.AcompanhamentoViewDTO;
import app.DTO.IngredienteDTO;
import app.DTO.ItemCreateRequest;
import app.DTO.ItemDTO;
import app.entity.Acompanhamento;
import app.entity.Categoria;
import app.entity.Ingrediente;
import app.entity.Item;
import app.entity.ItemIngrediente;
import app.entity.Quiosque;
import app.entity.Usuario;
import app.enums.UserRole;
import app.repository.AcompanhamentoRepository;
import app.repository.CategoriaRepository;
import app.repository.IngredienteRepository;
import app.repository.ItemRepository;
import app.repository.QuiosqueRepository;
import jakarta.transaction.Transactional;

@Service
public class ItemService {
	@Autowired
	private QuiosqueRepository quiosqueRepository;
	@Autowired
	private CategoriaRepository categoriaRepository;
	@Autowired
	private ItemRepository itemRepository;
	@Autowired
	private IngredienteRepository ingredienteRepository;
	@Autowired
	private AcompanhamentoRepository acompanhamentoRepository;
	@Autowired
	private ImageService imageService;
	
	public ItemDTO findById(Long id) {
		return ItemDTO.getFrom(this.itemRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Item não encontrado")));
	}	
	@Transactional
	public ItemDTO save(
			Usuario administrador,
			Long id_categoria,
			ItemCreateRequest data) {
		
	    Quiosque quiosque = findQuiosqueForUser(administrador);
	    
	    Categoria categoria = categoriaRepository.findByQuiosqueAndId(quiosque, id_categoria)
	    		.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Categoria não encontrada nesse quiosque"));
	    
	    if(categoria.getItens().size() >= 20) {
	    	throw new ResponseStatusException(
		            HttpStatus.BAD_REQUEST, "Quantidade de categorias limitada a 20 ");
	    }
	    
	    Item item = Item.from(data, categoria);
	    
	    List<Acompanhamento> acompanhamentos = this.acompanhamentoRepository.findByIdInAndQuiosque(data.acompanhamentoIds(), quiosque);
	    if(acompanhamentos.size() != data.acompanhamentoIds().size()) {
	    	throw new ResponseStatusException(
		            HttpStatus.BAD_REQUEST, "Algum acompanhamento não existe");
	    }
	    item.setAcompanhamentos(acompanhamentos);
	    
	    processarIngredientes(item, data.ingredientes());

	    categoria.addItem(item);
	    itemRepository.save(item); 
	    
	    return ItemDTO.from(item);
	}
	private void processarIngredientes(Item item, List<IngredienteDTO> dtos) {
	    // A coluna ingrediente.nome é UNIQUE com collation utf8mb4_0900_ai_ci, que
	    // ignora acento e caixa. A busca antiga (findByNomeInIgnoreCase -> UPPER)
	    // era sensível a acento, então um "limao" não encontrava o "limão" já
	    // existente e o INSERT batia na unique -> 500 ao editar/criar item.
	    // Resolvemos reutilizando a mesma semântica do banco (findFirstByNome,
	    // igualdade direta na coluna) e normalizando a chave para deduplicar
	    // ingredientes equivalentes dentro do próprio pedido.
	    Map<String, Ingrediente> resolvidos = new HashMap<>();
	    for (IngredienteDTO dto : dtos) {
	        String chave = normalizarIngrediente(dto.nome());
	        Ingrediente ing = resolvidos.get(chave);
	        if (ing == null) {
	            ing = ingredienteRepository.findFirstByNome(dto.nome())
	                    .orElseGet(() -> ingredienteRepository.save(new Ingrediente(dto.nome())));
	            resolvidos.put(chave, ing);
	        }
	        item.addIngrediente(ing);
	    }
	}

	/** Normaliza como a collation ai_ci do banco: sem acento e em minúsculas. */
	private static String normalizarIngrediente(String nome) {
	    return Normalizer.normalize(nome, Normalizer.Form.NFD)
	            .replaceAll("\\p{M}", "")
	            .toLowerCase();
	}
	
	
	@Transactional
	public ItemDTO update(
			Usuario administrador,
			Long id_categoria,
			Long id,
			ItemCreateRequest data) {
		
//		Quiosque quiosque = findQuiosqueForUser(administrador, id_quiosque);	
//		Categoria categoria = categoriaRepository.findByQuiosqueAndId(quiosque, id_categoria)
//	    		.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Categoria não encontrada nesse quiosque"));    
//		
//		Item item = categoria.getItem(id);
		
		Quiosque quiosque = findQuiosqueForUser(administrador);
		
		Item item = itemRepository.findByIdAndCategoriaIdAndCategoriaQuiosqueId(id, id_categoria, quiosque.getId())
	    		.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Item não encontrado nesse quiosque/categoria"));    
	    
	    item.atualizar(data);
	    if (ingredientesSaoDiferentes(item.getIngredientes(), data.ingredientes())) {
		    item.getIngredientes().clear();    
		    processarIngredientes(item, data.ingredientes());
	    }
	    if (acompanhamentosSaoDiferentes(item.getAcompanhamentos(), data.acompanhamentoIds())) {

	    	List<Acompanhamento> acompanhamentos = this.acompanhamentoRepository.findByIdInAndQuiosque(data.acompanhamentoIds(), quiosque);
		    if(acompanhamentos.size() != data.acompanhamentoIds().size()) {
		    	throw new ResponseStatusException(
			            HttpStatus.BAD_REQUEST, "Algum acompanhamento não existe");
		    }
	        item.setAcompanhamentos(acompanhamentos);
	    }

	    this.itemRepository.save(item);
	    
	    return ItemDTO.from(item);
	}
	
	
	@Transactional
	public String uploadImagemItem(
			Usuario administrador,
			Long id,
			MultipartFile file) {
		
		Item item = itemRepository.findByIdAndCategoriaQuiosqueId(id, findQuiosqueForUser(administrador).getId())
	    		.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Item não encontrado nesse quiosque/categoria"));    
	    
		return imageService.uploadImagemItem(item, file);	
	}
	
	
	@Transactional
	public void deleteImagemItem(
			Usuario administrador,
			Long id) {

		
		Item item = itemRepository.findByIdAndCategoriaQuiosqueId(id, findQuiosqueForUser(administrador).getId())
	    		.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Item não encontrado nesse quiosque/categoria"));    
	    
		
		imageService.deleteImagemItem(item);
	}
	
	private boolean ingredientesSaoDiferentes(
	        List<ItemIngrediente> atuais,
	        List<IngredienteDTO> novos) {

	    Set<Long> idsAtuais = atuais.stream()
	            .map(ii -> ii.getIngrediente().getId())
	            .collect(Collectors.toSet());

	    Set<Long> idsNovos = novos.stream()
	            .map(IngredienteDTO::id)
	            .collect(Collectors.toSet());

	    return !idsAtuais.equals(idsNovos);
	}
	
	private boolean acompanhamentosSaoDiferentes(
	        List<Acompanhamento> atuais,
	        List<Long> novosIds) {

	    Set<Long> idsAtuais = atuais.stream()
	            .map(Acompanhamento::getId)
	            .collect(Collectors.toSet());

	    Set<Long> idsNovos = new HashSet<>(novosIds);

	    return !idsAtuais.equals(idsNovos);
	}
	
	@Transactional
	public void deleteById(
			Usuario administrador,
			Long id_categoria,
			Long id){
		
		
//		Quiosque quiosque = findQuiosqueForUser(administrador, id_quiosque);
//		
//		Categoria categoria = categoriaRepository.findByQuiosqueAndId(quiosque, id_categoria)
//	    		.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Categoria não encontrada nesse quiosque"));    
//		
//		categoria.removeItem(categoria.getItem(id));
		
		findQuiosqueForUser(administrador);
		
		Item item = itemRepository.findByIdAndCategoriaIdAndCategoriaQuiosqueId(id, id_categoria, findQuiosqueForUser(administrador).getId())
	    		.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Item não encontrado nesse quiosque"));

		// Soft delete: o item pode estar referenciado por pedidos antigos
		// (item_pedido), então não pode ser apagado fisicamente. Marcamos
		// inativo para sumir do cardápio sem perder o histórico.
		item.setAtivo(false);
		// Libera o slot da unique (ordem, id_categoria) para não bloquear a
		// criação de um novo item com a mesma ordem. NULL não conflita.
		item.setOrdem(null);
		this.itemRepository.save(item);
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
