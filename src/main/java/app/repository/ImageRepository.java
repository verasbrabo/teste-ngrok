package app.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import app.entity.Imagem;

public interface ImageRepository extends JpaRepository<Imagem, Long> {

}
