package app.service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import app.DTO.CategoriaCreateDTO;
import app.DTO.CategoriaViewDTO;
import app.entity.Categoria;
import app.entity.Quiosque;
import app.entity.Usuario;
import app.enums.Categorias;
import app.enums.UserRole;
import app.repository.CategoriaRepository;
import app.repository.QuiosqueRepository;
import jakarta.transaction.Transactional;

@Service
public class CategoriaService {
	
	@Autowired
	private CategoriaRepository categoriaRepository;
	@Autowired
	private QuiosqueRepository quiosqueRepository;
	
	
	public List<CategoriaViewDTO> findByQuiosque(Long id_quiosque){
		Quiosque quiosque = this.quiosqueRepository.findById(id_quiosque)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,("Quiosque não encontrado")));
		
		List<Categoria> categorias = categoriaRepository.findByQuiosqueOrderByOrdem(quiosque);

		// Só categorias ativas vão para o cardápio (soft delete).
		return categorias.stream()
				.filter(Categoria::isAtivo)
				.map(CategoriaViewDTO::from)
				.toList();
	}

	public List<Categorias> findDisponiveisByQuiosque(Long id_quiosque) {
		if (!quiosqueRepository.existsById(id_quiosque)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiosque não encontrado");
		}

		Set<Categorias> utilizadas = new HashSet<>(
				categoriaRepository.buscarNomesAtivosPorQuiosqueId(id_quiosque));

		return Arrays.stream(Categorias.values())
				.filter(categoria -> !utilizadas.contains(categoria))
				.toList();
	}
		
	@Transactional
	public CategoriaViewDTO save(Usuario usuario, Long id_quiosque, CategoriaCreateDTO data) {

		Quiosque quiosque = findQuiosqueForUser(usuario, id_quiosque);
		Categorias nome = Categorias.fromString(data.nome());
		if (categoriaRepository.existsByQuiosqueIdAndNomeAndAtivoTrue(id_quiosque, nome)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Categoria já utilizada");
		}
	    long ativas = quiosque.getCategorias().stream().filter(Categoria::isAtivo).count();
	    if(ativas >= 20) {
	    	throw new ResponseStatusException(
		            HttpStatus.BAD_REQUEST, "Quantidade de categorias limitada a 20");
	    }
	    Categoria categoria = new Categoria(nome, data.ordem());
	    
	    categoria.setQuiosque(quiosque);

	    return CategoriaViewDTO.from(categoriaRepository.save(categoria));
	}
	
	@Transactional
	public CategoriaViewDTO update(Usuario usuario, Long id_quiosque, Long id, CategoriaCreateDTO data) {
		
		Quiosque quiosque = findQuiosqueForUser(usuario, id_quiosque);		
		
		Categoria categoria = this.categoriaRepository.findByQuiosqueAndId(quiosque, id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,("Categoria não encontrada nesse quiosque")));

		Categorias nome = Categorias.fromString(data.nome());
		if (categoriaRepository.existsByQuiosqueIdAndNomeAndAtivoTrueAndIdNot(
				id_quiosque, nome, id)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Categoria já utilizada");
		}

		categoria.setNome(nome);
		categoria.setOrdem(data.ordem());
		
	    return CategoriaViewDTO.from(categoriaRepository.save(categoria));
	}
	
	@Transactional
	public void deleteById(Usuario usuario, Long id_quiosque, Long id){
		
		Quiosque quiosque = findQuiosqueForUser(usuario, id_quiosque);
		
		Categoria categoria = this.categoriaRepository.findByQuiosqueAndId(quiosque, id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,("Categoria não encontrada nesse quiosque")));

		// Soft delete: apagar fisicamente cascatearia para os itens (cascade
		// ALL), que podem estar em pedidos antigos. Marcamos inativa para sumir
		// do cardápio preservando o histórico.
		categoria.setAtivo(false);
		// Libera o slot da unique (ordem, id_quiosque): sem isso, uma nova seção
		// com a mesma ordem colidiria com a categoria inativa. MySQL permite
		// vários NULL numa unique, então nulos não conflitam.
		categoria.setOrdem(null);
		categoriaRepository.save(categoria);
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



//private Quiosque findQuiosqueForUser(Usuario usuario, Long id) {
//
//    Quiosque quiosque = quiosqueRepository.findById(id)
//        .orElseThrow(() -> new ResponseStatusException(
//            HttpStatus.NOT_FOUND, "Quiosque não encontrado"));
//
//
//    boolean hasAccess = false;
//
//    if (usuario.getRole().equals(UserRole.PROPRIETARIO)) {
//        hasAccess = quiosque.getProprietario() != null &&
//                    quiosque.getProprietario().getId().equals(usuario.getId());
//    } else {
//        hasAccess = quiosque.getFuncionarios() != null &&
//                    quiosque.getFuncionarios().stream()
//                        .anyMatch(admin -> admin.getId().equals(usuario.getId()));
//    }
//
//    if (!hasAccess) {
//        throw new ResponseStatusException(
//            HttpStatus.FORBIDDEN, "Usuário não tem acesso a esse quiosque");
//    }
//
//    return quiosque;
//}
