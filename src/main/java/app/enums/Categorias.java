package app.enums;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public enum Categorias {
	Lanches("Lanches"),
	Porções("Porções"),   
	Bebidas("Bebidas"),
	Outros("Outros"),
	Petiscos("Petiscos"),
	Frutos_do_mar("Frutos do mar"),
	Sobremesas("Sobremesas"),
	Açaí("Açaí"),
	Sucos("Sucos"),
	Coquetéis("Coquetéis"),
	Cervejas("Cervejas"),
	Grelhados("Grelhados"),
	Saladas("Saladas"),
	Caldos("Caldos"),
	Sorvetes("Sorvetes");
	
	private String categorias;
	
	Categorias(String categorias){
		this.categorias = categorias;
	}
	
	public String getCategorias() {
		return categorias;
	}	
	
	public static Categorias fromString(String texto) {
        for (Categorias categoria : Categorias.values()) {
            if (categoria.categorias.equalsIgnoreCase(texto)) {
                return categoria;
            }
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,("Categoria inválida: " + texto));
    }
}
