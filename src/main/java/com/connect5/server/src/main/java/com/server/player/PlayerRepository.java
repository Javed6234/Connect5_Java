package com.server.player;

import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

@EnableDynamoDBRepositories("com.server")
public interface PlayerRepository extends CrudRepository<Player, String> {
    List<Player> findByName(String name);
    Optional<Player> findById(String id);

    Player findFirstByName(String name);
}