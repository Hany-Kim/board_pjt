package test.post.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import test.post.dto.BoardDTO;
import test.post.entity.BoardEntity;
import test.post.entity.BoardFileEntity;
import test.post.repository.BoardFileRepository;
import test.post.repository.BoardRepository;

import javax.swing.text.html.Option;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// DTO -> Entity (entity class에서 이루어질 작업)
// Entity -> DTO (DTO class에서 이루어질 작업)
@Service
@RequiredArgsConstructor // 생성자 주입방식으로 의존성 주입받음
public class BoardService {
    private final BoardRepository boardRepository; // 생성자 주입방식으로 의존성 주입받음
    private final BoardFileRepository boardFileRepository; // 생성자 주입방식으로 의존성 주입받음
    public void save(BoardDTO boardDTO) throws IOException {
        // DTO -> Entity로 옮겨 담음
        // 파일 첨부 여부에 따라 로직분리
        if(boardDTO.getBoardFile().isEmpty()){
            // 첨부 파일이 파일이 없음
            BoardEntity boardEntity = BoardEntity.toSaveEntity(boardDTO);
            boardRepository.save(boardEntity); // DB에 insert, save()는 entity클래스를 입력받고 반환한다
        } else{
            //첨부 파일 있음
            /*
                1. DTO에 담긴 파일 꺼냄
                2. 파일의 이름을 가져옴
                3. 서버 저장용 이름으로 이름을 만듬
                    사용자가 올린 파일 : 내사진.jpg => 서버에 저장할 이름 : 8397532432(난수)_내사진.jpg
                4. 저장 경로 설정
                5. 해당 경로에 파일 저장
                6. board_table에 해당 데이터 save 처리
                7. board_file_table에 해당 데이터 save 처리
             */
            // DB 저장------------------------------
            BoardEntity boardEntity = BoardEntity.toSaveFileEntity(boardDTO);
            Long savedId = boardRepository.save(boardEntity).getId(); // 자식 테이블에서는 부모의 pk값이 필요하다
            BoardEntity board = boardRepository.findById(savedId).get(); // 부모 Entity를 DB에서 가져옴
            // 부모 data가 먼저 저장된 후 부모 객체를 가져와야한다. 자식과 1:N 관계이기 때문
            
            // 파일 저장---------------------------
            for(MultipartFile boardFile: boardDTO.getBoardFile()) {
                //MultipartFile boardFile = boardDTO.getBoardFile(); // 1.
                String originalFilename = boardFile.getOriginalFilename(); // 2.
                String storedFileName = System.currentTimeMillis() + "_" + originalFilename; // 3. 시간을 밀리초로 바꾼 난수을 붙임
                String savePath = "C:/springboot_img/" + storedFileName; // 4.
                boardFile.transferTo(new File(savePath)); // 5.

                BoardFileEntity boardFileEntity = BoardFileEntity.toBoardFileEntity(board, originalFilename, storedFileName);
                boardFileRepository.save(boardFileEntity);
            }
        }
    }

    @Transactional
    public List<BoardDTO> findAll() {
        List<BoardEntity> boardEntityList = boardRepository.findAll(); // 리스트 형태의 entity가 넘어옴
        // Entity로 넘어온 객체를 DTO객체로 옮겨담아서 controller로 리턴해야한다.
        List<BoardDTO> boardDTOList = new ArrayList<>();
        for(BoardEntity boardEntity: boardEntityList){
            // Entity를 DTO에 하나씩 옮겨담음
            boardDTOList.add(BoardDTO.toBoardDTO(boardEntity));
        }
        return boardDTOList;
    }

    @Transactional // jpa가 제공하는 method가 아닌 별도 추가 method를 사용하는 경우
    // BoardRepository.interface에 상속받은 jpa외에 함수가 선언되어 있음
    public void updateHits(Long id) {
        boardRepository.updateHits(id);
    }

    @Transactional
    public BoardDTO findById(Long id) {
        Optional<BoardEntity> optionalBoardEntity = boardRepository.findById(id);
        if (optionalBoardEntity.isPresent()) {
            BoardEntity boardEntity = optionalBoardEntity.get();
            BoardDTO boardDTO = BoardDTO.toBoardDTO(boardEntity);
            return boardDTO;
        } else {
            return null;
        }
    }

    public BoardDTO update(BoardDTO boardDTO) {
        /*
            jpa에서 update를 위한 method가 없다.
            save method를 통해 update와 insert작업을 수행한다.
            해당 작업이 update인지 insert인지를 확인하는 방법은 id의 존재 유무이다.
            id값이 있다면 => update
            id값이 없다면 => insert
         */
        BoardEntity boardEntity = BoardEntity.toUpdateEntity(boardDTO);
        boardRepository.save(boardEntity);
        return findById(boardDTO.getId());
    }

    public void delete(Long id) {
        boardRepository.deleteById(id);
    }


    public Page<BoardDTO> paging(Pageable pageable) {
        int page = pageable.getPageNumber() - 1;
        int pageLimit = 3; // 한 페이지에 보여줄 글 갯수
        // 한 페이지에 3개씩 을을 보여주고 정렬 기준은 id 기준으로 내림차순 정렬
        // page 위치에 있는 값은 0부터 시작
        Page<BoardEntity> boardEntities =
                boardRepository.findAll(PageRequest.of(page, pageLimit, Sort.by(Sort.Direction.DESC,"id")));
        // PageRequest.of(보고 싶은 페이지, 한 페이지에 보여줄 글 갯수, 정렬 기준(내림차순, Entity에 작성한 id(DB컬럼이 아니다.)))

        System.out.println("boardEntities.getContent() = " + boardEntities.getContent()); // 요청 페이지에 해당하는 글
        System.out.println("boardEntities.getTotalElements() = " + boardEntities.getTotalElements()); // 전체 글갯수
        System.out.println("boardEntities.getNumber() = " + boardEntities.getNumber()); // DB로 요청한 페이지 번호
        System.out.println("boardEntities.getTotalPages() = " + boardEntities.getTotalPages()); // 전체 페이지 갯수
        System.out.println("boardEntities.getSize() = " + boardEntities.getSize()); // 한 페이지에 보여지는 글 갯수
        System.out.println("boardEntities.hasPrevious() = " + boardEntities.hasPrevious()); // 이전 페이지 존재 여부
        System.out.println("boardEntities.isFirst() = " + boardEntities.isFirst()); // 첫 페이지 여부
        System.out.println("boardEntities.isLast() = " + boardEntities.isLast()); // 마지막 페이지 여부

        // 목록 : id, writer, title, hit, createdTime => 관련 정보를 담을 BoardDTO객체를 생성해준다.
        Page<BoardDTO> boardDTOS = boardEntities.map(board -> new BoardDTO(board.getId(), board.getBoardWriter(), board.getBoardTitle(), board.getBoardHits(), board.getCreatedTime())); // boardEntities객체에서 board 매개변수에 담아서 하나씩 꺼내 BoardDTO에 옮긴다.
        return boardDTOS;
    }


}
