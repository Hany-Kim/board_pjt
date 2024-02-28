package test.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import test.post.entity.BoardFileEntity;

public interface BoardFileRepository extends JpaRepository<BoardFileEntity, Long> {
}
