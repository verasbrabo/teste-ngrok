package com.app.Projeto_Furlan;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import app.ProjetoFurlanApplication;

class ProjetoFurlanApplicationTests {

	@Test
	void deveManterClassePrincipalConfiguradaComoAplicacaoSpringBoot() {
		assertTrue(ProjetoFurlanApplication.class
				.isAnnotationPresent(SpringBootApplication.class));
	}

}
