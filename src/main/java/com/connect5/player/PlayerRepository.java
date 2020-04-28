package com.connect5.player;

import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

@EnableScan
public interface PlayerRepository extends CrudRepository<Player, String> {
    List<Player> findByName(String name);
    Optional<Player> findById(String id);
}