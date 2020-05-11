package com.connect5;

import com.connect5.game.Game;
import com.connect5.game.GameRepository;
import com.connect5.player.Player;
import com.connect5.player.PlayerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.hamcrest.Matchers;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.springframework.test.web.servlet.MvcResult;

import java.util.*;

@SpringBootTest
@AutoConfigureMockMvc
@ComponentScan(basePackages = "com.connect5")
public class ITGameControllerTest {

    private String playerId;
    private Player p1;
    private String gameId;
    private Game game;
    private String userName;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    GameRepository gameRepository;

    @MockBean
    PlayerRepository playerRepository;

    @BeforeEach
    public void setUp() {
        gameId = "testGameId";
        playerId = "playerId";
        userName = "Player1";
        p1 = new Player("Player1", null);
        p1.setId(playerId);
        Board board = new Board(6, 9);
        int boardHeight = board.getHeight();
        int boardWidth = board.getWidth();
        Map<String, List<String>> grid = board.getGrid();
        Map<String, String> p1Map = new HashMap<>();
        p1Map.put(p1.getName(), p1.getDisc());
        String turnToken = p1.getName();

        game = new Game(boardHeight, boardWidth, grid, p1Map, turnToken);
        game.setId(gameId);
    }

    @Test
    public void shouldReturnPlayerObject() throws Exception {
        when(playerRepository.findFirstByName(userName)).thenReturn(p1);

        mockMvc.perform(get("/Player1")).andDo(print()).andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Player1"))
                .andExpect(jsonPath("$.disc").value(IsNull.nullValue()))
                .andExpect(jsonPath("$.id").value("playerId"));
    }

    @Test
    public void shouldJoinFreeGame() throws Exception {
        when(gameRepository.findFirstByFreeTrue()).thenReturn(game);

        mockMvc.perform(post("/joinGame/{name}", userName)
                .content(asJsonString(p1))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.boardHeight").value(6))
                .andExpect(jsonPath("$.boardWidth").value(9))
                .andExpect(jsonPath("$.winner").value(IsNull.nullValue()))
                .andExpect(jsonPath("$.free").value(false))
                .andExpect(jsonPath("$.turnToken").value(userName))
                .andExpect(jsonPath("$.players", Matchers.hasEntry(userName, "o")));
    }

    @Test
    public void shouldJoinNewGame() throws Exception {
        when(gameRepository.findFirstByFreeTrue()).thenReturn(null);
        game.getPlayers().put(userName, "x");
        when(gameRepository.save(any(Game.class))).thenReturn(game);

        mockMvc.perform(post("/joinGame/{name}", userName)
                .content(asJsonString(p1))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.boardHeight").value(6))
                .andExpect(jsonPath("$.boardWidth").value(9))
                .andExpect(jsonPath("$.winner").value(IsNull.nullValue()))
                .andExpect(jsonPath("$.free").value(true))
                .andExpect(jsonPath("$.turnToken").value(userName))
                .andExpect(jsonPath("$.players", Matchers.hasEntry(userName, "x")));
    }

    @Test
    public void shouldCheckTurn() throws Exception {
        when(gameRepository.findFirstByFreeTrue()).thenReturn(null);
        game.getPlayers().put(userName, "z");
        when(gameRepository.save(any(Game.class))).thenReturn(game);

        MvcResult result = mockMvc.perform(post("/joinGame/{name}", userName)
                .content(asJsonString(p1))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        String returnedGameId = JsonPath.read(result.getResponse().getContentAsString(), "$.id");

        when(gameRepository.findById(returnedGameId)).thenReturn(Optional.ofNullable(game));

        mockMvc.perform(get("/checkTurn/{gameId}/{name}", returnedGameId, userName))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.boardHeight").value(6))
                .andExpect(jsonPath("$.boardWidth").value(9))
                .andExpect(jsonPath("$.winner").value(IsNull.nullValue()))
                .andExpect(jsonPath("$.free").value(true))
                .andExpect(jsonPath("$.turnToken").value(userName))
                .andExpect(jsonPath("$.players", Matchers.hasEntry(userName, "z")));
    }

    @Test
    public void shouldMakeMove() throws Exception {
        when(gameRepository.findFirstByFreeTrue()).thenReturn(null);
        game.getPlayers().put(userName, "z");
        when(gameRepository.save(any(Game.class))).thenReturn(game);

        MvcResult result = mockMvc.perform(post("/joinGame/{name}", userName)
                .content(asJsonString(p1))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        String returnedGameId = JsonPath.read(result.getResponse().getContentAsString(), "$.id");

        when(gameRepository.findById(returnedGameId)).thenReturn(Optional.ofNullable(game));

        int column = 3;

        mockMvc.perform(put("/makeMove/{gameId}/{name}/{column}", returnedGameId, userName, column))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.boardHeight").value(6))
                .andExpect(jsonPath("$.boardWidth").value(9))
                .andExpect(jsonPath("$.winner").value(IsNull.nullValue()))
                .andExpect(jsonPath("$.free").value(true))
                .andExpect(jsonPath("$.turnToken").value(userName))
                .andExpect(jsonPath("$.players", Matchers.hasEntry(userName, "z")))
                .andExpect(jsonPath("$.grid.5", Matchers.hasItem("z")));

    }

    public static String asJsonString(final Object obj) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            final String jsonContent = mapper.writeValueAsString(obj);
            return jsonContent;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
