package kuke.board.comment.entity;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class CommentPathTest {
    @Test
    void createChildCommentTest(){
        // 00000 생성
        createChildCommentTest(CommentPath.create(""),null,"00000");

        // 00000
        //      00000
        createChildCommentTest(CommentPath.create("00000"),null,"0000000000");

        // 00000
        // 00001
        createChildCommentTest(CommentPath.create(""),"00000","00001");

        // 0000z
        //      abcdz
        //           zzzzz
        //                zzzzz
        //      abce0
        createChildCommentTest(CommentPath.create("0000z"),"0000zabcdzzzzzzzzzzz","0000zabce0");

    }

    void createChildCommentTest(CommentPath commentPath,String descendantTopPath,String expectedChildPath){
        CommentPath childCommentPath = commentPath.creatChildCommentPath(descendantTopPath);
        assertThat(childCommentPath.getPath()).isEqualTo(expectedChildPath);
    }

    @Test
    void createChildCommentPathMAxDepthTest(){
        assertThatThrownBy(()->
                CommentPath.create("zzzzz".repeat(5)).creatChildCommentPath(null)
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void createChildCommentPathIfOverflowTest(){
        CommentPath commentPath = CommentPath.create("");

        assertThatThrownBy(()->commentPath.creatChildCommentPath("zzzzz"))
                .isInstanceOf(IllegalStateException.class);
    }
}