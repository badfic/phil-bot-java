package com.badfic.philbot.web;

import java.io.Writer;

public class ReusableStringWriter extends Writer {
    private static final ThreadLocal<ReusableStringWriter> TL = ThreadLocal.withInitial(ReusableStringWriter::new);
    private final StringBuilder buffer;

    public static ReusableStringWriter getCurrent() {
        return TL.get();
    }

    private ReusableStringWriter() {
        super();
        buffer = new StringBuilder(1_048_576);
    }

    @Override
    public void write(int c) {
        buffer.append((char) c);
    }

    @Override
    public void write(char[] cbuf) {
        buffer.append(cbuf);
    }

    @Override
    public void write(String str) {
        buffer.append(str);
    }

    @Override
    public void write(String str, int off, int len) {
        buffer.append(str, off, off + len);
    }

    @Override
    public Writer append(CharSequence csq) {
        buffer.append(csq);
        return this;
    }

    @Override
    public Writer append(CharSequence csq, int start, int end) {
        buffer.append(csq, start, end);
        return this;
    }

    @Override
    public Writer append(char c) {
        buffer.append(c);
        return this;
    }

    @Override
    public void write(char[] cbuf, int off, int len) {
        buffer.append(cbuf, off, len);
    }

    @Override
    public void flush() {
        // do nothing
    }

    @Override
    public void close() {
        buffer.setLength(0);
    }

    @Override
    public String toString() {
        return buffer.toString();
    }
}
