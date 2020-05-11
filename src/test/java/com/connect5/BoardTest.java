package com.connect5;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class BoardTest {

    private final int expectedHeight;
    private final int expectedWidth;

    public BoardTest(int expectedHeight, int expectedWidth) {
        this.expectedHeight = expectedHeight;
        this.expectedWidth = expectedWidth;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> getTestData() {
        return Arrays.asList(new Object[][]{
                {6, 9},
                {5, 6},
                {4, 8},
                {3, 10},
        });
    }

    @Test
    public void  checkGridMatchesHeightAndWidth() {
        System.out.println("Running test for: Height "+expectedHeight +" and Width "+ expectedWidth);
        Board board = new Board(expectedHeight, expectedWidth);

        List<String> row = board.getGrid().entrySet().iterator().next().getValue();

        assertEquals(expectedHeight, board.getGrid().size());
        assertEquals(expectedWidth, row.size());
    }
}
