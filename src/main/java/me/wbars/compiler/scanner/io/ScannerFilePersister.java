package me.wbars.compiler.scanner.io;

import me.wbars.compiler.scanner.models.PartOfSpeech;
import me.wbars.compiler.scanner.models.TransitionTable;
import me.wbars.compiler.utils.ObjectsUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Integer.parseInt;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toSet;

public class ScannerFilePersister {
    public static void writeToFile(TransitionTable table, String path) throws IOException {

        Files.write(Paths.get(path), getLines(table), Charset.forName("UTF-8"));
    }

    public static List<String> getLines(TransitionTable table) {
        List<String> lines = new ArrayList<>();

        lines.addAll(getTransitionTable(table.getTransitions()));
        lines.addAll(getAnyTransitionTable(table.getAnyTransitions()));
        lines.addAll(getPosTable(table.getPosMap()));
        return lines;
    }

    private static List<String> getAnyTransitionTable(Map<Integer, Integer> anyTransitions) {
        List<String> result = new ArrayList<>();
        result.add(String.valueOf(anyTransitions.size()));
        result.addAll(
                anyTransitions.entrySet().stream()
                        .map(e -> assignConcat(e.getKey().toString(), e.getValue().toString()))
                        .collect(toSet())
        );
        return result;
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
                        .collect(toSet())
        );
        return result;
    }

    private static String getTransitionsSection(Integer state, Map<Character, Integer> transitions) {
        String edges = transitions.entrySet().stream()
                .map(e -> e.getKey() + "" + e.getValue())
                .reduce(ObjectsUtils::spaceConcat).orElse("");
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
                .reduce(ObjectsUtils::spaceConcat).orElse(""));
    }

    public static TransitionTable fromFile(String path) {
        List<String> lines;
        try {
            lines = Files.readAllLines(Paths.get(path));
        } catch (IOException e) {
            return null;
        }

        int transitionsCount = parseInt(lines.get(0));
        int transitionsEnd = transitionsCount + 1;
        Map<Integer, Map<Character, Integer>> transitions = getTransitions(lines.subList(1, transitionsEnd));

        int anyTransitionsCount = parseInt(lines.get(transitionsEnd));
        int anyTransitionsEnd = transitionsEnd + anyTransitionsCount + 1;
        Map<Integer, Integer> anyTransitions = getAnyTransitions(lines.subList(transitionsEnd + 1, anyTransitionsEnd));

        int posCount = parseInt(lines.get(anyTransitionsEnd));
        assert lines.size() == posCount;
        Map<PartOfSpeech, Set<Integer>> dfaPos = getPos(lines.subList(anyTransitionsEnd + 1, lines.size()));

        return TransitionTable.create(transitions, dfaPos, 0, anyTransitions);
    }

    private static Map<Integer, Integer> getAnyTransitions(List<String> lines) {
        return lines.stream()
                .map(ScannerFilePersister::assignSplit)
                .collect(Collectors.toMap(
                        s -> parseInt(s[0]),
                        s -> parseInt(s[1])
                ));
    }

    private static Map<PartOfSpeech, Set<Integer>> getPos(List<String> lines) {
        return lines.stream()
                .map(ScannerFilePersister::assignSplit)
                .collect(Collectors.toMap(
                        s -> PartOfSpeech.getOrCreate(s[0]),
                        s -> stream(s[1].split(" ")).map(Integer::parseInt).collect(toSet()))
                );
    }

    private static Map<Integer, Map<Character, Integer>> getTransitions(List<String> lines) {
        return lines.stream()
                .map(ScannerFilePersister::assignSplit)
                .collect(Collectors.toMap(
                        s -> parseInt(s[0]),
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
            result.put(key, parseInt(sb.toString()));
            itt += 2;
        }
        return result;
    }
}
