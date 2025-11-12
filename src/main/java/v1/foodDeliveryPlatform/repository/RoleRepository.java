package v1.foodDeliveryPlatform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import v1.foodDeliveryPlatform.model.Role;

import java.util.Set;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    @Query(value = "SELECT * FROM role WHERE name = :name", nativeQuery = true)
    Set<Role> findByName(@Param("name") String name);
}
