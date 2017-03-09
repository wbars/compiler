package me.wbars.editor;

import me.wbars.compiler.Compiler;
import me.wbars.compiler.utils.ObjectsUtils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import static javax.swing.JOptionPane.showMessageDialog;

public class Editor extends JFrame {
    private JEditorPane editorPane;
    private JScrollPane scrollPane;
    private JMenuBar menuBar;
    private JMenu menuFile;
    private JMenuItem menuFileOpen;
    private JMenuItem menuFileSave;
    private JMenuItem menuFileExit;
    private String selectedPath;
    private JMenuItem menuFileSaveAs;
    private final Compiler compiler = new Compiler();
    private JMenu menuRun;
    private JMenuItem menuRunCompile;
    private JMenuItem menuRunRun;
    private JMenuItem menuRunQuickfixes;
    private JMenuItem menuRunSelectedQuickfix;

    public static void main(String[] args) {
        Editor editor = new Editor();
        editor.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        editor.setVisible(true);
    }

    // Create an editor.
    public Editor() {
        super("Editor");

        setSize(600, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());

        initTextComponent();
        initMenuBar();
        initCompilier();
    }

    private void initCompilier() {
        menuRun = new JMenu("Run");
        menuRunCompile = new JMenuItem("Compile");
        menuRunCompile.addActionListener(e -> {
            showMessageDialog(null, "Done! File class: " + compiler.compile(editorPane.getText()));
        });

        menuRunRun = new JMenuItem("Run");
        menuRunRun.addActionListener(e -> {
            String className = compiler.compile(editorPane.getText());
            try {
                showMessageDialog(null, execCommand("java -noverify " + className));
            } catch (IOException e1) {
                showMessageDialog(null, "Internal exception! " + e1.toString());
            }
        });

        menuRunQuickfixes = new JMenuItem("Run quickfixes");
        menuRunQuickfixes.addActionListener(e -> {
            CompilerDocument compilerDocument = ObjectsUtils.tryCast(editorPane.getDocument(), CompilerDocument.class);
            if (compilerDocument == null) throw new IllegalStateException();
            compilerDocument.runOptimizations();
        });

        menuRunSelectedQuickfix = new JMenuItem("Run selected quickfix");
        menuRunSelectedQuickfix.addActionListener(e -> {
            CompilerDocument compilerDocument = ObjectsUtils.tryCast(editorPane.getDocument(), CompilerDocument.class);
            if (compilerDocument == null) throw new IllegalStateException();
            compilerDocument.runSelectedOptimisation(editorPane.getCaretPosition());
        });

        menuRun.add(menuRunCompile);
        menuRun.add(menuRunRun);
        menuRun.add(menuRunQuickfixes);
        menuRun.add(menuRunSelectedQuickfix);
        menuBar.add(menuRun);
    }

    private String execCommand(String command) throws IOException {
        Runtime rt = Runtime.getRuntime();
        Process proc = rt.exec(command);

        BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));

        BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

        StringBuilder result = new StringBuilder();
        String s;
        while ((s = stdInput.readLine()) != null) {
            result.append(s);
            result.append("\n");
        }

        StringBuilder error = new StringBuilder();
        while ((s = stdError.readLine()) != null) {
            error.append(s);
        }
        return result.toString();
    }

    private void initMenuBar() {
        menuBar = new JMenuBar();
        menuFile = new JMenu("File");
        menuFileOpen = new JMenuItem("Open");
        menuFileSave = new JMenuItem("Save");
        menuFileSaveAs = new JMenuItem("Save as...");
        menuFileExit = new JMenuItem("Exit");

        menuFileSave.setEnabled(false);

        menuFile.add(menuFileOpen);
        menuFile.add(menuFileSave);
        menuFile.add(menuFileSaveAs);
        menuFile.add(menuFileExit);

        menuBar.add(menuFile);

        setJMenuBar(menuBar);

        menuFileOpen.addActionListener(e -> {
            JFileChooser chooser = createFileChooser(JFileChooser.OPEN_DIALOG);
            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                try {
                    selectedPath = chooser.getSelectedFile().getAbsolutePath();
                    editorPane.setText(getFileContents(selectedPath));
                    setTitle(chooser.getSelectedFile().getName());
                    menuFileSave.setEnabled(true);
                } catch (IOException e1) {
                    throw new RuntimeException();
                }
            }
        });

        menuFileSave.addActionListener(e -> writeCurrentText());

        menuFileSaveAs.addActionListener(e -> {
            JFileChooser chooser = createFileChooser(JFileChooser.SAVE_DIALOG);
            if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                selectedPath = chooser.getSelectedFile().getAbsolutePath();
                writeCurrentText();
                setTitle(chooser.getSelectedFile().getName());
                menuFileSave.setEnabled(true);
            }
        });

        menuFileExit.addActionListener(e -> {
            this.processWindowEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        });
    }

    private JFileChooser createFileChooser(int mode) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogType(mode);
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Only text files", "pas", "txt");
        chooser.setFileFilter(filter);
        return chooser;
    }

    private void writeCurrentText() {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(selectedPath));
            writer.write(editorPane.getText());
        } catch (IOException e1) {
            e1.printStackTrace();
        } finally {
            try {
                if (writer != null) writer.close();
            } catch (Exception ignored) {}
        }
    }

    private static String getFileContents(String path) throws IOException {
        return Files.readAllLines(Paths.get(path)).stream()
                .reduce((s, s2) -> s + "\n" + s2).orElse("");
    }

    public void initTextComponent() {
        editorPane = createEditorComponent();
        scrollPane = new JScrollPane(editorPane);
        getContentPane().add(scrollPane, BorderLayout.CENTER);
    }


    private JEditorPane createEditorComponent() {
        JTextPane editorPane = new JTextPane(new CompilerDocument(compiler));
        editorPane.setBackground(EditorStyle.backgroundColor);
        editorPane.setCaretColor(EditorStyle.caretColor);
        return editorPane;
    }
}