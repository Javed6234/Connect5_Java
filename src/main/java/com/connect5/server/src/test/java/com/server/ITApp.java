package com.server;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.DeleteTableRequest;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.server.game.Game;
import com.server.player.Player;
import org.hamcrest.Matchers;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ITApp {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    DynamoDBMapper dynamoDBMapper;

    @Autowired
    AmazonDynamoDB amazonDynamoDB;

    @Test
    public void contextLoads() { }

    @BeforeEach
    public void setUp() {
        createTable(Player.class);
        createTable(Game.class);

    }

    @AfterEach
    public void tearDown() {
        deleteTable(Player.class);
        deleteTable(Game.class);
    }

    private void createTable(Class table) {
        CreateTableRequest tableRequest = dynamoDBMapper.generateCreateTableRequest(table);

        tableRequest.setProvisionedThroughput(
                new ProvisionedThroughput(1L, 1L));

        amazonDynamoDB.createTable(tableRequest);
    }

    private void deleteTable(Class table) {
        DeleteTableRequest tableRequest = dynamoDBMapper.generateDeleteTableRequest(table);

        amazonDynamoDB.deleteTable(tableRequest);
    }

    @Test
    public void shouldJoinNewGame() throws Exception {
        String userName = "ITTest";
        Player player = new Player(userName, null);
        mockMvc.perform(post("/joinGame/{name}", userName)
                .content(asJsonString(player))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.boardHeight").value(6))
                .andExpect(jsonPath("$.boardWidth").value(9))
                .andExpect(jsonPath("$.winner").value(IsNull.nullValue()))
                .andExpect(jsonPath("$.free").value(true))
                .andExpect(jsonPath("$.turnToken").value(userName))
                .andExpect(jsonPath("$.players", Matchers.hasEntry(userName, "x")));
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
