package com.connect5.client;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
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

    private void displayGrid(Map<String, List<String>> grid, int width) {
        for (Map.Entry<String, List<String>> entry : grid.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
        System.out.print("    ");
        for(int col = 0; col < width; col++) {
            System.out.print(col + "  ");
        }
        System.out.println();
    }

    public static void main(String [] args) throws IOException, InterruptedException {
        Client client = new Client();

        System.out.println("What is your name? ");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String userName = reader.readLine();

        Game game = client.joinGame(userName);
        Scanner scan = new Scanner(System.in);
        String winner = null;
        while (winner == null) {
            game = client.checkTurn(game.getId(), userName);
            winner = game.getWinner();
            if (game != null) {
                if (game.isFree()) {
                    System.out.println("Waiting for a second player");
                    TimeUnit.SECONDS.sleep(3);
                    continue;
                }
                if (game.getTurnToken().equals(userName)) {
                    client.displayGrid(game.getGrid(), game.getBoardWidth());

                    System.out.print("Enter any column: ");
                    int column = scan.nextInt();

                    boolean columnIsValid = game.validateColumn(column);
                    if (columnIsValid) {
                        game = client.makeMove(game, userName, Integer.valueOf(column));
                        winner = game.getWinner();
                    } else {
                        System.out.println("That is an invalid column, please try again");
                    }
                } else {
                    System.out.println("Waiting for a player to make a move.");
                }
            }
            TimeUnit.SECONDS.sleep(3);
        }
        if (winner.equals(userName)) {
            System.out.println("Well done, you won!");
        } else {
            System.out.println("I'm sorry, you lost!");
        }
        scan.close();
    }
}
