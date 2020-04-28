package com.connect5;

import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;
import com.connect5.game.Game;
import com.connect5.game.GameRepository;
import com.connect5.player.Player;
import com.connect5.player.PlayerRepository;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class GameController {

    @Autowired
    PlayerRepository playerRepository;

    @Autowired
    GameRepository gameRepository;

    @GetMapping(value = "/{name}", produces = "application/json")
    public List<Player> getPlayer(@PathVariable String name) {
        return playerRepository.findByName(name);
    }

    @PostMapping(value = "joinGame/{name}", consumes = "application/json", produces = "application/json")
    public Game joinGame(@RequestBody Player newPlayer) {

        Game freeGame = gameRepository.findFirstByFreeTrue();
        System.out.println("FreeGame " + freeGame);
        Gson basicGson = new Gson();
        if (freeGame != null && freeGame.getPlayers().get(newPlayer.getName()) == null) {
            newPlayer.setDisc("o");
            freeGame.setFree(false);
            freeGame.addPlayer(newPlayer.getName(), newPlayer.getDisc());
            gameRepository.save(freeGame);

            Iterable<Game> games = gameRepository.findAll();
            for (Game gameObject : games) {
                System.out.println("Game object: " + basicGson.toJson(gameObject));
            }

            return freeGame;
        } else {
            newPlayer.setDisc("x");
            Board board = new Board(8, 7);

            int boardHeight = board.getHeight();
            int boardWidth = board.getWidth();
            Map<String, List<String>> grid = board.getGrid();
            Map<String, String> p1Map = new HashMap<String, String>();
            p1Map.put(newPlayer.getName(), newPlayer.getDisc());

            String turnToken = newPlayer.getName();

            Game newGame = new Game(boardHeight, boardWidth, grid, p1Map, turnToken);

            gameRepository.save(newGame);

            System.out.println("newGame " + newGame.getId());
            Optional<Game> gameQuery = gameRepository.findById(newGame.getId());

            System.out.println();
            if (gameQuery.get() != null) {
                System.out.println("Queried object: " + basicGson.toJson(gameQuery.get()));
            }

            Iterable<Game> games = gameRepository.findAll();
            for (Game gameObject : games) {
                System.out.println("Game object: " + basicGson.toJson(gameObject));
            }

            return newGame;
        }
    }

    @GetMapping(value = "/checkTurn/{gameId}/{name}")
    public Game checkTurn(@PathVariable String gameId, @PathVariable String name) {
        Game game = gameRepository.findById(gameId).orElse(null);
        if (game != null) {
            return game;
        }
        return null;
    }

    @PutMapping(value = "/makeMove/{gameId}/{name}/{column}")
    public Game makeMove(@PathVariable String gameId, @PathVariable String name, @PathVariable Integer column) {
        Game game = gameRepository.findById(gameId).orElse(null);
        if (game != null) {
            Map<String, List<String>> diskAdded = game.addDisc(column, name);
            if (diskAdded != null) {
                for (Map.Entry<String,String> entry : game.getPlayers().entrySet()) {
                    if (!entry.getKey().equals(name)) {
                        game.setTurnToken(entry.getKey());
                    }
                }
                gameRepository.save(game);
                return game;
            }
        }
        return null;
    }
}
