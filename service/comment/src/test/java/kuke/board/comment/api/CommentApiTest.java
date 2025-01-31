package kuke.board.comment.api;

import kuke.board.comment.service.response.CommentResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;


public class CommentApiTest {

    RestClient restClient = RestClient.create("http://localhost:9001");

    @Test
    void create(){
        CommentResponse response = createComment(new CommentCreateRequest(1L, "comment-1", null, 1L));
        CommentResponse response2 = createComment(new CommentCreateRequest(1L, "comment-2", response.getCommentId(), 1L));
        CommentResponse response3 = createComment(new CommentCreateRequest(1L, "comment-3", response.getCommentId(), 1L));

        System.out.println("commentId=%s".formatted(response.getCommentId()));
        System.out.println("\tcommentId=%s".formatted(response2.getParentCommentId()));
        System.out.println("\tcommentId=%s".formatted(response3.getParentCommentId()));
    }

    @Test
    void read(){
        CommentResponse response = restClient.get()
                .uri("/v1/comments/{commentId}", 143191383827804160L)
                .retrieve()
                .body(CommentResponse.class);
        System.out.println("response : " +response);
    }




    CommentResponse createComment(CommentCreateRequest request){
        return restClient.post()
                .uri("/v1/comments")
                .body(request)
                .retrieve()
                .body(CommentResponse.class);
    }

    @Getter
    @AllArgsConstructor
    public class CommentCreateRequest {
        private Long articleId;
        private String content;
        private Long parentCommentId;
        private Long writerId;
    }
}
