package io.ctlove0523.spring.gateway.mysql;

import io.ctlove0523.spring.gateway.mysql.entities.RouteDefinitionPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author chentong
 */
@Repository
public interface DbRouteDefinitionRepository extends JpaRepository<RouteDefinitionPO,String> {
}
