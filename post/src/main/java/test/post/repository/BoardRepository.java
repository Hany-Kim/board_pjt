package test.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import test.post.entity.BoardEntity;

public interface BoardRepository extends JpaRepository<BoardEntity, Long> { // Jpa레포를 상속받고, 상속받을때 BoardEntity클래스 이름과 BoardEntity클래스가 가진 pk타입을 상속받음
    /*
        조회수를 증가한다는 것은
        DB기준 native query는 : update board_table set board_hits=board_hits+1 where id = ?
     */

    @Modifying // 업데이트나 삭제 쿼리를 실행할때는 필수로 붙여야함
    @Query(value = "update BoardEntity b set b.boardHits=b.boardHits+1 where b.id=:id") // entity기준으로 Query 작성
    //@Query(value = "update {Entity이름 || table 이름} {약(식언)어} set {약(식언)어}.{Entity에 정의한 컬럼(DB컬럼 x)}=b.boardHits+1 where b.{boardEntity에 있는 id}=:{계속 바뀌는 부분, 아래 @Param의 id와 매칭}") // entity기준으로 Query 작성
    // @Query(~~~, nativeQuery = true) => nativeQuery = true 옵션을 사용하면 실제 DB 쿼리문을 사용할 수 있다.
    void updateHits(@Param("id") Long id);
}
