package me.wbars.editor;

import me.wbars.compiler.Compiler;
import me.wbars.compiler.utils.ObjectsUtils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

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
    private JPopupMenu autocompletePopup;
    private List<JMenuItem> autocompletePopupItems = new ArrayList<>();

    public static void main(String[] args) {
        Editor editor = new Editor();
        editor.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        editor.setVisible(true);
    }

    private Editor() {
        super("Editor");

        setSize(600, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());

        initTextComponent();
        initMenuBar();
        initCompiler();
    }

    private void initCompiler() {
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
            } catch (Exception ignored) {
            }
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
        CompilerDocument doc = new CompilerDocument(compiler);
        JTextPane editorPane = new JTextPane(doc);
        editorPane.setBackground(EditorStyle.backgroundColor);
        editorPane.setCaretColor(EditorStyle.caretColor);

        autocompletePopup = new JPopupMenu("Autocomplete");
        resetAutocompletePopup();

        KeyListener textInputListener = new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                SingleSelectionModel selectionModel = autocompletePopup.getSelectionModel();
                if (!autocompletePopup.isVisible()) return;
                if (e.getExtendedKeyCode() == KeyEvent.VK_ENTER) {
                    String replace = autocompletePopupItems.get(selectionModel.getSelectedIndex()).getText();
                    replaceCurrentChunk(replace, doc, editorPane);
                    e.consume();
                    return;
                }
                if (e.getExtendedKeyCode() == KeyEvent.VK_UP) {
                    selectionModel.setSelectedIndex(Math.max(0, selectionModel.getSelectedIndex() - 1));
                    e.consume();
                    return;
                }
                if (e.getExtendedKeyCode() == KeyEvent.VK_DOWN) {
                    selectionModel.setSelectedIndex(Math.min(autocompletePopupItems.size() - 1, selectionModel.getSelectedIndex() + 1));
                    e.consume();
                    return;
                }

                if (e.getExtendedKeyCode() == KeyEvent.VK_ESCAPE) {
                    resetAutocompletePopup();
                    e.consume();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                SingleSelectionModel selectionModel = autocompletePopup.getSelectionModel();

                resetAutocompletePopup();

                Point currentPoint = editorPane.getCaret().getMagicCaretPosition();
                if (currentPoint == null) return;

                List<String> currentAutocompleteList = doc.getCurrentAutocompleteList(editorPane.getCaretPosition() - 1);
                if (currentAutocompleteList.isEmpty()) return;

                currentAutocompleteList.forEach(s -> {
                    JMenuItem menuItem = new JMenuItem(s);
                    autocompletePopup.add(menuItem);
                    autocompletePopupItems.add(menuItem);
                    menuItem.addActionListener(e1 -> replaceCurrentChunk(s, doc, editorPane));
                });
                selectionModel.setSelectedIndex(0);

                if (autocompletePopup.isVisible()) {
                    SwingUtilities.convertPointToScreen(currentPoint, editorPane);
                    autocompletePopup.setLocation(currentPoint.x, currentPoint.y + 20);
                } else {
                    autocompletePopup.show(editorPane, currentPoint.x, currentPoint.y + 20);
                }
            }
        };
        editorPane.addKeyListener(textInputListener);

        return editorPane;
    }

    public void replaceCurrentChunk(String replace, CompilerDocument doc, JTextPane editorPane) {
        doc.replaceCurrentChunk(editorPane.getCaretPosition() - 1, replace);
        resetAutocompletePopup();
    }

    public void resetAutocompletePopup() {
        autocompletePopup.setVisible(false);
        autocompletePopup.setFocusable(false);
        autocompletePopup.removeAll();
        autocompletePopupItems.clear();
    }
}