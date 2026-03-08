package eusyaeusya.gmg.domain.feedback.repository;

import eusyaeusya.gmg.domain.feedback.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
}
