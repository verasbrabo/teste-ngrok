package app.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import app.auth.AuthRepository;
import app.entity.Imagem;
import app.entity.Item;
import app.entity.Quiosque;
import app.entity.Usuario;
import app.repository.ImageRepository;
import app.repository.ItemRepository;
import app.repository.QuiosqueRepository;
import jakarta.transaction.Transactional;

@Service
public class ImageService {
	
	@Autowired
	private AuthRepository authRepository;
	@Autowired
	private QuiosqueRepository quiosqueRepository;
	@Autowired
	private ItemRepository itemRepository;
	@Autowired
	private ImageRepository imageRepository;
	
	@Transactional
	public String uploadImagemUsuario(Usuario usuario, MultipartFile file) {
		if(usuario.getImagem() != null) {
			deletarArquivo(usuario.getImagem().getNomeArquivo());
			
			imageRepository.delete(usuario.getImagem());
		}
		
		String nome = salvarArquivo(file);
		
		Imagem imagem = new Imagem();
		
		imagem.setNomeArquivo(nome);
		
		imagem.setUrl("/uploads/" + nome);
		
		imageRepository.save(imagem);
		
		usuario.setImagem(imagem);
		
		authRepository.save(usuario);
		
		return imagem.getNomeArquivo();
	}
	
	@Transactional
	public void deleteImagemUsuario(Usuario usuario) {
		deletarArquivo(usuario.getImagem().getNomeArquivo());
	}
	
	@Transactional
	public String uploadImagemQuiosque(Quiosque quiosque, MultipartFile file) {
		if(quiosque.getImagem() != null) {
			deletarArquivo(quiosque.getImagem().getNomeArquivo());
			
			imageRepository.delete(quiosque.getImagem());
		}
		
		String nome = salvarArquivo(file);
		
		Imagem imagem = new Imagem();
		
		imagem.setNomeArquivo(nome);
	
		imagem.setUrl("/uploads/" + nome);
		
		imageRepository.save(imagem);
		
		quiosque.setImagem(imagem);
		
		quiosqueRepository.save(quiosque);
		
		return imagem.getNomeArquivo();
	}
	
	@Transactional
	public void deleteImagemQuiosque(Quiosque quiosque) {
		deletarArquivo(quiosque.getImagem().getNomeArquivo());
	}
	
	@Transactional
	public String uploadImagemItem(Item item, MultipartFile file) {
		if(item.getImagem() != null) {
			deletarArquivo(item.getImagem().getNomeArquivo());
			
			imageRepository.delete(item.getImagem());
		}
		
		String nome = salvarArquivo(file);
		
		Imagem imagem = new Imagem();
		
		imagem.setNomeArquivo(nome);

		imagem.setUrl("/uploads/" + nome);
		
		imageRepository.save(imagem);
		
		item.setImagem(imagem);
		
		itemRepository.save(item);
		
		return imagem.getNomeArquivo();
	}
	
	@Transactional
	public void deleteImagemItem(Item item) {
		deletarArquivo(item.getImagem().getNomeArquivo());
	}
	
	private String salvarArquivo(MultipartFile file) {
		if(file.isEmpty())
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Arquivo vazio");
		if(!ehImagem(file))
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Arquivo não é uma imagem");
		if(file.getSize() > 5 * 1024 * 1024)
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Arquivo muito grande");
		try {
			String nome = UUID.randomUUID() + "_" +
					      file.getOriginalFilename();
				Path path = Paths.get("uploads/" + nome);
				Files.createDirectories(path.getParent());
				Files.write(path, file.getBytes());
				
				return nome;
 		}	catch (IOException e) {
 			
 			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Erro ao salvar arquivo"); 
 		}
		
		
	}
	
	
	/**
	 * Aceita pelo content-type (`image/*`) ou, quando o cliente não o informa
	 * (o app Flutter envia `application/octet-stream`), pela extensão do nome
	 * do arquivo.
	 */
	private boolean ehImagem(MultipartFile file) {
		String tipo = file.getContentType();
		if (tipo != null && tipo.startsWith("image/"))
			return true;

		String nome = file.getOriginalFilename();
		if (nome == null)
			return false;
		String n = nome.toLowerCase();
		return n.endsWith(".jpg") || n.endsWith(".jpeg") || n.endsWith(".png")
				|| n.endsWith(".webp") || n.endsWith(".gif") || n.endsWith(".bmp")
				|| n.endsWith(".heic");
	}

	private void deletarArquivo(String nome) {
		try {
			Path path = Paths.get("uploads/" + nome);
			
			Files.deleteIfExists(path);
			
		}	catch (IOException e) {
 			
 			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Erro ao deletar arquivo"); 
 		}
	}
	
}
