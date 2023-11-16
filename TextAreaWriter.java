import java.io.IOException;
import java.io.OutputStream;

import javax.swing.JTextArea;

public class TextAreaWriter extends OutputStream {
    private JTextArea textarea;

    public TextAreaWriter(JTextArea textarea) {
        this.textarea = textarea;
    }

    @Override
    public void write(int b) throws IOException {
        // redirects data to the text area
        this.textarea.append(String.valueOf((char) b));
        // scrolls the text area to the end of data
        this.textarea.setCaretPosition(this.textarea.getDocument().getLength());
    }
}
