package app.service;

import java.util.List;

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
		
	@Transactional
	public CategoriaViewDTO save(Usuario usuario, CategoriaCreateDTO data) {

	    Quiosque quiosque = findQuiosqueForUser(usuario);
	    long ativas = quiosque.getCategorias().stream().filter(Categoria::isAtivo).count();
	    if(ativas >= 20) {
	    	throw new ResponseStatusException(
		            HttpStatus.BAD_REQUEST, "Quantidade de categorias limitada a 20");
	    }
	    Categoria categoria = new Categoria(Categorias.fromString(data.nome()), data.ordem());
	    
	    categoria.setQuiosque(quiosque);

	    return CategoriaViewDTO.from(categoriaRepository.save(categoria));
	}
	
	@Transactional
	public CategoriaViewDTO update(Usuario usuario, Long id, CategoriaCreateDTO data) {
		
		Quiosque quiosque = findQuiosqueForUser(usuario);		
		
		Categoria categoria = this.categoriaRepository.findByQuiosqueAndId(quiosque, id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,("Categoria não encontrada nesse quiosque")));
		
		categoria.setNome(Categorias.fromString(data.nome()));
		categoria.setOrdem(data.ordem());
		
	    return CategoriaViewDTO.from(categoriaRepository.save(categoria));
	}
	
	@Transactional
	public void deleteById(Usuario usuario, Long id){
		
		Quiosque quiosque = findQuiosqueForUser(usuario);
		
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
