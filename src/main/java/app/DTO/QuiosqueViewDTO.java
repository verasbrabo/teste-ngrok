package app.DTO;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

import app.entity.Imagem;
import app.entity.Quiosque;

public record QuiosqueViewDTO(
		String nome,
		BigDecimal nota,
		Long qtdAvaliacoes,
		Long distancia,
		Long tempoEstimado,
		String openingTime,
		String closingTime,
		List<CategoriaViewDTO> categorias,
		String imagem,
		// Endereço/coordenadas, para permitir reexibir/editar a localização.
		String cep,
		String uf,
		String cidade,
		Double latitude,
		Double longitude) {

	public static QuiosqueViewDTO from(Quiosque q) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

		String openTime = q.getOpeningTime().format(formatter);
		String closeTime = q.getClosingTime().format(formatter);
		var endereco = q.getEndereco();
		var coordenada = q.getCoordenada();
		return new QuiosqueViewDTO(
				q.getNome(),
				q.getAvaliacaoResumo() != null ? q.getAvaliacaoResumo().getMedia() : null,
				q.getAvaliacaoResumo() != null
					? q.getAvaliacaoResumo().getTotalAvaliacoes()
					: 0L,
				q.getDistancia(),
				q.calcularTempoEstimado(),
				openTime,
				closeTime,
				q.getCategorias() != null
			    	? q.getCategorias().stream()
			    		.filter(app.entity.Categoria::isAtivo)
			    		.map(CategoriaViewDTO::from).toList()
			    			: List.of(),
				q.getImagem() != null ? q.getImagem().getUrl() : null,
				endereco != null ? endereco.getCep() : null,
				endereco != null ? endereco.getUf() : null,
				endereco != null ? endereco.getCidade() : null,
				coordenada != null ? coordenada.getLatitude() : null,
				coordenada != null ? coordenada.getLongitude() : null
				);
	}

}
