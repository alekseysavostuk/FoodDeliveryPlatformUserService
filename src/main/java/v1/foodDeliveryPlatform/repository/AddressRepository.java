package v1.foodDeliveryPlatform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import v1.foodDeliveryPlatform.model.Address;

import java.util.List;
import java.util.UUID;

@Repository
public interface AddressRepository extends JpaRepository<Address, UUID> {

    @Query(value = "SELECT * FROM address WHERE user_id = :userId", nativeQuery = true)
    List<Address> findAllByUserId(@Param("userId") UUID userId);

    @Modifying
    @Query(value = "DELETE FROM address WHERE id = :id", nativeQuery = true)
    void deleteDirectlyById(@Param("id") UUID id);

}
