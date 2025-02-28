package kuke.board.comment.api;

import kuke.board.comment.service.response.CommentPageResponse;
import kuke.board.comment.service.response.CommentResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import java.util.List;

public class CommentApiV2Test {
    RestClient restClient = RestClient.create("http://localhost:9001");

    @Test
    void create(){
        CommentResponse response1 = create(new CommentCreateRequestV2(1L, "my comment1", null, 1L));
        CommentResponse response2 = create(new CommentCreateRequestV2(1L, "my comment2", response1.getPath(), 1L));
        CommentResponse response3 = create(new CommentCreateRequestV2(1L, "my comment3", response2.getPath(), 1L));

        System.out.println("response1.getCommentId() : " + response1.getPath());
        System.out.println("\tresponse2.getCommentId() : " + response2.getPath());
        System.out.println("\t\tresponse3.getCommentId() : " + response3.getPath());
        /*
        * response1.getCommentId() : 00001
	        response2.getCommentId() : 0000100000
		            response3.getCommentId() : 000010000000000
        * */
    }



    CommentResponse create(CommentCreateRequestV2 request){
        return restClient.post()
                .uri("/v2/comments")
                .body(request)
                .retrieve()
                .body(CommentResponse.class);
    }

    @Test
    void readAll(){
        CommentPageResponse response = restClient.get()
                .uri("/v2/comments?articleId=1&pageSize=10&page=1")
                .retrieve()
                .body(CommentPageResponse.class);

        System.out.println("response.getCommentCount() : " + response.getCommentCount());
        for(CommentResponse comment : response.getComments()){
            System.out.println("comment.getCommentId() : " + comment.getCommentId());
        }

        /**
         * comment.getCommentId() : 150223841721131008
         * comment.getCommentId() : 150223842501271552
         * comment.getCommentId() : 150223842576769024
         * comment.getCommentId() : 150224027155505152
         * comment.getCommentId() : 150224027579129856
         * comment.getCommentId() : 150224027663015936
         * comment.getCommentId() : 150226315386765313
         * comment.getCommentId() : 150226315441291266
         * comment.getCommentId() : 150226315441291276
         * comment.getCommentId() : 150226315445485575
         * */
    }

    @Test
    void readAllInfiniteScroll(){
        List<CommentResponse> response1 = restClient.get()
                .uri("/v2/comments/infinite-scroll?articleId=1&pageSize=5")
                .retrieve()
                .body(new ParameterizedTypeReference<List<CommentResponse>>() {
                });
        System.out.println("firstPage");
        for(CommentResponse response : response1){
            System.out.println("response.getCommentId() : " + response.getCommentId());
        }

        String lastPath = response1.getLast().getPath();

        List<CommentResponse> response2 = restClient.get()
                .uri("/v2/comments/infinite-scroll?articleId=1&pageSize=5&lastPath=%s".formatted(lastPath))
                .retrieve()
                .body(new ParameterizedTypeReference<List<CommentResponse>>() {
                });
        System.out.println("secondPage");
        for(CommentResponse response : response2){
            System.out.println("response.getCommentId() : " + response.getCommentId());
        }

        /**
         * firstPage
         * response.getCommentId() : 150223841721131008
         * response.getCommentId() : 150223842501271552
         * response.getCommentId() : 150223842576769024
         * response.getCommentId() : 150224027155505152
         * response.getCommentId() : 150224027579129856
         * secondPage
         * response.getCommentId() : 150224027663015936
         * response.getCommentId() : 150226315386765313
         * response.getCommentId() : 150226315441291266
         * response.getCommentId() : 150226315441291276
         * response.getCommentId() : 150226315445485575
         * */
    }
    @Getter
    @AllArgsConstructor
    public class CommentCreateRequestV2 {
        private Long articleId;
        private String content;
        private String parentPath;
        private Long writerId;
    }
}
