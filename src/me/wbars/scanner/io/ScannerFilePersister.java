package me.wbars.scanner.io;

import me.wbars.scanner.models.PartOfSpeech;
import me.wbars.scanner.models.TransitionTable;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class ScannerFilePersister {
    public static void writeToFile(TransitionTable table, String path) throws IOException {
        List<String> lines = new ArrayList<>();

        lines.addAll(getTransitionTable(table.getTransitions()));
        lines.addAll(getPosTable(table.getPosMap()));

        Files.write(Paths.get(path), lines, Charset.forName("UTF-8"));
    }

    private static String commaConcat(String s1, String s2) {
        return s1 + " " + s2;
    }

    private static String assignConcat(String s1, String s2) {
        return s1 + ":=" + s2;
    }

    private static String[] assignSplit(String s) {
        return s.split(":=");
    }

    private static List<String> getTransitionTable(Map<Integer, Map<Character, Integer>> transitions) {
        List<String> result = new ArrayList<>();
        result.add(String.valueOf(transitions.size()));
        result.addAll(
                transitions.entrySet().stream()
                        .map(e -> getTransitionsSection(e.getKey(), e.getValue()))
                        .collect(Collectors.toSet())
        );
        return result;
    }

    private static String getTransitionsSection(Integer state, Map<Character, Integer> transitions) {
        String edges = transitions.entrySet().stream()
                .map(e -> e.getKey() + "" + e.getValue())
                .reduce(ScannerFilePersister::commaConcat).orElse("");
        return assignConcat(state.toString(), edges);
    }

    private static List<String> getPosTable(Map<PartOfSpeech, Set<Integer>> posMap) {
        List<String> posTerminals = posMap.entrySet().stream()
                .sorted(Comparator.comparingInt(value -> value.getKey().getId()))
                .map(e -> getPosTerminalsSection(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        List<String> result = new ArrayList<>();
        result.add(String.valueOf(posMap.size()));
        result.addAll(posTerminals);
        return result;
    }

    private static String getPosTerminalsSection(PartOfSpeech pos, Set<Integer> terminals) {
        return assignConcat(pos.name, terminals.stream()
                .map(Object::toString)
                .reduce(ScannerFilePersister::commaConcat).orElse(""));
    }

    public static TransitionTable fromFile(String path) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(path));

        int transitionsCount = Integer.parseInt(lines.get(0));
        Map<Integer, Map<Character, Integer>> transitions = getTransitions(lines.subList(1, transitionsCount + 1));

        int posCount = Integer.parseInt(lines.get(transitionsCount + 1));
        assert lines.size() == posCount;
        Map<PartOfSpeech, Set<Integer>> dfaPos = getPos(lines.subList(transitionsCount + 2, lines.size()));

        return TransitionTable.create(transitions, dfaPos, 0);
    }

    private static Map<PartOfSpeech, Set<Integer>> getPos(List<String> lines) {
        return lines.stream()
                .map(ScannerFilePersister::assignSplit)
                .collect(Collectors.toMap(
                        s -> PartOfSpeech.getOrCreate(s[0]),
                        s -> Arrays.stream(s[1].split(" ")).map(Integer::parseInt).collect(Collectors.toSet()))
                );
    }

    private static Map<Integer, Map<Character, Integer>> getTransitions(List<String> lines) {
        return lines.stream()
                .map(ScannerFilePersister::assignSplit)
                .collect(Collectors.toMap(
                        s -> Integer.parseInt(s[0]),
                        s -> s.length > 1 ? getTransitions(s[1]) : Collections.emptyMap()
                ));
    }

    private static Map<Character, Integer> getTransitions(String transitionsString) {
        Map<Character, Integer> result = new HashMap<>();
        int itt = 0;
        while (itt < transitionsString.length()) {
            char key = transitionsString.charAt(itt);
            StringBuilder sb = new StringBuilder();
            while (itt < transitionsString.length() - 1 && transitionsString.charAt(itt + 1) != ' ') {
                itt++;
                sb.append(transitionsString.charAt(itt));
            }
            result.put(key, Integer.parseInt(sb.toString()));
            itt+= 2;
        }
        return result;
    }
}
