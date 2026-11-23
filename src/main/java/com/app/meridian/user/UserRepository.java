package com.app.meridian.user;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import com.app.meridian.user.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    boolean existsByIdentifierIgnoreCase(String value);
    boolean existsByIdentifierIgnoreCaseAndIdNot(String value, Long id);
    Optional<User> findByIdentifierIgnoreCase(String value);
    boolean existsByEmailIgnoreCase(String value);
    boolean existsByEmailIgnoreCaseAndIdNot(String value, Long id);
    Optional<User> findByEmailIgnoreCase(String value);

}
