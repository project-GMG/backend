package eusyaeusya.gmg.domain.feedback.entity;

import eusyaeusya.gmg.common.audit.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "feedbacks",
        indexes = {
                @Index(name = "idx_feedback_created_at", columnList = "created_at"),
                @Index(name = "idx_feedback_rating", columnList = "rating")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Feedback extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rating", nullable = false)
    private int rating;

    @Column(name = "comment", length = 500)
    private String comment;

    @Column(name = "page", length = 255)
    private String page;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Builder
    private Feedback(int rating, String comment, String page, String userAgent) {
        this.rating = rating;
        this.comment = comment;
        this.page = page;
        this.userAgent = userAgent;
    }

    public static Feedback create(int rating, String comment, String page, String userAgent) {
        return Feedback.builder()
                .rating(rating)
                .comment(comment)
                .page(page)
                .userAgent(userAgent)
                .build();
    }
}
