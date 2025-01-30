package kuke.board.comment.service;

import kuke.board.comment.entity.Comment;
import kuke.board.comment.repository.CommentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {
    @InjectMocks
    CommentService commentService;

    @Mock
    CommentRepository commentRepository;

    @Test
    @DisplayName("삭제할 댓글이 자식 있으면 삭제 표시만")
    void deleteShouldMarkDeletedIfHasChildren(){
        Long articleId = 1L;
        Long commentId = 2L;
        Comment comment = createComment(articleId, commentId);
        given(commentRepository.findById(commentId))
                .willReturn(Optional.of(comment));
        given(commentRepository.countBy(articleId,commentId,2L)).willReturn(2L);

        commentService.delete(commentId);

        verify(comment).delete();
    }
    @Test
    @DisplayName("하위 댓글이 삭제되고,삭제되지 않은 부모면 하위 댓글만 삭제한다.")
    void deleteShouldDeleteChildOnlyIfNotDeleteParent(){
        Long articleId = 1L;
        Long commentId = 2L;
        Long parentCommentId = 1L;

        Comment comment = createComment(articleId, commentId,parentCommentId);
        given(comment.isRoot()).willReturn(false);

        // 부모 댓글은 삭제 되지 않음
        Comment parentComment = mock(Comment.class);
        given(parentComment.getDeleted()).willReturn(false);

        // comment를 반환
        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));
        // commentId의 자식은 1개임을 설정
        given(commentRepository.countBy(articleId,commentId,2L)).willReturn(1L);
        // parentComment를 반환
        given(commentRepository.findById(parentCommentId)).willReturn(Optional.of(parentComment));

        // comment 삭제 실행
        commentService.delete(commentId);

        verify(commentRepository).delete(comment);
        verify(commentRepository, never()).delete(parentComment);
    }

    @Test
    @DisplayName("하위 댓글이 삭제되고,삭제된 부모면 재귀적으로 모두 삭제한다.")
    void deleteShouldDeleteAll(){
        Long articleId = 1L;
        Long commentId = 2L;
        Long parentCommentId = 1L;

        Comment comment = createComment(articleId, commentId,parentCommentId);
        given(comment.isRoot()).willReturn(false);

        // 부모 댓글은 삭제된 상태
        Comment parentComment = createComment(articleId,parentCommentId);
        given(parentComment.isRoot()).willReturn(true);
        given(parentComment.getDeleted()).willReturn(true);

        // comment를 반환
        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));
        // commentId의 자식은 1개임을 설정
        given(commentRepository.countBy(articleId,commentId,2L)).willReturn(1L);
        // parentComment를 반환
        given(commentRepository.findById(parentCommentId)).willReturn(Optional.of(parentComment));
        given(commentRepository.countBy(articleId,parentCommentId,2L)).willReturn(1L);

        // comment 삭제 실행
        commentService.delete(commentId);

        verify(commentRepository).delete(comment);
        verify(commentRepository).delete(parentComment);
    }
    private Comment createComment(Long articleId,Long commentId){
        Comment comment = mock(Comment.class); // Mock 객체 생성
        given(comment.getArticleId()).willReturn(articleId); // getArticleId() 호출 시 특정 값 반환 설정
        given(comment.getCommentId()).willReturn(commentId); // getCommentId() 호출 시 특정 값 반환 설정
        return comment; // Mock Comment 객체 반환
    }
    private Comment createComment(Long articleId,Long commentId,Long parentCommentId){
        Comment comment = createComment(articleId,commentId);
        given(comment.getParentCommentId()).willReturn(parentCommentId);
        return comment;
    }
}