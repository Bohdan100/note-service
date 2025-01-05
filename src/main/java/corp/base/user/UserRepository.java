package corp.base.user;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import corp.base.auth.User;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Transactional(readOnly = true)
    @Query(value = "SELECT * FROM `user` WHERE email = :email", nativeQuery = true)
    User findUserByEmail(String email);

    @Transactional(readOnly = true)
    @Query(value = "SELECT name FROM user WHERE email = :email", nativeQuery = true)
    String findNameByEmail(@Param("email") String email);
}
