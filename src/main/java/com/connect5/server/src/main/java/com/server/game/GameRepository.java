package com.server.game;

import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

@EnableScan
public interface GameRepository extends CrudRepository<Game, String> {
    Optional<Game> findById(String id);
    Game findFirstByFreeTrue();
}