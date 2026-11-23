package com.app.meridian.term;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import com.app.meridian.term.enums.TermName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TermRepository extends JpaRepository<Term, Long>, JpaSpecificationExecutor<Term> {

    boolean existsBySessionIdAndName(Long sessionId, TermName name);
    Optional<Term> findByIsActiveTrue();
    List<Term> findBySessionId(Long sessionId);

}
