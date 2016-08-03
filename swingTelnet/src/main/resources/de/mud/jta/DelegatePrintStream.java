package de.mud.jta;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Locale;

/**
 *
 * @author Martin Lukáš <lukasma1@fit.cvut.cz>
 */
public class DelegatePrintStream extends PrintStream {

    private boolean delegate;

    public DelegatePrintStream(OutputStream out, boolean delegate) {
        super(out);
        this.delegate = delegate;
    }

    @Override
    public PrintStream append(CharSequence csq) {
        return super.append(csq);
    }

    @Override
    public PrintStream append(CharSequence csq, int start, int end) {
        return super.append(csq, start, end);
    }

    @Override
    public PrintStream append(char c) {
        return super.append(c);
    }

    @Override
    public boolean checkError() {
        return super.checkError();
    }

    @Override
    protected void clearError() {
        super.clearError();
    }

    @Override
    public void close() {
        super.close();
    }

    @Override
    public void flush() {
        super.flush();
    }

    @Override
    public PrintStream format(String format, Object... args) {
        return super.format(format, args);
    }

    @Override
    public PrintStream format(Locale l, String format, Object... args) {
        return super.format(l, format, args);
    }

    @Override
    public void print(boolean b) {
        if (this.delegate) {
            super.print(b);
        }
    }

    @Override
    public void print(char c) {
        if (this.delegate) {
            super.print(c);
        }
    }

    @Override
    public void print(int i) {
        if (this.delegate) {
            super.print(i);
        }
    }

    @Override
    public void print(long l) {
        if (this.delegate) {
            super.print(l);
        }
    }

    @Override
    public void print(float f) {
        if (this.delegate) {
            super.print(f);
        }
    }

    @Override
    public void print(double d) {
        if (this.delegate) {
            super.print(d);
        }
    }

    @Override
    public void print(char[] s) {
        if (this.delegate) {
            super.print(s);
        }
    }

    @Override
    public void print(String s) {
        if (this.delegate) {
            super.print(s);
        }
    }

    @Override
    public void print(Object obj) {
        if (this.delegate) {
            super.print(obj);
        }
    }

    @Override
    public PrintStream printf(String format, Object... args) {
        return super.printf(format, args);
    }

    @Override
    public PrintStream printf(Locale l, String format, Object... args) {
        return super.printf(l, format, args);
    }

    @Override
    public void println() {
        if (this.delegate) {
            super.println();
        }
    }

    @Override
    public void println(boolean x) {
        if (this.delegate) {
            super.println(x);
        }
    }

    @Override
    public void println(char x) {
        if (this.delegate) {
            super.println(x);
        }
    }

    @Override
    public void println(int x) {
        if (this.delegate) {
            super.println(x);
        }
    }

    @Override
    public void println(long x) {
        if (this.delegate) {
            super.println(x);
        }
    }

    @Override
    public void println(float x) {
        if (this.delegate) {
            super.println(x);
        }
    }

    @Override
    public void println(double x) {
        if (this.delegate) {
            super.println(x);
        }
    }

    @Override
    public void println(char[] x) {
        if (this.delegate) {
            super.println(x);
        }
    }

    @Override
    public void println(String x) {
        if (this.delegate) {
            super.println(x);
        }
    }

    @Override
    public void println(Object x) {
        if (this.delegate) {
            super.println(x);
        }
    }

    @Override
    protected void setError() {
        super.setError();
    }

    @Override
    public void write(int b) {
        if (this.delegate) {
            super.write(b);
        }
    }

    @Override
    public void write(byte[] buf, int off, int len) {
        super.write(buf, off, len);
    }
}
