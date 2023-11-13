import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.io.*;

public class GarbageGeany implements ActionListener {
    JFrame frame = new JFrame("Garbage Geany");
    JTextArea textarea = new JTextArea();
    JScrollPane scroll = new JScrollPane(textarea);
    JMenuBar menubar = new JMenuBar();
    JMenu filemenu = new JMenu("File");
    JMenuItem openitem = new JMenuItem("Open");
    JMenuItem saveitem = new JMenuItem("Save");
    JFileChooser filechooser = new JFileChooser();

    public void actionPerformed(ActionEvent evt) {

    }

    GarbageGeany() {
        scroll.setPreferredSize(new Dimension(400, 400));

        menubar.add(filemenu);
        filemenu.add(openitem);
        filemenu.add(saveitem);

        openitem.addActionListener(this);
        saveitem.addActionListener(this);

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