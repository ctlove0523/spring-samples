package io.ctlove0523.spring.mysql.routes;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * @author chentong
 */
@Repository
public interface RouteDefinitionRepository extends JpaRepository<RouteDefinition, String> {

	@Transactional
	void deleteByUri(String uri);

	@Transactional
	@Modifying
	@Query(value = "UPDATE t_route_definition rd SET rd.route_order = ?2 WHERE rd.uri = ?1", nativeQuery = true)
	void updateByUri(String uri, Integer order);
}
