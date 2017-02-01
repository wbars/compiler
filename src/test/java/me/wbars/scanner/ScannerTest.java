package me.wbars.scanner;

import junit.framework.TestCase;
import me.wbars.scanner.io.StringGrammarReader;
import me.wbars.scanner.models.Token;
import me.wbars.scanner.models.TransitionTable;

import static org.junit.Assert.assertArrayEquals;

public class ScannerTest extends TestCase {
    private TransitionTable getTableFromString(String grammar) {
        return new StringGrammarReader(grammar).readTable();
    }

    public void testSimpleTokens() throws Exception {
        TransitionTable table = getTableFromString("" +
                "A a\n" +
                "AA aa\n" +
                "AB ab\n" +
                "BA ba");
        Token[] tokens = Scanner.scan(
                "a aa ab ba aaa aba abba baba baa baab",
                table
        ).toArray(new Token[0]);

        Token A = Token.create("A", "a");
        Token AA = Token.create("AA", "aa");
        Token AB = Token.create("AB", "ab");
        Token BA = Token.create("BA", "ba");

        assertArrayEquals(
                new Token[]{A, AA, AB, BA, AA, A, AB, A, AB, BA, BA, BA, BA, A, BA, AB},
                tokens
        );
    }

    public void testOrRegexp() throws Exception {
        TransitionTable table = getTableFromString("" +
                "A a\n" +
                "B b\n" +
                "OR ab|baa");
        Token[] tokens = Scanner.scan(
                "a b ab baa baaa aab aba",
                table
        ).toArray(new Token[0]);
        Token A = Token.create("A", "a");
        Token B = Token.create("B", "b");
        Token OR1 = Token.create("OR", "ab");
        Token OR2 = Token.create("OR", "baa");

        assertArrayEquals(
                new Token[]{A, B, OR1, OR2, OR2, A, A, OR1, OR1, A},
                tokens
        );
    }

    public void testKleene() throws Exception {
        Token A = Token.create("A", "a");
        Token B = Token.create("B", "b");
        Token KLEENE1 = Token.create("KLEENE", "ab");
        Token KLEENE4 = Token.create("KLEENE", "aaaab");

        TransitionTable table = getTableFromString("" +
                "A a\n" +
                "B b\n" +
                "KLEENE ((a)*)b");

        Token[] tokens = Scanner.scan(
                "a b ab aaaab abab b aa",
                table
        ).toArray(new Token[0]);

        assertArrayEquals(
                new Token[]{A, B, KLEENE1, KLEENE4, KLEENE1, KLEENE1, B, A, A},
                tokens
                );
    }

    public void testAtLeastOnce() throws Exception {
        Token A = Token.create("A", "a");
        Token AtLeastOnce1 = Token.create("AtLeastOnce", "ab");
        Token AtLeastOnce4 = Token.create("AtLeastOnce", "aaaab");
        Token KLEENE0 = Token.create("KLEENE0", "b");

        TransitionTable table = getTableFromString("" +
                "A a\n" +
                "AtLeastOnce ((a)+)b\n" +
                "KLEENE0 ((a)*)b"
        );

        Token[] tokens = Scanner.scan(
                "a b ab aaaab abab b aa",
                table
        ).toArray(new Token[0]);

        assertArrayEquals(
                new Token[]{A, KLEENE0, AtLeastOnce1, AtLeastOnce4, AtLeastOnce1, AtLeastOnce1, KLEENE0, A, A},
                tokens
        );
    }

    public void testMacros() throws Exception {
        Token d123 = Token.create("D", "123");
        Token d12 = Token.create("D", "12");
        Token wabc = Token.create("W", "abc");
        Token wa = Token.create("W", "a");

        TransitionTable table = getTableFromString("" +
                "D \\d+\n" +
                "W \\w+\n" +
                "A1 a\n" +
                "B1 b\n" +
                "1 1\n" +
                "123 123\n" +
                "12 12\n" +
                "ABC1 abc"
        );

        Token[] tokens = Scanner.scan(
                "123 12 abc a",
                table
        ).toArray(new Token[0]);

        assertArrayEquals(
                new Token[]{d123, d12, wabc, wa},
                tokens
        );
    }
}