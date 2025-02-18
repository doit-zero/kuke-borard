package kuke.board.comment.api;

import kuke.board.comment.service.response.CommentPageResponse;
import kuke.board.comment.service.response.CommentResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import java.util.List;


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

    @Test
    void reaAll(){
        CommentPageResponse response = restClient.get()
                .uri("/v1/comments?articleId=1&page=1&pageSize=10")
                .retrieve()
                .body(CommentPageResponse.class);
        System.out.println("response.getCommentCount() = " + response.getCommentCount());
        for(CommentResponse comment : response.getComments()){
            if(!comment.getCommentId().equals(comment.getParentCommentId())){
                System.out.print("\t");
            }
            System.out.println("comment.getCommentId() = " + comment.getCommentId());
        }
    }

    /**
     * 1번 페이지 수행 결과
     * response.getCommentCount() = 101
     * comment.getCommentId() = 143191383827804160
     *
     * comment.getCommentId() = 143191384402423808
     *
     * comment.getCommentId() = 143191384461144064
     * comment.getCommentId() = 143696073499557888
     *
     * comment.getCommentId() = 143696073533112325
     * comment.getCommentId() = 143696073499557889
     *
     * comment.getCommentId() = 143696073533112321
     * comment.getCommentId() = 143696073499557890
     *
     * comment.getCommentId() = 143696073533112324
     * comment.getCommentId() = 143696073499557891
     * */

    @Test
    void readAllInfiniteScroll(){
        List<CommentResponse> response1 = restClient.get()
                .uri("/v1/comments/infinite-scroll?articleId=1&pageSize=5")
                .retrieve()
                .body(new ParameterizedTypeReference<List<CommentResponse>>() {
                });
        System.out.println("firstPage");
        for(CommentResponse comment : response1){
            if(!comment.getCommentId().equals(comment.getParentCommentId())){
                System.out.print("\t");
            }
            System.out.println("comment.getCommentId() = "+ comment.getCommentId());
        }

        Long lastParentCommentId = response1.getLast().getParentCommentId();
        Long lastCommentId = response1.getLast().getCommentId();

        List<CommentResponse> response2 = restClient.get()
                .uri("/v1/comments/infinite-scroll?articleId=1&pageSize=5&lastParentCommentId=%s&lastCommentId=%s".formatted(lastParentCommentId,lastCommentId))
                .retrieve()
                .body(new ParameterizedTypeReference<List<CommentResponse>>() {
                });
        System.out.println("SecondPage");
        for(CommentResponse comment : response2){
            if(!comment.getCommentId().equals(comment.getParentCommentId())){
                System.out.print("\t");
            }
            System.out.println("comment.getCommentId() = " + comment.getCommentId());
        }

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
