// Garbage Geany created by Christopher Lee

// imports
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import javax.swing.JScrollPane;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

// main class
final public class GarbageGeany implements ActionListener, UndoableEditListener {
    // to see which os you're running.
    private static boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");

    // frame and main interface
    private JFrame frame = new JFrame("Garbage Geany");
    private JTextArea textarea = new JTextArea();
    private JScrollPane scroll = new JScrollPane(textarea);

    // menu bars
    private JMenuBar menubar = new JMenuBar();
    private JMenuBar outputbar = new JMenuBar();

    // menu items relating to file
    private JMenu filemenu = new JMenu("File");
    private JMenuItem openitem = new JMenuItem("Open");
    private JMenuItem saveitem = new JMenuItem("Save");
    private JMenuItem closeitem = new JMenuItem("Close");

    // menu items relating to edit
    private JMenu editmenu = new JMenu("Edit");
    private JMenuItem undoitem = new JMenuItem("Undo");
    private JMenuItem redoitem = new JMenuItem("Redo");
    private UndoManager undoer = new UndoManager();

    // menu items relating to code
    private JMenu codemenu = new JMenu("Code");
    private JMenuItem compileitem = new JMenuItem("Compile");
    private JMenuItem runitem = new JMenuItem("Run");

    // menu items relating to output window
    private JMenu outputmenu = new JMenu("Output");
    private JMenuItem clearoutputitem = new JMenuItem("Clear");

    // compile and run window
    private JFrame outputframe = new JFrame("Output");
    private JTextArea outputarea = new JTextArea();
    private JScrollPane outputscroll = new JScrollPane(outputarea);

    // fileio management
    private JFileChooser filechooser = new JFileChooser();
    private String path = "";
    private String parent_path = "";
    private String file_name = "";
    private String current_line;
    private String compareoutput;

    // actionListener stuff
    @Override
    public void actionPerformed(ActionEvent evt) {
        // open file, pretty basic stuff
        if (evt.getSource() == this.openitem) {
            if (this.filechooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                this.textarea.setText("");
                try {
                    this.path = this.filechooser.getSelectedFile().getPath();
                    this.file_name = this.filechooser.getSelectedFile().getName();
                    this.parent_path = this.filechooser.getSelectedFile().getParent();
                    BufferedReader openfile = new BufferedReader(new FileReader(path));
                    this.current_line = openfile.readLine();
                    this.textarea.append(current_line);
                    this.current_line = openfile.readLine();
                    while (current_line != null) {
                        this.textarea.append("\n" + current_line);
                        this.current_line = openfile.readLine();
                    }
                    openfile.close();
                } catch (IOException e) {
                }
            }
            // save item, see subsequent function
        } else if (evt.getSource() == this.saveitem) {
            this.saveFile();
            // compile item, see subsequent function
        } else if (evt.getSource() == this.compileitem) {
            this.compileFile();
            // run item, see subsequent function
        } else if (evt.getSource() == this.runitem) {
            this.runFile();
            // close item, reset text area and path link
        } else if (evt.getSource() == this.closeitem) {
            this.textarea.setText("");
            this.path = "";
            // undo item, use undo manager
        } else if (evt.getSource() == this.undoitem) {
            try {
                this.undoer.undo();
            } catch (CannotUndoException e) {
            }
        } else if (evt.getSource() == this.redoitem) {
            try {
                this.undoer.redo();
            } catch (CannotUndoException e) {
            }
        } else if (evt.getSource() == this.clearoutputitem) {
            this.outputarea.setText("");
        }
    }

    // Undo edit listener
    @Override
    public void undoableEditHappened(UndoableEditEvent e) {
        undoer.addEdit(e.getEdit());
    }

    // save file function
    private void saveFile() {
        // if the path is not empty then we can save to the current file
        if (this.path != "") {
            try {
                PrintWriter outfile = new PrintWriter(new FileWriter(path));
                outfile.println(textarea.getText());
                outfile.close();
                return;
            } catch (IOException e) {
                return;
            }
        }

        // if path empty, open save dialog and save to that file
        if (this.filechooser.showSaveDialog(this.frame) == JFileChooser.APPROVE_OPTION) {
            try {
                this.path = this.filechooser.getSelectedFile().getPath();
                this.file_name = this.filechooser.getSelectedFile().getName();
                this.parent_path = this.filechooser.getSelectedFile().getParent();
                PrintWriter outfile = new PrintWriter(new FileWriter(this.path));
                outfile.println(this.textarea.getText());
                outfile.close();
            } catch (IOException e) {
            }
        }
    }

    // compile file, thank god for JavaCompiler
    private void compileFile() {
        this.saveFile();
        if (this.path != "") {
            this.compareoutput = this.outputarea.getText();
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            PrintStream compileoutput = new PrintStream(new TextAreaWriter(this.outputarea));
            compiler.run(null, null, compileoutput, this.path);
            this.outputarea.append("% COMPILE COMPLETE %\n");
            this.outputarea.append("==================\n");
            this.compareoutput += "% COMPILE COMPLETE %\n";
            this.compareoutput += "==================\n";
        }
    }

    // run file, you need to make sure you're compiling first
    private void runFile() {
        this.compileFile();
        if (this.path == "" || !this.outputarea.getText().equals(this.compareoutput)) {
            return;
        }
        ProcessBuilder runfile = new ProcessBuilder();
        // determine which command to run based on the os
        if (isWindows) {
            runfile.command("cmd.exe", "/c",
                    "java -cp " + parent_path + " " + file_name.substring(0, file_name.lastIndexOf(".java")));
        } else {
            runfile.command("sh", "-c",
                    "java -cp " + parent_path + " " + file_name.substring(0, file_name.lastIndexOf(".java")));
        }

        // start the process and then close the stream when finished.
        try {
            Process process = runfile.start();
            OutputStream outputStream = process.getOutputStream();
            InputStream inputStream = process.getInputStream();
            InputStream errorStream = process.getErrorStream();

            this.printStream(inputStream);
            this.printStream(errorStream);
            process.waitFor();
            outputStream.flush();
            outputStream.close();
        } catch (IOException | InterruptedException e) {
        }

        this.outputarea.append("==================\n");
    }

    // used for printing out the stream... if there are system.out.printlns we will
    // see them here.
    private void printStream(InputStream inputStream) {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                this.outputarea.append(line + "\n");
            }

        } catch (IOException e) {
        }
    }

    // constructor
    GarbageGeany() {
        // text pane and output pane
        this.scroll.setPreferredSize(new Dimension(600, 600));
        this.outputscroll.setPreferredSize(new Dimension(600, 600));

        // change tab size, I like it better like this
        this.textarea.setTabSize(2);
        this.outputarea.setTabSize(2);

        // filemenu stuff
        this.menubar.add(this.filemenu);
        this.filemenu.setMnemonic(KeyEvent.VK_F);
        this.filemenu.add(this.openitem);
        this.openitem.setMnemonic(KeyEvent.VK_O);
        this.openitem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK));
        this.filemenu.add(this.saveitem);
        this.saveitem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK));
        this.saveitem.setMnemonic(KeyEvent.VK_S);
        this.filemenu.add(this.closeitem);
        this.closeitem.setMnemonic(KeyEvent.VK_C);

        // editmenu stuff
        this.menubar.add(this.editmenu);
        this.editmenu.setMnemonic(KeyEvent.VK_E);
        this.editmenu.add(this.undoitem);
        this.undoitem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK));
        this.undoitem.setMnemonic(KeyEvent.VK_U);
        this.editmenu.add(this.redoitem);
        this.redoitem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK));
        this.undoitem.setMnemonic(KeyEvent.VK_R);

        // codemenu stuff
        this.menubar.add(this.codemenu);
        this.codemenu.setMnemonic(KeyEvent.VK_C);
        this.codemenu.add(this.compileitem);
        this.compileitem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, KeyEvent.CTRL_DOWN_MASK));
        this.compileitem.setMnemonic(KeyEvent.VK_E);
        this.codemenu.add(this.runitem);
        this.runitem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        this.runitem.setMnemonic(KeyEvent.VK_R);

        // outputmenu stuff
        this.outputbar.add(this.outputmenu);
        this.outputmenu.setMnemonic(KeyEvent.VK_O);
        this.outputmenu.add(this.clearoutputitem);

        // adding actionlisteners
        this.openitem.addActionListener(this);
        this.saveitem.addActionListener(this);
        this.closeitem.addActionListener(this);

        this.undoitem.addActionListener(this);
        this.redoitem.addActionListener(this);

        this.compileitem.addActionListener(this);
        this.runitem.addActionListener(this);

        this.clearoutputitem.addActionListener(this);

        // undo listener
        this.textarea.getDocument().addUndoableEditListener(this.undoer);

        // frame stuff
        this.frame.setJMenuBar(this.menubar);
        this.frame.setContentPane(this.scroll);
        this.frame.pack();
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.frame.setVisible(true);

        this.outputframe.setJMenuBar(this.outputbar);
        this.outputframe.setContentPane(this.outputscroll);
        this.outputframe.setLocation(600, 0);
        this.outputframe.pack();
        this.outputframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.outputarea.setEditable(false);
        this.outputframe.setVisible(true);
    }

    // main method
    public static void main(String[] args) {
        new GarbageGeany();
    }
}
