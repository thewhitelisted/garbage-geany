import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

public class GarbageGeany implements ActionListener {
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

    public void actionPerformed(ActionEvent evt) {
        if (evt.getSource() == openitem) {
            if (filechooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                textarea.setText("");
                try {
                    path = filechooser.getSelectedFile().getPath();
                    BufferedReader openfile = new BufferedReader(new FileReader(path));
                    current_line = openfile.readLine();
                    while (current_line != null) {
                        textarea.append(current_line + "\n");
                        current_line = openfile.readLine();
                    }
                    openfile.close();
                } catch (IOException e) {
                }
            }
        } else if (evt.getSource() == saveitem) {
            saveFile();
        } else if (evt.getSource() == compileitem) {
            compileFile();
        } else if (evt.getSource() == runitem) {
            runFile();

        }
    }

    void saveFile() {
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

    void compileFile() {
        saveFile();
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        compiler.run(null, null, null, path);
    }

    void runFile() {
        ProcessBuilder runfile = new ProcessBuilder();
        if (isWindows) {
            runfile.command("cmd.exe", "/c", "java " + path);
        } else {
            runfile.command("sh", "-c", "java " + path);
        }

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

    private static void printStream(InputStream inputStream) {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                System.out.println(line);
            }

        } catch (IOException e) {
        }
    }

    GarbageGeany() {
        scroll.setPreferredSize(new Dimension(600, 600));

        textarea.setTabSize(2);

        menubar.add(filemenu);
        filemenu.add(openitem);
        filemenu.add(saveitem);

        menubar.add(codemenu);
        codemenu.add(compileitem);
        codemenu.add(runitem);

        openitem.addActionListener(this);
        saveitem.addActionListener(this);
        compileitem.addActionListener(this);
        runitem.addActionListener(this);

        frame.setJMenuBar(menubar);
        frame.setContentPane(scroll);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        new GarbageGeany();
    }
}
