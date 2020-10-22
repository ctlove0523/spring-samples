package io.ctlove0523.spring.mysql.routes;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author chentong
 */
@Repository
public interface RouteDefinitionRepository extends JpaRepository<RouteDefinition, String> {
}
