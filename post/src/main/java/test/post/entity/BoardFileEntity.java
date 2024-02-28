package test.post.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "board_file_table")
public class BoardFileEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String originalFileName;

    @Column
    private String storedFileName;

    @ManyToOne(fetch = FetchType.LAZY) // N:1 관계, 부모가 호출되더라도 자식을 쓰고자 한다면 값을 불러오고, 아니라면 불러오지 않음
    //@ManyToOne(fetch = FetchType.EAGER) // N:1 관계, 요청을 하든 안하든 부모가 호출되면 자식도 호출되어 모든 값을 불러옴
    @JoinColumn(name = "board_id") // table에 만들어지는 column이름을 정한다.
    private BoardEntity boardEntity; // DB에 BIGINT타입이지만 Long이 아닌 부모entity타입으로 입력해야한다. 실제 DB에는 그냥 ID값만 들어가게된다.

    public static BoardFileEntity toBoardFileEntity(BoardEntity boardEntity, String originalFileName, String storedFileName) {
        BoardFileEntity boardFileEntity = new BoardFileEntity();
        boardFileEntity.setOriginalFileName(originalFileName);
        boardFileEntity.setStoredFileName(storedFileName);
        boardFileEntity.setBoardEntity(boardEntity);
        return boardFileEntity;
    }
}
