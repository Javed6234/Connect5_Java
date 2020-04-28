package com.connect5.client;

import com.connect5.game.Game;
import com.connect5.player.Player;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.awt.print.Book;
import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Client {

    final String rootUrl;

    public Client() {
        rootUrl = "http://localhost:8080/";
    }

    private String createJoinGameUrl(String name) {
        return rootUrl + "joinGame/" + name;
    }

    private String createCheckTurnUrl(String gameId, String name) {
        return rootUrl + "checkTurn/" + String.valueOf(gameId) + "/" + name;
    }

    private Game joinGame(String name) {
        String url = this.createJoinGameUrl(name);
        RestTemplate restTemplate = new RestTemplate();

        Player newPlayer = new Player(name, null);

        return restTemplate.postForObject(url, newPlayer, Game.class);
    }

    private Game checkTurn(String gameId, String name) {
        String url = createCheckTurnUrl(gameId, name);
        RestTemplate restTemplate = new RestTemplate();

        return restTemplate.getForObject(url, Game.class);
    }

    private Game makeMove(Game game, String name, int column) {
        String url = rootUrl + "makeMove/" + game.getId() + "/" + name + "/" + column;

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Game> response = restTemplate.exchange(url, HttpMethod.PUT, null, Game.class);

        return response.getBody();
    }

    private String getPlayer(String name) {
        RestTemplate restTemplate = new RestTemplate();

        System.out.println(restTemplate.getForObject(rootUrl + name, String.class));
        return null;
    }

    private void displayGrid(Map<String, List<String>> grid) {
        for (Map.Entry<String, List<String>> entry : grid.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }

    public static void main(String [] args) throws IOException, InterruptedException {
        Client client = new Client();

        System.out.println("What is your name? ");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String userName = reader.readLine();

        Game game = client.joinGame(userName);
        Scanner scan = new Scanner(System.in);
        boolean gameOver = false;
        while (!gameOver) {
            game = client.checkTurn(game.getId(), userName);
            if (game != null) {
                if (game.isFree()) {
                    System.out.println("Waiting for a second player");
                    TimeUnit.SECONDS.sleep(3);
                    continue;
                }
                if (game.getTurnToken().equals(userName)) {
                    client.displayGrid(game.getGrid());

                    System.out.print("Enter any column: ");
                    int column = scan.nextInt();

                    game = client.makeMove(game, userName, Integer.valueOf(column));

                    gameOver = game.isGameOver();
                }
            }
            TimeUnit.SECONDS.sleep(3);
        }
        scan.close();
    }
}
