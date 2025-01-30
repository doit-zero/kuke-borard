package kuke.board.comment.service;

import kuke.board.comment.entity.Comment;
import kuke.board.comment.repository.CommentRepository;
import kuke.board.comment.service.request.CommentCreateRequest;
import kuke.board.comment.service.response.CommentResponse;
import kuke.board.common.snowflake.Snowflake;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static java.util.function.Predicate.not;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final Snowflake snowflake = new Snowflake();
    private final CommentRepository commentRepository;

    @Transactional
    public CommentResponse create(CommentCreateRequest request){
        Comment parent = findParent(request);
        Comment comment = commentRepository.save(
                Comment.create(
                        snowflake.nextId(),
                        request.getContent(),
                        parent == null ? null : parent.getCommentId(),
                        request.getArticleId(),
                        request.getWriterId()
                )
        );
        return CommentResponse.from(comment);
    }

    public CommentResponse read(Long commentId){
        return CommentResponse.from(commentRepository.findById(commentId).orElseThrow());
    }

    @Transactional
    public void delete(Long commentId){
        commentRepository.findById(commentId)
                .filter(not(Comment::getDeleted))
                .ifPresent(comment -> {
                    if(hasChildren(comment)){
                        comment.delete();
                    } else {
                        delete(comment);
                    }
                });
    }


    private Comment findParent(CommentCreateRequest request){
        Long parentCommentId = request.getParentCommentId();
        if(parentCommentId == null){
            return null;
        }

        return commentRepository.findById(parentCommentId)
                .filter(not(Comment::getDeleted))
                .filter(Comment::isRoot)
                .orElseThrow();
    }

    private boolean hasChildren(Comment comment) {
        return commentRepository.countBy(comment.getArticleId(),comment.getCommentId(),2L) == 2;
    }

    private void delete(Comment comment){
        commentRepository.delete(comment); // 현재 댓글 삭제
        if(!comment.isRoot()){ // 현재 댓글이 루트 댓글이 아닌 경우
            commentRepository.findById(comment.getParentCommentId()) // 부모 댓글 조회
                    .filter(Comment::getDeleted) // 부모 댓글이 삭제된 상태인지 확인
                    .filter(not(this::hasChildren)) // 부모 댓글이 자식 댓글이 없는지 확인
                    .ifPresent(this::delete); // 부모 댓글 삭제(재귀호출)
        }
    }
}
