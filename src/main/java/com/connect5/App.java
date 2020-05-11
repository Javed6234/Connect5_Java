package com.connect5;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.util.TableUtils;
import com.connect5.game.Game;
import com.connect5.game.GameRepository;
import com.connect5.player.Player;
import com.connect5.player.PlayerRepository;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@SpringBootApplication
public class App implements CommandLineRunner{

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Autowired
    AmazonDynamoDB amazonDynamoDB;

    @Autowired
    DynamoDBMapper dynamoDBMapper;

    @Autowired
    PlayerRepository playerRepository;

    @Autowired
    GameRepository gameRepository;

    @Override
    public void run(String... strings) throws Exception {

        dynamoDBMapper = new DynamoDBMapper(amazonDynamoDB);

        createTable(Game.class);
        createTable(Player.class);


        System.out.println();

        // demoCustomInterface(playerRepository);
    }

    private void createTable(Class table) {
        CreateTableRequest tableRequest = dynamoDBMapper
                .generateCreateTableRequest(table);

        tableRequest.setProvisionedThroughput(
                new ProvisionedThroughput(1L, 1L));

        TableUtils.createTableIfNotExists(amazonDynamoDB, tableRequest);
    }

    private void demoCustomInterface(PlayerRepository playerRepository) {
        Player player = new Player("Javed", "x");
        playerRepository.save(player);

        String playerId = player.getId();

        Optional<Player> playerQuery = playerRepository.findById(playerId);

        System.out.println();
        if (playerQuery.get() != null) {
            System.out.println("Queried object: " + new Gson().toJson(playerQuery.get()));
        }

        Iterable<Player> players = playerRepository.findAll();

        System.out.println();
        for (Player playerObject : players) {
            System.out.println("List object: " + new Gson().toJson(playerObject));
        }

        Board board = new Board(6, 9);
        int boardHeight = board.getHeight();
        int boardWidth = board.getWidth();
        Map<String, List<String>> grid = board.getGrid();
        Map<String, String> p1Map = new HashMap<>();
        p1Map.put(player.getName(), player.getDisc());
        String turnToken = player.getName();

        Game newGame = new Game(boardHeight, boardWidth, grid,  p1Map, turnToken);

        gameRepository.save(newGame);

        String gameId = newGame.getId();

        System.out.println("Game Id " + gameId);

        Optional<Game> gameQuery = gameRepository.findById(gameId);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Gson basicGson = new Gson();

        System.out.println();
        if (gameQuery.get() != null) {
            System.out.println("Queried object: " + gson.toJson(gameQuery.get()));
        }

        Iterable<Game> games = gameRepository.findAll();

        System.out.println();
        for (Game gameObject : games) {
           System.out.println("Game object: " + basicGson.toJson(gameObject));
        }
    }

}