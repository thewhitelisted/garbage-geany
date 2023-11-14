
// imports
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

// main class
public class GarbageGeany implements ActionListener {
    // to see which os you're running.
    private static boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");

    // frame and main interface
    JFrame frame = new JFrame("Garbage Geany");
    JTextArea textarea = new JTextArea();
    JScrollPane scroll = new JScrollPane(textarea);

    // menu bar
    JMenuBar menubar = new JMenuBar();

    // menu items relating to file
    JMenu filemenu = new JMenu("File");
    JMenuItem openitem = new JMenuItem("Open");
    JMenuItem saveitem = new JMenuItem("Save");
    JMenuItem closeitem = new JMenuItem("Close");

    // menu items relating to code
    JMenu codemenu = new JMenu("Code");
    JMenuItem compileitem = new JMenuItem("Compile");
    JMenuItem runitem = new JMenuItem("Run");

    // fileio management
    JFileChooser filechooser = new JFileChooser();
    String path = "";
    String current_line;

    // actionListener stuff
    public void actionPerformed(ActionEvent evt) {
        // open file, pretty basic stuff
        if (evt.getSource() == this.openitem) {
            if (this.filechooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                this.textarea.setText("");
                try {
                    this.path = this.filechooser.getSelectedFile().getPath();
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
        }
    }

    // save file function
    void saveFile() {
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
                PrintWriter outfile = new PrintWriter(new FileWriter(this.path));
                outfile.println(this.textarea.getText());
                outfile.close();
            } catch (IOException e) {
            }
        }
    }

    // compile file, thank god for JavaCompiler
    void compileFile() {
        this.saveFile();
        if (this.path != "") {
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            compiler.run(null, null, null, this.path);
        }
    }

    // run file, you need to make sure you're compiling first
    void runFile() {
        this.compileFile();
        if (this.path == "") {
            return;
        }
        ProcessBuilder runfile = new ProcessBuilder();
        // determine which command to run based on the os
        if (isWindows) {
            runfile.command("cmd.exe", "/c", "java " + path);
        } else {
            runfile.command("sh", "-c", "java " + path);
        }

        // start the process and then close the stream when finished.
        try {
            Process process = runfile.start();
            OutputStream outputStream = process.getOutputStream();
            InputStream inputStream = process.getInputStream();
            InputStream errorStream = process.getErrorStream();

            printStream(inputStream);
            printStream(errorStream);
            process.waitFor();
            outputStream.flush();
            outputStream.close();
        } catch (IOException | InterruptedException e) {
        }
    }

    // used for printing out the stream... if there are system.out.printlns we will
    // see them here.
    private static void printStream(InputStream inputStream) {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                System.out.println(line);
            }

        } catch (IOException e) {
        }
    }

    // constructor
    GarbageGeany() {
        // text pane
        this.scroll.setPreferredSize(new Dimension(600, 600));

        // change tab size, I like it better like this
        this.textarea.setTabSize(2);

        // filemenu stuff
        this.menubar.add(filemenu);
        this.filemenu.add(openitem);
        this.filemenu.add(saveitem);
        this.filemenu.add(closeitem);

        // codemenu stuff
        this.menubar.add(codemenu);
        this.codemenu.add(compileitem);
        this.codemenu.add(runitem);

        // adding actionlisteners
        this.openitem.addActionListener(this);
        this.saveitem.addActionListener(this);
        this.closeitem.addActionListener(this);
        this.compileitem.addActionListener(this);
        this.runitem.addActionListener(this);

        // frame stuff
        this.frame.setJMenuBar(menubar);
        this.frame.setContentPane(scroll);
        this.frame.pack();
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.frame.setVisible(true);
    }

    // main method
    public static void main(String[] args) {
        new GarbageGeany();
    }
}
