package microtrafficsim.utils.strings.builder;

/**
 * Implements the {@code StringBuilder}-interface using an instance of {@link java.lang.StringBuilder}. So this class
 * has the same functionality as java's {@code StringBuilder}, but this class can be extended.
 *
 * @author Dominic Parga Cacheiro
 */
public class BasicStringBuilder implements StringBuilder {

    private java.lang.StringBuilder stringBuilder;


    public BasicStringBuilder() {
        this.stringBuilder = new java.lang.StringBuilder();
    }

    public BasicStringBuilder(int capacity) {
        this.stringBuilder = new java.lang.StringBuilder(capacity);
    }

    public BasicStringBuilder(String str) {
        this.stringBuilder = new java.lang.StringBuilder(str);
    }

    public BasicStringBuilder(CharSequence seq) {
        this.stringBuilder = new java.lang.StringBuilder(seq);
    }


    @Override
    public String toString() {
        return stringBuilder.toString();
    }

    @Override
    public int length() {
        return stringBuilder.length();
    }

    @Override
    public StringBuilder append(Object obj) {
        stringBuilder.append(obj);
        return this;
    }

    @Override
    public StringBuilder append(String str) {
        stringBuilder.append(str);
        return this;
    }

    @Override
    public StringBuilder append(StringBuffer sb) {
        stringBuilder.append(sb);
        return this;
    }

    @Override
    public StringBuilder append(CharSequence s) {
        stringBuilder.append(s);
        return this;
    }

    @Override
    public StringBuilder append(CharSequence s, int start, int end) {
        stringBuilder.append(s, start, end);
        return this;
    }

    @Override
    public StringBuilder append(char[] str) {
        stringBuilder.append(str);
        return this;
    }

    @Override
    public StringBuilder append(char[] str, int offset, int len) {
        stringBuilder.append(str, offset, len);
        return this;
    }

    @Override
    public StringBuilder append(boolean b) {
        stringBuilder.append(b);
        return this;
    }

    @Override
    public StringBuilder append(char c) {
        stringBuilder.append(c);
        return this;
    }

    @Override
    public StringBuilder append(int i) {
        stringBuilder.append(i);
        return this;
    }

    @Override
    public StringBuilder append(long lng) {
        stringBuilder.append(lng);
        return this;
    }

    @Override
    public StringBuilder append(float f) {
        stringBuilder.append(f);
        return this;
    }

    @Override
    public StringBuilder append(double d) {
        stringBuilder.append(d);
        return this;
    }

    @Override
    public StringBuilder appendCodePoint(int codePoint) {
        stringBuilder.appendCodePoint(codePoint);
        return this;
    }


    @Override
    public StringBuilder appendln() {
        return append("\n");
    }

    @Override
    public StringBuilder appendln(Object obj) {
        return append(obj).append("\n");
    }

    @Override
    public StringBuilder appendln(String str) {
        return append(str).append("\n");
    }

    @Override
    public StringBuilder appendln(StringBuffer sb) {
        return append(sb).append("\n");
    }

    @Override
    public StringBuilder appendln(CharSequence s) {
        return append(s).append("\n");
    }

    @Override
    public StringBuilder appendln(CharSequence s, int start, int end) {
        return append(s, start, end).append("\n");
    }

    @Override
    public StringBuilder appendln(char[] str) {
        return append(str).append("\n");
    }

    @Override
    public StringBuilder appendln(char[] str, int offset, int len) {
        return append(str, offset, len).append("\n");
    }

    @Override
    public StringBuilder appendln(boolean b) {
        return append(b).append("\n");
    }

    @Override
    public StringBuilder appendln(char c) {
        return append(c).append("\n");
    }

    @Override
    public StringBuilder appendln(int i) {
        return append(i).append("\n");
    }

    @Override
    public StringBuilder appendln(long lng) {
        return append(lng).append("\n");
    }

    @Override
    public StringBuilder appendln(float f) {
        return append(f).append("\n");
    }

    @Override
    public StringBuilder appendln(double d) {
        return append(d).append("\n");
    }

    @Override
    public StringBuilder appendlnCodePoint(int codePoint) {
        return append(codePoint).append("\n");
    }


    @Override
    public StringBuilder delete(int start, int end) {
        stringBuilder.delete(start, end);
        return this;
    }

    @Override
    public StringBuilder deleteCharAt(int index) {
        stringBuilder.deleteCharAt(index);
        return this;
    }


    @Override
    public StringBuilder replace(int start, int end, String str) {
        stringBuilder.replace(start, end, str);
        return this;
    }


    @Override
    public StringBuilder insert(int index, char[] str, int offset, int len) {
        stringBuilder.insert(index, str, offset, len);
        return this;
    }

    @Override
    public StringBuilder insert(int offset, Object obj) {
        stringBuilder.insert(offset, obj);
        return this;
    }

    @Override
    public StringBuilder insert(int offset, String str) {
        stringBuilder.insert(offset, str);
        return this;
    }

    @Override
    public StringBuilder insert(int offset, char[] str) {
        stringBuilder.insert(offset, str);
        return this;
    }

    @Override
    public StringBuilder insert(int dstOffset, CharSequence s) {
        stringBuilder.insert(dstOffset, s);
        return this;
    }

    @Override
    public StringBuilder insert(int dstOffset, CharSequence s, int start, int end) {
        stringBuilder.insert(dstOffset, s, start, end);
        return this;
    }

    @Override
    public StringBuilder insert(int offset, boolean b) {
        stringBuilder.insert(offset, b);
        return this;
    }

    @Override
    public StringBuilder insert(int offset, char c) {
        stringBuilder.insert(offset, c);
        return this;
    }

    @Override
    public StringBuilder insert(int offset, int i) {
        stringBuilder.insert(offset, i);
        return this;
    }

    @Override
    public StringBuilder insert(int offset, long l) {
        stringBuilder.insert(offset, l);
        return this;
    }

    @Override
    public StringBuilder insert(int offset, float f) {
        stringBuilder.insert(offset, f);
        return this;
    }

    @Override
    public StringBuilder insert(int offset, double d) {
        stringBuilder.insert(offset, d);
        return this;
    }


    @Override
    public int indexOf(String str) {
        return stringBuilder.indexOf(str);
    }

    @Override
    public int indexOf(String str, int fromIndex) {
        return stringBuilder.indexOf(str, fromIndex);
    }

    @Override
    public int lastIndexOf(String str) {
        return stringBuilder.lastIndexOf(str);
    }

    @Override
    public int lastIndexOf(String str, int fromIndex) {
        return stringBuilder.lastIndexOf(str, fromIndex);
    }


    @Override
    public StringBuilder reverse() {
        stringBuilder.reverse();
        return this;
    }
}
