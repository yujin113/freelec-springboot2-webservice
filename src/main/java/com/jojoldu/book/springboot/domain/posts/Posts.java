package com.jojoldu.book.springboot.domain.posts;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@NoArgsConstructor // 롬복. 기본 생성자 자동 추가
@Entity // JPA 어노테이션. 테이블과 링크될 클래스
public class Posts extends BaseTimeEntity{

    @Id // 해당 테이블의 pk 필드
    @GeneratedValue(strategy = GenerationType.IDENTITY) // pk의 생성 규칙. GenerationType.IDENTITY 붙여야만 auto_increment 작동
    private Long id;

    @Column(length = 500, nullable = false) // 테이블의 칼럼. 굳이 선언하지 않아도 이 클래스의 필드는 모두 칼럼이 됨.
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    private String author;

    @Builder // 빌더 패턴 클래스. 생성자 대신 사용. 파라미터 순서에 더 유의하여 사용할 수 있음
    public Posts(String title, String content, String author) {
        this.title = title;
        this.content = content;
        this.author = author;
    }

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
