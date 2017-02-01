package me.wbars.scanner.io;

import me.wbars.scanner.models.TransitionTable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

@RunWith(PowerMockRunner.class)
public class ScannerFilePersisterTest {
    private TransitionTable getTableFromString(String grammar) {
        return new StringGrammarReader(grammar).readTable();
    }

    @Test
    @PrepareForTest
    public void getLines() throws Exception {
        Set<String> lines = new HashSet<>(
                ScannerFilePersister.getLines(
                        getTableFromString("" +
                                "A a\n" +
                                "BINARY (0|1)+")
                )
        );
        assertEquals(lines, new HashSet<>(Arrays.asList(
                "2",
                "BINARY:=1 2",
                "A:=3",
                "4",
                "3:=", //A
                "1:=01 12", //BINARY 0
                "2:=01 12", //BINARY 1
                "0:=01 12 a3"
        )));
    }

}