
// imports
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.*;
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
        if (evt.getSource() == openitem) {
            if (filechooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                textarea.setText("");
                try {
                    path = filechooser.getSelectedFile().getPath();
                    BufferedReader openfile = new BufferedReader(new FileReader(path));
                    current_line = openfile.readLine();
                    textarea.append(current_line);
                    current_line = openfile.readLine();
                    while (current_line != null) {
                        textarea.append("\n" + current_line);
                        current_line = openfile.readLine();
                    }
                    openfile.close();
                } catch (IOException e) {
                }
            }
            // save item, see subsequent function
        } else if (evt.getSource() == saveitem) {
            saveFile();
            // compile item, see subsequent function
        } else if (evt.getSource() == compileitem) {
            compileFile();
            // run item, see subsequent function
        } else if (evt.getSource() == runitem) {
            runFile();

        }
    }

    // save file function
    void saveFile() {
        // if the path is not empty then we can save to the current file
        if (path != "") {
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
        saveFile();
        if (this.path != "") {
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            compiler.run(null, null, null, path);
        }
    }

    // run file, you need to make sure you're compiling first
    void runFile() {
        compileFile();
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
        scroll.setPreferredSize(new Dimension(600, 600));

        // change tab size, I like it better like this
        textarea.setTabSize(2);

        // filemenu stuff
        menubar.add(filemenu);
        filemenu.add(openitem);
        filemenu.add(saveitem);

        // codemenu stuff
        menubar.add(codemenu);
        codemenu.add(compileitem);
        codemenu.add(runitem);

        // adding actionlisteners
        openitem.addActionListener(this);
        saveitem.addActionListener(this);
        compileitem.addActionListener(this);
        runitem.addActionListener(this);

        // frame stuff
        frame.setJMenuBar(menubar);
        frame.setContentPane(scroll);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    // main method
    public static void main(String[] args) {
        new GarbageGeany();
    }
}
