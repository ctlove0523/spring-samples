package io.ctlove0523.spring.gateway.mysql;

import io.ctlove0523.spring.gateway.mysql.entities.RouteDefinitionPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Optional;

/**
 * @author chentong
 */
@Repository
public interface DbRouteDefinitionRepository extends JpaRepository<RouteDefinitionPO,String> {

    @Transactional
    void deleteByRouteId(String routeId);

    Optional<RouteDefinitionPO> findByRouteId(String routedId);
}
